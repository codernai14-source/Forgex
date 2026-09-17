<template>
  <a-card :title="t('profile.mfaTitle')">
    <p>{{ t('profile.mfaHint') }}</p>
    <img v-if="qr" :src="qr" alt="MFA QR" style="width: 180px; height: 180px" />
    <div v-if="secret">{{ t('profile.mfaSecret') }}: {{ secret }}</div>
    <a-space style="margin-top: 12px">
      <a-button type="primary" @click="startBind">{{ t('profile.mfaBind') }}</a-button>
      <a-input v-model:value="code" :placeholder="t('common.login.mfaPlaceholder')" style="width: 160px" />
      <a-button @click="confirm">{{ t('profile.mfaConfirm') }}</a-button>
      <a-button danger @click="unbind">{{ t('profile.mfaUnbind') }}</a-button>
    </a-space>
    <div v-if="recoveryCodes.length" style="margin-top: 12px">
      {{ t('profile.mfaRecovery') }}: {{ recoveryCodes.join(', ') }}
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import QRCode from 'qrcode'
import { useI18n } from 'vue-i18n'
import { bindMfa, confirmMfa, unbindMfa } from '@/api/auth/mfa'

const { t } = useI18n()
const qr = ref('')
const secret = ref('')
const code = ref('')
const recoveryCodes = ref<string[]>([])

async function startBind() {
  const result = await bindMfa()
  secret.value = result.secret
  recoveryCodes.value = result.recoveryCodes || []
  qr.value = await QRCode.toDataURL(result.otpAuthUri)
}

async function confirm() {
  await confirmMfa(code.value)
}

async function unbind() {
  await unbindMfa()
  qr.value = ''
  secret.value = ''
  recoveryCodes.value = []
}
</script>
