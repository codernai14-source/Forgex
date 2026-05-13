<template>
  <a-form
    ref="formRef"
    :model="profileData"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 16 }"
  >
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item :label="t('system.user.profile.politicalStatus')">
          <a-select
            v-model:value="profileData.political状态"
            :placeholder="t('system.user.profile.selectPoliticalStatus')"
            allow-clear
          >
            <a-select-option v-for="option in politicalStatusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      
      <a-col :span="12">
        <a-form-item :label="t('system.user.profile.education')">
          <a-select
            v-model:value="profileData.education"
            :placeholder="t('system.user.profile.selectEducation')"
            allow-clear
          >
            <a-select-option v-for="option in educationOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="24">
        <a-form-item :label="t('system.user.profile.homeAddress')" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
          <a-input
            v-model:value="profileData.homeAddress"
            :placeholder="t('system.user.profile.homeAddressPlaceholder')"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item :label="t('system.user.profile.emergencyContact')">
          <a-input
            v-model:value="profileData.emergencyContact"
            :placeholder="t('system.user.profile.emergencyContactPlaceholder')"
          />
        </a-form-item>
      </a-col>
      
      <a-col :span="12">
        <a-form-item :label="t('system.user.profile.emergencyPhone')">
          <a-input
            v-model:value="profileData.emergencyPhone"
            :placeholder="t('system.user.profile.emergencyPhonePlaceholder')"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <a-row :gutter="16">
      <a-col :span="12">
        <a-form-item :label="t('system.user.profile.referrer')">
          <a-input
            v-model:value="profileData.referrer"
            :placeholder="t('system.user.profile.referrerPlaceholder')"
          />
        </a-form-item>
      </a-col>
    </a-row>
    
    <!-- 工作经历 -->
    <a-divider orientation="left">{{ t('system.user.profile.workHistory') }}</a-divider>

    <div
      v-for="(history, index) in profileData.workHistory"
      :key="index"
      class="work-history-item"
    >
      <a-row :gutter="16">
        <a-col :span="11">
          <a-form-item :label="t('system.user.profile.company')" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
            <a-input
              v-model:value="history.company"
              :placeholder="t('system.user.profile.companyPlaceholder')"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="11">
          <a-form-item :label="t('system.user.profile.position')" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
            <a-input
              v-model:value="history.position"
              :placeholder="t('system.user.profile.positionPlaceholder')"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="2" style="text-align: right;">
          <a-button
            type="link"
            danger
            @click="removeWorkHistory(index)"
          >
            {{ t('common.delete') }}
          </a-button>
        </a-col>
      </a-row>
      
      <a-row :gutter="16">
        <a-col :span="11">
          <a-form-item :label="t('system.user.profile.startDate')" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
            <a-date-picker
              v-model:value="history.startDate"
              :placeholder="t('system.user.profile.startDatePlaceholder')"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
        
        <a-col :span="11">
          <a-form-item :label="t('system.user.profile.endDate')" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
            <a-date-picker
              v-model:value="history.endDate"
              :placeholder="t('system.user.profile.endDatePlaceholder')"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-col>
      </a-row>
      
      <a-row :gutter="16">
        <a-col :span="22">
          <a-form-item :label="t('system.user.profile.description')" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
            <a-textarea
              v-model:value="history.description"
              :placeholder="t('system.user.profile.descriptionPlaceholder')"
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
          {{ t('system.user.profile.addWorkHistory') }}
        </a-button>
      </a-col>
    </a-row>
  </a-form>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
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
const { t } = useI18n({ useScope: 'global' })

const politicalStatusOptions = computed(() => [
  { value: '群众', label: t('system.user.profile.politicalOptions.mass') },
  { value: '共青团员', label: t('system.user.profile.politicalOptions.leagueMember') },
  { value: '中共党员', label: t('system.user.profile.politicalOptions.partyMember') },
  { value: '民主党派', label: t('system.user.profile.politicalOptions.democraticParty') },
  { value: '无党派人士', label: t('system.user.profile.politicalOptions.nonPartisan') },
])

const educationOptions = computed(() => [
  { value: '小学', label: t('system.user.profile.educationOptions.primary') },
  { value: '初中', label: t('system.user.profile.educationOptions.junior') },
  { value: '高中', label: t('system.user.profile.educationOptions.high') },
  { value: '中专', label: t('system.user.profile.educationOptions.secondaryTechnical') },
  { value: '大专', label: t('system.user.profile.educationOptions.juniorCollege') },
  { value: '本科', label: t('system.user.profile.educationOptions.bachelor') },
  { value: '硕士', label: t('system.user.profile.educationOptions.master') },
  { value: '博士', label: t('system.user.profile.educationOptions.doctor') },
])

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

<style scoped lang="less" src="@/styles/views/system/user/components/user-profile-form.less"></style>
