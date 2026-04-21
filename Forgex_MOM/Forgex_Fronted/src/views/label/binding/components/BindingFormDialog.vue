<template>
  <a-modal
      v-model:open="dialogVisible"
      :title="formTitle"
      width="700px"
      :confirm-loading="submitting"
      @ok="handleSubmit"
      @cancel="handleCancel"
  >
    <a-form :model="formData" layout="vertical">
      <a-form-item label="选择模板" required>
        <a-input
            v-model:value="formData.templateName"
            placeholder="请选择模板"
            readonly
            @click="handleSelectTemplate"
        >
          <template #suffix>
            <SearchOutlined style="color: #1890ff; cursor: pointer;" />
          </template>
        </a-input>
        <div v-if="formData.templateId" class="template-info">
          <a-tag color="blue">{{ formData.templateCode }}</a-tag>
          <span style="margin-left: 8px; font-size: 12px; color: #999;">模板 ID: {{ formData.templateId }}</span>
        </div>
      </a-form-item>

      <a-form-item label="绑定类型" required>
        <a-select v-model:value="formData.bindingType" placeholder="请选择绑定类型">
          <a-select-option value="MATERIAL">
            <a-tag color="blue">按物料匹配</a-tag>
          </a-select-option>
          <a-select-option value="SUPPLIER">
            <a-tag color="green">按供应商匹配</a-tag>
          </a-select-option>
          <a-select-option value="CUSTOMER">
            <a-tag color="orange">按客户匹配</a-tag>
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="绑定值" required>
        <a-input
            v-model:value="formData.bindingValue"
            :placeholder="getBindingValuePlaceholder()"
        />
      </a-form-item>

      <a-form-item label="工厂">
        <a-input
            v-model:value="formData.factoryName"
            placeholder="不填表示全局生效"
            readonly
            @click="handleSelectFactory"
        >
          <template #suffix>
            <SearchOutlined style="color: #1890ff; cursor: pointer;" />
          </template>
        </a-input>
        <div v-if="formData.factoryId" class="factory-info">
          <a-tag color="green">{{ formData.factoryName }}</a-tag>
          <span style="margin-left: 8px; font-size: 12px; color: #999;">工厂 ID: {{ formData.factoryId }}</span>
        </div>
      </a-form-item>

      <a-form-item label="优先级" required>
        <a-radio-group v-model:value="formData.priority">
          <a-radio :value="1">
            <a-tag color="red">高 (1)</a-tag>
          </a-radio>
          <a-radio :value="2">
            <a-tag color="orange">中 (2)</a-tag>
          </a-radio>
          <a-radio :value="3">
            <a-tag color="blue">低 (3)</a-tag>
          </a-radio>
        </a-radio-group>
        <div class="priority-tip">
          <InfoCircleOutlined style="color: #1890ff; margin-right: 4px;" />
          优先级越高，匹配时越优先使用
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { labelBindingApi } from '@/api/label/binding'

const props = defineProps<{
  visible: boolean
  bindingData?: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const submitting = ref(false)

const dialogVisible = computed({
  get: () => {
    console.log('BindingFormDialog visible get:', props.visible)
    return props.visible
  },
  set: (val) => {
    console.log('BindingFormDialog visible set:', val)
    emit('update:visible', val)
  }
})

const formTitle = computed(() =>
    props.bindingData ? '编辑绑定关系' : '新增绑定关系'
)

const formData = ref<any>({
  id: undefined,
  templateId: undefined,
  templateCode: '',
  templateName: '',
  bindingType: '',
  bindingValue: '',
  factoryId: undefined,
  factoryName: '',
  priority: 3
})

watch(() => props.visible, (newVal) => {
  console.log('BindingFormDialog visible 变化:', newVal, 'bindingData:', props.bindingData)
  if (newVal && props.bindingData) {
    // 编辑模式，填充数据
    formData.value = {
      id: props.bindingData.id,
      templateId: props.bindingData.templateId,
      templateCode: props.bindingData.templateCode,
      templateName: props.bindingData.templateName,
      bindingType: props.bindingData.bindingType,
      bindingValue: props.bindingData.bindingValue,
      factoryId: props.bindingData.factoryId,
      factoryName: props.bindingData.factoryName,
      priority: props.bindingData.priority ?? 3
    }
    console.log('填充编辑数据:', formData.value)
  } else if (newVal) {
    // 新增模式，重置表单
    resetForm()
  }
})

function resetForm() {
  formData.value = {
    id: undefined,
    templateId: undefined,
    templateCode: '',
    templateName: '',
    bindingType: '',
    bindingValue: '',
    factoryId: undefined,
    factoryName: '',
    priority: 3
  }
}

function getBindingValuePlaceholder() {
  const placeholders: Record<string, string> = {
    MATERIAL: '请输入物料编码（如：MAT001）',
    SUPPLIER: '请输入供应商编码（如：SUP001）',
    CUSTOMER: '请输入客户编码（如：CUS001）'
  }
  return placeholders[formData.value.bindingType] || '请先选择绑定类型'
}

function handleSelectTemplate() {
  message.info('模板选择功能开发中')
}

function handleSelectFactory() {
  message.info('工厂选择功能开发中')
}

async function handleSubmit() {
  // 表单验证
  if (!formData.value.templateId) {
    message.warning('请选择模板')
    return
  }
  if (!formData.value.bindingType) {
    message.warning('请选择绑定类型')
    return
  }
  if (!formData.value.bindingValue) {
    message.warning('请输入绑定值')
    return
  }
  if (!formData.value.priority) {
    message.warning('请选择优先级')
    return
  }

  submitting.value = true
  try {
    if (props.bindingData) {
      // 编辑模式
      await labelBindingApi.update({
        id: formData.value.id,
        priority: formData.value.priority,
        factoryId: formData.value.factoryId
      })
      message.success('更新成功')
    } else {
      // 新增模式
      await labelBindingApi.add({
        templateId: formData.value.templateId,
        bindingType: formData.value.bindingType,
        bindingValue: formData.value.bindingValue,
        factoryId: formData.value.factoryId,
        priority: formData.value.priority
      })
      message.success('创建成功')
    }
    emit('success')
    dialogVisible.value = false
  } catch (error: any) {
    message.error(error.message || (props.bindingData ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  dialogVisible.value = false
  resetForm()
}
</script>

<style scoped lang="less">
.template-info,
.factory-info {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 13px;
}

.priority-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  font-size: 12px;
  color: #1890ff;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
}

:deep(.ant-select-selector) {
  .ant-tag {
    margin-right: 0;
  }
}
</style>
