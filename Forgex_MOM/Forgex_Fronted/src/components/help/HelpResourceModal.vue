<template>
  <a-modal
    :open="open"
    :title="title"
    :width="1080"
    :footer="null"
    destroy-on-close
    @cancel="emit('update:open', false)"
  >
    <div class="help-resource-modal">
      <aside class="help-resource-modal__list">
        <a-input
          v-model:value="keyword"
          allow-clear
          :placeholder="t('layout.help.searchPlaceholder')"
        />
        <a-tag v-if="resolvedScope" class="help-resource-modal__scope">
          {{ resolvedScope === 'MENU' ? t('layout.help.scopeMenu') : t('layout.help.scopeGlobal') }}
        </a-tag>
        <a-empty v-if="!filteredItems.length" :description="t('layout.help.emptyDocs')" />
        <button
          v-for="item in filteredItems"
          :key="item.id"
          type="button"
          class="help-resource-modal__item"
          :class="{ 'is-active': item.id === selectedId }"
          @click="selectedId = item.id"
        >
          <strong>{{ item.title }}</strong>
          <span>{{ item.sourceType === 'EXTERNAL_URL' ? t('layout.help.sourceExternal') : item.fileName }}</span>
        </button>
      </aside>
      <section class="help-resource-modal__preview">
        <FilePreview
          v-if="selected"
          :key="`${selected.id || selected.title}-${selected.fileUrl || selected.externalUrl || ''}`"
          :file-url="selected.fileUrl"
          :file-ext="selected.fileExt"
          :file-name="selected.fileName"
          :source-type="selected.sourceType"
          :external-url="selected.externalUrl"
        />
        <a-empty v-else :description="t('layout.help.selectDoc')" />
      </section>
    </div>
    <div v-if="canMaintain" class="help-resource-modal__footer">
      <a @click="goMaintain">{{ t('layout.help.gotoMaintain') }}</a>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 帮助手册 / 视频只读弹窗。
 * <p>
 * 左侧列表支持标题过滤和范围标签，右侧走公共 {@code FilePreview}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FilePreview
 */
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import FilePreview from '@/components/common/FilePreview.vue'
import type { HelpDocType, HelpScopeType, SysHelpResource } from '@/api/system/helpResource'
import { usePermissionStore } from '@/stores/permission'

const props = defineProps<{
  open: boolean
  title: string
  docType: HelpDocType
  items: SysHelpResource[]
  resolvedScope?: HelpScopeType
  menuPath?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n({ useScope: 'global' })
const router = useRouter()
const permissionStore = usePermissionStore()
const keyword = ref('')
const selectedId = ref<number>()
const canMaintain = computed(() => permissionStore.hasAnyPermission(['sys:help:add', 'sys:help:edit', 'sys:help:view']))

const filteredItems = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) {
    return props.items
  }
  return props.items.filter(item => String(item.title || '').toLowerCase().includes(text))
})

const selected = computed(() => filteredItems.value.find(item => item.id === selectedId.value) || filteredItems.value[0])

watch(
  () => [props.open, props.items],
  () => {
    selectedId.value = props.items[0]?.id
    keyword.value = ''
  }
)

/**
 * 跳转管理页并带上当前类型与菜单路径。
 */
function goMaintain() {
  emit('update:open', false)
  void router.push({
    path: '/workspace/sys/maintenance/helpResource',
    query: {
      docType: props.docType,
      menuPath: props.menuPath || '',
    },
  })
}
</script>

<style scoped lang="less">
.help-resource-modal {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  min-height: 560px;
}

.help-resource-modal__preview {
  min-height: 560px;
}

.help-resource-modal__list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.help-resource-modal__item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.help-resource-modal__item.is-active,
.help-resource-modal__item:hover {
  background: var(--ant-color-fill-tertiary);
}

.help-resource-modal__item span {
  color: var(--ant-color-text-secondary);
  font-size: 12px;
}

.help-resource-modal__footer {
  margin-top: 12px;
  text-align: right;
}
</style>
