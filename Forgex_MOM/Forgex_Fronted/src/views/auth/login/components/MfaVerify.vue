<template>
  <a-modal :open="open" :title="t('login.mfaTitle')" :footer="null" @cancel="$emit('close')">
    <p>{{ t('login.mfaHint') }}</p>
    <a-input v-model:value="code" :placeholder="t('login.mfaPlaceholder')" maxlength="16" />
    <a-button type="primary" block style="margin-top: 16px" :loading="loading" @click="submit">
      {{ t('login.mfaSubmit') }}
    </a-button>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { verifyMfa } from '@/api/auth/mfa'
import type { LoginResult } from '@/api/auth/login'

const props = defineProps<{ open: boolean; challengeId: string }>()
const emit = defineEmits<{ close: []; success: [LoginResult] }>()
const { t } = useI18n()
const code = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  try {
    const result = await verifyMfa(props.challengeId, code.value)
    emit('success', result)
  } finally {
    loading.value = false
  }
}
</script>
