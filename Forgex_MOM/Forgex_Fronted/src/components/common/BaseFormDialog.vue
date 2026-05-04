<template>
  <!-- Modal 模式 -->
  <a-modal
    v-if="finalMode === 'modal'"
    :open="open"
    :title="title"
    :width="width"
    :confirm-loading="loading"
    :destroy-on-close="destroyOnClose"
    :body-style="bodyStyle"
    :wrap-class-name="wrapClassName"
    :mask-closable="maskClosable"
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
    :body-style="bodyStyle"
    :mask-closable="maskClosable"
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
import type { CSSProperties } from 'vue'

/**
 * 通用表单弹窗组件
 * 
 * 支持 Modal 和 Drawer 两种模式
 * 自动处理表单提交和取消逻辑
 */

const appStore = useAppStore()

interface Props {
  /** 对话框是否打开，用于控制组件的显示/隐藏状态 */
  open?: boolean
  /** 对话框标题 */
  title?: string
  /** 显示模式：modal=模态框，drawer=抽屉 */
  mode?: 'modal' | 'drawer'
  /** 对话框宽度，支持数字（单位 px）和字符串（如 "800px"） */
  width?: number | string
  /** 加载状态，true 表示提交中，会显示加载动画 */
  loading?: boolean
  /** 关闭时销毁子元素，默认 true，用于优化性能 */
  destroyOnClose?: boolean
  /** 内容区样式，供复杂表单页面按需透传 */
  bodyStyle?: CSSProperties
  /** Modal 外层类名，供个别页面定制浮层样式 */
  wrapClassName?: string
  /** 是否允许点击遮罩关闭，默认 true 以保持管理页新增编辑弹窗交互一致 */
  maskClosable?: boolean
}

interface Emits {
  /**
   * 更新对话框打开状态
   * @param value 新的打开状态
   */
  (e: 'update:open', value: boolean): void
  /**
   * 提交事件
   * 触发时机：点击确定按钮时触发
   */
  (e: 'submit'): void
  /**
   * 取消事件
   * 触发时机：点击取消按钮或关闭对话框时触发
   */
  (e: 'cancel'): void
  /**
   * 确认事件（兼容 Modal 的 ok 事件）
   * 触发时机：点击确定按钮时触发
   */
  (e: 'ok'): void
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  title: '',
  mode: undefined,
  width: 600,
  loading: false,
  destroyOnClose: true,
  bodyStyle: undefined,
  wrapClassName: '',
  maskClosable: true,
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
