<template>
  <!-- Modal 模式 -->
  <a-modal
    v-if="finalMode === 'modal'"
    :open="open"
    :title="title"
    :width="width"
    :confirm-loading="loading"
    :destroy-on-close="destroyOnClose"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <slot />
    
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
  </a-modal>

  <!-- Drawer 模式 -->
  <a-drawer
    v-else
    :open="open"
    :title="title"
    :width="width"
    :destroy-on-close="destroyOnClose"
    @close="handleCancel"
  >
    <slot />
    
    <template #footer>
      <div class="drawer-footer">
        <a-space>
          <a-button @click="handleCancel">
            {{ $t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleSubmit">
            {{ $t('common.confirm') }}
          </a-button>
        </a-space>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { Modal as AModal, Drawer as ADrawer, Space as ASpace, Button as AButton } from 'ant-design-vue'

/**
 * 通用表单弹窗组件
 * 
 * 支持 Modal 和 Drawer 两种模式
 * 自动处理表单提交和取消逻辑
 */

const appStore = useAppStore()

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
  (e: 'ok'): void // 兼容 Modal 的 ok 事件
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  title: '',
  mode: undefined,
  width: 600,
  loading: false,
  destroyOnClose: true,
})

const emit = defineEmits<Emits>()

const finalMode = computed(() => props.mode || appStore.formMode)

/**
 * 处理提交
 */
function handleSubmit() {
  emit('submit')
  emit('ok') // 兼容部分组件监听 ok
}

/**
 * 处理取消
 */
function handleCancel() {
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped>
.drawer-footer {
  text-align: right;
}
</style>
