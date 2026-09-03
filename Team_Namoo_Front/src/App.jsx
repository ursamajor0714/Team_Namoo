import { Route, Routes, useLocation } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import AdRail from './components/AdRail'
import HomePage from './pages/HomePage'
import PartyLayout from './pages/PartyLayout'
import PartyPage from './pages/PartyPage'
import BoardPage from './pages/BoardPage'
import BoardWritePage from './pages/BoardWritePage'
import PostDetailPage from './pages/PostDetailPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import AdminPage from './pages/AdminPage'
import { useAuthStore } from './store/authStore'
import './App.css'

function App() {
  const user = useAuthStore((state) => state.user)
  const logout = useAuthStore((state) => state.logout)
  const { pathname } = useLocation()

  // 광고 레일이 붙는 페이지: 메인('main') 또는 정당명. 그 외(로그인·관리자 등)엔 null.
  let adPage = null
  if (pathname === '/') {
    adPage = 'main'
  } else if (pathname.startsWith('/party/')) {
    const seg = pathname.split('/')[2]
    adPage = seg ? decodeURIComponent(seg) : null
  }

  return (
    <>
      <Navbar user={user} onLogout={logout} />

      {adPage && <AdRail page={adPage} side="left" />}
      {adPage && <AdRail page={adPage} side="right" />}

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/party/:name" element={<PartyLayout />}>
          <Route index element={<PartyPage />} />
          <Route path="board/:boardId" element={<BoardPage />} />
          <Route path="board/:boardId/write" element={<BoardWritePage />} />
          <Route path="board/:boardId/post/:postId" element={<PostDetailPage />} />
        </Route>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>

      <Footer />
    </>
  )
}

export default App
