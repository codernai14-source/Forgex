<template>
  <div class="profile-page">
    <a-card :title="$t('profile.title')" :loading="loading">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基本信息 -->
        <a-tab-pane key="basic" :tab="$t('profile.tabs.basic')">
          <a-row :gutter="24">
            <a-col :span="8">
              <div class="avatar-section">
                <AvatarUpload
                  v-model="formData.avatar"
                  @success="handleAvatarSuccess"
                />
              </div>
            </a-col>
            
            <a-col :span="16">
              <a-form
                :model="formData"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item :label="$t('profile.fields.account')">
                  <a-input v-model:value="formData.account" disabled />
                </a-form-item>
                
                <a-form-item :label="$t('profile.fields.username')">
                  <a-input v-model:value="formData.username" />
                </a-form-item>
                
                <a-form-item :label="$t('profile.fields.email')">
                  <a-input v-model:value="formData.email" />
                </a-form-item>
                
                <a-form-item :label="$t('profile.fields.phone')">
                  <a-input v-model:value="formData.phone" />
                </a-form-item>
                
                <a-form-item :label="$t('profile.fields.gender')">
                  <a-radio-group v-model:value="formData.gender">
                    <a-radio :value="1">{{ $t('profile.gender.male') }}</a-radio>
                    <a-radio :value="2">{{ $t('profile.gender.female') }}</a-radio>
                    <a-radio :value="0">{{ $t('profile.gender.unknown') }}</a-radio>
                  </a-radio-group>
                </a-form-item>
                
                <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                  <a-space>
                    <a-button type="primary" @click="handleSaveBasic">
                      {{ $t('profile.actions.save') }}
                    </a-button>
                    <a-button @click="loadUserInfo">
                      {{ $t('profile.actions.reset') }}
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-col>
          </a-row>
        </a-tab-pane>
        
        <!-- 组织信息 -->
        <a-tab-pane key="org" :tab="$t('profile.tabs.org')">
          <a-descriptions bordered :column="2">
            <a-descriptions-item :label="$t('profile.fields.department')">
              {{ formData.departmentName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('profile.fields.position')">
              {{ formData.positionName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('profile.fields.entryDate')">
              {{ formData.entryDate || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('profile.fields.status')">
              <a-tag v-if="formData.status === true" color="success">{{ $t('common.enabled') }}</a-tag>
              <a-tag v-else color="error">{{ $t('common.disabled') }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
        
        <!-- 安全设置 -->
        <a-tab-pane key="security" :tab="$t('profile.tabs.security')">
          <a-form
            :model="passwordForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 12 }"
            @finish="handleChangePassword"
          >
            <a-form-item
              :label="$t('profile.fields.oldPassword')"
              name="oldPassword"
              :rules="[{ required: true, message: $t('profile.validation.oldPasswordRequired') }]"
            >
              <a-input-password v-model:value="passwordForm.oldPassword" />
            </a-form-item>
            
            <a-form-item
              :label="$t('profile.fields.newPassword')"
              name="newPassword"
              :rules="[
                { required: true, message: $t('profile.validation.newPasswordRequired') },
                { min: 6, message: $t('profile.validation.newPasswordMin') }
              ]"
            >
              <a-input-password v-model:value="passwordForm.newPassword" />
            </a-form-item>
            
            <a-form-item
              :label="$t('profile.fields.confirmPassword')"
              name="confirmPassword"
              :rules="[
                { required: true, message: $t('profile.validation.confirmPasswordRequired') },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password v-model:value="passwordForm.confirmPassword" />
            </a-form-item>
            
            <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
              <a-button type="primary" html-type="submit">
                {{ $t('profile.actions.changePassword') }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="personalHomepage" :tab="$t('profile.tabs.personalHomepage')">
          <PersonalHomepageDesigner
            mode="current"
            :title="$t('profile.personalHomepage.title')"
            :description="$t('profile.personalHomepage.description')"
            :initial-edit-mode="true"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import PersonalHomepageDesigner from '@/components/personal-homepage/PersonalHomepageDesigner.vue'
import { getCurrentUserInfo, updateBasicInfo, changePassword } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('basic')

const formData = reactive({
  id: '',
  account: '',
  username: '',
  email: '',
  phone: '',
  gender: 1,
  avatar: '',
  departmentName: '',
  positionName: '',
  entryDate: '',
  status: true,
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/**
 * 加载用户信息
 */
async function loadUserInfo() {
  loading.value = true
  const data = await getCurrentUserInfo()
  Object.assign(formData, data)
  loading.value = false
}

/**
 * 保存基本信息
 */
async function handleSaveBasic() {
  loading.value = true
  try {
    await updateBasicInfo({
      id: formData.id,
      username: formData.username,
      email: formData.email,
      phone: formData.phone,
      gender: formData.gender,
      avatar: formData.avatar,
    })
    
    // 更新全局状态
    userStore.updateUserInfo({
      username: formData.username,
      email: formData.email,
      phone: formData.phone,
      avatar: formData.avatar,
    })
    
    loadUserInfo()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 头像上传成功
 */
function handleAvatarSuccess(url: string) {
  formData.avatar = url
}

/**
 * 修改密码
 */
async function handleChangePassword() {
  await changePassword({
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
  })
  
  // 清空表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  
  // 退出登录
  userStore.clearUserInfo()
  router.push('/login')
}

/**
 * 验证确认密码
 */
function validateConfirmPassword(_rule: any, value: string) {
  if (value !== passwordForm.newPassword) {
    return Promise.reject(t('profile.validation.passwordMismatch'))
  }
  return Promise.resolve()
}

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab as string
  }
  loadUserInfo()
})
</script>

<style scoped lang="less">
.profile-page {
  padding: 16px;
  
  .avatar-section {
    display: flex;
    justify-content: center;
    padding: 32px 0;
  }
}
</style>
