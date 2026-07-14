import { useEffect, useState, type FormEvent } from 'react'
import { api } from '../api'
import type { Article, ArticleType, ProductOrder } from '../types'

export function AdminArticlesPage() {
  const [articles, setArticles] = useState<Article[]>([])
  const [form, setForm] = useState({
    type: 'NEWS' as ArticleType,
    title: '',
    summary: '',
    body: '',
    published: true,
  })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function load() {
    setArticles(await api<Article[]>('/api/admin/articles'))
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      await api('/api/admin/articles', { method: 'POST', body: JSON.stringify(form) })
      setMessage('기사가 저장되었습니다.')
      setForm({ type: 'NEWS', title: '', summary: '', body: '', published: true })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>기사 관리</h1>
          <p className="muted">뉴스·공지를 등록합니다.</p>
        </div>
      </header>
      <form className="panel form-panel" onSubmit={onSubmit}>
        <div className="field-grid">
          <label className="field">
            <span>유형</span>
            <select
              value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value as ArticleType })}
            >
              <option value="NEWS">NEWS</option>
              <option value="NOTICE">NOTICE</option>
            </select>
          </label>
          <label className="field">
            <span>제목</span>
            <input
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              required
            />
          </label>
          <label className="field">
            <span>요약</span>
            <input
              value={form.summary}
              onChange={(e) => setForm({ ...form, summary: e.target.value })}
            />
          </label>
        </div>
        <label className="field">
          <span>본문</span>
          <textarea
            value={form.body}
            onChange={(e) => setForm({ ...form, body: e.target.value })}
            rows={6}
            required
          />
        </label>
        <label className="chip">
          <input
            type="checkbox"
            checked={form.published}
            onChange={(e) => setForm({ ...form, published: e.target.checked })}
          />
          즉시 공개
        </label>
        {message && <p className="success">{message}</p>}
        {error && <p className="error">{error}</p>}
        <button type="submit">등록</button>
      </form>
      <div className="list">
        {articles.map((a) => (
          <div key={a.id} className="panel listing-row">
            <div>
              <strong>
                [{a.type}] {a.title} {a.published ? '' : '(비공개)'}
              </strong>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export function AdminProductOrdersPage() {
  const [orders, setOrders] = useState<ProductOrder[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function load() {
    setOrders(await api<ProductOrder[]>('/api/admin/product-orders'))
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  async function decide(id: number, action: 'approve' | 'reject') {
    setError('')
    setMessage('')
    try {
      await api(`/api/admin/product-orders/${id}/${action}`, {
        method: 'POST',
        body: JSON.stringify({ adminMemo: action === 'approve' ? '승인' : '거절' }),
      })
      setMessage(`신청 #${id} ${action === 'approve' ? '승인' : '거절'} 완료`)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>상품 신청 승인</h1>
          <p className="muted">FEATURED_LISTING 승인 시 해당 공고에 추천 노출이 적용됩니다.</p>
        </div>
      </header>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
      <div className="list">
        {orders.map((o) => (
          <article key={o.id} className="panel listing-row">
            <div>
              <strong>
                #{o.id} {o.productName} · {o.status}
              </strong>
              <p className="muted">
                {o.organizationName}
                {o.listingId ? ` · 공고 #${o.listingId} ${o.listingTitle || ''}` : ''}
              </p>
              {o.memo && <p>{o.memo}</p>}
            </div>
            {o.status === 'PENDING' && (
              <div className="actions">
                <button type="button" onClick={() => decide(o.id, 'approve')}>
                  승인
                </button>
                <button type="button" className="ghost" onClick={() => decide(o.id, 'reject')}>
                  거절
                </button>
              </div>
            )}
          </article>
        ))}
        {orders.length === 0 && <p className="muted">신청이 없습니다.</p>}
      </div>
    </div>
  )
}
