import axios from 'axios'

// 세션 쿠키(JSESSIONID)를 주고받기 위해 withCredentials를 켠다.
// 백엔드 주소는 VITE_API_BASE_URL 환경변수로 주입(로컬은 미설정 시 localhost:8080).
// CORS에서 이 origin + allowCredentials 를 허용하고 있다.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
})
