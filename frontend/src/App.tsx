import { useEffect, useState } from 'react'
import { Link, Navigate, NavLink, Outlet, Route, Routes, useLocation } from 'react-router-dom'
import { useAuth } from './auth'
import { LoginPage, RegisterPage } from './pages/AuthPages'
import { CreateListingPage, ListingsPage, ListingDetailPage, MyListingsPage } from './pages/ListingsPages'
import { HomePage } from './pages/HomePage'
import { NewsDetailPage, NewsPage } from './pages/NewsPages'
import { ProductOrdersPage, ProductsPage } from './pages/ProductPages'
import {
  InquiriesPage,
  NotificationsPage,
  PreferencesPage,
} from './pages/WorkspacePages'
import { AdminCategoriesPage } from './pages/AdminCategoriesPage'
import { AdminArticlesPage, AdminProductOrdersPage } from './pages/AdminPortalPages'

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const location = useLocation()
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return children
}

function PortalNavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const { user } = useAuth()
  const close = () => onNavigate?.()
  return (
    <>
      <NavLink to="/" end onClick={close}>
        홈
      </NavLink>
      <NavLink to="/listings" onClick={close}>
        수급공고
      </NavLink>
      <NavLink to="/news" onClick={close}>
        뉴스·공지
      </NavLink>
      <NavLink to="/products" onClick={close}>
        상품신청
      </NavLink>
      {user && (
        <NavLink to="/listings/mine" onClick={close}>
          내 공고
        </NavLink>
      )}
      {user && (
        <NavLink to="/listings/new" onClick={close}>
          공고 등록
        </NavLink>
      )}
      {user && (
        <NavLink to="/inquiries" onClick={close}>
          문의함
        </NavLink>
      )}
      {user && (
        <NavLink to="/notifications" onClick={close}>
          알림
        </NavLink>
      )}
      {user?.role === 'PARTNER' && (
        <NavLink to="/preferences" onClick={close}>
          선호조건
        </NavLink>
      )}
      {user && (
        <NavLink to="/products/orders" onClick={close}>
          내 신청
        </NavLink>
      )}
      {user?.role === 'ADMIN' && (
        <NavLink to="/admin/categories" onClick={close}>
          카테고리
        </NavLink>
      )}
      {user?.role === 'ADMIN' && (
        <NavLink to="/admin/articles" onClick={close}>
          기사관리
        </NavLink>
      )}
      {user?.role === 'ADMIN' && (
        <NavLink to="/admin/product-orders" onClick={close}>
          상품승인
        </NavLink>
      )}
    </>
  )
}

function PortalShell() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    document.body.style.overflow = menuOpen ? 'hidden' : ''
    return () => {
      document.body.style.overflow = ''
    }
  }, [menuOpen])

  return (
    <div className="portal-shell">
      <header className="portal-topbar">
        <div className="portal-topbar-inner">
          <button
            type="button"
            className="menu-toggle"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((v) => !v)}
          >
            <span className={menuOpen ? 'menu-icon open' : 'menu-icon'} />
          </button>
          <Link to="/" className="brand portal-brand" onClick={() => setMenuOpen(false)}>
            PoultryShare
          </Link>
          <nav className="portal-nav portal-nav-desktop">
            <PortalNavLinks />
          </nav>
          <div className="portal-user portal-user-desktop">
            {user ? (
              <>
                <span className="muted small">
                  {user.organizationName}
                  {user.role === 'FARM' && (user.mesLinked ? ' · MES' : ' · 수동')}
                </span>
                <button type="button" className="ghost" onClick={logout}>
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link className="button-link compact" to="/login">
                  로그인
                </Link>
                <Link className="ghost button-link compact" to="/register">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {menuOpen && (
        <button
          type="button"
          className="menu-backdrop"
          aria-label="Close menu"
          onClick={() => setMenuOpen(false)}
        />
      )}
      <aside className={menuOpen ? 'mobile-drawer open' : 'mobile-drawer'} aria-hidden={!menuOpen}>
        <nav className="portal-nav portal-nav-mobile">
          <PortalNavLinks onNavigate={() => setMenuOpen(false)} />
        </nav>
        <div className="portal-user portal-user-mobile">
          {user ? (
            <>
              <p className="muted small">
                {user.organizationName}
                {user.role === 'FARM' && (user.mesLinked ? ' · MES' : ' · 수동')}
              </p>
              <button
                type="button"
                className="ghost"
                onClick={() => {
                  setMenuOpen(false)
                  logout()
                }}
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link className="button-link" to="/login" onClick={() => setMenuOpen(false)}>
                로그인
              </Link>
              <Link
                className="ghost button-link"
                to="/register"
                onClick={() => setMenuOpen(false)}
              >
                회원가입
              </Link>
            </>
          )}
        </div>
      </aside>

      <main className="portal-main">
        <Outlet />
      </main>
      <footer className="portal-footer muted small">
        PoultryShare — 양계 수급 공개 포털 · 관심/문의로 연결 · 계약은 오프라인
      </footer>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/" element={<PortalShell />}>
        <Route index element={<HomePage />} />
        <Route path="listings" element={<ListingsPage />} />
        <Route
          path="listings/new"
          element={
            <RequireAuth>
              <CreateListingPage />
            </RequireAuth>
          }
        />
        <Route
          path="listings/mine"
          element={
            <RequireAuth>
              <MyListingsPage />
            </RequireAuth>
          }
        />
        <Route path="listings/:id" element={<ListingDetailPage />} />
        <Route path="news" element={<NewsPage />} />
        <Route path="news/:id" element={<NewsDetailPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route
          path="products/orders"
          element={
            <RequireAuth>
              <ProductOrdersPage />
            </RequireAuth>
          }
        />
        <Route
          path="inquiries"
          element={
            <RequireAuth>
              <InquiriesPage />
            </RequireAuth>
          }
        />
        <Route
          path="notifications"
          element={
            <RequireAuth>
              <NotificationsPage />
            </RequireAuth>
          }
        />
        <Route
          path="preferences"
          element={
            <RequireAuth>
              <PreferencesPage />
            </RequireAuth>
          }
        />
        <Route
          path="admin/categories"
          element={
            <RequireAuth>
              <AdminCategoriesPage />
            </RequireAuth>
          }
        />
        <Route
          path="admin/articles"
          element={
            <RequireAuth>
              <AdminArticlesPage />
            </RequireAuth>
          }
        />
        <Route
          path="admin/product-orders"
          element={
            <RequireAuth>
              <AdminProductOrdersPage />
            </RequireAuth>
          }
        />
      </Route>
    </Routes>
  )
}
