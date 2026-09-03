import { create } from 'zustand'
import {
  fetchMe,
  login as loginApi,
  logout as logoutApi,
  signup as signupApi,
} from '../api/authApi'

/**
 * 인증 전역 스토어 (zustand).
 * user    : 로그인한 회원 { id, loginId, email, nickname } 또는 null
 * loading : 앱 시작 시 세션 확인(init)이 끝나기 전 true
 * 컴포넌트는 useAuthStore((state) => state.user) 처럼 필요한 조각만 구독한다.
 */
export const useAuthStore = create((set, get) => ({
  user: null,
  loading: true,

  /** 앱 시작 시 1회 호출 - 현재 세션을 확인해 user 를 채운다. */
  init: async () => {
    try {
      set({ user: await fetchMe() })
    } catch {
      set({ user: null })
    } finally {
      set({ loading: false })
    }
  },

  /** 서버 세션을 다시 읽어 user 를 갱신한다. */
  refresh: async () => {
    const me = await fetchMe()
    set({ user: me })
    return me
  },

  /** @param {{ loginId: string, password: string }} credentials */
  login: async (credentials) => {
    await loginApi(credentials)
    return get().refresh()
  },

  /** @param {{ loginId: string, password: string, email: string, nickname: string }} form */
  signup: (form) => signupApi(form),

  logout: async () => {
    await logoutApi()
    set({ user: null })
  },
}))
