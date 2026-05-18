<template>
  <div class="data-source-config">
    <a-form-item :label="t('common.dataSourceConfig.dataSourceType')" name="dataSourceType">
      <a-select
        v-model:value="dataSourceType"
        :placeholder="t('common.dataSourceConfig.selectType')"
        @change="handleTypeChange"
      >
        <a-select-option value="DICT">{{ t('common.dataSourceConfig.typeDict') }}</a-select-option>
        <a-select-option value="JSON">{{ t('common.dataSourceConfig.typeJson') }}</a-select-option>
        <a-select-option value="PROVIDER">{{ t('common.dataSourceConfig.typeProvider') }}</a-select-option>
      </a-select>
    </a-form-item>

    <a-form-item
      v-if="dataSourceType === 'DICT'"
      :label="t('common.dataSourceConfig.dictCode')"
      name="dictCode"
    >
      <a-select
        v-model:value="config.dictCode"
        :placeholder="t('common.dataSourceConfig.selectDict')"
        show-search
        :filter-option="filterDictOption"
        @change="handleConfigChange"
      >
        <a-select-option
          v-for="dict in dictList"
          :key="dict.dictCode"
          :value="dict.dictCode"
        >
          {{ dict.dictName }} ({{ dict.dictCode }})
        </a-select-option>
      </a-select>
    </a-form-item>

    <div v-if="dataSourceType === 'JSON'" class="json-editor-wrapper">
      <a-form-item
        :label="t('common.dataSourceConfig.dataSourceJson')"
        name="dataSourceJson"
      >
        <div
          class="json-summary-panel"
          :class="{
            'json-summary-panel--invalid': !jsonArrayState.valid,
            'json-summary-panel--empty': jsonItemCount === 0 && jsonArrayState.valid,
          }"
        >
          <div class="json-summary-panel__content">
            <div class="json-summary-panel__summary">{{ jsonSummaryText }}</div>
            <div class="json-summary-panel__hint">
              {{ !jsonArrayState.valid ? t('common.jsonArrayEditor.invalidJson') : t('common.dataSourceConfig.summaryHint') }}
            </div>
          </div>
          <a-button type="link" class="json-summary-panel__button" @click="jsonDetailOpen = true">
            {{ t('common.dataSourceConfig.viewDetail') }}
          </a-button>
        </div>
      </a-form-item>

      <JsonArrayDetailModal
        v-model:open="jsonDetailOpen"
        :value="config.dataSourceJson"
        :title="t('common.dataSourceConfig.detailTitle')"
        @confirm="handleJsonConfirm"
      />
    </div>

    <template v-if="dataSourceType === 'PROVIDER'">
      <a-form-item
        :label="t('common.dataSourceConfig.providerCode')"
        name="providerCode"
      >
        <a-select
          v-model:value="config.providerCode"
          :placeholder="t('common.dataSourceConfig.selectProvider')"
          show-search
          :filter-option="filterProviderOption"
          @change="handleProviderChange"
        >
          <a-select-option
            v-for="provider in providerList"
            :key="provider.providerCode"
            :value="provider.providerCode"
          >
            {{ provider.providerName }} ({{ provider.providerCode }})
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        v-if="config.providerCode"
        :label="t('common.dataSourceConfig.providerField')"
        name="providerField"
      >
        <a-select
          v-model:value="config.providerField"
          :placeholder="t('common.dataSourceConfig.selectProviderField')"
          show-search
          @change="handleConfigChange"
        >
          <a-select-option
            v-for="field in currentProviderFields"
            :key="field.fieldName"
            :value="field.fieldName"
          >
            {{ field.fieldLabel }} ({{ field.fieldName }})
          </a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import JsonArrayDetailModal from './JsonArrayDetailModal.vue'
import { parseJsonArrayValue } from './jsonArrayUtils'
import { getDictList } from '@/api/system/dict'
import { getProviderFields, getProviderList } from '@/api/system/excel'

interface Props {
  modelValue?: {
    dataSourceType?: string
    dictCode?: string
    dataSourceJson?: string
    providerCode?: string
    providerField?: string
  }
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
})

const emit = defineEmits<{
  'update:modelValue': [value: {
    dataSourceType?: string
    dictCode?: string
    dataSourceJson?: string
    providerCode?: string
    providerField?: string
  }]
}>()

const { t } = useI18n()

const dataSourceType = ref<string>(props.modelValue?.dataSourceType || '')
const jsonDetailOpen = ref(false)

const config = ref({
  dictCode: props.modelValue?.dictCode || '',
  dataSourceJson: props.modelValue?.dataSourceJson || '',
  providerCode: props.modelValue?.providerCode || '',
  providerField: props.modelValue?.providerField || '',
})

const dictList = ref<Array<{ dictCode: string; dictName: string }>>([])
const providerList = ref<Array<{ providerCode: string; providerName: string }>>([])
const currentProviderFields = ref<Array<{ fieldName: string; fieldLabel: string }>>([])

const jsonArrayState = computed(() => parseJsonArrayValue(config.value.dataSourceJson))
const jsonItemCount = computed(() => jsonArrayState.value.options.filter(item => item.value.trim() || item.label.trim()).length)
const jsonSummaryText = computed(() => {
  if (!jsonArrayState.value.valid) {
    return t('common.dataSourceConfig.summaryInvalid')
  }
  if (jsonItemCount.value === 0) {
    return t('common.dataSourceConfig.summaryEmpty')
  }
  return t('common.dataSourceConfig.summaryCount', { count: jsonItemCount.value })
})

const loadDictList = async () => {
  try {
    const res = await getDictList({})
    dictList.value = res || []
  } catch (error) {
    console.error('Failed to load dictionaries:', error)
  }
}

const loadProviderList = async () => {
  try {
    const res = await getProviderList({})
    providerList.value = res || []
  } catch (error) {
    console.error('Failed to load providers:', error)
  }
}

const loadProviderFields = async (providerCode: string) => {
  try {
    const res = await getProviderFields({ providerCode })
    currentProviderFields.value = res || []
  } catch (error) {
    console.error('Failed to load provider fields:', error)
    currentProviderFields.value = []
  }
}

const filterDictOption = (input: string, option: any) => {
  const text = String(option.children || '').toLowerCase()
  return text.includes(input.toLowerCase())
}

const filterProviderOption = (input: string, option: any) => {
  const text = String(option.children || '').toLowerCase()
  return text.includes(input.toLowerCase())
}

const handleTypeChange = (value: string) => {
  if (value !== 'DICT') {
    config.value.dictCode = ''
  }
  if (value !== 'JSON') {
    config.value.dataSourceJson = ''
  }
  if (value !== 'PROVIDER') {
    config.value.providerCode = ''
    config.value.providerField = ''
    currentProviderFields.value = []
  }

  emitConfigChange()
}

const handleProviderChange = (value: string) => {
  config.value.providerField = ''
  if (value) {
    loadProviderFields(value)
  } else {
    currentProviderFields.value = []
  }
  emitConfigChange()
}

const handleConfigChange = () => {
  emitConfigChange()
}

const handleJsonConfirm = (value: string) => {
  config.value.dataSourceJson = value
  emitConfigChange()
}

const emitConfigChange = () => {
  emit('update:modelValue', {
    dataSourceType: dataSourceType.value,
    dictCode: config.value.dictCode,
    dataSourceJson: config.value.dataSourceJson,
    providerCode: config.value.providerCode,
    providerField: config.value.providerField,
  })
}

watch(
  () => config.value,
  () => {
    emitConfigChange()
  },
  { deep: true },
)

watch(
  () => props.modelValue,
  newVal => {
    if (newVal) {
      dataSourceType.value = newVal.dataSourceType || ''
      config.value = {
        dictCode: newVal.dictCode || '',
        dataSourceJson: newVal.dataSourceJson || '',
        providerCode: newVal.providerCode || '',
        providerField: newVal.providerField || '',
      }
      if (config.value.providerCode) {
        loadProviderFields(config.value.providerCode)
      }
    }
  },
  { deep: true },
)

onMounted(() => {
  loadDictList()
  loadProviderList()
  if (config.value.providerCode) {
    loadProviderFields(config.value.providerCode)
  }
})
</script>

<style scoped lang="less" src="@/styles/components/excel/data-source-config.less"></style>
