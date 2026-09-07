// 회원 관리 화면 상수.
// 서버는 enum 이름(ACTIVE / USER ...)을 주고받고 화면은 한글로 보여주므로 그 대응표를 여기 둔다.
// 실제 데이터는 api/adminApi.js 를 통해 /api/admin/members 에서 받아온다.

/** 서버 MemberStatus → 화면 표기 */
export const STATUS_LABEL = {
  ACTIVE: '정상',
  SUSPENDED: '정지',
  WITHDRAWN: '탈퇴',
}

/** 화면 표기 → 서버 MemberStatus (상태로 검색할 때 쓴다) */
export const STATUS_VALUE = {
  정상: 'ACTIVE',
  정지: 'SUSPENDED',
  탈퇴: 'WITHDRAWN',
}

/** 서버 MemberRole → 화면 표기 */
export const ROLE_LABEL = {
  USER: '일반',
  ADMIN: '관리자',
  SUPER_ADMIN: '슈퍼관리자',
}

export const SIGNUP_CHANNELS = ['인스타그램', '페이스북', '커뮤니티', '검색']

/** 검색 분류 기준. key 는 서버 /api/admin/members 의 field 파라미터 값이다. */
export const SEARCH_FIELDS = [
  { key: 'loginId', label: '아이디' },
  { key: 'nickname', label: '닉네임' },
  { key: 'email', label: '이메일' },
  { key: 'supportedParty', label: '지지정당' },
  { key: 'signupChannel', label: '가입경로' },
  { key: 'status', label: '상태' },
]
