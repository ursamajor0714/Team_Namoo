/**
 * 관리자 권한 판별. 백엔드가 /api/members/me 에 내려주는 role 을 그대로 쓴다.
 * (MemberRole: USER | ADMIN | SUPER_ADMIN)
 */
const ADMIN_ROLES = ['ADMIN', 'SUPER_ADMIN']

/**
 * @param {{ role?: string } | null | undefined} user
 * @returns {boolean}
 */
export function isAdmin(user) {
  return Boolean(user && ADMIN_ROLES.includes(user.role))
}

/**
 * 슈퍼 관리자 여부. 다른 회원에게 관리자 권한을 임명/해제하는 게이트에 쓴다.
 * @param {{ role?: string } | null | undefined} user
 * @returns {boolean}
 */
export function isSuperAdmin(user) {
  return Boolean(user && user.role === 'SUPER_ADMIN')
}
