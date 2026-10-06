import type {
  ComplianceChecklistResponse,
  ComplianceQueryRequest,
} from '../types/regulation';

const apiBaseUrl = import.meta.env.DEV ? '' : (import.meta.env.VITE_NORMBUILD_API_URL ?? '');

interface ApiErrorResponse {
  message?: string;
  details?: string[];
}

export const requestComplianceChecklist = async (
  request: ComplianceQueryRequest,
): Promise<ComplianceChecklistResponse> => {
  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 190_000);
  try {
    const response = await fetch(`${apiBaseUrl}/api/regulations/compliance-checklist`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
      signal: controller.signal,
    });

    if (!response.ok) {
      const fallbackMessage = 'No fue posible consultar la normativa en este momento.';
      const errorBody = (await response.json().catch(() => ({ message: fallbackMessage }))) as ApiErrorResponse;
      const details = errorBody.details?.filter(Boolean).join(' ');
      throw new Error([errorBody.message ?? fallbackMessage, details].filter(Boolean).join(' '));
    }

    return await response.json() as ComplianceChecklistResponse;
  } catch (error) {
    if (controller.signal.aborted) {
      throw new Error('El análisis superó el tiempo de espera. Intenta nuevamente en unos momentos.');
    }
    if (error instanceof TypeError) {
      throw new Error('No fue posible conectar con el servidor. Comprueba que el backend esté en ejecución e intenta nuevamente.');
    }
    if (error instanceof SyntaxError) {
      throw new Error('El servidor devolvió una respuesta inválida. Intenta nuevamente en unos momentos.');
    }
    throw error;
  } finally {
    window.clearTimeout(timeout);
  }
};
