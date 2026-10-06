import type {
  ComplianceChecklistResponse,
  ComplianceQueryRequest,
} from '../types/regulation';

const apiBaseUrl = import.meta.env.VITE_NORMBUILD_API_URL ?? 'http://localhost:8080';

export const requestComplianceChecklist = async (
  request: ComplianceQueryRequest,
): Promise<ComplianceChecklistResponse> => {
  const response = await fetch(`${apiBaseUrl}/api/regulations/compliance-checklist`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const fallbackMessage = 'No fue posible consultar la normativa en este momento.';
    const errorBody = await response.json().catch(() => ({ message: fallbackMessage }));
    throw new Error(errorBody.message ?? fallbackMessage);
  }

  return response.json() as Promise<ComplianceChecklistResponse>;
};
