/**
 * 관리자 권한 판별. 백엔드 Member 에 role 필드가 생기기 전까지의 임시 방식으로,
 * loginId 가 이 목록에 있으면 관리자로 본다. role 이 /api/members/me 에 실리면
 * 그 값으로 교체할 것.
 */
export const ADMIN_LOGIN_IDS = ['dbrmsgh11']

/** 슈퍼 관리자. 다른 회원에게 관리자 권한을 임명/해제할 수 있는 유일한 계정. */
export const SUPER_ADMIN_LOGIN_ID = 'dbrmsgh11'

/**
 * @param {{ loginId?: string } | null | undefined} user
 * @returns {boolean}
 */
export function isAdmin(user) {
  return Boolean(user && ADMIN_LOGIN_IDS.includes(user.loginId))
}

/**
 * 슈퍼 관리자 여부. 관리자 임명 권한 게이트에 쓴다.
 * @param {{ loginId?: string } | null | undefined} user
 * @returns {boolean}
 */
export function isSuperAdmin(user) {
  return Boolean(user && user.loginId === SUPER_ADMIN_LOGIN_ID)
}
