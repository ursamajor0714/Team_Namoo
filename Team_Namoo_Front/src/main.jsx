import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { useAuthStore } from './store/authStore'

// 앱 시작 시 현재 세션을 확인한다. 인증 상태는 zustand 스토어가 밑에서 관리한다.
useAuthStore.getState().init()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
