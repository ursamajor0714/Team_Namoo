// 회원 관리 화면용 목(mock) 데이터. 회원 조회 API 가 없어 화면 확인용으로만 쓴다.
// 백엔드 관리자 API(/api/admin/members)가 붙으면 이 파일을 실제 fetch 로 교체한다.
// 필드는 회원가입 폼이 수집하는 정보(아이디/닉네임/이메일/주소/지지정당/가입경로) +
// 운영에 필요한 값(상태/최근접속/Plus/가입일/권한)으로 구성했다.

/** Plus(유료 회원권) 월 구독가 - 금주 매출 목 계산에 쓴다. */
export const PLUS_MONTHLY_PRICE = 4900

/** 장기 미접속 기준 일수 (1개월). */
export const INACTIVE_DAYS = 30

export const MEMBER_STATUSES = ['정상', '정지', '탈퇴']
export const SIGNUP_CHANNELS = ['인스타그램', '페이스북', '커뮤니티', '검색']

/** 회원 권한. '슈퍼관리자'는 슈퍼 어드민 전용이라 화면에서 부여/회수 불가. */
export const MEMBER_ROLES = ['일반', '관리자', '슈퍼관리자']

/** 검색 분류 기준. key 는 회원 객체 필드명. */
export const SEARCH_FIELDS = [
  { key: 'loginId', label: '아이디' },
  { key: 'nickname', label: '닉네임' },
  { key: 'email', label: '이메일' },
  { key: 'supportedParty', label: '지지정당' },
  { key: 'signupChannel', label: '가입경로' },
  { key: 'status', label: '상태' },
]

export const MOCK_MEMBERS = [
  { id: 1, loginId: 'dbrmsgh11', nickname: '관리자', email: 'admin@teamnamoo.local', emailVerified: true, supportedParty: '더불어민주당', signupChannel: '검색', zipcode: '04524', addressBase: '서울 중구 세종대로 110', addressDetail: '1101호', joinedAt: '2026-08-24', lastAccessAt: '2026-09-03', status: '정상', isPlus: true, agreeMarketing: false, role: '슈퍼관리자' },
  { id: 2, loginId: 'jinbo_kim', nickname: '초록불', email: 'greenlight@gmail.com', emailVerified: true, supportedParty: '조국혁신당', signupChannel: '커뮤니티', zipcode: '06236', addressBase: '서울 강남구 테헤란로 201', addressDetail: '302호', joinedAt: '2026-08-25', lastAccessAt: '2026-09-02', status: '정상', isPlus: true, agreeMarketing: true, role: '일반' },
  { id: 3, loginId: 'right_wing7', nickname: '보수의품격', email: 'dignity7@naver.com', emailVerified: true, supportedParty: '국민의힘', signupChannel: '검색', zipcode: '13529', addressBase: '경기 성남시 분당구 판교역로 4', addressDetail: '', joinedAt: '2026-08-26', lastAccessAt: '2026-07-20', status: '정상', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 4, loginId: 'news_lover', nickname: '뉴스중독', email: 'newsholic@daum.net', emailVerified: false, supportedParty: '개혁신당', signupChannel: '인스타그램', zipcode: '48058', addressBase: '부산 해운대구 센텀중앙로 55', addressDetail: '2층', joinedAt: '2026-08-27', lastAccessAt: '2026-09-01', status: '정상', isPlus: false, agreeMarketing: true, role: '일반' },
  { id: 5, loginId: 'silent_voter', nickname: '조용한다수', email: 'quiet.majority@gmail.com', emailVerified: true, supportedParty: '더불어민주당', signupChannel: '페이스북', zipcode: '35208', addressBase: '대전 서구 둔산로 100', addressDetail: '505호', joinedAt: '2026-08-27', lastAccessAt: '2026-06-30', status: '정지', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 6, loginId: 'factcheck_go', nickname: '팩트체크', email: 'factcheck@kakao.com', emailVerified: true, supportedParty: '진보당', signupChannel: '커뮤니티', zipcode: '61945', addressBase: '광주 서구 상무중앙로 30', addressDetail: '', joinedAt: '2026-08-28', lastAccessAt: '2026-09-03', status: '정상', isPlus: true, agreeMarketing: true, role: '관리자' },
  { id: 7, loginId: 'centrist_lee', nickname: '중도우파', email: 'moderate.lee@naver.com', emailVerified: true, supportedParty: '개혁신당', signupChannel: '검색', zipcode: '24232', addressBase: '강원 춘천시 중앙로 1', addressDetail: '3호', joinedAt: '2026-08-29', lastAccessAt: '2026-09-02', status: '정상', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 8, loginId: 'oldschool_park', nickname: '옛날사람', email: 'oldschool@hanmail.net', emailVerified: false, supportedParty: '국민의힘', signupChannel: '페이스북', zipcode: '54999', addressBase: '전북 전주시 완산구 효자로 225', addressDetail: '', joinedAt: '2026-08-29', lastAccessAt: '2026-07-15', status: '정상', isPlus: false, agreeMarketing: true, role: '일반' },
  { id: 9, loginId: 'young_gen', nickname: '이대남', email: 'younggen20@gmail.com', emailVerified: true, supportedParty: '개혁신당', signupChannel: '인스타그램', zipcode: '07268', addressBase: '서울 영등포구 여의대로 24', addressDetail: '1004호', joinedAt: '2026-08-30', lastAccessAt: '2026-09-01', status: '정상', isPlus: true, agreeMarketing: true, role: '일반' },
  { id: 10, loginId: 'debate_king', nickname: '토론왕', email: 'debate.king@daum.net', emailVerified: true, supportedParty: '더불어민주당', signupChannel: '커뮤니티', zipcode: '16489', addressBase: '경기 수원시 영통구 광교로 105', addressDetail: '지하1층', joinedAt: '2026-08-31', lastAccessAt: '2026-09-03', status: '정상', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 11, loginId: 'quit_user22', nickname: '탈퇴예정', email: 'byebye22@gmail.com', emailVerified: true, supportedParty: '조국혁신당', signupChannel: '검색', zipcode: '61475', addressBase: '광주 동구 제봉로 100', addressDetail: '', joinedAt: '2026-08-31', lastAccessAt: '2026-08-31', status: '탈퇴', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 12, loginId: 'policy_wonk', nickname: '정책덕후', email: 'policywonk@naver.com', emailVerified: true, supportedParty: '진보당', signupChannel: '커뮤니티', zipcode: '28644', addressBase: '충북 청주시 서원구 무심서로 377', addressDetail: '201호', joinedAt: '2026-09-01', lastAccessAt: '2026-09-02', status: '정상', isPlus: false, agreeMarketing: true, role: '일반' },
  { id: 13, loginId: 'floating_vote', nickname: '부동층', email: 'floating@kakao.com', emailVerified: false, supportedParty: '국민의힘', signupChannel: '검색', zipcode: '10380', addressBase: '경기 고양시 일산동구 정발산로 24', addressDetail: '', joinedAt: '2026-09-01', lastAccessAt: '2026-09-01', status: '정상', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 14, loginId: 'grandpa_hong', nickname: '태극기할배', email: 'taeguk1948@hanmail.net', emailVerified: true, supportedParty: '국민의힘', signupChannel: '페이스북', zipcode: '42192', addressBase: '대구 수성구 동대구로 350', addressDetail: '', joinedAt: '2026-09-02', lastAccessAt: '2026-06-28', status: '정지', isPlus: false, agreeMarketing: false, role: '일반' },
  { id: 15, loginId: 'idealist_j', nickname: '이상주의자', email: 'idealist.j@gmail.com', emailVerified: true, supportedParty: '진보당', signupChannel: '인스타그램', zipcode: '03163', addressBase: '서울 종로구 종로 1', addressDetail: '5층', joinedAt: '2026-09-02', lastAccessAt: '2026-09-03', status: '정상', isPlus: true, agreeMarketing: true, role: '일반' },
  { id: 16, loginId: 'suburb_mom', nickname: '분당맘', email: 'bundangmom@naver.com', emailVerified: true, supportedParty: '더불어민주당', signupChannel: '커뮤니티', zipcode: '13561', addressBase: '경기 성남시 분당구 황새울로 200', addressDetail: '아파트 101동 1201호', joinedAt: '2026-09-03', lastAccessAt: '2026-09-03', status: '정상', isPlus: false, agreeMarketing: true, role: '일반' },
]

/**
 * 목 회원 배열로 상단 카드 지표를 계산한다.
 * @param {typeof MOCK_MEMBERS} members
 * @param {string} today 기준일 'YYYY-MM-DD'
 */
export function buildMemberStats(members, today = '2026-09-03') {
  const total = members.length || 1
  const now = new Date(today).getTime()
  const inactiveMs = INACTIVE_DAYS * 24 * 60 * 60 * 1000

  const active = members.filter((m) => m.status === '정상').length
  const inactive = members.filter(
    (m) => now - new Date(m.lastAccessAt).getTime() > inactiveMs,
  ).length
  const plus = members.filter((m) => m.isPlus).length

  const pct = (n) => Math.round((n / total) * 1000) / 10

  return {
    active,
    inactive,
    inactivePct: pct(inactive),
    plus,
    plusPct: pct(plus),
    weeklyRevenue: plus * PLUS_MONTHLY_PRICE,
  }
}
