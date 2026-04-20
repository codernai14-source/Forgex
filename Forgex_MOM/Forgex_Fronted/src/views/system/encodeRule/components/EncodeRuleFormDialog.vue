<template>
  <a-modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    width="900px"
    @ok="handleSubmit"
    @cancel="handleDialogClose"
    destroy-on-close
  >
    <a-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item :label="t('system.encodeRule.ruleName')" name="ruleName">
        <a-input
          v-model:value="form.ruleName"
          :placeholder="t('system.encodeRule.ruleNamePlaceholder')"
          :maxlength="50"
        />
      </a-form-item>
      
      <a-form-item :label="t('system.encodeRule.ruleCode')" name="ruleCode">
        <a-input
          v-model:value="form.ruleCode"
          :placeholder="t('system.encodeRule.ruleCodePlaceholder')"
          :maxlength="50"
          :disabled="isEdit"
        />
      </a-form-item>
      
      <a-form-item :label="t('system.encodeRule.businessType')" name="businessType">
        <a-input
          v-model:value="form.businessType"
          :placeholder="t('system.encodeRule.businessTypePlaceholder')"
          :maxlength="50"
        />
      </a-form-item>
      
      <a-form-item :label="t('system.encodeRule.description')" name="description">
        <a-textarea
          v-model:value="form.description"
          :placeholder="t('system.encodeRule.descriptionPlaceholder')"
          :rows="2"
          :maxlength="200"
        />
      </a-form-item>
      
      <a-form-item :label="t('system.encodeRule.status')" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio v-for="item in statusRadioOptions" :key="String(item.value)" :value="item.value">
            {{ item.label }}
          </a-radio>
        </a-radio-group>
      </a-form-item>
      
      <a-divider orientation="left">
        {{ t('system.encodeRule.ruleDetails') }}
      </a-divider>
      
      <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
        <a-button type="dashed" block @click="handleAddDetail">
          <PlusOutlined />
          {{ t('system.encodeRule.addDetail') }}
        </a-button>
      </a-form-item>
      
      <a-form-item
        v-for="(detail, index) in form.ruleDetails"
        :key="detail.id || index"
        :label="t('system.encodeRule.detailSegment') + (detail.segmentOrder || index + 1)"
        :name="['ruleDetails', index]"
        :rules="detailRules"
      >
        <div class="detail-item">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-form-item :name="['ruleDetails', index, 'segmentType']" no-style>
                <a-select
                  v-model:value="detail.segmentType"
                  :placeholder="t('system.encodeRule.segmentType')"
                  @change="handleSegmentTypeChange(detail)"
                >
                  <a-select-option
                    v-for="item in segmentTypeSelectOptions"
                    :key="String(item.value)"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            
            <a-col :span="8">
              <a-form-item :name="['ruleDetails', index, 'segmentValue']" no-style>
                <a-input
                  v-if="detail.segmentType === 'FIXED' || detail.segmentType === 'CUSTOM'"
                  v-model:value="detail.segmentValue"
                  :placeholder="t('system.encodeRule.segmentValue')"
                />
                <a-input
                  v-else-if="detail.segmentType === 'DATE'"
                  v-model:value="detail.date表单at"
                  :placeholder="t('system.encodeRule.date表单atPlaceholder')"
                />
                <a-input
                  v-else
                  v-model:value="detail.segmentValue"
                  :placeholder="t('system.encodeRule.segmentValue')"
                  disabled
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="6" v-if="detail.segmentType === 'SEQ'">
              <a-row :gutter="8">
                <a-col :span="12">
                  <a-form-item :name="['ruleDetails', index, 'seqStart']" no-style>
                    <a-input-number
                      v-model:value="detail.seqStart"
                      :placeholder="t('system.encodeRule.seqStart')"
                      :min="0"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item :name="['ruleDetails', index, 'seqLength']" no-style>
                    <a-input-number
                      v-model:value="detail.seqLength"
                      :placeholder="t('system.encodeRule.seqLength')"
                      :min="1"
                      :max="20"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-col>
            
            <a-col :span="6" v-if="detail.segmentType === 'SEQ'">
              <a-form-item :name="['ruleDetails', index, 'seqResetType']" no-style>
                <a-select
                  v-model:value="detail.seqResetType"
                  :placeholder="t('system.encodeRule.seqResetType')"
                >
                  <a-select-option
                    v-for="item in seqResetTypeSelectOptions"
                    :key="String(item.value)"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            
            <a-col :span="4">
              <a-space>
                <a-button
                  type="link"
                  danger
                  @click="handleRemoveDetail(index)"
                >
                  <DeleteOutlined />
                </a-button>
              </a-space>
            </a-col>
          </a-row>
          
          <a-row :gutter="16" v-if="detail.remark">
            <a-col :span="20" :offset="4">
              <a-typography-text type="secondary" style="font-size: 12px">
                {{ detail.remark }}
              </a-typography-text>
            </a-col>
          </a-row>
        </div>
      </a-form-item>
      
      <a-form-item :label="t('system.encodeRule.remark')" name="remark">
        <a-textarea
          v-model:value="form.remark"
          :placeholder="t('system.encodeRule.remarkPlaceholder')"
          :rows="2"
          :maxlength="200"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 编码规则表单弹窗组件
 * 
 * 功能：
 * 1. 支持新增和编辑模式
 * 2. 规则明细动态增删
 * 3. 段类型下拉选择使用字典数据
 * 4. 包含表单校验
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, type 表单Instance, type 表单Rule } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { encodeRuleApi } from '@/api/system/encodeRule'
import { useDict } from '@/hooks/useDict'
import type { EncodeRuleDetail, SaveEncodeRuleParam } from '@/api/system/encodeRule'

// 国际化
const { t } = useI18n()
const { dictItems: encodeRuleEnabledOptions } = useDict('encode_rule_enabled')
const { dictItems: segmentTypeDictItems } = useDict('encode_rule_segment_type')
const { dictItems: seqResetTypeDictItems } = useDict('encode_rule_seq_reset_type')

// Props
interface Props {
  open: boolean
  isEdit?: boolean
  ruleId?: string
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false,
  ruleId: undefined,
})

// Emits
const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}>()

// 弹窗状态
const dialogVisible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

// 表单引用
const formRef = ref<表单Instance>()

interface EncodeRule表单Detail extends Omit<EncodeRuleDetail, 'segmentType'> {
  segmentType?: 'FIXED' | 'DATE' | 'SEQ' | 'CUSTOM'
  date表单at?: string
  seqStart?: number
  seqLength?: number
  seqResetType?: number
}

interface EncodeRule表单Model {
  id?: string
  ruleName: string
  ruleCode: string
  businessType: string
  description: string
  status: number
  ruleDetails: EncodeRule表单Detail[]
  remark: string
}
// 表单数据
const form = reactive<EncodeRule表单Model>({
  ruleName: '',
  ruleCode: '',
  businessType: '',
  description: '',
  status: 1,
  ruleDetails: [],
  remark: '',
})

const statusRadioOptions = computed(() =>
  (encodeRuleEnabledOptions.value || []).map((item: { label: string; value: string | number | boolean }) => ({
    label: item.label,
    value:
      item.value === true || item.value === 'true' || item.value === 1 || item.value === '1'
        ? 1
        : 0,
  })),
)

const segmentTypeSelectOptions = computed(() =>
  (segmentTypeDictItems.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: String(item.value),
  })),
)

const seqResetTypeSelectOptions = computed(() =>
  (seqResetTypeDictItems.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)

// 表单验证规则
const formRules: Record<string, 表单Rule[]> = {
  ruleName: [
    { required: true, message: t('system.encodeRule.ruleNameRequired'), trigger: 'blur' },
    { max: 50, message: t('system.encodeRule.ruleNameMaxLength'), trigger: 'blur' },
  ],
  ruleCode: [
    { required: true, message: t('system.encodeRule.ruleCodeRequired'), trigger: 'blur' },
    { max: 50, message: t('system.encodeRule.ruleCodeMaxLength'), trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: t('system.encodeRule.ruleCodePattern'), trigger: 'blur' },
  ],
}

// 明细验证规则
const detailRules: 表单Rule[] = [
  {
    required: true,
    validator: (_rule, value) => {
      if (!value || !value.segmentType) {
        return Promise.reject(t('system.encodeRule.segmentTypeRequired'))
      }
      if (value.segmentType === 'FIXED' && !value.segmentValue) {
        return Promise.reject(t('system.encodeRule.segmentValueRequired'))
      }
      if (value.segmentType === 'DATE' && !value.date表单at) {
        return Promise.reject(t('system.encodeRule.date表单atRequired'))
      }
      if (value.segmentType === 'SEQ') {
        if (!value.seqStart && value.seqStart !== 0) {
          return Promise.reject(t('system.encodeRule.seqStartRequired'))
        }
        if (!value.seqLength) {
          return Promise.reject(t('system.encodeRule.seqLengthRequired'))
        }
      }
      return Promise.resolve()
    },
    trigger: 'change',
  },
]

// 弹窗标题
const dialogTitle = computed(() => {
  return props.isEdit ? t('system.encodeRule.edit') : t('system.encodeRule.add')
})

function mapBackendDetailTo表单(detail: EncodeRuleDetail): EncodeRule表单Detail {
  const backendType = String(detail?.segmentType || '').toUpperCase()
  const formType =
    backendType === 'SERIAL'
      ? 'SEQ'
      : backendType === 'EXPRESSION'
        ? 'CUSTOM'
        : backendType || 'FIXED'

  return {
    id: detail?.id != null ? String(detail.id) : undefined,
    ruleId: detail?.ruleId != null ? String(detail.ruleId) : undefined,
    segmentOrder: detail?.segmentOrder,
    segmentType: formType,
    segmentValue: detail?.segmentValue || detail?.conditionExpression || '',
    date表单at: formType === 'DATE' ? (detail?.segmentValue || '') : undefined,
    remark: detail?.remark || '',
    connector: detail?.connector,
    isRequired: detail?.isRequired,
    conditionExpression: detail?.conditionExpression,
  }
}

function map表单DetailToBackend(detail: EncodeRule表单Detail, index: number): EncodeRuleDetail {
  const formType = String(detail?.segmentType || 'FIXED').toUpperCase()
  const backendType =
    formType === 'SEQ'
      ? 'SERIAL'
      : formType === 'CUSTOM'
        ? 'EXPRESSION'
        : formType

  const segmentValue = formType === 'DATE'
    ? (detail.date表单at || detail.segmentValue || '')
    : (detail.segmentValue || '')

  return {
    id: detail.id,
    ruleId: detail.ruleId,
    segmentOrder: detail.segmentOrder || index + 1,
    segmentType: backendType,
    segmentValue,
    connector: detail.connector,
    isRequired: detail.isRequired ?? true,
    conditionExpression: backendType === 'EXPRESSION' ? (detail.conditionExpression || segmentValue) : undefined,
    remark: detail.remark || '',
  }
}

// 重置表单
function reset表单() {
  form.id = undefined
  form.ruleName = ''
  form.ruleCode = ''
  form.businessType = ''
  form.description = ''
  form.status = 1
  form.ruleDetails = []
  form.remark = ''
}

// 加载规则详情
async function loadRuleDetail() {
  if (!props.ruleId) return
  try {
    const data = await encodeRuleApi.getEncodeRule(props.ruleId)
    form.id = data.id
    form.ruleName = data.ruleName || ''
    form.ruleCode = data.ruleCode || ''
    form.businessType = data.module || ''
    form.description = ''
    form.status = data.isEnabled === false ? 0 : 1
    form.ruleDetails = (data.detailList || []).map((detail) => mapBackendDetailTo表单(detail))
    form.remark = data.remark || ''
  } catch (error) {
    console.error('加载规则详情失败:', error)
    message.error(t('system.encodeRule.loadDetailFailed'))
  }
}

// 添加明细
function handleAddDetail() {
  const newDetail: EncodeRule表单Detail = {
    segmentOrder: (form.ruleDetails?.length || 0) + 1,
    segmentType: 'FIXED',
    segmentValue: '',
    seqStart: 1,
    seqLength: 6,
    seqResetType: 0,
  }
  if (!form.ruleDetails) {
    form.ruleDetails = []
  }
  form.ruleDetails.push(newDetail)
}

// 删除明细
function handleRemoveDetail(index: number) {
  form.ruleDetails?.splice(index, 1)
  // 重新排序
  form.ruleDetails?.forEach((detail, idx) => {
    detail.segmentOrder = idx + 1
  })
}

// 段类型变化处理
function handleSegmentTypeChange(detail: EncodeRule表单Detail) {
  // 根据段类型清空或设置默认值
  if (detail.segmentType === 'FIXED') {
    detail.segmentValue = ''
    detail.date表单at = undefined
    detail.seqStart = undefined
    detail.seqLength = undefined
    detail.seqResetType = undefined
  } else if (detail.segmentType === 'DATE') {
    detail.segmentValue = undefined
    detail.date表单at = 'YYYYMMDD'
    detail.seqStart = undefined
    detail.seqLength = undefined
    detail.seqResetType = undefined
  } else if (detail.segmentType === 'SEQ') {
    detail.segmentValue = undefined
    detail.date表单at = undefined
    detail.seqStart = 1
    detail.seqLength = 6
    detail.seqResetType = 0
  } else if (detail.segmentType === 'CUSTOM') {
    detail.segmentValue = ''
    detail.date表单at = undefined
    detail.seqStart = undefined
    detail.seqLength = undefined
    detail.seqResetType = undefined
  }
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    
    // 校验明细至少有一条
    if (!form.ruleDetails || form.ruleDetails.length === 0) {
      message.warning(t('system.encodeRule.detailRequired'))
      return
    }
    
    // 校验明细的序号连续性
    const orders = form.ruleDetails.map((d, i) => d.segmentOrder || (i + 1))
    for (let i = 0; i < orders.length; i++) {
      if (orders[i] !== i + 1) {
        message.error(t('system.encodeRule.detailOrderError'))
        return
      }
    }
    
    const requestPayload: SaveEncodeRuleParam = {
      id: form.id,
      ruleCode: form.ruleCode,
      ruleName: form.ruleName,
      module: form.businessType,
      isEnabled: form.status === 1,
      remark: form.remark,
      detailList: (form.ruleDetails || []).map((detail, index) => map表单DetailToBackend(detail, index)),
    }

    await encodeRuleApi.saveEncodeRule(requestPayload)
    message.success(props.isEdit ? t('system.encodeRule.updateSuccess') : t('system.encodeRule.addSuccess'))
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 关闭弹窗
function handleDialogClose() {
  dialogVisible.value = false
  reset表单()
  formRef.value?.resetFields()
}

// 监听 open 变化
watch(() => props.open, async (newVal) => {
  if (!newVal) {
    return
  }

  reset表单()

  if (props.isEdit) {
    await loadRuleDetail()
    return
  }

  handleAddDetail()
})

// 初始化
onMounted(() => {
  // 初始添加一条明细
  if (!props.isEdit) {
    handleAddDetail()
  }
})
</script>

<style scoped lang="less">
.detail-item {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--fx-border-color, #d9d9d9);
  border-radius: 4px;
  margin-bottom: 12px;
  background-color: var(--fx-content-bg, #fafafa);
  
  &:hover {
    border-color: var(--fx-theme-color, #1890ff);
    background-color: var(--fx-tab-hover-bg, #f0f5ff);
  }
}
</style>
