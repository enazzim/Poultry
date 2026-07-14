import { useEffect, useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import type { AuthUser, Category, UserRole } from '../types'

export function LoginPage() {
  const { setUser } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from || '/'
  const [username, setUsername] = useState('farm1')
  const [password, setPassword] = useState('farm1234')
  const [error, setError] = useState('')

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    try {
      const auth = await api<AuthUser>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      })
      setUser(auth)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : '로그인 실패')
    }
  }

  return (
    <div className="auth-shell">
      <form className="panel auth-panel" onSubmit={onSubmit}>
        <p className="brand">PoultryShare</p>
        <h1>로그인</h1>
        <label className="field">
          <span>아이디</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label className="field">
          <span>비밀번호</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit">로그인</button>
        <p className="muted">
          계정이 없으신가요? <Link to="/register">회원가입</Link>
        </p>
        <p className="muted">
          <Link to="/">로그인 없이 공고 둘러보기</Link>
        </p>
        <p className="hint">
          시드: farm1(MES)/farm1234 · farm2(수동)/farm1234 · dealer1/dealer1234 · admin/admin1234
        </p>
      </form>
    </div>
  )
}

export function RegisterPage() {
  const { setUser } = useAuth()
  const navigate = useNavigate()
  const [categories, setCategories] = useState<Category[]>([])
  const [form, setForm] = useState({
    username: '',
    password: '',
    displayName: '',
    role: 'FARM' as UserRole,
    organizationName: '',
    regionCode: '',
    phone: '',
    farmCode: '',
    categoryCodes: [] as string[],
  })
  const [error, setError] = useState('')

  useEffect(() => {
    api<Category[]>('/api/categories').then(setCategories).catch(() => setCategories([]))
  }, [])

  function toggleCategory(code: string) {
    setForm((prev) => ({
      ...prev,
      categoryCodes: prev.categoryCodes.includes(code)
        ? prev.categoryCodes.filter((c) => c !== code)
        : [...prev.categoryCodes, code],
    }))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    try {
      const auth = await api<AuthUser>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      setUser(auth)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : '가입 실패')
    }
  }

  return (
    <div className="auth-shell">
      <form className="panel auth-panel wide" onSubmit={onSubmit}>
        <p className="brand">PoultryShare</p>
        <h1>회원가입</h1>
        <div className="field-grid">
          <label className="field">
            <span>아이디</span>
            <input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>비밀번호</span>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>표시명</span>
            <input
              value={form.displayName}
              onChange={(e) => setForm({ ...form, displayName: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>역할</span>
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}
            >
              <option value="FARM">농가</option>
              <option value="PARTNER">파트너(도매·입추·계분·사료·백신 등)</option>
            </select>
          </label>
          <label className="field">
            <span>조직명</span>
            <input
              value={form.organizationName}
              onChange={(e) => setForm({ ...form, organizationName: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>지역</span>
            <input
              value={form.regionCode}
              onChange={(e) => setForm({ ...form, regionCode: e.target.value })}
              placeholder="예: 경북"
            />
          </label>
          <label className="field">
            <span>연락처</span>
            <input
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
            />
          </label>
          {form.role === 'FARM' && (
            <label className="field">
              <span>MES farmCode (선택)</span>
              <input
                value={form.farmCode}
                onChange={(e) => setForm({ ...form, farmCode: e.target.value })}
                placeholder="예: FARM-GB-001 · 비워두면 수동 공고만"
              />
              <span className="hint">
                EggFactory/MES를 쓰는 농가만 입력합니다. 비워두면 플랫폼에서 직접 공고를 등록합니다.
              </span>
            </label>
          )}
        </div>
        {form.role === 'PARTNER' && (
          <div className="chip-group">
            <span className="field-label">취급 카테고리 *</span>
            {categories.map((c) => (
              <label key={c.code} className="chip">
                <input
                  type="checkbox"
                  checked={form.categoryCodes.includes(c.code)}
                  onChange={() => toggleCategory(c.code)}
                />
                {c.name}
              </label>
            ))}
          </div>
        )}
        {error && <p className="error">{error}</p>}
        <button type="submit">가입</button>
        <p className="muted">
          <Link to="/login">로그인으로</Link>
          {' · '}
          <Link to="/">공고 둘러보기</Link>
        </p>
      </form>
    </div>
  )
}
