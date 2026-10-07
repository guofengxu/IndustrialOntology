import { describe, expect, it } from 'vitest';

import { ApiError, isProblemDetail, toApiError } from '@/api/client';

describe('toApiError', () => {
  it('keeps the problem details returned by the backend', () => {
    const error = toApiError(409, {
      type: 'about:blank',
      title: 'Conflict',
      status: 409,
      detail: 'Frame was modified by another user',
      code: 'FRAME_CONFLICT',
    });

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(409);
    expect(error.problem.code).toBe('FRAME_CONFLICT');
    expect(error.message).toBe('Frame was modified by another user');
  });

  it('falls back to a synthetic problem for non-RFC 7807 bodies', () => {
    const error = toApiError(502, '<html>Bad Gateway</html>');

    expect(isProblemDetail('<html>')).toBe(false);
    expect(error.problem).toEqual({ status: 502, title: 'HTTP 502' });
  });
});
