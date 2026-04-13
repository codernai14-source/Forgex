<template>
  <div class="i18n-input-demo">
    <a-card title="I18nInput 组件示例" :bordered="false">
      <a-space direction="vertical" :size="24" style="width: 100%">
        <div class="demo-section">
          <h3>示例 1：简单模式</h3>
          <p class="description">适合在表单中使用，提供一个输入框和多语言配置按钮。</p>

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="菜单名称">
              <I18nInput
                v-model="form1.nameI18nJson"
                mode="simple"
                placeholder="请输入菜单名称"
              />
            </a-form-item>

            <a-form-item label="当前 JSON">
              <a-textarea
                :value="form1.nameI18nJson"
                :rows="3"
                disabled
                placeholder="这里会显示生成后的 JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <div class="demo-section">
          <h3>示例 2：表格模式</h3>
          <p class="description">直接显示多语言表格，适合独立的多语言配置页面。</p>

          <I18nInput v-model="form2.dictValueI18nJson" mode="table" />

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" style="margin-top: 16px">
            <a-form-item label="当前 JSON">
              <a-textarea
                :value="form2.dictValueI18nJson"
                :rows="3"
                disabled
                placeholder="这里会显示生成后的 JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <div class="demo-section">
          <h3>示例 3：表单集成</h3>
          <p class="description">模拟菜单管理表单中的多语言输入场景。</p>

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="菜单名称" required>
              <I18nInput
                v-model="menuForm.nameI18nJson"
                mode="simple"
                placeholder="请输入菜单名称"
              />
            </a-form-item>

            <a-form-item label="菜单路径">
              <a-input v-model:value="menuForm.path" placeholder="请输入菜单路径" />
            </a-form-item>

            <a-form-item label="菜单图标">
              <a-input v-model:value="menuForm.icon" placeholder="请输入图标名称" />
            </a-form-item>

            <a-form-item label="排序号">
              <a-input-number v-model:value="menuForm.orderNum" :min="0" style="width: 100%" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-space>
                <a-button type="primary" @click="handleSubmit">提交</a-button>
                <a-button @click="handleReset">重置</a-button>
                <a-button @click="handleViewJson">查看 JSON</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </a-space>
    </a-card>

    <a-modal
      v-model:open="jsonModalVisible"
      title="JSON 内容预览"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="菜单名称多语言 JSON">
          <pre>{{ menuForm.nameI18nJson || '未配置' }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="完整表单数据">
          <pre>{{ JSON.stringify(menuForm, null, 2) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from './I18nInput.vue'

const form1 = reactive({
  nameI18nJson: '',
})

const form2 = reactive({
  dictValueI18nJson: '',
})

const menuForm = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0,
})

const jsonModalVisible = ref(false)

const handleSubmit = () => {
  if (!menuForm.nameI18nJson) {
    message.warning('请先配置菜单名称的多语言内容')
    return
  }

  message.success('提交成功')
  console.log('menu form data:', menuForm)
}

const handleReset = () => {
  menuForm.nameI18nJson = ''
  menuForm.path = ''
  menuForm.icon = ''
  menuForm.orderNum = 0
  message.info('表单已重置')
}

const handleViewJson = () => {
  jsonModalVisible.value = true
}
</script>

<style scoped lang="less">
.i18n-input-demo {
  padding: 24px;

  .demo-section {
    h3 {
      margin-bottom: 8px;
      color: #1890ff;
    }

    .description {
      margin-bottom: 16px;
      color: #666;
      font-size: 14px;
    }
  }

  pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 0;
  }
}
</style>
