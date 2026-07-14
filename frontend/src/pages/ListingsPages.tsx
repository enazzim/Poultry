import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { useAuth } from '../auth'
import { DynamicAttributeFields } from '../components/DynamicAttributeFields'
import type {
  Category,
  Listing,
  ListingSide,
  ListType,
  LogisticsType,
} from '../types'

function canActOnListing(user: ReturnType<typeof useAuth>['user'], item: Listing) {
  if (!user) return false
  if (user.role === 'ADMIN') return true
  return user.organizationId !== item.organizationId
}

export function ListingsPage() {
  const { user } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const [categories, setCategories] = useState<Category[]>([])
  const [listings, setListings] = useState<Listing[]>([])
  const [error, setError] = useState('')

  const listType = (searchParams.get('listType') as ListType) || 'ING'
  const categoryId = searchParams.get('categoryId') || ''
  const regionCode = searchParams.get('regionCode') || ''
  const side = searchParams.get('side') || ''

  useEffect(() => {
    api<Category[]>('/api/categories').then(setCategories).catch(() => setCategories([]))
  }, [])

  useEffect(() => {
    const qs = new URLSearchParams({
      listType,
      ...(categoryId ? { categoryId } : {}),
      ...(regionCode ? { regionCode } : {}),
      ...(side ? { side } : {}),
    })
    api<Listing[]>(`/api/listings?${qs}`)
      .then(setListings)
      .catch((e) => setError(e.message))
  }, [listType, categoryId, regionCode, side])

  function setParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams)
    if (!value) next.delete(key)
    else next.set(key, value)
    setSearchParams(next)
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>수급 공고</h1>
          <p className="muted">카테고리·지역·유형으로 검색하세요. OFFER(공급)와 NEED(수요) 모두 확인할 수 있습니다.</p>
        </div>
      </header>

      <div className="tab-row">
        {(
          [
            ['ING', '진행중'],
            ['TODAY', '오늘마감'],
            ['CLOSED', '마감'],
            ['ALL', '전체'],
          ] as const
        ).map(([value, label]) => (
          <button
            key={value}
            type="button"
            className={listType === value ? '' : 'ghost'}
            onClick={() => setParam('listType', value)}
          >
            {label}
          </button>
        ))}
      </div>

      <div className="toolbar panel">
        <label className="field inline">
          <span>카테고리</span>
          <select value={categoryId} onChange={(e) => setParam('categoryId', e.target.value)}>
            <option value="">전체</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <label className="field inline">
          <span>유형</span>
          <select value={side} onChange={(e) => setParam('side', e.target.value)}>
            <option value="">전체</option>
            <option value="OFFER">OFFER 공급</option>
            <option value="NEED">NEED 수요</option>
          </select>
        </label>
        <label className="field inline">
          <span>지역</span>
          <input
            value={regionCode}
            onChange={(e) => setParam('regionCode', e.target.value)}
            placeholder="예: 경북"
          />
        </label>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="list">
        {listings.map((item) => (
          <Link key={item.id} to={`/listings/${item.id}`} className="panel listing-row listing-link">
            <div>
              <p className="eyebrow">
                {item.featured ? '추천 · ' : ''}
                {item.categoryName} · {item.side} · {item.status}
              </p>
              <h2>{item.title || `${item.categoryName} #${item.id}`}</h2>
              <p>
                {item.quantity}
                {item.unit} · {item.regionCode}
                {item.targetPrice != null ? ` · 희망가 ${item.targetPrice}` : ''}
              </p>
              <p className="muted">
                {item.organizationName}
                {user && item.organizationPhone ? ` · ${item.organizationPhone}` : ''}
              </p>
            </div>
          </Link>
        ))}
        {listings.length === 0 && <p className="muted">조건에 맞는 공고가 없습니다.</p>}
      </div>
    </div>
  )
}

export function ListingDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [item, setItem] = useState<Listing | null>(null)
  const [inquiryText, setInquiryText] = useState('')
  const [showInquiry, setShowInquiry] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    api<Listing>(`/api/listings/${id}`)
      .then(setItem)
      .catch((e) => setError(e.message))
  }, [id])

  async function expressInterest() {
    if (!item) return
    setError('')
    setMessage('')
    try {
      await api(`/api/listings/${item.id}/interests`, { method: 'POST' })
      setMessage('관심 등록이 완료되었습니다.')
    } catch (e) {
      setError(e instanceof Error ? e.message : '실패')
    }
  }

  async function sendInquiry() {
    if (!item) return
    setError('')
    setMessage('')
    try {
      await api(`/api/listings/${item.id}/inquiries`, {
        method: 'POST',
        body: JSON.stringify({ message: inquiryText }),
      })
      setShowInquiry(false)
      setInquiryText('')
      setMessage('문의가 전송되었습니다.')
    } catch (e) {
      setError(e instanceof Error ? e.message : '실패')
    }
  }

  if (error && !item) return <p className="error">{error}</p>
  if (!item) return <p className="muted">불러오는 중…</p>

  const canAct = canActOnListing(user, item)

  return (
    <div className="stack">
      <button type="button" className="ghost" onClick={() => navigate(-1)}>
        목록으로
      </button>
      <article className="panel detail-panel">
        <p className="eyebrow">
          {item.featured ? '추천 · ' : ''}
          {item.categoryName} · {item.side} · {item.logisticsType} · {item.source}
        </p>
        <h1>{item.title || `${item.categoryName} 공고 #${item.id}`}</h1>
        <p>
          {item.quantity}
          {item.unit} · {item.regionCode}
          {item.targetPrice != null ? ` · 희망가 ${item.targetPrice}` : ''}
        </p>
        <p className="muted">
          {item.organizationName}
          {item.organizationPhone ? ` · ${item.organizationPhone}` : !user ? ' · 연락처는 로그인 후 표시' : ''}
        </p>
        {item.expiresAt && <p className="muted">마감: {item.expiresAt}</p>}
        {item.memo && <p>{item.memo}</p>}
        <div className="attr-tags">
          {Object.entries(item.attributes ?? {}).map(([k, v]) => (
            <span key={k} className="tag">
              {k}: {String(v)}
            </span>
          ))}
        </div>

        {message && <p className="success">{message}</p>}
        {error && <p className="error">{error}</p>}

        <div className="actions">
          {!user && (
            <Link className="button-link" to="/login">
              로그인 후 관심·문의
            </Link>
          )}
          {user && canAct && (
            <>
              <button type="button" className="ghost" onClick={expressInterest}>
                관심
              </button>
              <button type="button" onClick={() => setShowInquiry(true)}>
                문의
              </button>
            </>
          )}
          {user && !canAct && <p className="muted">본인 조직 공고입니다.</p>}
        </div>

        {showInquiry && (
          <div className="inquiry-box">
            <textarea
              value={inquiryText}
              onChange={(e) => setInquiryText(e.target.value)}
              placeholder="문의 내용과 연락 가능한 시간을 적어 주세요."
              rows={3}
            />
            <button type="button" onClick={sendInquiry}>
              문의 보내기
            </button>
          </div>
        )}
      </article>
    </div>
  )
}

export function MyListingsPage() {
  const [listings, setListings] = useState<Listing[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function load() {
    const data = await api<Listing[]>('/api/listings/mine')
    setListings(data)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  async function closeListing(id: number) {
    setError('')
    setMessage('')
    try {
      await api(`/api/listings/${id}/close`, { method: 'POST' })
      setMessage('공고를 마감했습니다.')
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>내 공고</h1>
          <p className="muted">등록한 공고를 확인하고 마감할 수 있습니다.</p>
        </div>
        <Link className="button-link" to="/listings/new">
          새 공고
        </Link>
      </header>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
      <div className="list">
        {listings.map((item) => (
          <article key={item.id} className="panel listing-row">
            <div>
              <p className="eyebrow">
                {item.featured ? '추천 · ' : ''}
                {item.categoryName} · {item.side} · {item.status}
              </p>
              <h2>
                <Link to={`/listings/${item.id}`}>
                  {item.title || `${item.categoryName} #${item.id}`}
                </Link>
              </h2>
              <p className="muted">
                {item.quantity}
                {item.unit} · {item.regionCode}
              </p>
            </div>
            {item.status === 'OPEN' && (
              <button type="button" className="ghost" onClick={() => closeListing(item.id)}>
                마감
              </button>
            )}
          </article>
        ))}
        {listings.length === 0 && <p className="muted">등록한 공고가 없습니다.</p>}
      </div>
    </div>
  )
}

export function CreateListingPage() {
  const { user } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [side, setSide] = useState<ListingSide>('OFFER')
  const [regionCode, setRegionCode] = useState('경북')
  const [quantity, setQuantity] = useState(50)
  const [unit, setUnit] = useState('TRAY')
  const [targetPrice, setTargetPrice] = useState<number | ''>(4500)
  const [logisticsType, setLogisticsType] = useState<LogisticsType>('PICKUP')
  const [title, setTitle] = useState('')
  const [memo, setMemo] = useState('')
  const [attributes, setAttributes] = useState<Record<string, unknown>>({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const category = useMemo(
    () => categories.find((c) => c.id === categoryId),
    [categories, categoryId],
  )

  useEffect(() => {
    api<Category[]>('/api/categories').then((cats) => {
      setCategories(cats)
      const egg = cats.find((c) => c.code === 'EGG')
      if (egg) {
        setCategoryId(egg.id)
        setUnit(egg.defaultUnit || 'TRAY')
      }
    })
  }, [])

  useEffect(() => {
    if (category?.defaultUnit) setUnit(category.defaultUnit)
    setAttributes({})
  }, [categoryId])

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setMessage('')
    if (!categoryId) return
    try {
      await api('/api/listings', {
        method: 'POST',
        body: JSON.stringify({
          categoryId,
          side,
          regionCode,
          quantity,
          unit,
          targetPrice: targetPrice === '' ? null : targetPrice,
          logisticsType,
          title,
          memo,
          attributes,
        }),
      })
      setMessage('공고가 등록되었습니다.')
    } catch (err) {
      setError(err instanceof Error ? err.message : '등록 실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>공고 등록</h1>
          <p className="muted">
            농가·파트너 모두 OFFER/NEED 공고를 등록할 수 있습니다.
            {user?.role === 'FARM' && user.mesLinked
              ? ` MES(${user.farmCode}) 연동 농가는 EggFactory 유입도 가능합니다.`
              : ''}
          </p>
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
              <option value="" disabled>
                선택
              </option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span>유형</span>
            <select value={side} onChange={(e) => setSide(e.target.value as ListingSide)}>
              <option value="OFFER">제안/공급 (OFFER)</option>
              <option value="NEED">요청/수요 (NEED)</option>
            </select>
          </label>
          <label className="field">
            <span>지역</span>
            <input value={regionCode} onChange={(e) => setRegionCode(e.target.value)} required />
          </label>
          <label className="field">
            <span>수량</span>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
              required
            />
          </label>
          <label className="field">
            <span>단위</span>
            <input value={unit} onChange={(e) => setUnit(e.target.value)} required />
          </label>
          <label className="field">
            <span>희망가</span>
            <input
              type="number"
              value={targetPrice}
              onChange={(e) =>
                setTargetPrice(e.target.value === '' ? '' : Number(e.target.value))
              }
            />
          </label>
          <label className="field">
            <span>물류</span>
            <select
              value={logisticsType}
              onChange={(e) => setLogisticsType(e.target.value as LogisticsType)}
            >
              <option value="PICKUP">상차(수거)</option>
              <option value="DELIVERY">배달</option>
              <option value="ON_SITE">현장</option>
            </select>
          </label>
          <label className="field">
            <span>제목</span>
            <input value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
        </div>
        {category && (
          <DynamicAttributeFields
            defs={category.attributes}
            values={attributes}
            onChange={setAttributes}
          />
        )}
        <label className="field">
          <span>메모</span>
          <textarea value={memo} onChange={(e) => setMemo(e.target.value)} rows={3} />
        </label>
        {message && <p className="success">{message}</p>}
        {error && <p className="error">{error}</p>}
        <button type="submit">등록</button>
      </form>
    </div>
  )
}
