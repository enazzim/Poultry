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
    interestCategoryCodes: [] as string[],
    smsConsent: true,
    alimtalkConsent: true,
  })
  const [error, setError] = useState('')
  const [categoriesError, setCategoriesError] = useState('')
  const [categoriesLoading, setCategoriesLoading] = useState(true)

  function loadCategories() {
    setCategoriesLoading(true)
    setCategoriesError('')
    api<Category[]>('/api/categories')
      .then((list) => {
        setCategories(list)
        if (!list.length) {
          setCategoriesError('등록된 카테고리가 없습니다. 백엔드 시드 또는 DB를 확인해 주세요.')
        }
      })
      .catch((e) => {
        setCategories([])
        setCategoriesError(
          e instanceof Error
            ? `카테고리를 불러오지 못했습니다. 백엔드(8080) 기동 여부를 확인해 주세요. (${e.message})`
            : '카테고리를 불러오지 못했습니다.',
        )
      })
      .finally(() => setCategoriesLoading(false))
  }

  useEffect(() => {
    loadCategories()
  }, [])

  function toggleTradeCategory(code: string) {
    setForm((prev) => ({
      ...prev,
      categoryCodes: prev.categoryCodes.includes(code)
        ? prev.categoryCodes.filter((c) => c !== code)
        : [...prev.categoryCodes, code],
    }))
  }

  function toggleInterest(code: string) {
    setForm((prev) => ({
      ...prev,
      interestCategoryCodes: prev.interestCategoryCodes.includes(code)
        ? prev.interestCategoryCodes.filter((c) => c !== code)
        : [...prev.interestCategoryCodes, code],
    }))
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    if (form.interestCategoryCodes.length === 0) {
      setError('관심 분야를 1개 이상 선택해 주세요.')
      return
    }
    if (!form.smsConsent && !form.alimtalkConsent) {
      setError('SMS 또는 알림톡 수신 동의가 필요합니다.')
      return
    }
    if (!form.phone.trim()) {
      setError('알림 수신용 휴대폰 번호를 입력해 주세요.')
      return
    }
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
            <span>휴대폰 (알림 수신용) *</span>
            <input
              value={form.phone}
              onChange={(e) => setForm({ ...form, phone: e.target.value })}
              placeholder="010-1234-5678"
              required
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

        <div className="chip-group">
          <span className="field-label">관심 분야 * (신규 공고 알림)</span>
          {categoriesLoading && <p className="muted">카테고리 불러오는 중…</p>}
          {categoriesError && (
            <p className="error">
              {categoriesError}{' '}
              <button type="button" className="ghost" onClick={loadCategories}>
                다시 시도
              </button>
            </p>
          )}
          {!categoriesLoading &&
            !categoriesError &&
            categories.map((c) => (
              <label key={c.code} className="chip">
                <input
                  type="checkbox"
                  checked={form.interestCategoryCodes.includes(c.code)}
                  onChange={() => toggleInterest(c.code)}
                />
                {c.name}
              </label>
            ))}
        </div>

        {form.role === 'PARTNER' && (
          <div className="chip-group">
            <span className="field-label">취급 카테고리 *</span>
            {categoriesLoading && <p className="muted">카테고리 불러오는 중…</p>}
            {!categoriesLoading &&
              categories.map((c) => (
                <label key={`trade-${c.code}`} className="chip">
                  <input
                    type="checkbox"
                    checked={form.categoryCodes.includes(c.code)}
                    onChange={() => toggleTradeCategory(c.code)}
                  />
                  {c.name}
                </label>
              ))}
          </div>
        )}

        <div className="consent-box panel">
          <p className="field-label">수신 동의 *</p>
          <p className="muted small">
            관심 분야에 새 공고가 등록되면 동의한 채널로 알림을 보냅니다. (앱 알림 + SMS/알림톡)
          </p>
          <label className="chip">
            <input
              type="checkbox"
              checked={form.smsConsent}
              onChange={(e) => setForm({ ...form, smsConsent: e.target.checked })}
            />
            SMS(문자) 수신 동의
          </label>
          <label className="chip">
            <input
              type="checkbox"
              checked={form.alimtalkConsent}
              onChange={(e) => setForm({ ...form, alimtalkConsent: e.target.checked })}
            />
            카카오 알림톡 수신 동의
          </label>
        </div>

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
