import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api'
import type { Article, ArticleType } from '../types'

export function NewsPage() {
  const [articles, setArticles] = useState<Article[]>([])
  const [type, setType] = useState<ArticleType | ''>('')
  const [error, setError] = useState('')

  useEffect(() => {
    const qs = type ? `?type=${type}` : ''
    api<Article[]>(`/api/articles${qs}`)
      .then(setArticles)
      .catch((e) => setError(e.message))
  }, [type])

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>뉴스·공지</h1>
          <p className="muted">업계 소식과 플랫폼 공지를 확인하세요.</p>
        </div>
      </header>
      <div className="tab-row">
        <button type="button" className={type === '' ? '' : 'ghost'} onClick={() => setType('')}>
          전체
        </button>
        <button
          type="button"
          className={type === 'NEWS' ? '' : 'ghost'}
          onClick={() => setType('NEWS')}
        >
          뉴스
        </button>
        <button
          type="button"
          className={type === 'NOTICE' ? '' : 'ghost'}
          onClick={() => setType('NOTICE')}
        >
          공지
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="list">
        {articles.map((a) => (
          <Link key={a.id} to={`/news/${a.id}`} className="panel listing-row listing-link">
            <div>
              <p className="eyebrow">{a.type}</p>
              <h2>{a.title}</h2>
              <p className="muted">{a.summary}</p>
            </div>
          </Link>
        ))}
        {articles.length === 0 && <p className="muted">게시된 글이 없습니다.</p>}
      </div>
    </div>
  )
}

export function NewsDetailPage() {
  const { id } = useParams()
  const [article, setArticle] = useState<Article | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) return
    api<Article>(`/api/articles/${id}`)
      .then(setArticle)
      .catch((e) => setError(e.message))
  }, [id])

  if (error) return <p className="error">{error}</p>
  if (!article) return <p className="muted">불러오는 중…</p>

  return (
    <div className="stack">
      <Link to="/news">목록으로</Link>
      <article className="panel detail-panel">
        <p className="eyebrow">{article.type}</p>
        <h1>{article.title}</h1>
        <p className="muted">{article.publishedAt || article.createdAt}</p>
        <p className="lede">{article.summary}</p>
        <pre className="article-body">{article.body}</pre>
      </article>
    </div>
  )
}
