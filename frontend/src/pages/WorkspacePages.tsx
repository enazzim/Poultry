import { useEffect, useState, type FormEvent } from 'react'
import { api } from '../api'
import { useAuth } from '../auth'
import type { Category, Inquiry, NotificationItem, Preference } from '../types'

export function InquiriesPage() {
  const { user } = useAuth()
  const [received, setReceived] = useState<Inquiry[]>([])
  const [sent, setSent] = useState<Inquiry[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      api<Inquiry[]>('/api/inquiries/received'),
      api<Inquiry[]>('/api/inquiries/sent'),
    ])
      .then(([r, s]) => {
        setReceived(r)
        setSent(s)
      })
      .catch((e) => setError(e.message))
  }, [])

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>문의함</h1>
          <p className="muted">관심/문의는 플랫폼에서 연결하고, 계약은 전화·오프라인으로 진행합니다.</p>
        </div>
      </header>
      {error && <p className="error">{error}</p>}
      {(user?.role === 'FARM' || user?.role === 'ADMIN') && (
        <section className="panel">
          <h2>수신 문의</h2>
          <div className="list compact">
            {received.map((item) => (
              <div key={item.id} className="listing-row">
                <div>
                  <strong>{item.fromOrganizationName}</strong>
                  <p>{item.message}</p>
                  <p className="muted">
                    공고 #{item.listingId} · {item.contactPhone || '-'} · {item.createdAt}
                  </p>
                </div>
              </div>
            ))}
            {received.length === 0 && <p className="muted">수신 문의가 없습니다.</p>}
          </div>
        </section>
      )}
      <section className="panel">
        <h2>발신 문의</h2>
        <div className="list compact">
          {sent.map((item) => (
            <div key={item.id} className="listing-row">
              <div>
                <strong>공고 #{item.listingId}</strong>
                <p>{item.message}</p>
                <p className="muted">{item.status} · {item.createdAt}</p>
              </div>
            </div>
          ))}
          {sent.length === 0 && <p className="muted">보낸 문의가 없습니다.</p>}
        </div>
      </section>
    </div>
  )
}

export function PreferencesPage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [prefs, setPrefs] = useState<Preference[]>([])
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [regions, setRegions] = useState('경북,대구')
  const [minQuantity, setMinQuantity] = useState<number | ''>('')
  const [pushEnabled, setPushEnabled] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function load() {
    const [cats, list] = await Promise.all([
      api<Category[]>('/api/categories'),
      api<Preference[]>('/api/preferences/me'),
    ])
    setCategories(cats)
    setPrefs(list)
    if (!categoryId && cats[0]) setCategoryId(cats[0].id)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!categoryId) return
    setError('')
    setMessage('')
    try {
      await api('/api/preferences/me', {
        method: 'PUT',
        body: JSON.stringify({
          categoryId,
          regions: regions
            .split(',')
            .map((r) => r.trim())
            .filter(Boolean),
          minQuantity: minQuantity === '' ? null : minQuantity,
          attributeFilters: {},
          pushEnabled,
        }),
      })
      setMessage('선호조건이 저장되었습니다.')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '저장 실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>매칭 선호조건</h1>
          <p className="muted">지역·최소수량 조건에 맞는 신규 공고가 오면 인앱 알림을 받습니다.</p>
        </div>
      </header>
      <form className="panel form-panel" onSubmit={onSubmit}>
        <div className="field-grid">
          <label className="field">
            <span>카테고리</span>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(Number(e.target.value))}
              required
            >
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>지역 (쉼표 구분)</span>
            <input value={regions} onChange={(e) => setRegions(e.target.value)} />
          </label>
          <label className="field">
            <span>최소 수량</span>
            <input
              type="number"
              value={minQuantity}
              onChange={(e) =>
                setMinQuantity(e.target.value === '' ? '' : Number(e.target.value))
              }
            />
          </label>
          <label className="field checkbox">
            <input
              type="checkbox"
              checked={pushEnabled}
              onChange={(e) => setPushEnabled(e.target.checked)}
            />
            <span>알림 수신</span>
          </label>
        </div>
        {message && <p className="success">{message}</p>}
        {error && <p className="error">{error}</p>}
        <button type="submit">저장</button>
      </form>
      <section className="panel">
        <h2>저장된 조건</h2>
        <div className="list compact">
          {prefs.map((p) => (
            <div key={p.id} className="listing-row">
              <div>
                <strong>{p.categoryName}</strong>
                <p className="muted">
                  지역: {(p.regions || []).join(', ') || '전체'} · 알림{' '}
                  {p.pushEnabled ? 'ON' : 'OFF'}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export function NotificationsPage() {
  const [items, setItems] = useState<NotificationItem[]>([])
  const [error, setError] = useState('')

  async function load() {
    const list = await api<NotificationItem[]>('/api/notifications')
    setItems(list)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  async function markRead(id: number) {
    await api(`/api/notifications/${id}/read`, { method: 'POST' })
    await load()
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>알림</h1>
          <p className="muted">매칭·관심·문의 알림을 확인합니다.</p>
        </div>
      </header>
      {error && <p className="error">{error}</p>}
      <div className="list">
        {items.map((n) => (
          <article key={n.id} className={`panel listing-row ${n.readFlag ? '' : 'unread'}`}>
            <div>
              <h2>{n.title}</h2>
              <p>{n.body}</p>
              <p className="muted">{n.createdAt}</p>
            </div>
            {!n.readFlag && (
              <button type="button" className="ghost" onClick={() => markRead(n.id)}>
                읽음
              </button>
            )}
          </article>
        ))}
        {items.length === 0 && <p className="muted">알림이 없습니다.</p>}
      </div>
    </div>
  )
}
