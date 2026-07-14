import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import type { Listing, PortalProduct, ProductOrder } from '../types'

export function ProductsPage() {
  const { user } = useAuth()
  const [products, setProducts] = useState<PortalProduct[]>([])
  const [mine, setMine] = useState<Listing[]>([])
  const [productId, setProductId] = useState<number | ''>('')
  const [listingId, setListingId] = useState<number | ''>('')
  const [memo, setMemo] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    api<PortalProduct[]>('/api/products').then((list) => {
      setProducts(list)
      if (list[0]) setProductId(list[0].id)
    })
  }, [])

  useEffect(() => {
    if (!user) return
    api<Listing[]>('/api/listings/mine')
      .then(setMine)
      .catch(() => setMine([]))
  }, [user])

  const selected = products.find((p) => p.id === productId)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!user) return
    setError('')
    setMessage('')
    try {
      await api('/api/products/orders', {
        method: 'POST',
        body: JSON.stringify({
          productId,
          listingId: listingId === '' ? null : listingId,
          memo,
        }),
      })
      setMessage('신청이 접수되었습니다. 운영자 승인 후 노출이 적용됩니다.')
      setMemo('')
    } catch (err) {
      setError(err instanceof Error ? err.message : '신청 실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>유료 노출 상품</h1>
          <p className="muted">
            하이브레인넷 상품신청과 같이, 결제 PG 없이 신청·승인으로 운영합니다.
          </p>
        </div>
      </header>

      <div className="list">
        {products.map((p) => (
          <article key={p.id} className="panel listing-row">
            <div>
              <h2>{p.name}</h2>
              <p>{p.description}</p>
              <p className="muted">
                {p.durationDays}일
                {p.priceHint != null ? ` · 안내가 ${Number(p.priceHint).toLocaleString()}원` : ''}
              </p>
            </div>
          </article>
        ))}
      </div>

      {user ? (
        <form className="panel form-panel" onSubmit={onSubmit}>
          <h2>상품 신청</h2>
          <div className="field-grid">
            <label className="field">
              <span>상품</span>
              <select
                value={productId}
                onChange={(e) => setProductId(Number(e.target.value))}
                required
              >
                {products.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </label>
            {selected?.code === 'FEATURED_LISTING' && (
              <label className="field">
                <span>연결 공고</span>
                <select
                  value={listingId}
                  onChange={(e) =>
                    setListingId(e.target.value ? Number(e.target.value) : '')
                  }
                  required
                >
                  <option value="">선택</option>
                  {mine
                    .filter((l) => l.status === 'OPEN')
                    .map((l) => (
                      <option key={l.id} value={l.id}>
                        #{l.id} {l.title || l.categoryName}
                      </option>
                    ))}
                </select>
              </label>
            )}
          </div>
          <label className="field">
            <span>메모</span>
            <textarea value={memo} onChange={(e) => setMemo(e.target.value)} rows={3} />
          </label>
          {message && <p className="success">{message}</p>}
          {error && <p className="error">{error}</p>}
          <button type="submit">신청하기</button>
          <p className="muted">
            <Link to="/products/orders">내 신청 내역</Link>
          </p>
        </form>
      ) : (
        <p className="panel muted">
          신청은 <Link to="/login">로그인</Link> 후 이용할 수 있습니다.
        </p>
      )}
    </div>
  )
}

export function ProductOrdersPage() {
  const [orders, setOrders] = useState<ProductOrder[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    api<ProductOrder[]>('/api/products/orders/me')
      .then(setOrders)
      .catch((e) => setError(e.message))
  }, [])

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>내 상품 신청</h1>
          <p className="muted">신청·승인 상태를 확인합니다.</p>
        </div>
      </header>
      {error && <p className="error">{error}</p>}
      <div className="list">
        {orders.map((o) => (
          <article key={o.id} className="panel listing-row">
            <div>
              <strong>
                {o.productName} · {o.status}
              </strong>
              <p className="muted">
                {o.listingTitle ? `공고: ${o.listingTitle}` : '공고 미연결'} · {o.createdAt}
              </p>
              {o.adminMemo && <p>운영메모: {o.adminMemo}</p>}
            </div>
          </article>
        ))}
        {orders.length === 0 && <p className="muted">신청 내역이 없습니다.</p>}
      </div>
    </div>
  )
}
