/* Standardized API client for VisaFlow modular backend */

const API_BASE = '/api';

export interface ApiRequestOptions extends RequestInit {
  bodyData?: any;
}

export class ApiError extends Error {
  status: number;
  detail?: string;

  constructor(status: number, message: string, detail?: string) {
    super(message);
    this.status = status;
    this.detail = detail;
  }
}

/**
 * Gets JWT Access token from localStorage
 */
export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('vf_access_token');
}

/**
 * Saves tokens and session metadata to localStorage
 */
export function setSession(accessToken: string, refreshToken: string, email: string, role: string, companyId: string, userId: string) {
  localStorage.setItem('vf_access_token', accessToken);
  localStorage.setItem('vf_refresh_token', refreshToken);
  localStorage.setItem('vf_email', email);
  localStorage.setItem('vf_role', role);
  localStorage.setItem('vf_company_id', companyId);
  localStorage.setItem('vf_user_id', userId);
}

/**
 * Clears current session
 */
export function clearSession() {
  localStorage.removeItem('vf_access_token');
  localStorage.removeItem('vf_refresh_token');
  localStorage.removeItem('vf_email');
  localStorage.removeItem('vf_role');
  localStorage.removeItem('vf_company_id');
  localStorage.removeItem('vf_user_id');
  if (typeof window !== 'undefined') {
    window.location.href = '/login';
  }
}

/**
 * Attempts to obtain a new access token using the stored refresh token
 */
async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem('vf_refresh_token');
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const response = await fetch(`${API_BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    throw new Error('Refresh session expired');
  }

  const data = await response.json();
  setSession(
    data.accessToken,
    data.refreshToken,
    data.email,
    data.role,
    data.companyId,
    data.userId
  );
  return data.accessToken;
}

/**
 * Custom fetch wrapper with automatic JWT inject, error parsing, and automated 401 token refresh interceptor.
 */
export async function apiFetch<T>(endpoint: string, options: ApiRequestOptions = {}): Promise<T> {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint}`;
  
  // Set headers
  const headers = new Headers(options.headers || {});
  if (!headers.has('Content-Type') && !(options.body instanceof File || options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  // Inject authentication header
  const token = getAccessToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const fetchOptions: RequestInit = {
    ...options,
    headers,
  };

  if (options.bodyData) {
    fetchOptions.body = JSON.stringify(options.bodyData);
  }

  let response = await fetch(url, fetchOptions);

  // If unauthorized, token might be expired. Retry once with refresh token.
  if (response.status === 401) {
    try {
      const newAccessToken = await refreshAccessToken();
      headers.set('Authorization', `Bearer ${newAccessToken}`);
      response = await fetch(url, fetchOptions);
    } catch (refError) {
      console.warn('Authentication token expired & refresh failed. Logging out.', refError);
      clearSession();
      throw new ApiError(401, 'Session expired. Please log in again.');
    }
  }

  if (!response.ok) {
    let errorMessage = `HTTP Error ${response.status}`;
    let detailMessage = '';
    try {
      const errorData = await response.json();
      errorMessage = errorData.detail || errorData.message || errorMessage;
      detailMessage = errorData.cause || '';
    } catch (e) {
      // payload not JSON
    }
    throw new ApiError(response.status, errorMessage, detailMessage);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}
