import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080'

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
