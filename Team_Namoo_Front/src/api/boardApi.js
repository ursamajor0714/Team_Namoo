import { apiClient } from './client'

// 게시판 API (com.example.team_navigation_server.board.PostController, ReportController)
//
//   GET  /api/parties/{party}/boards/{boardId}/posts?page=&size= -> { notices, posts, totalCount }
//   POST /api/parties/{party}/boards/{boardId}/posts { title, content } -> 201 PostDetailResponse
//   GET  /api/posts/{postId}                  -> PostDetailResponse (조회수 증가)
//   POST /api/posts/{postId}/vote { type }    -> { likes, dislikes, myVote }  type: LIKE|DISLIKE
//   GET  /api/posts/{postId}/comments         -> CommentResponse[]
//   POST /api/posts/{postId}/comments { content } -> 201 CommentResponse
//   POST /api/posts/{postId}/report { reason }    -> 201
//   POST /api/comments/{commentId}/report { reason } -> 201
//
// 실패 시 백엔드는 한글 메시지 문자열을 그대로 body 로 준다(GlobalExceptionHandler).

/** 한 번에 받아올 글 수. 목록에 페이지 이동 UI 가 없어 넉넉히 한 번에 받는다. */
const PAGE_SIZE = 50

/**
 * 백엔드가 내려준 한글 에러 메시지를 꺼낸다.
 * @param {unknown} error
 * @param {string} fallback
 * @returns {string}
 */
export function boardErrorMessage(error, fallback) {
  const body = error?.response?.data
  if (typeof body === 'string' && body) {
    return body
  }
  if (error?.response?.status === 401) {
    return '로그인이 필요합니다.'
  }
  return fallback
}

/**
 * @param {string} partyName
 * @param {string|number} boardId
 * @returns {Promise<{ notices: object[], posts: object[], totalCount: number }>}
 */
export async function fetchBoardPosts(partyName, boardId) {
  const response = await apiClient.get(
    `/api/parties/${encodeURIComponent(partyName)}/boards/${boardId}/posts`,
    { params: { page: 0, size: PAGE_SIZE } },
  )
  return response.data
}

/**
 * @param {string} partyName
 * @param {string|number} boardId
 * @param {{ title: string, content: string }} draft
 */
export async function createPost(partyName, boardId, draft) {
  const response = await apiClient.post(
    `/api/parties/${encodeURIComponent(partyName)}/boards/${boardId}/posts`,
    draft,
  )
  return response.data
}

/** @param {string|number} postId */
export async function fetchPost(postId) {
  const response = await apiClient.get(`/api/posts/${postId}`)
  return response.data
}

/** @param {string|number} postId */
export async function fetchComments(postId) {
  const response = await apiClient.get(`/api/posts/${postId}/comments`)
  return response.data
}

/**
 * @param {string|number} postId
 * @param {string} content
 */
export async function createComment(postId, content) {
  const response = await apiClient.post(`/api/posts/${postId}/comments`, { content })
  return response.data
}

/**
 * 추천/비추천. 같은 것을 다시 누르면 서버가 취소로 처리한다.
 * @param {string|number} postId
 * @param {'LIKE'|'DISLIKE'} type
 * @returns {Promise<{ likes: number, dislikes: number, myVote: string|null }>}
 */
export async function votePost(postId, type) {
  const response = await apiClient.post(`/api/posts/${postId}/vote`, { type })
  return response.data
}

/**
 * @param {string|number} postId
 * @param {string} reason
 */
export async function reportPost(postId, reason) {
  await apiClient.post(`/api/posts/${postId}/report`, { reason })
}

/**
 * @param {string|number} commentId
 * @param {string} reason
 */
export async function reportComment(commentId, reason) {
  await apiClient.post(`/api/comments/${commentId}/report`, { reason })
}
