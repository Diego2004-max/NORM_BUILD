# NormBuild

NormBuild guides citizens through official building regulations using an AI-assisted RAG workflow.

## Project Layout

- `backend`: Spring Boot API, PostgreSQL, pgvector, asynchronous RAG services.
- `frontend`: Vue 3, Composition API, Tailwind CSS, Web Worker assisted validation.
- `docker-compose.yml`: Local PostgreSQL database with pgvector enabled.

## Local Development

1. Start PostgreSQL:
   ```bash
   docker compose up -d database
   ```
2. Run the backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. Run the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

All source code identifiers are written in English. End-user interface text is written in Spanish.
