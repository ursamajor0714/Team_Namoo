import { apiClient } from './client'

// 광고 API (com.example.team_navigation_server.admin.AdminAdController, ad.AdController)
//
//   POST   /api/admin/ads/image/presign { contentType, size } -> { uploadUrl, imageUrl }
//   GET    /api/admin/ads?page=                               -> AdminAdResponse[]
//   POST   /api/admin/ads { page, side, imageUrl, linkUrl, startAt, endAt } -> 201
//   DELETE /api/admin/ads/{id}                                -> 200
//   GET    /api/ads?page=&side=                               -> 200 { id, imageUrl, linkUrl } | 204(없음)
//
// 이미지는 서버를 거치지 않는다. 서버에서 presigned URL(5분 유효)을 받아 브라우저가 S3 로 직접 PUT 한다.
// side 는 서버 enum(AdSide) 이라 'LEFT' | 'RIGHT' 대문자를 주고받는다.
// 실패 시 백엔드는 400/403 + 한글 메시지 문자열을 그대로 body 로 준다(GlobalExceptionHandler).

/** 서버(AdminAdService)가 허용하는 형식. SVG 는 XSS 위험이 있어 서버가 거부한다. */
export const ALLOWED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/webp']

/** 서버가 허용하는 최대 용량. 이 값을 넘기면 presign 단계에서 400 이 난다. */
export const MAX_IMAGE_BYTES = 2 * 1024 * 1024

/**
 * 백엔드가 내려준 한글 에러 메시지를 꺼낸다. 없으면 fallback.
 * @param {unknown} error
 * @param {string} fallback
 * @returns {string}
 */
export function adErrorMessage(error, fallback) {
  const body = error?.response?.data
  return typeof body === 'string' && body ? body : fallback
}

/**
 * 이미지 한 장을 S3 에 올리고 광고에 쓸 공개 URL 을 돌려준다.
 * @param {File} file
 * @returns {Promise<string>} imageUrl
 */
export async function uploadAdImage(file) {
  const { data } = await apiClient.post('/api/admin/ads/image/presign', {
    contentType: file.type,
    size: file.size,
  })
  // presigned URL 의 서명에 Content-Type 이 포함돼 있어 presign 때와 같은 값을 보내야 한다.
  const response = await fetch(data.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: file,
  })
  if (!response.ok) {
    throw new Error('이미지 업로드에 실패했습니다. 잠시 후 다시 시도해주세요.')
  }
  return data.imageUrl
}

/**
 * 관리자용 광고 목록.
 * @param {string} [page] 비우면 전체
 */
export async function fetchAdminAds(page) {
  const response = await apiClient.get('/api/admin/ads', {
    params: page ? { page } : undefined,
  })
  return response.data
}

/**
 * @param {{ page: string, side: 'LEFT'|'RIGHT', imageUrl: string|null,
 *           linkUrl: string|null, startAt: string|null, endAt: string|null }} ad
 */
export async function createAd(ad) {
  const response = await apiClient.post('/api/admin/ads', ad)
  return response.data
}

/** @param {number} id */
export async function deleteAd(id) {
  await apiClient.delete(`/api/admin/ads/${id}`)
}

/**
 * (page, side) 슬롯에 지금 노출할 광고. 없으면 서버가 204 를 주므로 null 을 돌려준다.
 * @param {string} page
 * @param {'LEFT'|'RIGHT'} side
 * @returns {Promise<{ id: number, imageUrl: string|null, linkUrl: string|null }|null>}
 */
export async function fetchActiveAd(page, side) {
  const response = await apiClient.get('/api/ads', { params: { page, side } })
  if (response.status === 204) {
    return null
  }
  return response.data
}
