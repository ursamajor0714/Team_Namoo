import { apiClient } from './client'

// 관리자 콘솔 API (com.example.team_navigation_server.admin.*)
// 전부 세션 쿠키 + 관리자 권한이 필요하다. 권한이 없으면 401/403 이 온다.
// 실패 시 백엔드는 한글 메시지 문자열을 그대로 body 로 준다(GlobalExceptionHandler).
//
//   회원  GET   /api/admin/members?field=&q=&page=&size=  -> Page<AdminMemberResponse>
//         GET   /api/admin/members/stats                  -> { activeCount, inactiveCount, inactivePct,
//                                                              plusCount, plusPct, weeklyRevenue }
//         PATCH /api/admin/members/{id}                    (프로필 필드, 보낸 것만 반영)
//         PATCH /api/admin/members/{id}/status  { status } (ACTIVE|SUSPENDED|WITHDRAWN)
//         PATCH /api/admin/members/{id}/role    { role }   (USER|ADMIN, 슈퍼관리자만)
//         DELETE /api/admin/members/{id}                   (회원 탈퇴, DB 에서 실제 삭제)
//   기사  GET   /api/admin/articles?party=&scope=&q=       -> AdminArticleResponse[]
//         PATCH /api/admin/articles/{id}/visibility
//   게시글 GET   /api/admin/parties/{party}/boards/{boardId}/posts?page=&size=
//         PATCH /api/admin/posts/{id}/visibility
//         PATCH /api/admin/posts/{id}/pinned
//   신고  GET   /api/admin/reports?status=&targetType=&page=&size= -> Page<AdminReportResponse>
//         PATCH /api/admin/reports/{id}/status

/** 한 번에 받아올 목록 크기. 화면에 페이지 이동 UI 가 없어 넉넉히 한 번에 받는다. */
const PAGE_SIZE = 100

/**
 * 백엔드가 내려준 한글 에러 메시지를 꺼낸다. 없으면 fallback.
 * @param {unknown} error
 * @param {string} fallback
 * @returns {string}
 */
export function adminErrorMessage(error, fallback) {
  const body = error?.response?.data
  if (typeof body === 'string' && body) {
    return body
  }
  if (error?.response?.status === 401) {
    return '로그인이 필요합니다. 다시 로그인해주세요.'
  }
  if (error?.response?.status === 403) {
    return '권한이 없습니다.'
  }
  return fallback
}

/* ── 회원 ───────────────────────────────────────────── */

/**
 * @param {{ field?: string, q?: string }} [criteria]
 * @returns {Promise<{ content: object[], totalElements: number }>}
 */
export async function fetchMembers({ field, q } = {}) {
  const params = { page: 0, size: PAGE_SIZE }
  if (field && q) {
    params.field = field
    params.q = q
  }
  const response = await apiClient.get('/api/admin/members', { params })
  return response.data
}

export async function fetchMemberStats() {
  const response = await apiClient.get('/api/admin/members/stats')
  return response.data
}

/**
 * 프로필 수정. 보낸 필드만 반영된다.
 * @param {number} id
 * @param {object} patch
 */
export async function updateMember(id, patch) {
  await apiClient.patch(`/api/admin/members/${id}`, patch)
}

/**
 * @param {number} id
 * @param {'ACTIVE'|'SUSPENDED'|'WITHDRAWN'} status
 */
export async function updateMemberStatus(id, status) {
  await apiClient.patch(`/api/admin/members/${id}/status`, { status })
}

/**
 * @param {number} id
 * @param {'USER'|'ADMIN'} role
 */
export async function updateMemberRole(id, role) {
  await apiClient.patch(`/api/admin/members/${id}/role`, { role })
}

/**
 * 회원 탈퇴. 회원 행을 DB 에서 실제로 지운다 - 되돌릴 수 없다.
 * 그 회원이 쓴 글·댓글은 지워지지 않고 작성자가 '탈퇴한 회원' 으로 바뀐다.
 * 관리자 계정과 자기 자신은 서버가 거부한다.
 * @param {number} id
 */
export async function deleteMember(id) {
  await apiClient.delete(`/api/admin/members/${id}`)
}

/* ── 기사 ───────────────────────────────────────────── */

/**
 * @param {{ party?: string, scope?: string, q?: string }} [criteria]
 * @returns {Promise<object[]>}
 */
export async function fetchArticles(criteria = {}) {
  const response = await apiClient.get('/api/admin/articles', { params: criteria })
  return response.data
}

/**
 * @param {number} id
 * @param {string} visibility
 */
export async function updateArticleVisibility(id, visibility) {
  await apiClient.patch(`/api/admin/articles/${id}/visibility`, { visibility })
}

/**
 * 여러 기사의 노출 상태를 한 번에 바꾼다.
 * @param {number[]} ids
 * @param {'NORMAL'|'HIDDEN'|'DELETED'} visibility
 */
export async function updateArticleVisibilityBulk(ids, visibility) {
  await apiClient.patch('/api/admin/articles/visibility', { ids, visibility })
}

/* ── 게시글 ─────────────────────────────────────────── */

/**
 * @param {string} partyName
 * @param {number} boardId
 * @returns {Promise<object[]>}
 */
export async function fetchPosts(partyName, boardId) {
  const response = await apiClient.get(
    `/api/admin/parties/${encodeURIComponent(partyName)}/boards/${boardId}/posts`,
    { params: { page: 0, size: PAGE_SIZE } },
  )
  return response.data
}

export async function updatePostVisibility(id, visibility) {
  await apiClient.patch(`/api/admin/posts/${id}/visibility`, { visibility })
}

/**
 * @param {number[]} ids
 * @param {'NORMAL'|'HIDDEN'|'DELETED'} visibility
 */
export async function updatePostVisibilityBulk(ids, visibility) {
  await apiClient.patch('/api/admin/posts/visibility', { ids, visibility })
}

export async function updatePostPinned(id, pinned) {
  await apiClient.patch(`/api/admin/posts/${id}/pinned`, { pinned })
}

/* ── 신고 ───────────────────────────────────────────── */

/**
 * @param {{ status?: string, targetType?: string }} [criteria]
 * @returns {Promise<{ content: object[], totalElements: number }>}
 */
export async function fetchReports(criteria = {}) {
  const response = await apiClient.get('/api/admin/reports', {
    params: { ...criteria, page: 0, size: PAGE_SIZE },
  })
  return response.data
}

export async function updateReportStatus(id, status) {
  await apiClient.patch(`/api/admin/reports/${id}/status`, { status })
}
