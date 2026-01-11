<template>
  <div class="profile-page">
    <a-card title="个人信息" :loading="loading">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基本信息 -->
        <a-tab-pane key="basic" tab="基本信息">
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
                <a-form-item label="账号">
                  <a-input v-model:value="formData.account" disabled />
                </a-form-item>
                
                <a-form-item label="用户名">
                  <a-input v-model:value="formData.username" />
                </a-form-item>
                
                <a-form-item label="邮箱">
                  <a-input v-model:value="formData.email" />
                </a-form-item>
                
                <a-form-item label="手机号">
                  <a-input v-model:value="formData.phone" />
                </a-form-item>
                
                <a-form-item label="性别">
                  <a-radio-group v-model:value="formData.gender">
                    <a-radio :value="1">男</a-radio>
                    <a-radio :value="2">女</a-radio>
                    <a-radio :value="0">未知</a-radio>
                  </a-radio-group>
                </a-form-item>
                
                <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                  <a-space>
                    <a-button type="primary" @click="handleSaveBasic">
                      保存
                    </a-button>
                    <a-button @click="loadUserInfo">
                      重置
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-col>
          </a-row>
        </a-tab-pane>
        
        <!-- 组织信息 -->
        <a-tab-pane key="org" tab="组织信息">
          <a-descriptions bordered :column="2">
            <a-descriptions-item label="所属部门">
              {{ formData.departmentName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="职位">
              {{ formData.positionName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="入职时间">
              {{ formData.entryDate || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag v-if="formData.status === true" color="success">启用</a-tag>
              <a-tag v-else color="error">禁用</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-tab-pane>
        
        <!-- 安全设置 -->
        <a-tab-pane key="security" tab="安全设置">
          <a-form
            :model="passwordForm"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 12 }"
            @finish="handleChangePassword"
          >
            <a-form-item
              label="旧密码"
              name="oldPassword"
              :rules="[{ required: true, message: '请输入旧密码' }]"
            >
              <a-input-password v-model:value="passwordForm.oldPassword" />
            </a-form-item>
            
            <a-form-item
              label="新密码"
              name="newPassword"
              :rules="[
                { required: true, message: '请输入新密码' },
                { min: 6, message: '密码长度不能少于6位' }
              ]"
            >
              <a-input-password v-model:value="passwordForm.newPassword" />
            </a-form-item>
            
            <a-form-item
              label="确认密码"
              name="confirmPassword"
              :rules="[
                { required: true, message: '请确认新密码' },
                { validator: validateConfirmPassword }
              ]"
            >
              <a-input-password v-model:value="passwordForm.confirmPassword" />
            </a-form-item>
            
            <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
              <a-button type="primary" html-type="submit">
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import AvatarUpload from '@/components/AvatarUpload.vue'
import { getCurrentUserInfo, updateBasicInfo, changePassword } from '@/api/profile'
import { useUserStore } from '@/stores/user'

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
  try {
    const data = await getCurrentUserInfo()
    Object.assign(formData, data)
  } catch (error) {
    console.error('加载用户信息失败:', error)
    message.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
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
    
    message.success('保存成功')
    loadUserInfo()
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
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
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    message.success('密码修改成功，请重新登录')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    // 退出登录
    userStore.clearUserInfo()
    router.push('/login')
  } catch (error) {
    console.error('密码修改失败:', error)
    message.error('密码修改失败')
  }
}

/**
 * 验证确认密码
 */
function validateConfirmPassword(_rule: any, value: string) {
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
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
