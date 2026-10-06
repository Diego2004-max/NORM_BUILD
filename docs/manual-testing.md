# Compliance Testing

Run the frontend and backend with PostgreSQL and Ollama available. Send one request
at a time, recording elapsed time, answer, citations, risk label and backend logs.
These cases verify application behavior, not legal approval for a building.

## Recorded Checks (2026-10-06)

The corrected backend was run temporarily on port 8091 against the local PostgreSQL
and Ollama services. Both required models appeared in Ollama's installed-model list.
Three previously missing baseline documents were indexed successfully.

| Check | Observed result |
| --- | --- |
| Original residential case | 18.9 seconds, `GUIDED`, 5 citations, 2 floors, 7.5 m, 180 square meters |
| LLM generation in that case | Timed out after 18 seconds; guided fallback was used |
| Direct minimal LLM request (48 output tokens, context 2048) | Client timed out at 60 seconds |
| Medellin without corpus | `NO_CONTEXT`, 0 citations |
| Direct API description `casa` | HTTP 400 |
| Backend unit regressions | 13 tests passed |
| Frontend production build | Passed |

These measurements describe this run and are not a latency guarantee. Browser
interaction was not automated in this run.

The local Ollama log reported approximately 33 seconds to load `llama3.1` during
the direct check, exceeding the backend's 18-second generation limit before any
complete answer. Real LLM generation performance remains unresolved on this machine;
the passing unit tests and guided result must not be reported as an end-to-end LLM success.

## Backend Log Interpretation

- `Tomcat started on port 8080`: the HTTP server started.
- `HikariPool-1 - Start completed`: the database connection was established.
- `Indexing ... missing baseline regulatory documents`: indexing is still pending.
- `Could not index ...`: a document was not inserted. Read the exception cause;
  the earlier 12-second limit could expire while Ollama loads a model.
- `Checklist completed: mode=GENERATIVE`: the local LLM returned the answer.
- `Checklist completed: mode=GUIDED`: the LLM failed and the guided builder returned
  a preliminary answer with retrieved sources. This is not successful LLM generation.

The embedding timeout defaults to 30 seconds and can be configured with
`NORMBUILD_EMBEDDING_TIMEOUT_SECONDS`. The generation timeout defaults to 18 seconds
and can be configured with `NORMBUILD_LLM_TIMEOUT_SECONDS`. Keep their combined
budget below the 90-second HTTP async timeout, allowing time for database access
and queueing. Increasing timeouts alone does not establish model availability.

## Manual Cases

Use `Bogotá D.C.` as jurisdiction for cases 1-5.

### 1. Original Regression

> Quiero construir una casa de dos pisos con una altura total de 7.5 metros en un lote de 180 metros cuadrados. Necesito revisar uso residencial, aislamientos posterior y lateral, índice de ocupación y licencia de construcción.

Expected: the guided answer detects 2 floors, 7.5 m and 180 square meters. The risk
remains unverified; semantic similarity must not become a low-risk assessment.

### 2. Decimal Comma and Area Before Height

> Tengo un lote de 120 m² y quiero construir una vivienda de tres pisos con 8,5 m de altura. ¿Qué documentos y condiciones de edificabilidad debo verificar?

Expected: 3 floors, 8.5 m and 120 square meters. The lot area must not become height.
No numeric permitted height or maximum floor area may be invented from lot size.

### 3. Missing Height

> Quiero construir una casa de dos pisos en un lote de 180 metros cuadrados y dejar un retiro posterior de 3 metros. ¿Qué información me falta para revisar el proyecto?

Expected: the guided answer marks height as missing. Neither 180 nor the 3-meter
setback is the building height. A requested setback is not proof it is permitted.

### 4. Proposed Areas

> Mi lote tiene 200 m². Propongo ocupar 100 m² en el primer piso y construir 200 m² en total, en una casa de dos pisos con altura total de 6 metros. ¿Puedo construir así y qué norma del predio necesito?

Expected: no authorization to build based only on those numbers. If the LLM derives
ratios, proposed occupancy is 100/200 = 0.5 and proposed construction is 200/200 = 1;
these are project ratios, not permitted regulatory limits. The guided extractor
currently reports only the first area, and does not distinguish all three areas
or calculate these ratios. Record that limitation instead of accepting invented limits.

### 5. Insufficient Input

> casa

Expected: frontend validation blocks submission and asks for a more detailed
description. A direct API request below the minimum length must return HTTP 400.

### 6. Jurisdiction Without Corpus

Use `Medellín` as jurisdiction, provided no documents for that city have been loaded.

> Quiero construir una vivienda de dos pisos en un lote de 150 m² con altura total de 6 metros. ¿Qué normas de mi ciudad debo revisar?

Expected: `NO_CONTEXT`, no citations and a request for jurisdiction-specific official
documents. Bogotá documents must not be used for Medellín.

## Real Service Checks

In PowerShell, inspect installed and loaded models without starting a second server:

```powershell
Invoke-RestMethod http://localhost:11434/api/tags
Invoke-RestMethod http://localhost:11434/api/ps
Invoke-RestMethod http://localhost:8080/actuator/health
```

Send a real request and inspect generation mode:

```powershell
$payload = @{
    jurisdiction = 'Bogotá D.C.'
    projectDescription = 'Casa de dos pisos con altura total de 7.5 metros en un lote de 180 metros cuadrados. Quiero revisar aislamientos y licencia.'
} | ConvertTo-Json
$timer = [System.Diagnostics.Stopwatch]::StartNew()
$response = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/regulations/compliance-checklist' -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($payload)) -TimeoutSec 95
$timer.Stop()
$response | ConvertTo-Json -Depth 6
$timer.Elapsed.TotalSeconds
```

The percentage attached to citations measures retrieval similarity, not legal
certainty. The baseline catalog contains authored summaries; it is not a verified
collection of verbatim official articles. Exact legal citation and parcel-specific
limits require importing and checking the official applicable documents.

## Automated Regression Checks

```powershell
cd backend
mvn test
```

These unit tests cover extracted measurements and RAG branching using test doubles.
They do not prove PostgreSQL, Ollama or the browser are working. Use the real service
checks and manual cases above for end-to-end validation.
