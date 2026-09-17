export const MESSAGE_SPEECH_STORAGE_KEY = 'fx-message-speech-enabled'
export const MESSAGE_SPEECH_EVENT = 'fx:speech-setting'

export function isSpeechSupported(): boolean {
  return typeof window !== 'undefined' && 'speechSynthesis' in window && 'SpeechSynthesisUtterance' in window
}

export function getSpeechEnabled(): boolean {
  if (typeof window === 'undefined') return true
  const stored = window.localStorage.getItem(MESSAGE_SPEECH_STORAGE_KEY)
  return stored === null ? true : stored === 'true'
}

export function setSpeechEnabled(enabled: boolean): void {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(MESSAGE_SPEECH_STORAGE_KEY, String(enabled))
    window.dispatchEvent(new CustomEvent(MESSAGE_SPEECH_EVENT, { detail: enabled }))
    if (!enabled && isSpeechSupported()) window.speechSynthesis.cancel()
  }
}

export function composeMessageSpeech(message: { title?: unknown; content?: unknown }): string {
  const title = String(message.title ?? '').trim()
  const content = String(message.content ?? '').trim()
  const parts = [title, content, '请及时处理'].filter(Boolean)
  return parts.join('。')
}

export function speakMessage(message: { title?: unknown; content?: unknown }): boolean {
  if (!getSpeechEnabled() || !isSpeechSupported()) return false
  const text = composeMessageSpeech(message)
  if (!text) return false
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = /^\p{Script=Han}/u.test(text) ? 'zh-CN' : (navigator.language || 'en-US')
  utterance.rate = 1
  utterance.pitch = 1
  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utterance)
  return true
}
