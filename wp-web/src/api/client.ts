import createClient from 'openapi-fetch';

import type { paths } from './schema';

/** RFC 7807 body the backend returns for every error (docs/00 §4.5). */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  /** Business error code carried by WpException. */
  code?: string;
}

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `HTTP ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }
}

export function isProblemDetail(value: unknown): value is ProblemDetail {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return typeof candidate.title === 'string' || typeof candidate.status === 'number';
}

/** Normalises any error body so callers only ever handle {@link ApiError}. */
export function toApiError(status: number, body: unknown): ApiError {
  return new ApiError(status, isProblemDetail(body) ? body : { status, title: `HTTP ${status}` });
}

/**
 * Typed client over the generated OpenAPI schema (docs/00 §5.3: no hand-written DTOs).
 * Same-origin, so the Vite dev proxy and the SPA host both work unchanged.
 */
export const api = createClient<paths>();
