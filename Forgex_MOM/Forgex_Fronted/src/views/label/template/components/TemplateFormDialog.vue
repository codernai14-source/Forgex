
<template>
  <a-drawer
      v-model:open="drawerVisible"
      :title="drawerTitle"
      width="1000px"
      :closable="true"
      :mask-closable="false"
      placement="right"
  >
    <a-form :model="formData" layout="vertical">
      <!-- 基本信息 -->
      <a-card title="基本信息" size="small" class="section-card">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="模板编码" required>
              <a-input
                  v-model:value="formData.templateCode"
                  placeholder="请输入模板编码"
                  :disabled="!!props.templateData"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模板名称" required>
              <a-input
                  v-model:value="formData.templateName"
                  placeholder="请输入模板名称"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="模板类型" required>
              <a-select
                  v-model:value="formData.templateType"
                  placeholder="请选择模板类型"
              >
                <a-select-option value="PRODUCT">
                  <a-tag color="blue">产品标签</a-tag>
                </a-select-option>
                <a-select-option value="MATERIAL">
                  <a-tag color="orange">物料标签</a-tag>
                </a-select-option>
                <a-select-option value="BATCH">
                  <a-tag color="purple">批次标签</a-tag>
                </a-select-option>
                <a-select-option value="SUPPLIER">
                  <a-tag color="cyan">供应商标签</a-tag>
                </a-select-option>
                <a-select-option value="CUSTOMER_MARK">
                  <a-tag color="pink">客户唛头</a-tag>
                </a-select-option>
                <a-select-option value="WORKSTATION">
                  <a-tag color="green">工位标签</a-tag>
                </a-select-option>
                <a-select-option value="EQUIPMENT">
                  <a-tag color="volcano">设备标签</a-tag>
                </a-select-option>
                <a-select-option value="LOCATION">
                  <a-tag color="geekblue">库位标签</a-tag>
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模板版本">
              <a-input
                  v-model:value="formData.templateVersion"
                  placeholder="如: v1.0"
                  disabled
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">
                  <a-tag color="green">启用</a-tag>
                </a-radio>
                <a-radio :value="0">
                  <a-tag color="red">禁用</a-tag>
                </a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="设为默认">
              <a-switch
                  v-model:checked="formData.isDefault"
                  checked-children="是"
                  un-checked-children="否"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="描述">
          <a-textarea
              v-model:value="formData.description"
              placeholder="请输入模板描述"
              :rows="3"
              :maxlength="500"
              show-count
          />
        </a-form-item>
      </a-card>

      <!-- 模板内容设计 -->
      <a-card title="模板内容设计" size="small" class="section-card" style="margin-top: 16px;">
        <div class="editor-container">
          <LabelTemplateEditor
              v-model="formData.templateContent"
          />
        </div>
      </a-card>
    </a-form>

    <template #footer>
      <div class="drawer-footer">
        <a-button @click="handleCancel">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ props.templateData ? '保存修改' : '创建模板' }}
        </a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { labelTemplateApi } from '@/api/label/template'
import LabelTemplateEditor from './LabelTemplateEditor.vue'

const props = defineProps<{
  visible: boolean
  templateData?: any
}>()

const emit = defineEmits(['update:visible', 'success'])

const submitting = ref(false)

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const drawerTitle = computed(() =>
    props.templateData ? '编辑标签模板' : '新增标签模板'
)

const formData = ref<any>({
  id: undefined,
  templateCode: '',
  templateName: '',
  templateType: '',
  templateVersion: 'v1.0',
  templateContent: '{}',
  description: '',
  status: 1,
  isDefault: false
})

watch(() => props.visible, async (newVal) => {
  if (newVal) {
    if (props.templateData) {
      await loadTemplateDetail()
    } else {
      resetForm()
    }
  }
})

async function loadTemplateDetail() {
  try {
    const detail = await labelTemplateApi.detail(props.templateData.id)
    formData.value = {
      id: detail.id,
      templateCode: detail.templateCode,
      templateName: detail.templateName,
      templateType: detail.templateType,
      templateVersion: `v${detail.templateVersion || '1.0'}`,
      templateContent: typeof detail.templateContent === 'string'
          ? detail.templateContent
          : JSON.stringify(detail.templateContent, null, 2),
      description: detail.description || '',
      status: detail.status ?? 1,
      isDefault: detail.isDefault ?? false
    }
  } catch (error) {
    message.error('获取模板详情失败')
    drawerVisible.value = false
  }
}

function resetForm() {
  formData.value = {
    id: undefined,
    templateCode: '',
    templateName: '',
    templateType: '',
    templateVersion: 'v1.0',
    templateContent: '{}',
    description: '',
    status: 1,
    isDefault: false
  }
}

async function handleSubmit() {
  // 表单验证
  if (!formData.value.templateCode) {
    message.warning('请输入模板编码')
    return
  }
  if (!formData.value.templateName) {
    message.warning('请输入模板名称')
    return
  }
  if (!formData.value.templateType) {
    message.warning('请选择模板类型')
    return
  }

  submitting.value = true
  try {
    const submitData = {
      ...formData.value,
      templateVersion: formData.value.templateVersion.replace('v', '')
    }

    if (props.templateData) {
      await labelTemplateApi.update(submitData)
      message.success('更新成功')
    } else {
      await labelTemplateApi.add(submitData)
      message.success('创建成功')
    }
    emit('success')
    drawerVisible.value = false
  } catch (error) {
    message.error(props.templateData ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  drawerVisible.value = false
  resetForm()
}
</script>

<style scoped lang="less">
.section-card {
  :deep(.ant-card-head) {
    background-color: #fafafa;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.ant-card-body) {
    padding: 16px;
  }
}

.editor-container {
  min-height: 400px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
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
