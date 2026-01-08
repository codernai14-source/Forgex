<template>
  <component
    :is="mode === 'modal' ? AModal : ADrawer"
    :open="open"
    :title="title"
    :width="width"
    :destroy-on-close="destroyOnClose"
    @cancel="handleCancel"
  >
    <!-- 表单内容插槽 -->
    <slot />
    
    <!-- 底部按钮 -->
    <template #footer>
      <a-space>
        <a-button @click="handleCancel">
          {{ $t('common.cancel') }}
        </a-button>
        <a-button type="primary" :loading="loading" @click="handleSubmit">
          {{ $t('common.confirm') }}
        </a-button>
      </a-space>
    </template>
  </component>
</template>

<script setup lang="ts">
import { Modal as AModal, Drawer as ADrawer, Space as ASpace, Button as AButton } from 'ant-design-vue'

/**
 * 通用表单弹窗组件
 * 
 * 支持 Modal 和 Drawer 两种模式
 * 自动处理表单提交和取消逻辑
 */

interface Props {
  /** 是否显示 */
  open?: boolean
  /** 标题 */
  title?: string
  /** 模式：modal=模态框，drawer=抽屉 */
  mode?: 'modal' | 'drawer'
  /** 宽度 */
  width?: number | string
  /** 加载状态 */
  loading?: boolean
  /** 关闭时销毁子元素 */
  destroyOnClose?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'submit'): void
  (e: 'cancel'): void
}

withDefaults(defineProps<Props>(), {
  open: false,
  title: '',
  mode: 'modal',
  width: 600,
  loading: false,
  destroyOnClose: true,
})

const emit = defineEmits<Emits>()

/**
 * 处理提交
 */
function handleSubmit() {
  emit('submit')
}

/**
 * 处理取消
 */
function handleCancel() {
  emit('update:open', false)
  emit('cancel')
}
</script>
