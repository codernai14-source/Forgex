<!--
  - Copyright 2026 coder_nai@163.com
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="register-wrap">
    <div class="register-mask"></div>
    <div class="register-card">
      <div class="register-brand">{{ systemConfig.systemName }}</div>

      <!-- 注册成功提示 -->
      <template v-if="registerSuccess">
        <h1 class="register-title">🎉 注册成功</h1>
        <p class="register-desc">您的账号已创建成功，请前往登录页面登录系统。</p>
        <a-button type="primary" size="large" @click="goLogin">
          前往登录
        </a-button>
      </template>

      <!-- 注册表单 -->
      <template v-else>
        <h1 class="register-title">邀请码注册</h1>
        <p class="register-desc">请填写以下信息完成注册</p>

        <a-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          layout="vertical"
          class="register-form"
        >
          <a-form-item label="邀请码" name="inviteCode">
            <div style="display: flex; gap: 8px;">
              <a-input
                v-model:value="formData.inviteCode"
                placeholder="请输入邀请码"
                style="flex: 1;"
              />
              <a-button @click="handleCheckInvite" :loading="checkingInvite">
                校验
              </a-button>
            </div>
            <div v-if="inviteValid === true" class="invite-check-ok">✅ 邀请码有效</div>
            <div v-if="inviteValid === false" class="invite-check-fail">❌ {{ inviteError }}</div>
          </a-form-item>

          <a-form-item label="账号" name="account">
            <a-input
              v-model:value="formData.account"
              placeholder="请输入账号"
            />
          </a-form-item>

          <a-form-item label="用户名" name="username">
            <a-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
            />
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="formData.password"
              placeholder="请输入密码"
            />
          </a-form-item>

          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password
              v-model:value="formData.confirmPassword"
              placeholder="请再次输入密码"
            />
          </a-form-item>

          <a-form-item label="手机号" name="phone">
            <a-input
              v-model:value="formData.phone"
              placeholder="请输入手机号（可选）"
            />
          </a-form-item>

          <a-form-item label="邮箱" name="email">
            <a-input
              v-model:value="formData.email"
              placeholder="请输入邮箱（可选）"
            />
          </a-form-item>

          <!-- 验证码 -->
          <a-form-item label="验证码" name="captcha" v-if="captchaMode === 'image'">
            <div style="display: flex; gap: 8px;">
              <a-input
                v-model:value="formData.captcha"
                placeholder="请输入验证码"
                style="flex: 1;"
              />
              <img
                v-if="captchaImage"
                :src="captchaImage"
                class="captcha-img"
                @click="loadCaptcha"
                title="点击刷新验证码"
              />
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              block
              size="large"
              :loading="submitting"
              @click="handleRegister"
            >
              注册
            </a-button>
          </a-form-item>

          <div class="register-footer">
            <span>已有账号？</span>
            <a @click="goLogin">返回登录</a>
          </div>
        </a-form>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import router from '@/router'
import { createDefaultSystemBasicConfig, getSystemBasicConfig, type SystemBasicConfig } from '@/api/system/config'
import { register, checkInviteCode, getPublicKey } from '@/api/auth/login'
import { captchaImage as getImageCaptcha } from '@/api/auth/captcha'
import { sm2 } from 'sm-crypto'

const { t: i18nT } = useI18n({ useScope: 'global' })
const systemConfig = ref<SystemBasicConfig>(createDefaultSystemBasicConfig())

const formRef = ref()
const submitting = ref(false)
const registerSuccess = ref(false)

const captchaMode = ref('image')
const captchaImage = ref('')
const captchaId = ref('')
const publicKeyCache = ref('')

const checkingInvite = ref(false)
const inviteValid = ref<boolean | null>(null)
const inviteError = ref('')

const formData = reactive({
  inviteCode: '',
  account: '',
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  captcha: ''
})

const rules = {
  inviteCode: [{ required: true, message: '请输入邀请码', trigger: 'blur' }],
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 32, message: '账号长度3-32位', trigger: 'blur' }
  ],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (value !== formData.password) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

async function goLogin() {
  await router.push('/login')
}

async function loadCaptcha() {
  try {
    const res = await getImageCaptcha()
    if (res) {
      captchaId.value = res.captchaId || ''
      captchaImage.value = res.imageBase64 ? `data:image/png;base64,${res.imageBase64}` : ''
    }
  } catch (_) {}
}

async function handleCheckInvite() {
  if (!formData.inviteCode) {
    message.warning('请输入邀请码')
    return
  }
  checkingInvite.value = true
  inviteValid.value = null
  inviteError.value = ''
  try {
    await checkInviteCode(formData.inviteCode)
    inviteValid.value = true
  } catch (e: any) {
    inviteValid.value = false
    inviteError.value = e?.message || '邀请码无效'
  } finally {
    checkingInvite.value = false
  }
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    // SM2 加密密码
    let pwdToSend = formData.password
    try {
      if (!publicKeyCache.value) {
        publicKeyCache.value = await getPublicKey() as string
      }
      if (publicKeyCache.value) {
        const cipherHex = sm2.doEncrypt(formData.password, publicKeyCache.value, 1)
        pwdToSend = cipherHex
      }
    } catch (_) {}

    await register({
      account: formData.account,
      username: formData.username,
      password: pwdToSend,
      phone: formData.phone || undefined,
      email: formData.email || undefined,
      inviteCode: formData.inviteCode,
      captcha: formData.captcha || undefined,
      captchaId: captchaId.value || undefined
    })

    registerSuccess.value = true
  } catch (e: any) {
    if (e.errorFields) return
    // 刷新验证码
    await loadCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const config = await getSystemBasicConfig()
    if (config) {
      systemConfig.value = {
        ...createDefaultSystemBasicConfig(),
        ...config,
      }
    }
  } catch (_) {}

  await loadCaptcha()

  try {
    publicKeyCache.value = await getPublicKey() as string
  } catch (_) {}
})
</script>

<style scoped>
.register-wrap {
  position: relative;
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at top, rgba(5, 217, 232, 0.18), transparent 40%), #0d0221;
}

.register-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.78), rgba(13, 2, 33, 0.92));
}

.register-card {
  position: relative;
  z-index: 1;
  width: min(520px, 100%);
  max-height: 90vh;
  overflow-y: auto;
  padding: 36px 32px;
  border-radius: 18px;
  border: 1px solid rgba(5, 217, 232, 0.35);
  background: rgba(15, 23, 42, 0.68);
  box-shadow: 0 0 24px rgba(5, 217, 232, 0.18);
  text-align: center;
  color: #e5e7eb;
}

.register-brand {
  font-size: 14px;
  letter-spacing: 0.14em;
  color: #67e8f9;
  margin-bottom: 12px;
}

.register-title {
  margin: 0;
  font-size: 26px;
  color: #fff;
}

.register-desc {
  margin: 12px 0 24px;
  line-height: 1.6;
  color: #cbd5e1;
  font-size: 14px;
}

.register-form {
  text-align: left;
}

.register-form :deep(.ant-form-item-label > label) {
  color: #cbd5e1;
}

.register-form :deep(.ant-input),
.register-form :deep(.ant-input-password input) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(5, 217, 232, 0.25);
  color: #e5e7eb;
}

.register-form :deep(.ant-input:focus),
.register-form :deep(.ant-input-password input:focus) {
  border-color: #05d9e8;
  box-shadow: 0 0 0 2px rgba(5, 217, 232, 0.15);
}

.register-form :deep(.ant-input::placeholder),
.register-form :deep(.ant-input-password input::placeholder) {
  color: rgba(203, 213, 225, 0.5);
}

.register-footer {
  text-align: center;
  margin-top: 8px;
  color: #94a3b8;
}

.register-footer a {
  color: #67e8f9;
  cursor: pointer;
  margin-left: 4px;
}

.register-footer a:hover {
  text-decoration: underline;
}

.captcha-img {
  height: 38px;
  cursor: pointer;
  border-radius: 4px;
}

.invite-check-ok {
  margin-top: 4px;
  font-size: 12px;
  color: #52c41a;
}

.invite-check-fail {
  margin-top: 4px;
  font-size: 12px;
  color: #ff4d4f;
}
</style>
