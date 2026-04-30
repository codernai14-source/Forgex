<template>
  <span class="fx-action-group">
    <template v-for="item in inlineActions" :key="item.key">
      <a
        :class="getActionClass(item)"
        :data-guide-id="item.guideId"
        @click="event => handleActionClick(item, event)"
      >
        {{ item.label }}
      </a>
    </template>

    <a-dropdown v-if="overflowActions.length > 0" :trigger="['click']">
      <a class="fx-action-group__more" @click.prevent>
        {{ moreLabel }}
        <DownOutlined />
      </a>
      <template #overlay>
        <a-menu>
          <a-menu-item
            v-for="item in overflowActions"
            :key="item.key"
            :disabled="item.disabled"
            :data-guide-id="item.guideId"
            @click="() => handleMenuActionClick(item)"
          >
            <span :class="getActionClass(item)">{{ item.label }}</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DownOutlined } from '@ant-design/icons-vue'
import { use权限Store } from '@/stores/permission'

export interface ActionItem {
  key: string
  label: string
  permission?: string
  danger?: boolean
  disabled?: boolean
  hidden?: boolean
  guideId?: string
  onClick?: () => void
}

const props = withDefaults(
  defineProps<{
    actions: ActionItem[]
    maxInline?: number
    moreLabel?: string
  }>(),
  {
    maxInline: 3,
    moreLabel: '更多',
  },
)

const permissionStore = use权限Store()

function hasPermission(permission?: string) {
  if (!permission) {
    return true
  }
  const checker = permissionStore.has权限 as any
  if (typeof checker === 'function') {
    return checker(permission)
  }
  if (checker && typeof checker.value === 'function') {
    return checker.value(permission)
  }
  return false
}

const visibleActions = computed(() => (
  props.actions.filter(item => !item.hidden && hasPermission(item.permission))
))

const inlineLimit = computed(() => Math.max(Number(props.maxInline || 3), 1))
const inlineActions = computed(() => {
  if (visibleActions.value.length <= inlineLimit.value) {
    return visibleActions.value
  }
  return visibleActions.value.slice(0, inlineLimit.value)
})
const overflowActions = computed(() => {
  if (visibleActions.value.length <= inlineLimit.value) {
    return []
  }
  return visibleActions.value.slice(inlineLimit.value)
})

function getActionClass(item: ActionItem) {
  return {
    'fx-action-group__item': true,
    'fx-action-group__item--danger': item.danger,
    'fx-action-group__item--disabled': item.disabled,
  }
}

function runAction(item: ActionItem) {
  if (item.disabled) {
    return
  }
  item.onClick?.()
}

function handleActionClick(item: ActionItem, event: MouseEvent) {
  event.preventDefault()
  runAction(item)
}

function handleMenuActionClick(item: ActionItem) {
  runAction(item)
}
</script>

<style scoped lang="less">
.fx-action-group {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  white-space: nowrap;
}

.fx-action-group__item,
.fx-action-group__more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.fx-action-group__item--danger {
  color: #ff4d4f;
}

.fx-action-group__item--disabled {
  color: var(--fx-text-disabled, #bfbfbf);
  cursor: not-allowed;
  pointer-events: none;
}
</style>
