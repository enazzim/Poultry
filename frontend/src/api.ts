import type { AuthUser } from './types'

const API_BASE = import.meta.env.VITE_API_BASE ?? ''

export class ApiError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

function authHeaders(): HeadersInit {
  const raw = localStorage.getItem('auth')
  if (!raw) return { 'Content-Type': 'application/json' }
  const auth = JSON.parse(raw) as AuthUser
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${auth.token}`,
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...authHeaders(),
      ...(options.headers ?? {}),
    },
  })
  if (!res.ok) {
    let message = `요청 실패 (${res.status})`
    try {
      const body = await res.json()
      if (body?.message) message = body.message
    } catch {
      /* ignore */
    }
    throw new ApiError(message)
  }
  if (res.status === 204) {
    return undefined as T
  }
  const text = await res.text()
  if (!text) return undefined as T
  return JSON.parse(text) as T
}

export function saveAuth(user: AuthUser) {
  localStorage.setItem('auth', JSON.stringify(user))
}

export function loadAuth(): AuthUser | null {
  const raw = localStorage.getItem('auth')
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as AuthUser
    return {
      ...parsed,
      mesLinked: Boolean(parsed.mesLinked ?? parsed.farmCode),
      farmCode: parsed.farmCode ?? null,
    }
  } catch {
    return null
  }
}

export function clearAuth() {
  localStorage.removeItem('auth')
}
