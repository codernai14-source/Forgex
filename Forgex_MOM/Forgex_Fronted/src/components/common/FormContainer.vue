<template>
  <template v-if="appStore.formMode === 'modal'">
    <a-modal
      :open="open"
      :title="title"
      :width="width"
      :confirm-loading="confirmLoading"
      :mask-closable="false"
      centered
      @update:open="handleUpdateOpen"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <slot></slot>
    </a-modal>
  </template>
  <template v-else>
    <a-drawer
      :open="open"
      :title="title"
      :width="width || '45%'"
      :mask-closable="false"
      :placement="placement || 'right'"
      :footer-style="{ textAlign: 'right' }"
      @update:open="handleUpdateOpen"
      @close="handleCancel"
    >
      <slot></slot>
      <template #footer>
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button type="primary" :loading="confirmLoading" @click="handleOk">确定</a-button>
        </a-space>
      </template>
    </a-drawer>
  </template>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'

const props = defineProps<{
  open: boolean
  title: string
  width?: string | number
  confirmLoading?: boolean
  // Drawer specific props
  placement?: 'right' | 'left' | 'top' | 'bottom'
}>()

const emit = defineEmits(['update:open', 'ok', 'cancel'])

const appStore = useAppStore()

function handleUpdateOpen(val: boolean) {
  emit('update:open', val)
  if (!val) {
    emit('cancel')
  }
}

function handleOk() {
  emit('ok')
}

function handleCancel() {
  emit('update:open', false)
  emit('cancel')
}
</script>
