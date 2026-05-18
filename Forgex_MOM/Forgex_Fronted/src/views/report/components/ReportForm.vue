<template>
  <a-modal
    v-model:open="visible"
    :title="formTitle"
    width="700px"
    :destroy-on-close="true"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="form"
      :rules="formRules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 16, offset: 1 }"
    >
      <a-form-item :label="$tl('报表名称')" name="name">
        <a-input
          v-model:value="form.name"
          :placeholder="$tl('请输入报表名称')"
          maxlength="100"
          show-count
        />
      </a-form-item>

      <a-form-item :label="$tl('报表编码')" name="code">
        <a-input
          v-model:value="form.code"
          :placeholder="$tl('请输入报表编码，首字母必须为字母，可包含字母、数字和下划线')"
          maxlength="50"
          show-count
          :disabled="!!form.id"
        />
      </a-form-item>

      <a-form-item :label="$tl('引擎类型')" name="engineType">
        <a-radio-group v-model:value="form.engineType">
          <a-radio value="UREPORT">
            <a-tag color="blue">UReport2</a-tag>
          </a-radio>
          <a-radio value="JIMU">
            <a-tag color="green">JimuReport</a-tag>
          </a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item :label="$tl('报表分类')" name="categoryId">
        <a-tree-select
          v-model:value="form.categoryId"
          :tree-data="categoryTreeData"
          :placeholder="$tl('请选择报表分类')"
          allow-clear
          tree-node-filter-prop="label"
          show-search
        />
      </a-form-item>

      <a-form-item :label="$tl('数据源')" name="datasourceId">
        <a-select
          v-model:value="form.datasourceId"
          :options="datasourceSelectOptions"
          :placeholder="$tl('请选择数据源')"
          allow-clear
          show-search
        />
      </a-form-item>

      <a-form-item :label="$tl('报表状态')" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio :value="1">{{ $tl('启用') }}</a-radio>
          <a-radio :value="0">{{ $tl('禁用') }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item :label="$tl('备注')" name="remark">
        <a-textarea
          v-model:value="form.remark"
          :placeholder="$tl('请输入备注')"
          :rows="3"
          maxlength="500"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { type FormInstance } from 'ant-design-vue'
import type { ReportTemplate, ReportSaveDTO } from '@/report/types'
import { save } from '@/api/report'
import { translateLegacyText } from '@/utils/legacyI18n'

interface Props {
  open: boolean
  formData?: Partial<ReportTemplate>
  categoryOptions?: Array<{ label: string; value: number }>
  datasourceOptions?: Array<{ label: string; value: number }>
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'ok'): void
}

const props = withDefaults(defineProps<Props>(), {
  formData: () => ({}),
  categoryOptions: () => [],
  datasourceOptions: () => [],
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const formRef = ref<FormInstance>()
const formTitle = computed(() => (form.id ? translateLegacyText('编辑报表') : translateLegacyText('新增报表')))

const form = reactive<ReportSaveDTO>({
  id: undefined,
  name: '',
  code: '',
  engineType: 'UREPORT',
  categoryId: undefined,
  datasourceId: undefined,
  content: undefined,
  status: 1,
  remark: '',
})

const formRules = {
  name: [
    { required: true, message: translateLegacyText('请输入报表名称'), trigger: 'blur' },
    { max: 100, message: translateLegacyText('报表名称不能超过 100 个字符'), trigger: 'blur' },
  ],
  code: [
    { required: true, message: translateLegacyText('请输入报表编码'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/,
      message: translateLegacyText('报表编码必须以字母开头，且只能包含字母、数字和下划线'),
      trigger: 'blur',
    },
    { max: 50, message: translateLegacyText('报表编码不能超过 50 个字符'), trigger: 'blur' },
  ],
  engineType: [{ required: true, message: translateLegacyText('请选择引擎类型'), trigger: 'change' }],
}

const categoryTreeData = computed(() => {
  return (props.categoryOptions || []).map((item) => ({
    title: item.label,
    label: item.label,
    value: item.value,
    key: item.value,
  }))
})

const datasourceSelectOptions = computed(() => props.datasourceOptions || [])

function resetForm() {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.engineType = 'UREPORT'
  form.categoryId = undefined
  form.datasourceId = undefined
  form.content = undefined
  form.status = 1
  form.remark = ''
  formRef.value?.resetFields()
}

function loadFormData(data: Partial<ReportTemplate>) {
  form.id = data.id
  form.name = data.name || ''
  form.code = data.code || ''
  form.engineType = (data.engineType as 'UREPORT' | 'JIMU') || 'UREPORT'
  form.categoryId = data.categoryId
  form.datasourceId = data.datasourceId
  form.content = data.content
  form.status = data.status ?? 1
  form.remark = data.remark || ''
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    await save(form)
    emit('ok')
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    console.error('保存报表失败', error)
  }
}

function handleCancel() {
  resetForm()
  emit('update:open', false)
}

watch(
  () => props.formData,
  (newData) => {
    if (newData && Object.keys(newData).length > 0) {
      nextTick(() => {
        loadFormData(newData)
      })
    } else {
      resetForm()
    }
  },
  { deep: true },
)

onMounted(() => {
  if (!props.formData || Object.keys(props.formData).length === 0) {
    resetForm()
  }
})
</script>


