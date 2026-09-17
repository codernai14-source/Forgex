<template>
  <a-modal
    :open="open"
    :title="t('layout.help.contact')"
    :width="560"
    :footer="null"
    destroy-on-close
    @cancel="emit('update:open', false)"
  >
    <a-descriptions :column="1" bordered size="small">
      <a-descriptions-item :label="t('layout.help.phone')">{{ contact.phone || '-' }}</a-descriptions-item>
      <a-descriptions-item :label="t('layout.help.email')">{{ contact.email || '-' }}</a-descriptions-item>
      <a-descriptions-item :label="t('layout.help.workTime')">{{ contact.workTime || '-' }}</a-descriptions-item>
      <a-descriptions-item :label="t('layout.help.address')">{{ contact.address || '-' }}</a-descriptions-item>
      <a-descriptions-item :label="t('layout.help.remark')">{{ contact.remark || '-' }}</a-descriptions-item>
      <a-descriptions-item :label="t('layout.help.wechatQr')">
        <img
          v-if="qrUrl"
          :src="qrUrl"
          class="help-contact-qr"
          :alt="t('layout.help.wechatQr')"
        />
        <span v-else>-</span>
      </a-descriptions-item>
    </a-descriptions>
    <div class="help-contact-footer">
      <a v-permission="'sys:help:contact:edit'" @click="goMaintain">{{ t('layout.help.gotoMaintainContact') }}</a>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 帮助中心「联系我们」只读弹窗。
 * <p>
 * 仅展示 {@code system.help.contact} 配置；有权限时跳转管理页联系 Tab。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysHelpContact
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { normalizeMediaUrl } from '@/utils/media'
import type { SysHelpContact } from '@/api/system/helpResource'

const props = defineProps<{
  open: boolean
  contact: SysHelpContact
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const qrUrl = computed(() => normalizeMediaUrl(props.contact.wechatQrUrl) || props.contact.wechatQrUrl || '')

function goMaintain() {
  emit('update:open', false)
  void router.push({
    path: '/workspace/sys/maintenance/helpResource',
    query: { tab: 'contact' },
  })
}
</script>

<style scoped lang="less">
.help-contact-qr {
  width: 160px;
  height: 160px;
  object-fit: contain;
}

.help-contact-footer {
  margin-top: 16px;
  text-align: right;
}
</style>
