<template>
  <a-modal
    v-model:open="visible"
    title="第三方系统授权"
    width="900px"
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
      <a-form-item label="授权方式" name="authType">
        <a-radio-group v-model:value="formData.authType" button-style="solid">
          <a-radio-button value="WHITELIST">白名单</a-radio-button>
          <a-radio-button value="TOKEN">Token 授权</a-radio-button>
        </a-radio-group>
      </a-form-item>

      <!-- Token 授权配置 -->
      <template v-if="formData.authType === 'TOKEN'">
        <a-form-item label="Token 值" name="tokenValue">
          <a-input
            v-model:value="formData.tokenValue"
            placeholder="请输入 Token 值（留空则自动生成）"
          />
        </a-form-item>

        <a-form-item label="有效期（小时）" name="tokenExpireHours">
          <a-input-number
            v-model:value="formData.tokenExpireHours"
            :min="1"
            :max="8760"
            placeholder="请输入有效期（1-8760 小时）"
            style="width: 100%"
          />
          <div class="form-item-tip">
            最长不超过 1 年（8760 小时）
          </div>
        </a-form-item>
      </template>

      <!-- 白名单授权配置 -->
      <template v-if="formData.authType === 'WHITELIST'">
        <a-form-item label="白名单 IP" name="whitelistIps">
          <a-textarea
            v-model:value="formData.whitelistIps"
            placeholder="请输入允许的 IP 地址，多个 IP 用逗号分隔"
            :rows="4"
          />
          <div class="form-item-tip">
            支持 IPv4 和 IPv6，例如：192.168.1.1,10.0.0.1
          </div>
        </a-form-item>
      </template>

      <a-form-item label="状态" name="status">
        <a-radio-group v-model:value="formData.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          placeholder="请输入备注信息"
          :rows="3"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { AuthorizationInfo } from '../types'

interface Props {
  open: boolean
  systemId?: number
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  systemId: undefined
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit'): void
}>()

const visible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<AuthorizationInfo>({
  thirdSystemId: props.systemId || 0,
  authType: 'TOKEN',
  tokenValue: '',
  tokenExpireHours: 24,
  whitelistIps: '',
  status: 1,
  remark: ''
})

const rules = {
  authType: [
    { required: true, message: '请选择授权方式', trigger: 'change' }
  ],
  tokenValue: [
    { max: 255, message: 'Token 值不能超过 255 个字符', trigger: 'blur' }
  ],
  tokenExpireHours: [
    { required: true, message: '请输入有效期', trigger: 'change' },
    { type: 'number', min: 1, max: 8760, message: '有效期必须在 1-8760 之间', trigger: 'change' }
  ],
  whitelistIps: [
    { required: true, message: '请输入白名单 IP', trigger: 'blur' }
  ]
}

watch(() => props.open, (val) => {
  visible.value = val
  if (val && props.systemId) {
    formData.thirdSystemId = props.systemId
    // TODO: 加载已有的授权信息
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

<style scoped lang="less">
.form-item-tip {
  color: #999;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
