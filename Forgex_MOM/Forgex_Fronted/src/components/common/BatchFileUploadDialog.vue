<template>
  <a-modal
    :open="open"
    :title="title"
    :width="560"
    :confirm-loading="loading"
    :ok-button-props="{ disabled: !files.length }"
    :ok-text="resolvedOkText"
    :cancel-text="resolvedCancelText"
    wrap-class-name="batch-file-upload-modal"
    destroy-on-close
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <div class="batch-file-upload-dialog" :class="{ 'is-filled': files.length }">
      <a-upload-dragger
        class="batch-file-upload-dialog__dropzone"
        :accept="accept"
        :multiple="multiple"
        :show-upload-list="false"
        :disabled="loading || reachedMaxCount"
        :before-upload="beforeUpload"
      >
        <div class="batch-file-upload-dialog__content">
          <span class="batch-file-upload-dialog__icon-well" aria-hidden="true">
            <InboxOutlined />
          </span>
          <p class="batch-file-upload-dialog__title">
            {{ resolvedDragText }}
            <span>{{ resolvedChooseText }}</span>
          </p>
          <p class="batch-file-upload-dialog__hint">{{ resolvedHint }}</p>
          <p v-if="acceptLabel" class="batch-file-upload-dialog__accept">{{ acceptLabel }}</p>
        </div>
      </a-upload-dragger>

      <div v-if="files.length" class="batch-file-upload-dialog__list">
        <div class="batch-file-upload-dialog__list-title">
          <span>{{ resolvedSelectedText }} · {{ files.length }}</span>
          <button
            class="batch-file-upload-dialog__link"
            type="button"
            :disabled="loading"
            @click="clearFiles"
          >
            {{ resolvedClearText }}
          </button>
        </div>
        <div
          v-for="item in files"
          :key="item.uid"
          class="batch-file-upload-dialog__item"
        >
          <span class="batch-file-upload-dialog__file-icon" aria-hidden="true">
            <FileOutlined />
          </span>
          <div class="batch-file-upload-dialog__file-info">
            <span class="batch-file-upload-dialog__file-name" :title="item.file.name">{{ item.file.name }}</span>
            <span class="batch-file-upload-dialog__file-size">{{ formatFileSize(item.file.size) }}</span>
          </div>
          <button
            class="batch-file-upload-dialog__remove"
            type="button"
            :disabled="loading"
            :aria-label="resolvedRemoveText"
            @click="removeFile(item.uid)"
          >
            <DeleteOutlined />
          </button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 公共批量文件选择弹窗。
 * <p>
 * 只负责本地选文件，不直接打上传接口。确认后把 {@code File[]} 交给调用方。
 * 视觉对齐 {@link CommonImportDialog}：主题色拖拽区、空态主次、已选文件抬一层。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @see CommonImportDialog
 */

import { computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { DeleteOutlined, FileOutlined, InboxOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'

/**
 * 弹窗内已选文件的稳定包装。
 */
interface SelectedFile {
  /** 列表渲染键，避免同名文件冲突。 */
  uid: string
  /** 原始浏览器文件对象。 */
  file: File
}

interface Props {
  open?: boolean
  title?: string
  accept?: string
  multiple?: boolean
  maxCount?: number
  maxSize?: number
  loading?: boolean
  dragText?: string
  chooseText?: string
  hint?: string
  selectedText?: string
  clearText?: string
  removeText?: string
  okText?: string
  cancelText?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  title: '',
  accept: '',
  multiple: true,
  maxCount: 0,
  maxSize: 0,
  loading: false,
  dragText: '',
  chooseText: '',
  hint: '',
  selectedText: '',
  clearText: '',
  removeText: '',
  okText: '',
  cancelText: '',
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', files: File[]): void
}>()

const { t } = useI18n({ useScope: 'global' })
const files = defineModel<SelectedFile[]>('files', { default: () => [] })
const reachedMaxCount = computed(() => props.maxCount > 0 && files.value.length >= props.maxCount)

const resolvedDragText = computed(() => props.dragText || t('common.batchFileUpload.dragText'))
const resolvedChooseText = computed(() => props.chooseText || t('common.batchFileUpload.chooseFile'))
const resolvedHint = computed(() => {
  if (props.hint) {
    return props.hint
  }
  return props.multiple ? t('common.batchFileUpload.hint') : t('common.batchFileUpload.hintSingle')
})
const resolvedSelectedText = computed(() => props.selectedText || t('common.batchFileUpload.selected'))
const resolvedClearText = computed(() => props.clearText || t('common.batchFileUpload.clear'))
const resolvedRemoveText = computed(() => props.removeText || t('common.batchFileUpload.remove'))
const resolvedOkText = computed(() => props.okText || t('common.upload'))
const resolvedCancelText = computed(() => props.cancelText || t('common.cancel'))

/**
 * 把 {@code accept} 转成可读格式提示，例如 {@code .jpg,.png} → {@code JPG / PNG}。
 *
 * @returns 已本地化的格式说明；未配置 accept 时返回空字符串。
 */
const acceptLabel = computed(() => {
  if (!props.accept) {
    return ''
  }

  const types = props.accept
    .split(',')
    .map(item => item.trim().replace(/^\./, '').toUpperCase())
    .filter(Boolean)

  if (!types.length) {
    return ''
  }

  return t('common.batchFileUpload.formats', { types: types.join(' / ') })
})

watch(
  () => props.open,
  (visible, previousVisible) => {
    if (previousVisible && !visible) {
      clearFiles()
    }
  },
)

/**
 * 拦截 Ant Upload 的自动上传，只把合法文件收入本地列表。
 *
 * @param file 用户刚选择的文件。
 * @returns 始终 {@code false}，避免组件自行 POST。
 */
function beforeUpload(file: File) {
  if (props.maxSize > 0 && file.size > props.maxSize) {
    message.error(t('common.batchFileUpload.sizeExceeded', {
      name: file.name,
      size: formatFileSize(props.maxSize),
    }))
    return false
  }

  if (isDuplicate(file)) {
    return false
  }

  if (!props.multiple) {
    files.value = [toSelectedFile(file)]
    return false
  }

  if (reachedMaxCount.value) {
    message.warning(t('common.batchFileUpload.maxCount', { count: props.maxCount }))
    return false
  }

  files.value = [...files.value, toSelectedFile(file)]
  return false
}

/**
 * 把当前选中的文件交给调用方，由页面继续走实际上传接口。
 */
function handleSubmit() {
  if (files.value.length) {
    emit('submit', files.value.map(item => item.file))
  }
}

/**
 * 关闭弹窗。关闭后 {@code open} 监听会清空已选文件。
 */
function handleCancel() {
  emit('update:open', false)
}

/**
 * 清空待上传列表，拖拽区回到空态。
 */
function clearFiles() {
  files.value = []
}

/**
 * 从待上传列表移除单个文件。
 *
 * @param uid 文件包装键。
 */
function removeFile(uid: string) {
  files.value = files.value.filter(item => item.uid !== uid)
}

/**
 * 用文件名、大小和修改时间判断是否已在列表中。
 *
 * @param file 待检查文件。
 * @returns 已存在时返回 {@code true}。
 */
function isDuplicate(file: File) {
  return files.value.some(item => (
    item.file.name === file.name
    && item.file.size === file.size
    && item.file.lastModified === file.lastModified
  ))
}

/**
 * 生成列表渲染用的稳定 uid。
 *
 * @param file 原始文件。
 * @returns 带 uid 的包装对象。
 */
function toSelectedFile(file: File): SelectedFile {
  return {
    uid: `${file.name}-${file.size}-${file.lastModified}-${Math.random().toString(36).slice(2)}`,
    file,
  }
}

/**
 * 把字节数格式化为 B / KB / MB。
 *
 * @param size 文件大小，单位字节。
 * @returns 带单位的短文本。
 */
function formatFileSize(size: number) {
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}
</script>

<style scoped lang="less">
.batch-file-upload-dialog {
  display: flex;
  flex-direction: column;
  gap: 14px;

  :deep(.ant-upload-drag) {
    min-height: 168px;
    padding: 0;
    border-color: color-mix(in srgb, var(--fx-text-tertiary) 42%, transparent);
    border-style: dashed;
    border-radius: var(--fx-radius-lg);
    background:
      linear-gradient(
        145deg,
        color-mix(in srgb, var(--fx-bg-container) 94%, var(--fx-primary) 6%),
        color-mix(in srgb, var(--fx-bg-base) 68%, var(--fx-bg-container) 32%)
      );
    box-shadow: inset 0 1px 0 color-mix(in srgb, white 6%, transparent);
    transition: border-color 0.2s ease, background 0.2s ease;

    &:hover,
    &:focus-within {
      border-color: var(--fx-primary);
      background:
        linear-gradient(
          145deg,
          color-mix(in srgb, var(--fx-bg-container) 88%, var(--fx-primary) 12%),
          color-mix(in srgb, var(--fx-bg-base) 58%, var(--fx-primary) 8%)
        );
    }
  }

  :deep(.ant-upload-btn) {
    padding: 22px 16px;
  }

  :deep(.ant-upload.ant-upload-disabled .ant-upload-drag) {
    opacity: 0.64;
  }

  &.is-filled :deep(.ant-upload-drag) {
    min-height: 92px;
  }

  &.is-filled :deep(.ant-upload-btn) {
    padding: 14px 16px;
  }

  &__content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  &__icon-well {
    display: grid;
    place-items: center;
    width: 52px;
    height: 52px;
    border-radius: var(--fx-radius-lg);
    color: var(--fx-primary);
    background: var(--fx-primary-soft);
    font-size: 24px;
    line-height: 1;
    box-shadow: inset 0 1px 0 color-mix(in srgb, white 8%, transparent);
  }

  &.is-filled &__icon-well {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  &__title {
    margin: 0;
    color: var(--fx-text-primary);
    font-size: 15px;
    font-weight: 600;
    line-height: 1.4;

    span {
      color: var(--fx-primary);
    }
  }

  &__hint,
  &__accept {
    margin: 0;
    color: var(--fx-text-secondary);
    font-size: 13px;
    line-height: 1.4;
  }

  &__accept {
    color: var(--fx-text-tertiary);
    font-size: 12px;
  }

  &__list {
    overflow: hidden;
    border: 1px solid color-mix(in srgb, var(--fx-border-color) 72%, transparent);
    border-radius: var(--fx-radius-lg);
    background: color-mix(in srgb, var(--fx-bg-base) 54%, transparent);
  }

  &__list-title,
  &__item {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__list-title {
    justify-content: space-between;
    min-height: 40px;
    padding: 0 14px;
    border-bottom: 1px solid color-mix(in srgb, var(--fx-border-color) 72%, transparent);
    color: var(--fx-text-secondary);
    font-size: 13px;
  }

  &__item {
    min-height: 56px;
    padding: 8px 10px 8px 14px;
  }

  &__item + &__item {
    border-top: 1px solid color-mix(in srgb, var(--fx-border-color) 55%, transparent);
  }

  &__file-icon {
    display: grid;
    place-items: center;
    width: 32px;
    height: 36px;
    border-radius: var(--fx-radius-sm);
    color: var(--fx-primary);
    background: var(--fx-primary-soft);
    flex: 0 0 auto;
    font-size: 16px;
  }

  &__file-info {
    display: flex;
    flex: 1;
    min-width: 0;
    flex-direction: column;
    gap: 2px;
  }

  &__file-name {
    overflow: hidden;
    color: var(--fx-text-primary);
    font-size: 14px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__file-size {
    color: var(--fx-text-tertiary);
    font-size: 12px;
  }

  &__link,
  &__remove {
    border: 0;
    background: transparent;
    cursor: pointer;
    color: var(--fx-primary);
    font-size: 13px;
    line-height: 1;

    &:disabled {
      cursor: not-allowed;
      color: var(--fx-text-disabled);
    }
  }

  &__remove {
    display: grid;
    place-items: center;
    width: 28px;
    height: 28px;
    border-radius: var(--fx-radius-sm);
    color: var(--fx-text-tertiary);

    &:hover:not(:disabled) {
      color: var(--fx-error);
      background: var(--fx-error-bg);
    }
  }
}
</style>
