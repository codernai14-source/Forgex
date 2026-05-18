<template>
  <a-modal
    :open="open"
    :title="title"
    :width="1200"
    destroy-on-close
    :footer="null"
    @cancel="emit('update:open', false)"
  >
    <a-empty v-if="!files.length" :description="t('system.codegen.previewEmpty')" />
    <div v-else class="codegen-preview">
      <div class="codegen-preview__tree">
        <a-tree
          :tree-data="fileTreeData"
          :selected-keys="selectedKeys"
          block-node
          @select="handleFileSelect"
        />
      </div>
      <div class="codegen-preview__viewer">
        <div class="codegen-preview__viewer-header">{{ activeFile?.path || title }}</div>
        <pre class="codegen-preview__code">{{ activeFile?.content || '' }}</pre>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { CodegenPreviewFile } from '@/api/system/codegen'

interface Props {
  open: boolean
  title: string
  files: CodegenPreviewFile[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

interface FileTreeNode {
  title: string
  key: string
  children?: FileTreeNode[]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })
const selectedFilePath = ref('')

const activeFile = computed(() =>
  props.files.find((item) => item.path === selectedFilePath.value),
)

const selectedKeys = computed(() => (selectedFilePath.value ? [selectedFilePath.value] : []))
const fileTreeData = computed<FileTreeNode[]>(() => buildFileTree(props.files))

function handleFileSelect(keys: string[]) {
  selectedFilePath.value = keys[0] || ''
}

function buildFileTree(files: CodegenPreviewFile[]) {
  const rootMap = new Map<string, any>()
  files.forEach((file) => {
    const parts = file.path.split('/').filter(Boolean)
    let currentMap = rootMap
    let currentPath = ''
    parts.forEach((part, index) => {
      currentPath = currentPath ? `${currentPath}/${part}` : part
      if (!currentMap.has(part)) {
        currentMap.set(part, {
          title: part,
          key: index === parts.length - 1 ? file.path : currentPath,
          children: new Map<string, any>(),
        })
      }
      const node = currentMap.get(part)
      currentMap = node.children
    })
  })
  return Array.from(rootMap.values()).map(mapNode)
}

function mapNode(item: any): FileTreeNode {
  return {
    title: item.title,
    key: item.key,
    children: item.children.size > 0 ? Array.from(item.children.values()).map(mapNode) : undefined,
  }
}

watch(
  () => props.open,
  (visible) => {
    if (visible) {
      selectedFilePath.value = props.files[0]?.path || ''
    }
  },
  { immediate: true },
)
</script>

<style scoped lang="less" src="@/styles/views/system/codegen/components/codegen-preview-modal.less"></style>
