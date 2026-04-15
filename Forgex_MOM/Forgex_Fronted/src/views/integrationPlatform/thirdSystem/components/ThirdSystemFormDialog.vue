<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑第三方系统' : '新增第三方系统'"
    width="800px"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="系统编码" name="systemCode">
        <a-input
          v-model:value="formData.systemCode"
          placeholder="请输入系统编码（如：ERP、CRM 等）"
          :disabled="isEdit"
        />
      </a-form-item>

      <a-form-item label="系统名称" name="systemName">
        <a-input
          v-model:value="formData.systemName"
          placeholder="请输入系统名称"
        />
      </a-form-item>

      <a-form-item label="IP 地址" name="ipAddress">
        <a-textarea
          v-model:value="formData.ipAddress"
          placeholder="请输入 IP 地址，多个 IP 用逗号分隔"
          :rows="2"
        />
      </a-form-item>

      <a-form-item label="联系信息" name="contactInfo">
        <a-input
          v-model:value="formData.contactInfo"
          placeholder="请输入联系信息（联系人、电话等）"
        />
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注信息"
          :rows="3"
        />
      </a-form-item>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { ThirdSystemSubmit } from './types'

interface Props {
  open: boolean
  formData?: ThirdSystemSubmit
  isEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit'): void
}>()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<ThirdSystemSubmit>({
  systemCode: '',
  systemName: '',
  ipAddress: '',
  contactInfo: '',
  remark: '',
  status: 1
})

const rules = {
  systemCode: [
    { required: true, message: '请输入系统编码', trigger: 'blur' },
    { max: 64, message: '系统编码不能超过 64 个字符', trigger: 'blur' }
  ],
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' },
    { max: 128, message: '系统名称不能超过 128 个字符', trigger: 'blur' }
  ]
}

watch(() => props.open, (val) => {
  visible.value = val
  if (val && props.formData) {
    Object.assign(formData, props.formData)
  }
})

watch(visible, (val) => {
  emit('update:open', val)
})

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    emit('submit')
  } catch (error) {
    console.error('表单验证失败', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  visible.value = false
  formRef.value?.resetFields()
}
</script>
