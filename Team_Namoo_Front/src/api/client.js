import axios from 'axios'

// 세션 쿠키(JSESSIONID)를 주고받기 위해 withCredentials를 켠다.
// 백엔드는 http://localhost:8080, CORS에서 이 origin + allowCredentials 를 허용하고 있다.
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
})
