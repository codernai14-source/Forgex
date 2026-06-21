import { computed, onBeforeUnmount, ref } from 'vue'
import {
  getUserFavoriteManageMenus,
  getUserFavoriteMenus,
  toggleUserFavoriteMenu,
  type PersonalMenuEntry,
} from '@/api/system/personalHomepage'

export const MENU_FAVORITES_REFRESH_EVENT = 'fx:menu-favorites-refresh'

export function normalizeFavoritePath(path?: string | null) {
  const cleanPath = String(path || '').trim().split('?')[0].split('#')[0]
  return cleanPath.startsWith('/workspace') ? cleanPath : ''
}

export function dispatchMenuFavoritesRefresh(path?: string | null, favorite?: boolean) {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent(MENU_FAVORITES_REFRESH_EVENT, {
    detail: {
      path: normalizeFavoritePath(path),
      favorite,
    },
  }))
}

export function listenMenuFavoritesRefresh(handler: (event: CustomEvent<{ path?: string; favorite?: boolean }>) => void) {
  if (typeof window === 'undefined') {
    return () => {}
  }
  const listener = (event: Event) => handler(event as CustomEvent<{ path?: string; favorite?: boolean }>)
  window.addEventListener(MENU_FAVORITES_REFRESH_EVENT, listener)
  return () => window.removeEventListener(MENU_FAVORITES_REFRESH_EVENT, listener)
}

export function useMenuFavorites() {
  const favoriteMenus = ref<PersonalMenuEntry[]>([])
  const loading = ref(false)
  const togglingPath = ref('')

  const favoritePathSet = computed(() => new Set(
    favoriteMenus.value
      .map(item => normalizeFavoritePath(item.path))
      .filter(Boolean),
  ))

  async function loadFavorites(manage = true) {
    loading.value = true
    try {
      const list = manage ? await getUserFavoriteManageMenus() : await getUserFavoriteMenus(200)
      favoriteMenus.value = Array.isArray(list) ? list : []
    } finally {
      loading.value = false
    }
  }

  function isFavorite(path?: string | null) {
    const normalized = normalizeFavoritePath(path)
    return !!normalized && favoritePathSet.value.has(normalized)
  }

  async function toggleFavorite(path?: string | null) {
    const normalized = normalizeFavoritePath(path)
    if (!normalized || togglingPath.value) {
      return isFavorite(normalized)
    }

    togglingPath.value = normalized
    try {
      const nextFavorite = await toggleUserFavoriteMenu(normalized)
      if (typeof nextFavorite === 'boolean') {
        if (nextFavorite && !favoriteMenus.value.some(item => normalizeFavoritePath(item.path) === normalized)) {
          favoriteMenus.value = [{ path: normalized } as PersonalMenuEntry, ...favoriteMenus.value]
        }
        if (!nextFavorite) {
          favoriteMenus.value = favoriteMenus.value.filter(item => normalizeFavoritePath(item.path) !== normalized)
        }
      } else {
        await loadFavorites()
      }
      dispatchMenuFavoritesRefresh(normalized, typeof nextFavorite === 'boolean' ? nextFavorite : undefined)
      return typeof nextFavorite === 'boolean' ? nextFavorite : isFavorite(normalized)
    } finally {
      togglingPath.value = ''
    }
  }

  const stopRefreshListener = listenMenuFavoritesRefresh(() => {
    void loadFavorites()
  })
  onBeforeUnmount(stopRefreshListener)

  return {
    favoriteMenus,
    favoritePathSet,
    loading,
    togglingPath,
    loadFavorites,
    isFavorite,
    toggleFavorite,
  }
}
