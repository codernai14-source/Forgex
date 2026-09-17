<template>
  <div class="fx-help-toolbar" :class="{ 'is-expanded': expanded }">
    <Transition name="fx-help-toolbar-fade">
      <div v-if="expanded" class="fx-help-toolbar__stack">
        <a-tooltip :title="t('layout.help.manual')" placement="left">
          <a-button class="fx-help-toolbar__action" shape="circle" @click="openManual">
            <template #icon><FileTextOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip :title="t('layout.help.video')" placement="left">
          <a-button class="fx-help-toolbar__action" shape="circle" @click="openVideo">
            <template #icon><PlayCircleOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip :title="t('layout.help.contact')" placement="left">
          <a-button class="fx-help-toolbar__action" shape="circle" @click="openContact">
            <template #icon><PhoneOutlined /></template>
          </a-button>
        </a-tooltip>
        <a-tooltip v-if="hasPageGuide" :title="t('layout.help.pageGuide')" placement="left">
          <a-button class="fx-help-toolbar__action" shape="circle" @click="replayGuide">
            <template #icon><CompassOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>
    </Transition>
    <a-tooltip :title="expanded ? t('layout.help.collapse') : t('layout.help.expand')" placement="left">
      <a-button class="fx-help-toolbar__toggle" type="primary" shape="circle" @click="toggleExpanded">
        <template #icon>
          <CloseOutlined v-if="expanded" />
          <QuestionCircleOutlined v-else />
        </template>
      </a-button>
    </a-tooltip>
  </div>

  <HelpResourceModal
    v-model:open="manualOpen"
    :title="t('layout.help.manual')"
    doc-type="MANUAL"
    :items="manualItems"
    :resolved-scope="manualScope"
    :menu-path="currentPath"
  />
  <HelpResourceModal
    v-model:open="videoOpen"
    :title="t('layout.help.video')"
    doc-type="VIDEO"
    :items="videoItems"
    :resolved-scope="videoScope"
    :menu-path="currentPath"
  />
  <HelpContactModal v-model:open="contactOpen" :contact="contact" />
</template>

<script setup lang="ts">
/**
 * 工作区右下角帮助悬浮栏。
 * <p>
 * 提供手册、视频、联系我们与可选本页引导入口；弹窗只读，维护跳转管理页。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see HelpResourceModal
 * @see HelpContactModal
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { CloseOutlined, CompassOutlined, FileTextOutlined, PhoneOutlined, PlayCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue'
import { helpResourceApi, type HelpScopeType, type SysHelpContact, type SysHelpResource } from '@/api/system/helpResource'
import { hasSystemPageGuide } from '@/guide/systemPageGuides'
import { normalizeWorkspacePath } from '@/utils/workspacePath'
import HelpContactModal from './HelpContactModal.vue'
import HelpResourceModal from './HelpResourceModal.vue'

const STORAGE_KEY = 'fx-help-toolbar-expanded'
const { t } = useI18n({ useScope: 'global' })
const route = useRoute()
const expanded = ref(localStorage.getItem(STORAGE_KEY) !== '0')
const manualOpen = ref(false)
const videoOpen = ref(false)
const contactOpen = ref(false)
const manualItems = ref<SysHelpResource[]>([])
const videoItems = ref<SysHelpResource[]>([])
const manualScope = ref<HelpScopeType>()
const videoScope = ref<HelpScopeType>()
const contact = ref<SysHelpContact>({})

const currentPath = computed(() => normalizeWorkspacePath(route.fullPath || route.path))
const hasPageGuide = computed(() => hasSystemPageGuide(currentPath.value))

function toggleExpanded() {
  expanded.value = !expanded.value
  localStorage.setItem(STORAGE_KEY, expanded.value ? '1' : '0')
}

function collapseAfterOpen() {
  expanded.value = false
  localStorage.setItem(STORAGE_KEY, '0')
}

async function openManual() {
  try {
    const result = await helpResourceApi.listForPage('MANUAL', currentPath.value)
    manualItems.value = result?.items || []
    manualScope.value = result?.resolvedScope
    manualOpen.value = true
    collapseAfterOpen()
  } catch {
    // 错误提示由 http 封装处理，失败时不打开空弹窗。
  }
}

async function openVideo() {
  try {
    const result = await helpResourceApi.listForPage('VIDEO', currentPath.value)
    videoItems.value = result?.items || []
    videoScope.value = result?.resolvedScope
    videoOpen.value = true
    collapseAfterOpen()
  } catch {
    // 错误提示由 http 封装处理，失败时不打开空弹窗。
  }
}

async function openContact() {
  try {
    contact.value = (await helpResourceApi.getContact(false)) || {}
    contactOpen.value = true
    collapseAfterOpen()
  } catch {
    // 错误提示由 http 封装处理。
  }
}

function replayGuide() {
  window.dispatchEvent(new CustomEvent('fx:replay-page-guide'))
  collapseAfterOpen()
}

onMounted(() => {
  void helpResourceApi.getContact().then((data) => {
    contact.value = data || {}
  }).catch(() => undefined)
})
</script>

<style scoped lang="less">
.fx-help-toolbar {
  position: absolute;
  right: 24px;
  bottom: 96px;
  z-index: 30;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.fx-help-toolbar__stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.fx-help-toolbar__action,
.fx-help-toolbar__toggle {
  width: 48px;
  height: 48px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.24);
}

.fx-help-toolbar__action {
  font-size: 20px;
}

.fx-help-toolbar__action :deep(.anticon),
.fx-help-toolbar__toggle :deep(.anticon) {
  font-size: 20px;
}

.fx-help-toolbar-fade-enter-active,
.fx-help-toolbar-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fx-help-toolbar-fade-enter-from,
.fx-help-toolbar-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
