<template>
  <router-view />
  <MessageNotification />
  <div v-if="loaderOpen" style="position:fixed;inset:0;z-index:2000;background:rgba(255,255,255,0.12);display:flex;align-items:center;justify-content:center;backdrop-filter:blur(2px)">
    <video src="/loading.mp4" autoplay muted loop style="max-width:70%;max-height:70%;opacity:0.85;border-radius:12px;box-shadow:0 8px 32px rgba(0,0,0,0.45)"></video>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
const loaderOpen = ref(false)
onMounted(() => {
  (window as any).__globalLoader = {
    show: () => { loaderOpen.value = true },
    hide: () => { loaderOpen.value = false }
  }
})
</script>

<style>
html, body, #app {
  height: 100%;
  overflow: hidden;
}

html[data-theme='light'],
body[data-theme='light'] {
  color-scheme: light;
}

html[data-theme='dark'],
body[data-theme='dark'] {
  color-scheme: dark;
}

html,
body {
  background: var(--fx-layout-bg, #f3f4f6);
  color: var(--fx-text-primary, #111827);
}

#app {
  background: var(--fx-layout-bg, #f3f4f6);
}

input,
textarea,
select {
  caret-color: var(--fx-theme-color, #1677ff);
}

input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
textarea:-webkit-autofill,
textarea:-webkit-autofill:hover,
textarea:-webkit-autofill:focus,
select:-webkit-autofill,
select:-webkit-autofill:hover,
select:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--fx-text-primary, #111827) !important;
  box-shadow: 0 0 0 1000px var(--fx-bg-container, #ffffff) inset !important;
  -webkit-box-shadow: 0 0 0 1000px var(--fx-bg-container, #ffffff) inset !important;
  transition: background-color 9999s ease-out 0s, color 9999s ease-out 0s;
  caret-color: var(--fx-theme-color, #1677ff);
}

.ant-input-affix-wrapper input:-webkit-autofill,
.ant-input-affix-wrapper input:-webkit-autofill:hover,
.ant-input-affix-wrapper input:-webkit-autofill:focus,
.ant-picker input:-webkit-autofill,
.ant-picker input:-webkit-autofill:hover,
.ant-picker input:-webkit-autofill:focus,
.ant-input-number input:-webkit-autofill,
.ant-input-number input:-webkit-autofill:hover,
.ant-input-number input:-webkit-autofill:focus,
.ant-input-textarea textarea:-webkit-autofill,
.ant-input-textarea textarea:-webkit-autofill:hover,
.ant-input-textarea textarea:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--fx-text-primary, #111827) !important;
  box-shadow: 0 0 0 1000px var(--fx-bg-container, #ffffff) inset !important;
  -webkit-box-shadow: 0 0 0 1000px var(--fx-bg-container, #ffffff) inset !important;
  caret-color: var(--fx-theme-color, #1677ff);
}

.ant-input,
.ant-input-affix-wrapper,
.ant-input-number,
.ant-input-number-input,
.ant-input-number-input-wrap,
.ant-input-number-group-wrapper,
.ant-input-number-group-addon,
.ant-select-selector,
.ant-select-selection-search,
.ant-select-selection-item,
.ant-select-selection-placeholder,
.ant-picker,
.ant-picker-input > input,
.ant-mentions,
.ant-mentions textarea,
.ant-input-textarea textarea,
.ant-input-outlined,
.ant-input-affix-wrapper-focused,
.ant-input-affix-wrapper:hover {
  background: var(--fx-bg-container, #ffffff) !important;
  color: var(--fx-text-primary, #111827) !important;
}

.ant-select-selection-search-input,
.ant-picker-input > input,
.ant-input-number input,
.ant-mentions textarea,
.ant-input-affix-wrapper .ant-input {
  color: var(--fx-text-primary, #111827) !important;
}

.ant-input::placeholder,
.ant-input-number-input::placeholder,
.ant-select-selection-search-input::placeholder,
.ant-picker-input > input::placeholder,
.ant-mentions textarea::placeholder,
textarea::placeholder {
  color: var(--fx-text-tertiary, #9ca3af) !important;
}

.ant-input-affix-wrapper > input.ant-input,
.ant-input-affix-wrapper input,
.ant-picker input,
.ant-select-selection-search-input,
.ant-input-textarea textarea,
.ant-input-number input {
  background: transparent !important;
  color: var(--fx-text-primary, #111827) !important;
}

.ant-select:not(.ant-select-customize-input) .ant-select-selector,
.ant-picker,
.ant-input-number,
.ant-input-affix-wrapper,
.ant-mentions,
.ant-input-textarea-show-count::after {
  border-color: var(--fx-border-color, #e5e7eb) !important;
}

.ant-select:not(.ant-select-disabled):hover .ant-select-selector,
.ant-picker:hover,
.ant-input-number:hover,
.ant-input-affix-wrapper:hover {
  border-color: var(--fx-primary, #1677ff) !important;
}

.ant-input-status-error:not(.ant-input-disabled).ant-input,
.ant-input-affix-wrapper-status-error:not(.ant-input-affix-wrapper-disabled),
.ant-input-number-status-error:not(.ant-input-number-disabled),
.ant-picker-status-error:not(.ant-picker-disabled) {
  background: var(--fx-bg-container, #ffffff) !important;
}

.ant-card,
.ant-card-head,
.ant-card-body,
.ant-modal-content,
.ant-modal-header,
.ant-modal-body,
.ant-modal-footer,
.ant-drawer-content,
.ant-drawer-header,
.ant-drawer-body,
.ant-drawer-footer,
.ant-dropdown-menu,
.ant-popover-inner,
.ant-popconfirm-inner-content,
.ant-tooltip-inner,
.ant-select-dropdown,
.ant-cascader-dropdown,
.ant-picker-dropdown .ant-picker-panel-container,
.ant-tree-select-dropdown,
.ant-table-filter-dropdown,
.ant-empty-normal {
  background: var(--fx-bg-container, #ffffff) !important;
  color: var(--fx-text-primary, #111827) !important;
}

.ant-modal-header,
.ant-drawer-header,
.ant-card-head,
.ant-dropdown-menu,
.ant-picker-dropdown .ant-picker-panel-container,
.ant-table-filter-dropdown {
  border-color: var(--fx-border-color, #e5e7eb) !important;
}

.ant-dropdown-menu-item,
.ant-dropdown-menu-submenu-title,
.ant-select-item,
.ant-cascader-menu-item,
.ant-table-filter-dropdown .ant-dropdown-menu-item {
  color: var(--fx-text-primary, #111827) !important;
}

.ant-dropdown-menu-item:hover,
.ant-dropdown-menu-submenu-title:hover,
.ant-select-item-option-active,
.ant-select-item-option-selected,
.ant-cascader-menu-item:hover,
.ant-table-filter-dropdown .ant-dropdown-menu-item:hover {
  background: var(--fx-fill, #f3f4f6) !important;
  color: var(--fx-text-primary, #111827) !important;
}

.ant-form-item-label > label,
.ant-descriptions-item-label,
.ant-modal-title,
.ant-drawer-title {
  color: var(--fx-text-primary, #111827) !important;
}
</style>
