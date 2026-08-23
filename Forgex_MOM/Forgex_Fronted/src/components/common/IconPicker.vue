<template>
  <div class="fx-icon-picker">
    <a-input-group compact>
      <a-input
        v-model:value="innerValue"
        :placeholder="placeholder"
        :maxlength="maxlength"
        style="width: calc(100% - 40px)"
        allow-clear
        @change="onInputChange"
      />
      <a-button type="default" :aria-label="resolvedTitle" @click="open = true">
        <template #icon><FxIcon :name="innerValue" :size="iconSize" :color="iconColor" /></template>
      </a-button>
    </a-input-group>

    <a-modal
      v-model:open="open"
      :title="resolvedTitle"
      width="980px"
      :footer="null"
      destroy-on-close
      wrap-class-name="fx-icon-picker-modal"
    >
      <div class="fx-icon-picker-layout">
        <aside class="fx-icon-picker-sidebar">
          <div class="fx-icon-picker-sidebar-title">{{ t('common.iconPicker.library') }}</div>
          <a-menu
            :selected-keys="[activeLibrary]"
            mode="vertical"
            @click="({ key }) => (activeLibrary = String(key) as IconLibraryKey)"
          >
            <a-menu-item v-for="library in libraries" :key="library.key">
              <span class="fx-icon-picker-library-label">{{ libraryLabel(library) }}</span>
              <span>{{ library.items.length }}</span>
            </a-menu-item>
          </a-menu>
        </aside>

        <section class="fx-icon-picker-content">
          <a-input
            v-model:value="keyword"
            allow-clear
            :placeholder="resolvedSearchPlaceholder"
            class="fx-icon-picker-search"
          />
          <div class="fx-icon-picker-toolbar">
            <div class="fx-icon-picker-color-control">
              <span>{{ t('common.iconPicker.color') }}</span>
              <button
                v-for="color in presetColors"
                :key="color"
                type="button"
                class="fx-icon-picker-swatch"
                :class="{ active: color === iconColor }"
                :style="{ backgroundColor: color }"
                :aria-label="color"
                @click="iconColor = color"
              />
              <input v-model="iconColor" type="color" :aria-label="t('common.iconPicker.customColor')" />
            </div>
            <div class="fx-icon-picker-size-control">
              <span>{{ t('common.iconPicker.size') }}</span>
              <a-slider v-model:value="iconSize" :min="12" :max="64" :step="1" />
              <a-input-number v-model:value="iconSize" :min="12" :max="64" />
              <span>px</span>
            </div>
            <div class="fx-icon-picker-preview">
              <FxIcon :name="innerValue" :size="iconSize" :color="iconColor" />
            </div>
          </div>

          <div class="fx-icon-picker-grid">
            <button
              v-for="item in filteredIcons"
              :key="item.name"
              type="button"
              class="fx-icon-picker-item"
              :class="{ active: item.name === innerValue }"
              @click="select(item.name)"
            >
              <FxIcon :name="item.name" :size="iconSize" :color="iconColor" />
              <span class="lbl">{{ item.label }}</span>
              <span class="name">{{ item.name }}</span>
            </button>
            <a-empty v-if="filteredIcons.length === 0" class="fx-icon-picker-empty" />
          </div>
        </section>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as Icons from '@ant-design/icons-vue'
import { pinyin } from 'pinyin-pro'
import FxIcon from './FxIcon.vue'
import { ICONIFY_PRESET_NAMES } from '@/utils/icon'

type IconLibraryKey =
  | 'lucide'
  | 'materialSymbols'
  | 'materialDesign'
  | 'tabler'
  | 'heroicons'
  | 'carbon'
  | 'bootstrap'
  | 'phosphor'
  | 'remix'
  | 'solar'
  | 'fluent'
  | 'fontAwesome'
  | 'iconPark'
  | 'ant'

type IconItem = { name: string; label: string; search: string }
type IconLibrary = { key: IconLibraryKey; labelKey: string; names: string[]; items: IconItem[] }

const props = withDefaults(defineProps<{
  value?: string
  placeholder?: string
  title?: string
  searchPlaceholder?: string
  maxlength?: number
  iconColor?: string
  iconSize?: number
}>(), {
  value: '',
  placeholder: '',
  title: '',
  searchPlaceholder: '',
  maxlength: 100,
  iconColor: '#1677ff',
  iconSize: 22,
})

const emit = defineEmits<{
  (e: 'update:value', v: string | undefined): void
  (e: 'update:iconColor', v: string): void
  (e: 'update:iconSize', v: number): void
}>()

const { t } = useI18n()
const open = ref(false)
const keyword = ref('')
const activeLibrary = ref<IconLibraryKey>('lucide')
const innerValue = ref(props.value || '')
const iconColor = ref(props.iconColor)
const iconSize = ref(props.iconSize)
const presetColors = ['#1677ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2', '#262626']

const resolvedTitle = computed(() => props.title || t('common.iconPicker.title'))
const resolvedSearchPlaceholder = computed(() => props.searchPlaceholder || t('common.iconPicker.searchPlaceholder'))

const iconAliases: Record<string, string> = {
  home: '首页', user: '用户', users: '用户 用户组', account: '用户账号', team: '团队', dashboard: '仪表盘 工作台',
  settings: '设置 系统', setting: '设置 系统', search: '搜索', calendar: '日历', clock: '时间', mail: '邮件', phone: '电话',
  folder: '文件夹', file: '文件', download: '下载', upload: '上传', delete: '删除', trash: '删除', edit: '编辑',
  plus: '新增 添加', add: '新增 添加', check: '确认 成功', close: '关闭', x: '关闭', warning: '警告', alert: '警告',
  lock: '锁定', unlock: '解锁', cloud: '云', chart: '图表', book: '文档 书', message: '消息', bell: '通知',
  shield: '安全', key: '密钥', link: '链接', package: '包', database: '数据库', monitor: '监控', menu: '菜单',
}

const iconLibraryNames: Array<{ key: IconLibraryKey; labelKey: string; names: string[] }> = [
  { key: 'lucide', labelKey: 'lucide', names: [...ICONIFY_PRESET_NAMES] },
  { key: 'materialSymbols', labelKey: 'materialSymbols', names: ['material-symbols:dashboard-outline', 'material-symbols:settings-outline', 'material-symbols:group-outline', 'material-symbols:person-outline', 'material-symbols:business-outline', 'material-symbols:factory-outline', 'material-symbols:inventory-2-outline', 'material-symbols:database-outline', 'material-symbols:folder-outline', 'material-symbols:description-outline', 'material-symbols:home-outline', 'material-symbols:search', 'material-symbols:calendar-month-outline', 'material-symbols:schedule-outline', 'material-symbols:mail-outline', 'material-symbols:phone-outline', 'material-symbols:download', 'material-symbols:upload', 'material-symbols:lock-outline', 'material-symbols:warning-outline'] },
  { key: 'materialDesign', labelKey: 'materialDesign', names: ['mdi:view-dashboard-outline', 'mdi:cog-outline', 'mdi:account-group-outline', 'mdi:account-outline', 'mdi:office-building-outline', 'mdi:factory', 'mdi:package-variant-closed', 'mdi:database-outline', 'mdi:folder-outline', 'mdi:file-outline', 'mdi:home-outline', 'mdi:magnify', 'mdi:calendar-month-outline', 'mdi:clock-outline', 'mdi:email-outline', 'mdi:phone-outline', 'mdi:download', 'mdi:upload', 'mdi:lock-outline', 'mdi:alert-outline'] },
  { key: 'tabler', labelKey: 'tabler', names: ['tabler:dashboard', 'tabler:settings', 'tabler:users', 'tabler:user', 'tabler:building', 'tabler:building-factory-2', 'tabler:package', 'tabler:database', 'tabler:folder', 'tabler:file', 'tabler:home', 'tabler:search', 'tabler:calendar', 'tabler:clock', 'tabler:mail', 'tabler:phone', 'tabler:download', 'tabler:upload', 'tabler:lock', 'tabler:alert-triangle'] },
  { key: 'heroicons', labelKey: 'heroicons', names: ['heroicons-outline:home', 'heroicons-outline:cog-6-tooth', 'heroicons-outline:user-group', 'heroicons-outline:user', 'heroicons-outline:building-office-2', 'heroicons-outline:archive-box', 'heroicons-outline:circle-stack', 'heroicons-outline:folder', 'heroicons-outline:document', 'heroicons-outline:magnifying-glass', 'heroicons-outline:calendar-days', 'heroicons-outline:clock', 'heroicons-outline:envelope', 'heroicons-outline:phone', 'heroicons-outline:arrow-down-tray', 'heroicons-outline:arrow-up-tray', 'heroicons-outline:lock-closed', 'heroicons-outline:exclamation-triangle'] },
  { key: 'carbon', labelKey: 'carbon', names: ['carbon:dashboard', 'carbon:settings', 'carbon:group', 'carbon:user', 'carbon:building', 'carbon:factory', 'carbon:package', 'carbon:data-base', 'carbon:folder', 'carbon:document', 'carbon:home', 'carbon:search', 'carbon:calendar', 'carbon:time', 'carbon:email', 'carbon:phone', 'carbon:download', 'carbon:upload', 'carbon:locked', 'carbon:warning'] },
  { key: 'bootstrap', labelKey: 'bootstrap', names: ['bi: speedometer2', 'bi:gear', 'bi:people', 'bi:person', 'bi:building', 'bi:boxes', 'bi:database', 'bi:folder', 'bi:file-earmark', 'bi:house', 'bi:search', 'bi:calendar3', 'bi:clock', 'bi:envelope', 'bi:telephone', 'bi:download', 'bi:upload', 'bi:lock', 'bi:exclamation-triangle'] .map(name => name.replace('bi: ', 'bi:')) },
  { key: 'phosphor', labelKey: 'phosphor', names: ['ph:gauge', 'ph:gear', 'ph:users-three', 'ph:user', 'ph:buildings', 'ph:factory', 'ph:package', 'ph:database', 'ph:folder', 'ph:file', 'ph:house', 'ph:magnifying-glass', 'ph:calendar', 'ph:clock', 'ph:envelope', 'ph:phone', 'ph:download', 'ph:upload', 'ph:lock', 'ph:warning'] },
  { key: 'remix', labelKey: 'remix', names: ['ri:dashboard-line', 'ri:settings-3-line', 'ri:group-line', 'ri:user-line', 'ri:building-line', 'ri:factory-line', 'ri:archive-line', 'ri:database-2-line', 'ri:folder-line', 'ri:file-line', 'ri:home-line', 'ri:search-line', 'ri:calendar-line', 'ri:time-line', 'ri:mail-line', 'ri:phone-line', 'ri:download-line', 'ri:upload-line', 'ri:lock-line', 'ri:alert-line'] },
  { key: 'solar', labelKey: 'solar', names: ['solar:widget-5-outline', 'solar:settings-outline', 'solar:users-group-rounded-outline', 'solar:user-outline', 'solar:buildings-2-outline', 'solar:buildings-3-outline', 'solar:box-outline', 'solar:database-outline', 'solar:folder-with-files-outline', 'solar:file-text-outline', 'solar:home-2-outline', 'solar:magnifer-outline', 'solar:calendar-outline', 'solar:clock-circle-outline', 'solar:letter-outline', 'solar:phone-outline', 'solar:download-outline', 'solar:upload-outline', 'solar:lock-keyhole-outline', 'solar:danger-triangle-outline'] },
  { key: 'fluent', labelKey: 'fluent', names: ['fluent:home-24-regular', 'fluent:settings-24-regular', 'fluent:people-24-regular', 'fluent:person-24-regular', 'fluent:building-24-regular', 'fluent:factory-24-regular', 'fluent:box-24-regular', 'fluent:database-24-regular', 'fluent:folder-24-regular', 'fluent:document-24-regular', 'fluent:search-24-regular', 'fluent:calendar-24-regular', 'fluent:clock-24-regular', 'fluent:mail-24-regular', 'fluent:phone-24-regular', 'fluent:arrow-download-24-regular', 'fluent:arrow-upload-24-regular', 'fluent:lock-closed-24-regular', 'fluent:warning-24-regular'] },
  { key: 'fontAwesome', labelKey: 'fontAwesome', names: ['fa6-solid:gauge-high', 'fa6-solid:gear', 'fa6-solid:users', 'fa6-solid:user', 'fa6-solid:building', 'fa6-solid:industry', 'fa6-solid:box', 'fa6-solid:database', 'fa6-solid:folder', 'fa6-solid:file', 'fa6-solid:house', 'fa6-solid:magnifying-glass', 'fa6-solid:calendar-days', 'fa6-solid:clock', 'fa6-solid:envelope', 'fa6-solid:phone', 'fa6-solid:download', 'fa6-solid:upload', 'fa6-solid:lock', 'fa6-solid:triangle-exclamation'] },
  { key: 'iconPark', labelKey: 'iconPark', names: ['icon-park-outline:dashboard', 'icon-park-outline:setting', 'icon-park-outline:peoples', 'icon-park-outline:people', 'icon-park-outline:building-one', 'icon-park-outline:factory-building', 'icon-park-outline:box', 'icon-park-outline:database', 'icon-park-outline:folder', 'icon-park-outline:file-text', 'icon-park-outline:home', 'icon-park-outline:search', 'icon-park-outline:calendar', 'icon-park-outline:time', 'icon-park-outline:email', 'icon-park-outline:phone-telephone', 'icon-park-outline:download', 'icon-park-outline:upload', 'icon-park-outline:lock', 'icon-park-outline:attention'] },
]

function buildSearchText(name: string) {
  const segment = name.split(':').pop() || name
  const words = segment.replace(/[-_]/g, ' ').toLowerCase()
  const aliases = Object.entries(iconAliases).filter(([key]) => words.includes(key)).map(([, value]) => value).join(' ')
  const label = `${aliases} ${words}`.trim()
  try {
    const full = pinyin(label, { toneType: 'none', pattern: 'pinyin', type: 'array', nonZh: 'consecutive', v: true }).join('').toLowerCase()
    const initials = pinyin(label, { toneType: 'none', pattern: 'first', type: 'array', nonZh: 'consecutive', v: true }).join('').toLowerCase()
    return `${name} ${label} ${full} ${initials}`.toLowerCase()
  } catch {
    return `${name} ${label}`.toLowerCase()
  }
}

const antNames = computed(() => Object.keys(Icons).filter(name => name.endsWith('Outlined') || name.endsWith('Filled') || name.endsWith('TwoTone')))
const libraries = computed<IconLibrary[]>(() => [
  ...iconLibraryNames.map(library => ({ ...library, items: library.names.map(name => ({ name, label: name.split(':').pop() || name, search: buildSearchText(name) })) })),
  {
    key: 'ant',
    labelKey: 'ant',
    names: antNames.value,
    items: antNames.value.map(name => ({ name, label: name.replace(/(Outlined|Filled|TwoTone)$/i, ''), search: buildSearchText(name) })),
  },
])
const activeDefinition = computed(() => libraries.value.find(library => library.key === activeLibrary.value) || libraries.value[0])
const filteredIcons = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return q ? activeDefinition.value.items.filter(item => item.search.includes(q)) : activeDefinition.value.items
})

function libraryLabel(library: IconLibrary) {
  return t(`common.iconPicker.libraries.${library.labelKey}`)
}

watch(() => props.value, value => (innerValue.value = value || ''))
watch(() => props.iconColor, value => (iconColor.value = value || '#1677ff'))
watch(() => props.iconSize, value => (iconSize.value = value || 22))
watch(iconColor, value => emit('update:iconColor', value))
watch(iconSize, value => emit('update:iconSize', Number(value || 22)))

function onInputChange() {
  emit('update:value', innerValue.value || undefined)
}

function select(name: string) {
  innerValue.value = name
  emit('update:value', name)
  open.value = false
}
</script>

<style scoped lang="less" src="@/styles/components/common/icon-picker.less"></style>
