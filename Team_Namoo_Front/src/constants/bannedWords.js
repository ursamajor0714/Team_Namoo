// 닉네임에 사용할 수 없는 비속어·혐오 표현 목록.
// 입력값에 아래 문자열이 부분적으로라도 포함되면 거부한다. 운영하며 지속 보강 필요.
export const BANNED_NICKNAME_WORDS = [
  '시발',
  '씨발',
  '씨팔',
  '개새끼',
  '새끼',
  '병신',
  '지랄',
  '좆',
  '자지',
  '보지',
  '엠창',
  '느금',
  '니애미',
  '창녀',
  '한남',
  '한녀',
  '김치녀',
  '된장녀',
  '틀딱',
  '급식충',
  '맘충',
  '전라디언',
  '홍어',
  '일베',
  '메갈',
  '워마드',
  'fuck',
  'shit',
  'bitch',
  'asshole',
  'nigger',
]

/**
 * 주어진 문자열에 금지어가 포함되어 있는지 검사한다(대소문자 무시).
 * @param {string} text
 * @returns {boolean}
 */
export function containsBannedWord(text) {
  const lower = text.toLowerCase()
  return BANNED_NICKNAME_WORDS.some((word) => lower.includes(word.toLowerCase()))
}
