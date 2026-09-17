<template>
  <a-modal :open="open" :title="t('login.forceChangeTitle')" :footer="null" @cancel="$emit('close')">
    <p>{{ t('login.forceChangeHint') }}</p>
    <a-input-password v-model:value="password" :placeholder="t('login.newPassword')" />
    <a-button type="primary" block style="margin-top: 16px" :loading="loading" @click="submit">
      {{ t('common.submit') }}
    </a-button>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { forceChangePassword } from '@/api/auth/mfa'
import { encryptSensitiveText } from '@/utils/crypto'
import type { LoginResult } from '@/api/auth/login'

const props = defineProps<{ open: boolean; ticket: string }>()
const emit = defineEmits<{ close: []; success: [LoginResult] }>()
const { t } = useI18n()
const password = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  try {
    const cipher = await encryptSensitiveText(password.value)
    const result = await forceChangePassword(props.ticket, cipher)
    emit('success', result)
  } finally {
    loading.value = false
  }
}
</script>
