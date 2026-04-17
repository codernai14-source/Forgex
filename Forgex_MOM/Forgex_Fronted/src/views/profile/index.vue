<template>
  <div class="profile-page">
    <a-card :title="$t('profile.title')" :loading="loading">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 当前用户可直接编辑的基础资料区域。 -->
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
        
        <!-- 接口返回的组织信息，只读展示，不允许在此页直接修改。 -->
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
        
        <!-- 安全设置区域，当前主要用于修改登录密码。 -->
        <a-tab-pane key="security" :tab="$t('profile.tabs.security')">
          <a-form
            :model="password表单"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 12 }"
            @finish="handleChangePassword"
          >
            <a-form-item
              :label="$t('profile.fields.oldPassword')"
              name="oldPassword"
              :rules="[{ required: true, message: $t('profile.validation.oldPasswordRequired') }]"
            >
              <a-input-password v-model:value="password表单.oldPassword" />
            </a-form-item>
            
            <a-form-item
              :label="$t('profile.fields.newPassword')"
              name="newPassword"
              :rules="[
                { required: true, message: $t('profile.validation.newPasswordRequired') },
                { min: 6, message: $t('profile.validation.newPasswordMin') }
              ]"
            >
              <a-input-password v-model:value="password表单.newPassword" />
            </a-form-item>
            
            <a-form-item
              :label="$t('profile.fields.confirmPassword')"
              name="confirmPassword"
              :rules="[
                { required: true, message: $t('profile.validation.confirmPasswordRequired') },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password v-model:value="password表单.confirmPassword" />
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
        <a-tab-pane key="guide" :tab="$t('profile.tabs.guide')">
          <div class="guide-setting-panel">
            <a-alert
              :message="$t('profile.guide.title')"
              :description="$t('profile.guide.description')"
              type="info"
              show-icon
              style="margin-bottom: 16px"
            />
            <a-card size="small">
              <a-space direction="vertical" style="width: 100%" :size="16">
                <div class="guide-setting-row">
                  <div>
                    <div class="guide-setting-title">{{ $t('profile.guide.babyModeTitle') }}</div>
                    <div class="guide-setting-desc">{{ $t('profile.guide.babyModeDesc') }}</div>
                  </div>
                  <a-switch
                    :checked="guideStore.babyModeEnabled"
                    :loading="guideStore.saving"
                    @change="handleBabyModeChange"
                  />
                </div>

                <div class="guide-setting-row guide-setting-row--action">
                  <div>
                    <div class="guide-setting-title">{{ $t('profile.guide.replayTitle') }}</div>
                    <div class="guide-setting-desc">{{ $t('profile.guide.replayDesc') }}</div>
                  </div>
                  <a-button type="primary" :loading="guideStore.saving" @click="handleReplaySystemGuide">
                    {{ $t('profile.guide.replayAction') }}
                  </a-button>
                </div>
              </a-space>
            </a-card>
          </div>
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
import { useGuideStore } from '@/stores/guide'
import { useUserStore } from '@/stores/user'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const guideStore = useGuideStore()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('basic')

/**
 * 当前用户的可编辑资料表单。
 */
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

/**
 * 与基础资料分离的密码表单状态。
 */
const password表单 = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/**
 * 加载最新的用户资料，并回填到页面表单。
 */
async function loadUserInfo() {
  loading.value = true
  const data = await getCurrentUserInfo()
  Object.assign(formData, data)
  loading.value = false
}

/**
 * 保存可编辑的基础资料字段。
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
    
    // 同步更新全局用户信息，确保页头等区域立即显示最新资料。
    userStore.updateUserInfo({
      username: formData.username,
      email: formData.email,
      phone: formData.phone,
      avatar: formData.avatar,
    })
    
    loadUserInfo()
  } catch (error) {
    console.error('淇濆瓨澶辫触:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 头像上传成功后，将返回地址同步到本地表单。
 */
function handleAvatarSuccess(url: string) {
  formData.avatar = url
}

/**
 * 提交密码修改，并在成功后跳回登录页重新认证。
 */
async function handleChangePassword() {
  await changePassword({
    oldPassword: password表单.oldPassword,
    newPassword: password表单.newPassword,
  })
  
  // 修改成功后清空敏感字段，避免继续停留在页面中。
  password表单.oldPassword = ''
  password表单.newPassword = ''
  password表单.confirmPassword = ''
  
  // 主动清理登录态，确保用户使用新密码重新登录。
  userStore.clearUserInfo()
  router.push('/login')
}

async function handleBabyModeChange(checked: boolean) {
  await guideStore.setBabyModeEnabled(checked)
  if (checked) {
    await guideStore.resetGuideState('system.main', 'v1')
  }
}

async function handleReplaySystemGuide() {
  await guideStore.resetGuideState('system.main', 'v1')
  router.push('/workspace/home')
}

/**
 * 校验确认密码是否与新密码保持一致。
 */
function validateConfirmPassword(_rule: any, value: string) {
  if (value !== password表单.newPassword) {
    return Promise.reject(t('profile.validation.passwordMismatch'))
  }
  return Promise.resolve()
}

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab as string
  }
  guideStore.loadPreference().catch(() => {})
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

.guide-setting-panel {
  max-width: 860px;
}

.guide-setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.guide-setting-row--action {
  align-items: flex-start;
}

.guide-setting-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--fx-text-primary, #1f2937);
}

.guide-setting-desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);
  line-height: 1.6;
}
</style>
