import axios from 'axios'

import { API_BASE_URL } from './client'

export async function fetchNews({ query = '정치', display = 12 } = {}) {
  const response = await axios.get(`${API_BASE_URL}/api/news`, {
    params: { query, display },
  })
  return response.data
}

export async function fetchNewsByLeaning({ leaning, partyKeyword, query = '정치', count = 12 }) {
  const response = await axios.get(`${API_BASE_URL}/api/news/by-leaning`, {
    params: { leaning, partyKeyword, query, count },
  })
  return response.data
}

export async function classifyArticle({ title, content }) {
  const response = await axios.post(`${API_BASE_URL}/api/news/classify`, { title, content })
  return response.data.leaning
}

/**
 * AI 체험 페이지용. 라벨과 확신도를 함께 받는다.
 * content 대신 url 을 주면 서버가 그 주소의 기사를 읽어와 분류하고, 읽어온 제목을 title 로 돌려준다.
 * 분류하지 못하면 leaning 이 null 이고, 사유가 있으면 message 에 담겨 온다.
 * @param {{ title?: string, content?: string, url?: string }} article
 * @returns {Promise<{ leaning: string|null, confidence: number, title: string|null, message: string|null }>}
 */
export async function classifyArticleWithConfidence({ title = '', content = '', url = '' }) {
  const response = await axios.post(`${API_BASE_URL}/api/news/classify`, { title, content, url })
  return response.data
}
