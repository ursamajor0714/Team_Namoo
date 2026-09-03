// 게시판 API 가 없어 화면 확인용으로만 쓰는 더미 데이터.
// BoardPage(목록)와 PostDetailPage(상세)가 같은 id 로 같은 글을 찾아야 해서 한 곳에 모아둔다.
// 백엔드 게시판 API가 붙으면 이 파일 전체를 실제 fetch 호출로 교체하면 된다.

export const NOTICE_POSTS = [
  {
    id: 'n1',
    title: '게시판 이용 안내',
    author: '운영자',
    date: '08.20',
    views: 1024,
    likes: 12,
    content:
      '게시판 이용 안내입니다.\n\n1. 상호 존중하는 언어를 사용해 주세요.\n2. 허위사실 유포, 명예훼손성 게시글은 삭제될 수 있습니다.\n3. 광고성 게시글은 사전 안내 없이 삭제됩니다.',
  },
  {
    id: 'n2',
    title: '커뮤니티 이용규칙 (필독)',
    author: '운영자',
    date: '08.15',
    views: 2310,
    likes: 34,
    content:
      '커뮤니티 이용규칙입니다.\n\n타인을 비방하거나 특정 집단을 혐오하는 게시글은 금지됩니다.\n반복 위반 시 계정 이용이 제한될 수 있습니다.',
  },
]

const AUTHORS = ['ㅇㅇ', '정치덕후', '민심', '지나가던시민', '팩폭러', 'ㅇㅇ(211)', '역사왜곡금지', '중립기어']
const DATES = ['09.01', '09.01', '08.31', '08.31', '08.30', '08.30', '08.29', '08.29', '08.28', '08.27']

export function buildDummyPosts(boardId) {
  return Array.from({ length: 15 }, (_, i) => {
    const num = 15 - i
    return {
      id: `${boardId}-${num}`,
      num,
      title: `게시판${boardId} 관련 글 제목입니다 ${num}`,
      content: `게시판${boardId} 관련 글 본문입니다 ${num}.\n\n이 글은 실제 데이터가 아니라 화면 확인용 더미 텍스트입니다.\n백엔드 게시판 API가 붙으면 실제 작성 내용으로 교체됩니다.`,
      commentCount: (num * 7) % 23,
      author: AUTHORS[num % AUTHORS.length],
      date: DATES[num % DATES.length],
      views: 40 + ((num * 37) % 900),
      likes: (num * 3) % 40,
    }
  })
}

const COMMENT_AUTHORS = ['ㅇㅇ', '지나가던사람', '정치덕후', '중립기어', 'ㅇㅇ(211)', '팩폭러']
const COMMENT_BODIES = [
  '맞말이네',
  '이건 좀 아닌 듯',
  '자료 출처 좀',
  'ㅋㅋㅋㅋ',
  '정확한 지적입니다',
  '반박 시 니 말이 맞음',
  '오늘도 평화롭네',
]

export function buildDummyComments(postId) {
  let seed = 0
  for (const ch of String(postId)) {
    seed += ch.charCodeAt(0)
  }
  const count = (seed % 5) + 1

  return Array.from({ length: count }, (_, i) => ({
    id: `${postId}-c${i + 1}`,
    author: COMMENT_AUTHORS[(seed + i) % COMMENT_AUTHORS.length],
    date: DATES[(seed + i) % DATES.length],
    content: COMMENT_BODIES[(seed + i * 3) % COMMENT_BODIES.length],
  }))
}
