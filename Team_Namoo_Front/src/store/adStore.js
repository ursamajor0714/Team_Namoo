import { create } from 'zustand'
import { PARTIES } from '../constants/parties'
import { createAd, deleteAd, fetchAdminAds } from '../api/adApi'

// 광고 슬롯 상태 저장소 (관리자 화면 전용).
// 서버 /api/admin/ads 가 원본이고, 이 스토어는 화면이 보고 있는 페이지의 목록만 들고 있는다.
// 등록/삭제 후에는 서버 계산값(status 등)을 그대로 쓰기 위해 목록을 다시 읽는다.

/** 광고를 붙일 페이지. main + 정당 5개. */
export const AD_PAGES = [
  { key: 'main', label: '메인' },
  ...PARTIES.map((p) => ({ key: p, label: p })),
]

/**
 * @typedef {{ id: number, page: string, side: 'LEFT'|'RIGHT', imageUrl: string|null,
 *             linkUrl: string|null, startAt: string|null, endAt: string|null,
 *             createdBy: string|null, createdAt: string, status: string }} Ad
 */

export const useAdStore = create((set, get) => ({
  /** @type {Ad[]} */
  ads: [],
  loading: false,
  /** 현재 목록이 어느 페이지의 것인지 - 등록/삭제 후 같은 페이지를 다시 읽는 데 쓴다. */
  loadedPage: null,

  /** @param {string} page */
  loadAds: async (page) => {
    set({ loading: true })
    try {
      set({ ads: await fetchAdminAds(page), loadedPage: page })
    } finally {
      set({ loading: false })
    }
  },

  /** @param {Parameters<typeof createAd>[0]} ad */
  addAd: async (ad) => {
    await createAd(ad)
    await get().loadAds(get().loadedPage ?? ad.page)
  },

  /** @param {number} id */
  removeAd: async (id) => {
    await deleteAd(id)
    const page = get().loadedPage
    if (!page) {
      return
    }
    await get().loadAds(page)
  },
}))
