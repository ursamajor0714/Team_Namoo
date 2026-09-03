import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { isAdmin } from '../constants/admin'
import MemberManage from '../components/MemberManage'
import ArticleManage from '../components/ArticleManage'
import PostManage from '../components/PostManage'
import AdManage from '../components/AdManage'

const ADMIN_TABS = ['회원관리', '기사관리', '게시글 관리', '광고관리']

/**
 * 관리자 전용 페이지. 상단 탭으로 영역을 나눈다(아직 내용은 전부 준비중).
 * 관리자가 아니면(비로그인 포함) 홈으로 돌려보낸다.
 */
function AdminPage() {
  const user = useAuthStore((state) => state.user)
  const loading = useAuthStore((state) => state.loading)
  const [activeTab, setActiveTab] = useState(ADMIN_TABS[0])

  // 세션 확인이 끝나기 전엔 판단 보류 (직접 URL 진입 시 잘못된 리다이렉트 방지)
  if (loading) {
    return null
  }
  if (!isAdmin(user)) {
    return <Navigate to="/" replace />
  }

  return (
    <>
      <header className="admin-bar">
        <Link to="/" className="admin-bar__brand">
          나무 관리자
        </Link>
        <nav className="admin-bar__tabs" aria-label="관리 메뉴">
          {ADMIN_TABS.map((tab) => (
            <button
              key={tab}
              type="button"
              className={
                tab === activeTab
                  ? 'admin-bar__tab admin-bar__tab--active'
                  : 'admin-bar__tab'
              }
              onClick={() => setActiveTab(tab)}
            >
              {tab}
            </button>
          ))}
        </nav>
      </header>

      <main className="admin-page">
        {activeTab === '회원관리' && <MemberManage />}
        {activeTab === '기사관리' && <ArticleManage />}
        {activeTab === '게시글 관리' && <PostManage />}
        {activeTab === '광고관리' && <AdManage />}
      </main>
    </>
  )
}

export default AdminPage
