<template>
  <div class="json-array-editor">
    <a-card v-if="!cardless" size="small" :bordered="false">
      <div class="header-actions">
        <span class="title">{{ t('common.jsonArrayEditor.optionsList') }}</span>
        <a-button type="primary" size="small" @click="addOption">
          <PlusOutlined />
          {{ t('common.jsonArrayEditor.addOption') }}
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="options"
        :pagination="false"
        size="small"
        bordered
        row-key="_key"
        :scroll="{ x: 560 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'value'">
            <a-input
              v-model:value="record.value"
              :placeholder="t('common.jsonArrayEditor.valuePlaceholder')"
              @update:value="handleInputChange"
            />
          </template>

          <template v-else-if="column.key === 'label'">
            <a-input
              v-model:value="record.label"
              :placeholder="t('common.jsonArrayEditor.labelPlaceholder')"
              @update:value="handleInputChange"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a
              type="link"
              danger
              size="small"
              @click="removeOption(index)"
            >
              <DeleteOutlined />
              {{ t('common.remove') }}
            </a>
          </template>
        </template>
      </a-table>

      <template v-if="showPreview">
        <a-divider orientation="left">{{ t('common.jsonArrayEditor.jsonPreview') }}</a-divider>
        <pre class="json-preview">{{ jsonPreview }}</pre>
      </template>
    </a-card>

    <div v-else class="json-array-editor__plain">
      <div class="header-actions">
        <span class="title">{{ t('common.jsonArrayEditor.optionsList') }}</span>
        <a-button type="primary" size="small" @click="addOption">
          <PlusOutlined />
          {{ t('common.jsonArrayEditor.addOption') }}
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="options"
        :pagination="false"
        size="small"
        bordered
        row-key="_key"
        :scroll="{ x: 560 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'value'">
            <a-input
              v-model:value="record.value"
              :placeholder="t('common.jsonArrayEditor.valuePlaceholder')"
              @update:value="handleInputChange"
            />
          </template>

          <template v-else-if="column.key === 'label'">
            <a-input
              v-model:value="record.label"
              :placeholder="t('common.jsonArrayEditor.labelPlaceholder')"
              @update:value="handleInputChange"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a
              type="link"
              danger
              size="small"
              @click="removeOption(index)"
            >
              <DeleteOutlined />
              {{ t('common.remove') }}
            </a>
          </template>
        </template>
      </a-table>

      <template v-if="showPreview">
        <a-divider orientation="left">{{ t('common.jsonArrayEditor.jsonPreview') }}</a-divider>
        <pre class="json-preview">{{ jsonPreview }}</pre>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons-vue'
import {
  createJsonArrayOption,
  parseJsonArrayValue,
  serializeJsonArrayOptions,
  type JsonArrayEditorOption,
} from './jsonArrayUtils'

interface Props {
  modelValue?: string
  showPreview?: boolean
  cardless?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  showPreview: true,
  cardless: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

const columns = computed(() => [
  {
    title: t('common.jsonArrayEditor.columnValue'),
    key: 'value',
    dataIndex: 'value',
    width: 200,
  },
  {
    title: t('common.jsonArrayEditor.columnLabel'),
    key: 'label',
    dataIndex: 'label',
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 100,
    align: 'center',
  },
])

const options = ref<JsonArrayEditorOption[]>([])

function initOptions() {
  options.value = parseJsonArrayValue(props.modelValue).options
}

const jsonPreview = computed(() => {
  return JSON.stringify(JSON.parse(serializeJsonArrayOptions(options.value)), null, 2)
})

function handleInputChange() {
  emit('update:modelValue', serializeJsonArrayOptions(options.value))
}

function addOption() {
  options.value.push(createJsonArrayOption())
  handleInputChange()
}

function removeOption(index: number) {
  options.value.splice(index, 1)
  handleInputChange()
}

watch(
  () => props.modelValue,
  newVal => {
    if (newVal !== undefined) {
      initOptions()
    }
  },
  { deep: true, immediate: true },
)
</script>

<style scoped lang="less">
.json-array-editor {
  width: 100%;
  min-width: 0;

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .title {
      font-weight: 500;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.85);
    }
  }

  :deep(.ant-card-body) {
    padding: 12px;
  }

  :deep(.ant-input) {
    min-width: 0;
  }

  &__plain {
    width: 100%;
  }

  .json-preview {
    background-color: #f5f5f5;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    padding: 12px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
    word-wrap: break-word;
    overflow: auto;
    min-height: 100px;
    max-height: 220px;
    margin-top: 8px;
  }
}
</style>
