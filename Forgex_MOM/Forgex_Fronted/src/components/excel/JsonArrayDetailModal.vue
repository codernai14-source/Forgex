<template>
  <a-modal
    :open="open"
    :title="title || t('common.jsonArrayEditor.detailTitle')"
    :width="900"
    :ok-text="t('common.save')"
    :cancel-text="t('common.cancel')"
    @update:open="handleOpenChange"
    @ok="handleConfirm"
    @cancel="closeModal"
  >
    <div class="json-array-detail-modal">
      <a-alert
        v-if="showInvalidAlert"
        type="warning"
        show-icon
        :message="t('common.jsonArrayEditor.invalidJson')"
        class="json-array-detail-modal__alert"
      />

      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="structured" :tab="t('common.jsonArrayEditor.structuredView')">
          <JsonArrayEditor
            v-model="localJsonValue"
            :show-preview="false"
            :cardless="true"
          />
        </a-tab-pane>
        <a-tab-pane key="raw" :tab="t('common.jsonArrayEditor.rawJson')">
          <div v-if="rawJsonPreview === '[]'" class="json-array-detail-modal__empty">
            <a-empty :description="t('common.jsonArrayEditor.emptyData')" />
          </div>
          <pre v-else class="json-array-detail-modal__preview">{{ rawJsonPreview }}</pre>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import JsonArrayEditor from './JsonArrayEditor.vue'
import { parseJsonArrayValue } from './jsonArrayUtils'

interface Props {
  open: boolean
  value?: string
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  title: '',
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: [value: string]
}>()

const { t } = useI18n()

const activeTab = ref('structured')
const localJsonValue = ref('[]')
const initialInvalid = ref(false)

const parsedState = computed(() => parseJsonArrayValue(localJsonValue.value))
const showInvalidAlert = computed(() => initialInvalid.value || !parsedState.value.valid)
const rawJsonPreview = computed(() => {
  if (!parsedState.value.valid) {
    return String(localJsonValue.value || '').trim() || '[]'
  }
  return parsedState.value.formattedJson || '[]'
})

function syncLocalValue() {
  localJsonValue.value = String(props.value || '').trim() || '[]'
  initialInvalid.value = !parseJsonArrayValue(props.value).valid
  activeTab.value = initialInvalid.value ? 'raw' : 'structured'
}

function handleOpenChange(value: boolean) {
  emit('update:open', value)
}

function closeModal() {
  emit('update:open', false)
}

function handleConfirm() {
  const parsed = parsedState.value
  if (!parsed.valid) {
    activeTab.value = 'raw'
    message.warning(t('common.jsonArrayEditor.invalidJson'))
    return
  }

  emit('confirm', parsed.serializedJson || '[]')
  closeModal()
}

watch(
  () => props.open,
  value => {
    if (value) {
      syncLocalValue()
    }
  },
  { immediate: true },
)

watch(
  () => props.value,
  () => {
    if (props.open) {
      syncLocalValue()
    }
  },
)
</script>

<style scoped lang="less">
.json-array-detail-modal {
  &__alert {
    margin-bottom: 12px;
  }

  &__empty {
    padding: 24px 0 8px;
  }

  &__preview {
    min-height: 320px;
    margin: 0;
    padding: 16px;
    border: 1px solid #d9d9d9;
    border-radius: 8px;
    background: #fafafa;
    color: rgba(0, 0, 0, 0.88);
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    overflow: auto;
  }
}
</style>
