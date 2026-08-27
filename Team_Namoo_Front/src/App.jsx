import { useState } from 'react'
import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import PartyPage from './pages/PartyPage'
import './App.css'

function App() {
  // 로그인 연동 전까지는 항상 비로그인 상태 - 로그인/회원가입 버튼이 뜬다.
  const [user] = useState(null)

  return (
    <>
      <Navbar user={user} />

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/party/:name" element={<PartyPage />} />
      </Routes>
    </>
  )
}

export default App
