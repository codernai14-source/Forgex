export interface SiteBranding {
  title?: string
  icon?: string
}

export function normalizeBrowserTitle(value: unknown, fallback = 'Forgex MOM'): string {
  const title = String(value ?? '').trim()
  return title || fallback
}

export function normalizeBrowserIcon(value: unknown): string {
  return String(value ?? '').trim()
}

export function applySiteBranding(branding: SiteBranding, fallbackTitle = 'Forgex MOM'): void {
  if (typeof document === 'undefined') return
  document.title = normalizeBrowserTitle(branding.title, fallbackTitle)
  const icon = normalizeBrowserIcon(branding.icon)
  let link = document.querySelector<HTMLLinkElement>('link[data-fx-favicon]')
  if (!icon) {
    link?.remove()
    return
  }
  if (!link) {
    link = document.createElement('link')
    link.rel = 'icon'
    link.dataset.fxFavicon = 'true'
    document.head.appendChild(link)
  }
  link.href = icon
}
