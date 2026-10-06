export interface ComplianceQueryRequest {
  jurisdiction: string;
  projectDescription: string;
}

export interface CitedRegulationResponse {
  id: string;
  title: string;
  regulationCode: string;
  articleReference: string;
  similarity: number;
  excerpt: string;
}

export interface ComplianceChecklistResponse {
  answer: string;
  riskLevel: string;
  citations: CitedRegulationResponse[];
  generatedAt: string;
  generationMode?: 'GENERATIVE' | 'GUIDED' | 'NO_CONTEXT';
}

export interface WorkerValidationIssue {
  field: 'jurisdiction' | 'projectDescription';
  message: string;
}

export interface WorkerValidationResult {
  formattedDescription: string;
  wordCount: number;
  issues: WorkerValidationIssue[];
}
