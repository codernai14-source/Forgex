<template>
  <a-form
    ref="formRef"
    :model="profileData"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 16 }"
  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item label="政治面貌">
          <a-select
            v-model:value="profileData.political状态"
            placeholder="请选择政治面貌"
            allow-clear
          >
            <a-select-option value="群众">群众</a-select-option>
            <a-select-option value="共青团员">共青团员</a-select-option>
            <a-select-option value="中共党员">中共党员</a-select-option>
            <a-select-option value="民主党派">民主党派</a-select-option>
            <a-select-option value="无党派人士">无党派人士</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      
      <a-col :span="12">
        <a-form-item label="学历">
          <a-select
            v-model:value="profileData.education"
            placeholder="请选择学历"
            allow-clear
          >
            <a-select-option value="小学">小学</a-select-option>
            <a-select-option value="初中">初中</a-select-option>
            <a-select-option value="高中">高中</a-select-option>
            <a-select-option value="中专">中专</a-select-option>
            <a-select-option value="大专">大专</a-select-option>
            <a-select-option value="本科">本科</a-select-option>
            <a-select-option value="硕士">硕士</a-select-option>
            <a-select-option value="博士">博士</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item label="家庭住址" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
          <a-input
            v-model:value="profileData.homeAddress"
            placeholder="请输入家庭住址"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item label="紧急联系人">
          <a-input
            v-model:value="profileData.emergencyContact"
            placeholder="请输入紧急联系人"
          />
        </a-form-item>
      </a-col>
      
      <a-col :span="12">
        <a-form-item label="紧急联系电话">
          <a-input
            v-model:value="profileData.emergencyPhone"
            placeholder="请输入紧急联系电话"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item label="介绍人">
          <a-input
            v-model:value="profileData.referrer"
            placeholder="请输入介绍人"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <!-- 工作经历 -->
    <a-divider orientation="left">工作经历</a-divider>

    <div
      v-for="(history, index) in profileData.workHistory"
      :key="index"
      class="work-history-item"
    >
      <a-row :gutter="16">
        <a-col :span="11">
          <a-form-item label="公司名称" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
            <a-input
              v-model:value="history.company"
              placeholder="请输入公司名称"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="11">
          <a-form-item label="职位" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
            <a-input
              v-model:value="history.position"
              placeholder="请输入职位"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="2" style="text-align: right;">
          <a-button
            type="link"
            danger
            @click="removeWorkHistory(index)"
          >
            删除
          </a-button>
        </a-col>
      </a-row>
      
      <a-row :gutter="16">
        <a-col :span="11">
          <a-form-item label="开始时间" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
            <a-date-picker
              v-model:value="history.startDate"
              placeholder="请选择开始时间"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="11">
          <a-form-item label="结束时间" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
            <a-date-picker
              v-model:value="history.endDate"
              placeholder="请选择结束时间"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
      </a-row>
      
      <a-row :gutter="16">
        <a-col :span="22">
          <a-form-item label="工作描述" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
            <a-textarea
              v-model:value="history.description"
              placeholder="请输入工作描述"
              :rows="2"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </div>
    
    <a-row>
      <a-col :span="24" style="text-align: center;">
        <a-button type="dashed" block @click="addWorkHistory">
          <template #icon><PlusOutlined /></template>
          添加工作经历
        </a-button>
      </a-col>
    </a-row>
  </a-form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { UserProfile } from '../types'

// Props
interface Props {
  modelValue: Partial<UserProfile>
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: Partial<UserProfile>]
}>()

// 响应式数据
const formRef = ref()
const profileData = ref(props.modelValue)

/**
 * 添加工作经历
 */
function addWorkHistory() {
  if (!profileData.value.workHistory) {
    profileData.value.workHistory = []
  }
  profileData.value.workHistory.push({
    company: '',
    position: '',
    startDate: '',
    endDate: '',
    description: '',
  })
  emit('update:modelValue', profileData.value)
}

/**
 * 删除工作经历
 */
function removeWorkHistory(index: number) {
  profileData.value.workHistory?.splice(index, 1)
  emit('update:modelValue', profileData.value)
}

/**
 * 暴露表单校验方法
 */
defineExpose({
  validate: () => formRef.value?.validate(),
  clearValidate: () => formRef.value?.clearValidate(),
})
</script>

<style scoped lang="less">
.work-history-item {
  padding: 16px;
  margin-bottom: 16px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #ffffff), var(--fx-fill-secondary, #fafafa));
  border-radius: var(--fx-radius, 6px);
  border: 1px solid var(--fx-border-color, #f0f0f0);
  box-shadow: var(--fx-shadow-secondary, 0 1px 2px rgba(0, 0, 0, 0.08));
}
</style>
