import { apiClient } from './client'

// 백엔드 세션 기반 인증 API (com.example.team_navigation_server.member.MemberController,
// com.example.team_navigation_server.email.EmailVerificationController)
//
//   POST /api/members/signup   { loginId, password, email, nickname, supportedParty,
//                                 signupChannel, zipcode, addressBase, addressDetail,
//                                 agreeMarketing } -> 200 "회원가입 성공"
//                              (이메일 인증 선행 필수 - isVerified 아니면 400)
//   POST /api/members/login    { loginId, password }                  -> 200 "로그인 성공" (+ JSESSIONID 쿠키)
//   GET  /api/members/me                                              -> 200 { id, loginId, email, nickname } | 401
//   POST /api/members/logout                                          -> 200 "로그아웃 성공"
//   GET  /api/members/check-id?loginId=xxx                            -> 200 { available: boolean }
//   GET  /api/members/check-email?email=xxx                           -> 200 { available: boolean }
//   POST /api/email/send-code   { email }                             -> 200 (6자리 코드 메일, TTL 5분)
//   POST /api/email/verify-code { email, code }                       -> 200 | 400 "만료/불일치/미요청"
//
// 실패 시 백엔드는 400 + 한글 메시지 문자열을 그대로 body로 준다(GlobalExceptionHandler).

export async function signup({
  loginId,
  password,
  email,
  nickname,
  supportedParty,
  signupChannel,
  zipcode,
  addressBase,
  addressDetail,
  agreeMarketing,
}) {
  const response = await apiClient.post('/api/members/signup', {
    loginId,
    password,
    email,
    nickname,
    supportedParty,
    signupChannel,
    zipcode,
    addressBase,
    addressDetail,
    agreeMarketing,
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
 * 아이디 중복 확인. 사용 가능하면 true.
 * @param {string} loginId
 * @returns {Promise<boolean>}
 */
export async function checkLoginId(loginId) {
  const response = await apiClient.get('/api/members/check-id', { params: { loginId } })
  return response.data.available
}

/**
 * 이메일 중복 확인. 사용 가능하면 true.
 * @param {string} email
 * @returns {Promise<boolean>}
 */
export async function checkEmail(email) {
  const response = await apiClient.get('/api/members/check-email', { params: { email } })
  return response.data.available
}

/**
 * 이메일 인증 코드 발송. 6자리 코드가 메일로 가고 5분간 유효하다.
 * @param {string} email
 */
export async function sendEmailCode(email) {
  const response = await apiClient.post('/api/email/send-code', { email })
  return response.data
}

/**
 * 이메일 인증 코드 확인. 실패 시 백엔드가 400 + 한글 메시지를 던진다.
 * @param {string} email
 * @param {string} code
 */
export async function verifyEmailCode(email, code) {
  const response = await apiClient.post('/api/email/verify-code', { email, code })
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
