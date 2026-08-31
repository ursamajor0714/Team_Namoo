import { apiClient } from './client'

// 백엔드 세션 기반 인증 API (com.example.team_navigation_server.member.MemberController)
//
//   POST /api/members/signup  { loginId, password, email, nickname } -> 200 "회원가입 성공"
//   POST /api/members/login   { loginId, password }                  -> 200 "로그인 성공" (+ JSESSIONID 쿠키)
//   GET  /api/members/me                                             -> 200 { id, loginId, email, nickname } | 401
//   POST /api/members/logout                                         -> 200 "로그아웃 성공"
//
// 실패 시 백엔드는 400 + 한글 메시지 문자열을 그대로 body로 준다(GlobalExceptionHandler).

export async function signup({ loginId, password, email, nickname }) {
  const response = await apiClient.post('/api/members/signup', {
    loginId,
    password,
    email,
    nickname,
  })
  return response.data
}

export async function login({ loginId, password }) {
  const response = await apiClient.post('/api/members/login', { loginId, password })
  return response.data
}

export async function logout() {
  const response = await apiClient.post('/api/members/logout')
  return response.data
}

/**
 * 현재 로그인한 회원 정보. 비로그인(401)이면 null을 반환한다.
 * 그 외 에러는 그대로 던진다(서버 다운 등).
 */
export async function fetchMe() {
  try {
    const response = await apiClient.get('/api/members/me')
    return response.data
  } catch (error) {
    if (error.response?.status === 401) {
      return null
    }
    throw error
  }
}
