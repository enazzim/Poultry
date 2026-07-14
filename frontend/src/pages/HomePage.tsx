import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api'
import type { Article, Category, Listing, PortalProduct } from '../types'

export function HomePage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [listings, setListings] = useState<Listing[]>([])
  const [articles, setArticles] = useState<Article[]>([])
  const [products, setProducts] = useState<PortalProduct[]>([])

  useEffect(() => {
    Promise.all([
      api<Category[]>('/api/categories'),
      api<Listing[]>('/api/listings?listType=ING'),
      api<Article[]>('/api/articles'),
      api<PortalProduct[]>('/api/products'),
    ])
      .then(([cats, feed, news, prods]) => {
        setCategories(cats)
        setListings(feed.slice(0, 8))
        setArticles(news.slice(0, 4))
        setProducts(prods)
      })
      .catch(() => undefined)
  }, [])

  return (
    <div className="stack home-stack">
      <section className="hero-panel">
        <p className="eyebrow">양계 수급 공개 포털</p>
        <h1 className="brand">PoultryShare</h1>
        <p className="lede">
          농가와 파트너가 양방향으로 공급·수요 공고를 올리고, 관심·문의로 연결합니다.
        </p>
        <div className="actions">
          <Link className="button-link" to="/listings">
            공고 둘러보기
          </Link>
          <Link className="ghost button-link" to="/listings/new">
            공고 등록
          </Link>
        </div>
      </section>

      <section className="panel">
        <div className="section-head">
          <h2>카테고리</h2>
          <Link to="/listings">전체 공고</Link>
        </div>
        <div className="chip-row">
          {categories.map((c) => (
            <Link key={c.id} className="tag hub-tag" to={`/listings?categoryId=${c.id}`}>
              {c.name}
            </Link>
          ))}
        </div>
      </section>

      <section className="panel">
        <div className="section-head">
          <h2>추천·최근 공고</h2>
          <Link to="/listings">더보기</Link>
        </div>
        <div className="list compact">
          {listings.map((item) => (
            <Link key={item.id} to={`/listings/${item.id}`} className="listing-row listing-link">
              <div>
                <strong>
                  {item.featured ? '[추천] ' : ''}
                  {item.title || `${item.categoryName} #${item.id}`}
                </strong>
                <p className="muted">
                  {item.side} · {item.regionCode} · {item.organizationName}
                </p>
              </div>
            </Link>
          ))}
          {listings.length === 0 && <p className="muted">등록된 공고가 없습니다.</p>}
        </div>
      </section>

      <div className="home-grid">
        <section className="panel">
          <div className="section-head">
            <h2>뉴스·공지</h2>
            <Link to="/news">더보기</Link>
          </div>
          <div className="list compact">
            {articles.map((a) => (
              <Link key={a.id} to={`/news/${a.id}`} className="listing-row listing-link">
                <div>
                  <strong>
                    [{a.type}] {a.title}
                  </strong>
                  <p className="muted">{a.summary}</p>
                </div>
              </Link>
            ))}
          </div>
        </section>

        <section className="panel">
          <div className="section-head">
            <h2>유료 노출 상품</h2>
            <Link to="/products">상품신청</Link>
          </div>
          <div className="list compact">
            {products.map((p) => (
              <div key={p.id} className="listing-row">
                <div>
                  <strong>{p.name}</strong>
                  <p className="muted">
                    {p.durationDays}일
                    {p.priceHint != null ? ` · 안내가 ${p.priceHint.toLocaleString()}원` : ''}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}
