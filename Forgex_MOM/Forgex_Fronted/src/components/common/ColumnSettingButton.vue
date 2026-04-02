<template>
  <!--
    使用 Popover 而非 Dropdown：Dropdown 会将 overlay 当作 Menu 处理（注入 ant-dropdown-menu 等），
    自定义面板在受控 open 下易与 Trigger 的 document 点击检测产生竞态，表现为闪一下即关。
    Popover 基于 Tooltip，适合任意内容浮层。
  -->
  <a-popover
    v-model:open="dropdownOpen"
    trigger="click"
    placement="bottomRight"
    :arrow="false"
    overlay-class-name="fx-column-setting-popover"
    :overlay-inner-style="popoverOverlayInnerStyle"
    :get-popup-container="getColumnSettingPopupContainer"
    :destroy-tooltip-on-hide="false"
  >
    <template #content>
      <div class="column-setting-panel" :style="panelSurfaceStyle">
        <div class="column-setting-header">
          <span class="title">{{ t('system.tableConfig.columnSetting.title') }}</span>
          <a-space>
            <a-button type="link" size="small" @click="handleReset">
              {{ t('system.tableConfig.columnSetting.reset') }}
            </a-button>
            <a-button type="primary" size="small" @click="handleSave" :loading="saving">
              {{ t('common.save') }}
            </a-button>
          </a-space>
        </div>

        <div class="column-setting-body">
          <div class="column-list">
            <div
              v-for="(col, index) in localColumns"
              :key="col.field"
              class="column-item"
            >
              <div class="column-item-left">
                <a-checkbox
                  v-model:checked="col.visible"
                  :disabled="col.field === 'action'"
                >
                  {{ col.title }}
                </a-checkbox>
              </div>
              <div class="column-item-right">
                <a-button
                  type="text"
                  size="small"
                  :disabled="index === 0"
                  @click="moveUp(index)"
                >
                  <UpOutlined />
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  :disabled="index === localColumns.length - 1"
                  @click="moveDown(index)"
                >
                  <DownOutlined />
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <div class="column-setting-footer">
          <span class="hint">{{ t('system.tableConfig.columnSetting.hint') }}</span>
        </div>
      </div>
    </template>

    <a-button type="primary" html-type="button" class="column-setting-btn">
      <template #icon>
        <SettingOutlined />
      </template>
      {{ t('system.tableConfig.columnSetting.title') }}
    </a-button>
  </a-popover>
</template>

<script setup lang="ts">
/**
 * 列设置按钮组件
 * <p>
 * 提供表格列的显示/隐藏和排序设置功能，支持保存用户个性化配置。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
import { nextTick, ref, watch } from 'vue'
import type { CSSProperties } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { SettingOutlined, UpOutlined, DownOutlined } from '@ant-design/icons-vue'
import {
  getUserColumns,
  saveUserColumns,
  resetUserColumns,
  type FxTableColumn,
  type UserColumnItem
} from '@/api/system/tableConfig'

/**
 * Popover 内层（.ant-popover-inner）样式。
 * <p>
 * 使用与面板一致的 elevated 背景，避免仅最内层 div 着色时 Ant 结构缝隙透底；
 * 变量依赖 {@link getColumnSettingPopupContainer} 将浮层挂到 {@code .fx-main-layout} 内解析。
 * </p>
 */
const popoverOverlayInnerStyle: CSSProperties = {
  padding: 0,
  backgroundColor: 'var(--fx-bg-elevated)',
  color: 'var(--fx-text-primary)',
  boxShadow: 'none',
}

/**
 * 列表面板表面（与 elevated 浮层同色系，并保留边框与圆角）。
 */
const panelSurfaceStyle: CSSProperties = {
  backgroundColor: 'var(--fx-bg-elevated)',
  color: 'var(--fx-text-primary)',
  border: '1px solid var(--fx-border-color)',
  borderRadius: 'var(--fx-radius-lg)',
  padding: '12px',
  width: '300px',
  minWidth: '300px',
  maxWidth: '360px',
}

/**
 * 将列设置浮层挂到主布局内，使 {@code --fx-*} 主题变量生效（默认挂 body 时变量未定义，导致透底与浅色字色）。
 *
 * @param triggerNode 触发器 DOM 节点
 * @returns 挂载容器元素
 */
function getColumnSettingPopupContainer(triggerNode: HTMLElement): HTMLElement {
  const layout = triggerNode?.closest?.('.fx-main-layout')
  if (layout instanceof HTMLElement) {
    return layout
  }
  return document.body
}

/**
 * 组件属性
 */
const props = defineProps<{
  /**
   * 表格编码，用于获取和保存用户的列配置
   */
  tableCode: string

  /**
   * 原始列配置数组，来自表格配置的 columns 字段
   */
  columns: FxTableColumn[]
}>()

/**
 * 组件事件
 */
const emit = defineEmits<{
  /**
   * 列配置变更事件
   * 触发时机：用户点击保存按钮成功保存列配置后触发
   * @param columns 更新后的列配置数组，包含可见性和排序信息
   */
  (e: 'change', columns: FxTableColumn[]): void
}>()

const { t } = useI18n()

/**
 * 下拉层是否展开（Ant Design Vue 4 使用 open，与 visible 对应）
 */
const dropdownOpen = ref(false)

/**
 * 保存中状态
 */
const saving = ref(false)

/**
 * 本地列配置（用于编辑）
 */
const localColumns = ref<Array<{ field: string; title: string; visible: boolean; order: number }>>([])

/**
 * 初始化本地列配置
 * <p>
 * 将原始列配置转换为本地可编辑格式。
 * </p>
 */
function initLocalColumns() {
  localColumns.value = props.columns.map((col, index) => ({
    field: col.field,
    title: col.title,
    visible: col.visible !== false, // 默认显示
    order: col.order ?? index
  }))
  // 按 order 排序
  localColumns.value.sort((a, b) => a.order - b.order)
}

/**
 * 加载用户列配置
 * <p>
 * 从后端加载用户个性化的列配置。
 * </p>
 */
async function loadUserConfig() {
  try {
    const result = await getUserColumns(props.tableCode)
    if (result && result.columns && result.columns.length > 0) {
      // 应用用户配置
      const userColumnsMap = new Map<string, FxTableColumn>()
      result.columns.forEach(col => {
        userColumnsMap.set(col.field, col)
      })

      localColumns.value = props.columns.map((col, index) => {
        const userCol = userColumnsMap.get(col.field)
        return {
          field: col.field,
          title: col.title,
          visible: userCol?.visible !== false,
          order: userCol?.order ?? index
        }
      })
      // 按 order 排序
      localColumns.value.sort((a, b) => a.order - b.order)
    } else {
      // 用户没有配置，使用默认配置
      initLocalColumns()
    }
  } catch (error) {
    console.error('加载用户列配置失败:', error)
    // 使用默认配置
    initLocalColumns()
  }
}

/**
 * 展开时先同步填充默认列，再异步拉取用户列配置。
 * <p>
 * 使用 {@link nextTick} 推迟首帧数据写入，避免与 Popover/Tooltip 打开过程同一 tick 内强刷子树导致 open 状态异常。
 * </p>
 */
watch(dropdownOpen, open => {
  if (!open) {
    return
  }
  void nextTick(() => {
    initLocalColumns()
    void loadUserConfig()
  })
})

/**
 * 向上移动列
 *
 * @param index 当前索引
 */
function moveUp(index: number) {
  if (index <= 0) return
  const cols = localColumns.value
  // 交换 order
  const currentOrder = cols[index].order
  const prevOrder = cols[index - 1].order
  cols[index].order = prevOrder
  cols[index - 1].order = currentOrder
  // 重新排序
  cols.sort((a, b) => a.order - b.order)
}

/**
 * 向下移动列
 *
 * @param index 当前索引
 */
function moveDown(index: number) {
  if (index >= localColumns.value.length - 1) return
  const cols = localColumns.value
  // 交换 order
  const currentOrder = cols[index].order
  const nextOrder = cols[index + 1].order
  cols[index].order = nextOrder
  cols[index + 1].order = currentOrder
  // 重新排序
  cols.sort((a, b) => a.order - b.order)
}

/**
 * 保存用户列配置
 */
async function handleSave() {
  saving.value = true
  try {
    // 构建保存参数
    const columns: UserColumnItem[] = localColumns.value.map((col, index) => ({
      field: col.field,
      visible: col.visible,
      order: index // 使用当前顺序作为 order
    }))

    await saveUserColumns({
      tableCode: props.tableCode,
      columns
    })

    message.success(t('common.saveSuccess'))
    dropdownOpen.value = false

    // 触发变更事件，更新表格列
    const updatedColumns: FxTableColumn[] = props.columns.map(baseCol => {
      const localCol = localColumns.value.find(lc => lc.field === baseCol.field)
      return {
        ...baseCol,
        visible: localCol?.visible ?? true,
        order: localCol?.order ?? 0
      }
    })
    // 按 order 排序并过滤隐藏列
    const sortedColumns = updatedColumns
      .filter(col => col.visible !== false)
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))

    emit('change', sortedColumns)
  } catch (error) {
    console.error('保存用户列配置失败:', error)
    message.error(t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}

/**
 * 重置用户列配置
 */
async function handleReset() {
  try {
    await resetUserColumns(props.tableCode)
    message.success(t('system.tableConfig.columnSetting.resetSuccess'))
    // 重置为默认配置
    initLocalColumns()
    dropdownOpen.value = false

    // 触发变更事件，恢复默认列配置
    emit('change', props.columns)
  } catch (error) {
    console.error('重置用户列配置失败:', error)
    message.error(t('common.operationFailed'))
  }
}

// 监听原始列配置变化
watch(
  () => props.columns,
  () => {
    if (dropdownOpen.value) {
      loadUserConfig()
    }
  },
  { deep: true }
)

// 监听 tableCode 变化
watch(
  () => props.tableCode,
  () => {
    if (dropdownOpen.value) {
      loadUserConfig()
    }
  }
)
</script>

<style scoped>
/**
 * 主按钮样式由 Ant Design Vue token / 全局主题（含系统主题色）统一控制，
 * 与工具栏「新增」等 type="primary" 按钮一致。
 */
.column-setting-btn {
  flex-shrink: 0;
}

/* 背景/边框/阴影见 panelSurfaceStyle 内联 */

.column-setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--fx-border-color, #f0f0f0);
}

.column-setting-header .title {
  font-weight: 500;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
}

.column-setting-body {
  max-height: 300px;
  overflow-y: auto;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.column-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 8px;
  border-radius: var(--fx-radius-sm, 4px);
  transition: background-color 0.2s;
}

.column-item:hover {
  background-color: var(--fx-fill-alter, var(--fx-fill, rgba(0, 0, 0, 0.04)));
}

.column-item-left {
  flex: 1;
  min-width: 0;
}

.column-item-right {
  display: flex;
  gap: 4px;
}

/* 勾选列名：与面板主字同色，避免 Ant Checkbox 在浮层内继承错误 token */
.column-setting-panel :deep(.ant-checkbox-wrapper) {
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
}

.column-setting-panel :deep(.ant-checkbox-wrapper .ant-checkbox + span) {
  color: inherit;
}

/* 上移/下移：图标用次级字色，hover 用主色，保证深浅色均可见 */
.column-item-right :deep(.ant-btn.ant-btn-text) {
  color: var(--fx-text-secondary, rgba(0, 0, 0, 0.65));
}

.column-item-right :deep(.ant-btn.ant-btn-text:not(:disabled):hover) {
  color: var(--fx-primary, #1677ff);
  background: transparent;
}

.column-setting-footer {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--fx-border-color, #f0f0f0);
}

.column-setting-footer .hint {
  color: var(--fx-text-secondary, rgba(0, 0, 0, 0.45));
  font-size: 12px;
}
</style>

<!--
  与 overlayInnerStyle 一致：不透底、主字色；变量在挂到 .fx-main-layout 后可用。
-->
<style lang="less">
.fx-column-setting-popover.ant-popover .ant-popover-inner {
  padding: 0 !important;
  background: var(--fx-bg-elevated) !important;
  background-color: var(--fx-bg-elevated) !important;
  color: var(--fx-text-primary) !important;
  box-shadow: var(--fx-shadow) !important;
}

.fx-column-setting-popover.ant-popover .ant-popover-inner-content {
  padding: 0 !important;
  color: var(--fx-text-primary) !important;
}
</style>