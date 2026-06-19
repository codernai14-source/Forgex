import { sm2 } from 'sm-crypto'
import { getPublicKey } from '@/api/auth/login'

let publicKeyCache = ''

export async function encryptSensitiveText(value: string) {
  if (!value) {
    throw new Error('敏感信息不能为空')
  }
  const publicKey = await getTransportPublicKey()
  try {
    const cipherText = sm2.doEncrypt(value, publicKey, 1)
    if (!cipherText) {
      throw new Error()
    }
    return cipherText.startsWith('04') ? cipherText : `04${cipherText}`
  } catch {
    throw new Error('敏感信息加密失败')
  }
}

export async function getTransportPublicKey() {
  if (!publicKeyCache) {
    publicKeyCache = normalizeSm2PublicKey(await getPublicKey() as string)
  }
  if (!publicKeyCache) {
    throw new Error('SM2 传输加密公钥未配置或格式不正确')
  }
  return publicKeyCache
}

export function clearTransportPublicKeyCache() {
  publicKeyCache = ''
}

function normalizeSm2PublicKey(publicKey?: string) {
  const key = publicKey?.trim()
  if (!key) {
    return ''
  }

  const compactKey = key.replace(/\s+/g, '')
  if (/^04[0-9a-fA-F]{128}$/.test(compactKey)) {
    return compactKey
  }
  if (/^[0-9a-fA-F]{128}$/.test(compactKey)) {
    return `04${compactKey}`
  }

  const bytes = decodeBase64Key(key)
  if (bytes.length === 65 && bytes[0] === 0x04) {
    return bytesToHex(bytes)
  }
  if (bytes.length === 64) {
    return `04${bytesToHex(bytes)}`
  }

  const bitStringPoint = findSpkiUncompressedPoint(bytes)
  if (bitStringPoint.length === 65) {
    return bytesToHex(bitStringPoint)
  }

  const pointStart = bytes.findIndex((byte, index) => byte === 0x04 && bytes.length - index >= 65)
  if (pointStart >= 0) {
    return bytesToHex(bytes.slice(pointStart, pointStart + 65))
  }
  return ''
}

function findSpkiUncompressedPoint(bytes: number[]) {
  for (let index = 0; index <= bytes.length - 68; index++) {
    if (bytes[index] === 0x03 && bytes[index + 1] === 0x42 && bytes[index + 2] === 0x00 && bytes[index + 3] === 0x04) {
      return bytes.slice(index + 3, index + 68)
    }
  }
  return []
}

function decodeBase64Key(publicKey: string) {
  const normalized = publicKey
    .replace(/-----BEGIN PUBLIC KEY-----/g, '')
    .replace(/-----END PUBLIC KEY-----/g, '')
    .replace(/\s+/g, '')
  try {
    const binary = window.atob(normalized)
    return Array.from(binary, (char) => char.charCodeAt(0))
  } catch {
    return []
  }
}

function bytesToHex(bytes: number[]) {
  return bytes.map((byte) => byte.toString(16).padStart(2, '0')).join('')
}
