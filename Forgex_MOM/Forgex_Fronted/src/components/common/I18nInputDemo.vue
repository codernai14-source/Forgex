<template>
  <div class="i18n-input-demo">
    <a-card title="多语言输入组件使用示例" :bordered="false">
      <a-space direction="vertical" :size="24" style="width: 100%">
        
        <!-- 示例1：简单模式（适合表单） -->
        <div class="demo-section">
          <h3>示例1：简单模式（Simple Mode）</h3>
          <p class="description">适合在表单中使用，提供一个输入框和多语言配置按钮</p>
          
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="菜单名称">
              <I18nInput 
                v-model="form1.nameI18nJson" 
                mode="simple" 
                placeholder="请输入菜单名称"
              />
            </a-form-item>
            
            <a-form-item label="当前JSON值">
              <a-textarea 
                :value="form1.nameI18nJson" 
                :rows="3" 
                disabled
                placeholder="这里会显示生成的JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <!-- 示例2：表格模式（适合独立配置页面） -->
        <div class="demo-section">
          <h3>示例2：表格模式（Table Mode）</h3>
          <p class="description">直接显示表格，适合在独立的多语言配置页面使用</p>
          
          <I18nInput 
            v-model="form2.dictValueI18nJson" 
            mode="table"
          />
          
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" style="margin-top: 16px">
            <a-form-item label="当前JSON值">
              <a-textarea 
                :value="form2.dictValueI18nJson" 
                :rows="3" 
                disabled
                placeholder="这里会显示生成的JSON"
              />
            </a-form-item>
          </a-form>
        </div>

        <a-divider />

        <!-- 示例3：实际应用场景 -->
        <div class="demo-section">
          <h3>示例3：实际应用场景</h3>
          <p class="description">模拟菜单管理表单</p>
          
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
                <a-button @click="handleViewJson">查看JSON</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>

      </a-space>
    </a-card>

    <!-- JSON预览弹窗 -->
    <a-modal
      v-model:open="jsonModalVisible"
      title="JSON数据预览"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="菜单名称多语言">
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
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from './I18nInput.vue'

// 示例1数据
const form1 = reactive({
  nameI18nJson: ''
})

// 示例2数据
const form2 = reactive({
  dictValueI18nJson: ''
})

// 示例3数据（菜单表单）
const menuForm = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0
})

const jsonModalVisible = ref(false)

/**
 * 提交表单
 */
const handleSubmit = () => {
  if (!menuForm.nameI18nJson) {
    message.warning('请配置菜单名称的多语言')
    return
  }
  
  message.success('提交成功！')
  console.log('提交的数据：', menuForm)
}

/**
 * 重置表单
 */
const handleReset = () => {
  menuForm.nameI18nJson = ''
  menuForm.path = ''
  menuForm.icon = ''
  menuForm.orderNum = 0
  message.info('表单已重置')
}

/**
 * 查看JSON
 */
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

