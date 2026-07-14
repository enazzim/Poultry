import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { clearAuth, loadAuth, saveAuth } from './api'
import type { AuthUser } from './types'

interface AuthContextValue {
  user: AuthUser | null
  setUser: (user: AuthUser | null) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUserState] = useState<AuthUser | null>(() => loadAuth())

  const setUser = useCallback((next: AuthUser | null) => {
    if (next) saveAuth(next)
    else clearAuth()
    setUserState(next)
  }, [])

  const logout = useCallback(() => setUser(null), [setUser])

  const value = useMemo(() => ({ user, setUser, logout }), [user, setUser, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('AuthProvider 없음')
  return ctx
}
