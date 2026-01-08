<template>
  <div class="login-wrap">
    <video
      class="bg-video"
      autoplay
      muted
      loop
      playsinline
      src="/@fs/D:/product/test/Forgex/Forgex_MOM/public/jws.mp4"
    ></video>
    <div class="mask"></div>
    <div class="grid"></div>
    <div class="content">
      <div class="brand" v-show="!tenantOpen">
        <span class="brand-blue">FORGEX</span><span class="brand-red">_MOM</span>
        <div class="brand-line"></div>
      </div>
      <div class="brand-sub" v-show="!tenantOpen">欢迎来到FORGEX_MOM！</div>
      <div class="glass-card" v-show="!tenantOpen">
        <form class="cyber-form" @submit.prevent="onPreLogin">
          <div class="field">
            <label class="cyber-label">账号</label>
            <input
              class="cyber-input"
              type="text"
              v-model="account"
              placeholder="请输入账号"
            />
          </div>
          <div class="field">
            <label class="cyber-label">密码</label>
            <input
              class="cyber-input"
              type="password"
              v-model="password"
              placeholder="请输入密码"
            />
          </div>
          <div class="field" v-if="mode === 'image'">
            <label class="cyber-label">验证码</label>
            <div class="captcha-row">
              <input
                class="cyber-input captcha-input"
                type="text"
                v-model="captcha"
                placeholder="请输入验证码"
              />
              <img class="captcha-img" :src="imageBase64" @click="loadImage" />
            </div>
          </div>
          <div class="field" v-if="mode === 'slider'">
            <label class="cyber-label">行为验证码</label>
            <div>
              <a-button size="small" @click="openSlider">启动滑块验证</a-button>
              <span style="margin-left: 8px; color: #9ca3af;"
                >完成后自动填充验证码</span
              >
            </div>
          </div>
          <div class="form-tools">
            <label class="remember">
              <input type="checkbox" v-model="remember" />
              <span>记住我</span>
            </label>
            <a class="forgot" href="#">忘记密码?</a>
          </div>
          <button
            type="submit"
            class="btn-gradient block-btn"
            :disabled="logging"
            :class="{ 'btn-disabled': logging }"
          >
            <span>身份校验</span>
            <span v-if="logging" class="spinner"></span>
          </button>
          <div class="divider"><span>更多登录方式</span></div>
          <div class="oauth-row">
            <button type="button" class="oauth-btn gitee" title="Gitee">
              <img src="/tubiao/GITEE.svg" alt="Gitee" />
            </button>
            <button type="button" class="oauth-btn wechat" title="微信">
              <img src="/tubiao/weixin2.svg" alt="微信" />
            </button>
            <button type="button" class="oauth-btn dingtalk" title="钉钉">
              <img src="/tubiao/dingding.svg" alt="钉钉" />
            </button>
          </div>
        </form>
      </div>
      <div class="copyright">© 2025 FORGEX_MOM</div>
    </div>
    <div v-if="tenantOpen" class="identity-overlay">
      <div class="identity-global-tools">
        <a-button size="small" type="default" @click="toggleSort">设置排序</a-button>
      </div>
      <div class="identity-container">
        <div
          class="tenant-grid identity-grid"
          :class="{ single: tenants.length === 1 }"
        >
          <div
            v-for="(t, idx) in tenants"
            :key="t.id"
            class="tenant-card"
            :class="{ selected: t.id === chosenTenant, big: tenants.length === 1 }"
            @click="choose(t)"
          >
            <div class="tenant-logo">
              <img v-if="t.logo" :src="t.logo" alt="logo" />
              <div v-else class="logo-fallback">
                {{ t.name?.[0] || 'T' }}
              </div>
            </div>
            <div class="tenant-info">
              <div class="tenant-name">{{ t.name }}</div>
              <div class="tenant-intro">{{ t.intro || '暂无简介' }}</div>
            </div>
            <div class="tenant-tools" v-if="showSort" @click.stop>
              <a-button size="small" @click="moveUp(idx)" style="margin-right: 6px;"
                >上移</a-button
              >
              <a-button size="small" @click="moveDown(idx)" style="margin-right: 6px;"
                >下移</a-button
              >
              <a-button
                size="small"
                type="default"
                :class="{ star: t.isDefault === 1 }"
                @click="toggleDefault(t)"
                >默认</a-button
              >
            </div>
          </div>
        </div>
        <div class="tenant-actions identity-actions">
          <a-button
            v-if="showSort"
            type="default"
            @click="savePreferences"
            class="action-btn"
            >保存排序</a-button
          >
          <a-button
            type="primary"
            @click="confirmTenant"
            class="action-btn primary"
            >选择你的身份</a-button
          >
        </div>
      </div>
    </div>
    <a-modal
      v-model:open="sliderOpen"
      title="滑块验证"
      :footer="null"
      width="600"
      @afterOpen="initSlider"
    >
      <div ref="sliderBox" style="height: 420px;"></div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  login,
  chooseTenant,
  getPublicKey,
  updateTenantPreferences
} from '../../../api/auth/login'
import { captchaImage, captchaSlider, captchaSliderValidate } from '../../../api/auth/captcha'
import { getRoutes } from '../../../api/sys/route'
import router, { injectDynamicRoutes } from '../../../router'
import { getLoginCaptcha } from '../../../api/sys/config'
import { reloadTenantIgnore } from '../../../api/sys/tenant'
import { getInitStatus } from '../../../api/sys/init'
import { sm2 } from 'sm-crypto'

const account = ref('admin')
const password = ref('password')
const remember = ref(false)
const captcha = ref('')
const captchaId = ref('')
const imageBase64 = ref('')
const mode = ref<'none' | 'image' | 'slider'>('none')
const tenants = ref<{ id: string; name: string; logo?: string; intro?: string; isDefault?: number }[]>([])
const tenantOpen = ref(false)
const chosenTenant = ref<string | null>(null)
const sliderOpen = ref(false)
const sliderBox = ref<HTMLDivElement | null>(null)
const logging = ref(false)
const publicKeyCache = ref<string>('')
const showSort = ref(false)

async function loadMode() {
  try {
    const cfg = await getLoginCaptcha()
    mode.value = cfg && cfg.mode ? cfg.mode : 'none'
    if (mode.value === 'image') {
      await loadImage()
    }
  } catch (_) {}
}

async function loadImage() {
  try {
    const img = await captchaImage()
    captchaId.value = img && img.captchaId ? img.captchaId : ''
    imageBase64.value = img && img.imageBase64 ? 'data:image/png;base64,' + img.imageBase64 : ''
  } catch (_) {}
}

async function onPreLogin() {
  try {
    if (logging.value) {
      return
    }
    logging.value = true
    tenantOpen.value = false
    tenants.value = []
    chosenTenant.value = null
    let pwdToSend = password.value
    try {
      if (!publicKeyCache.value) {
        publicKeyCache.value = await getPublicKey()
      }
      if (publicKeyCache.value) {
        const cipherHex = sm2.doEncrypt(password.value, publicKeyCache.value, 1)
        pwdToSend = cipherHex
      }
    } catch (e) {}
    const res = await login({
      account: account.value,
      password: pwdToSend,
      captcha: captcha.value,
      captchaId: captchaId.value
    })
    tenants.value = Array.isArray(res) ? res : []
    if (tenants.value.length > 0) {
      tenantOpen.value = true
    } else {
      message.error('当前账号未绑定任何租户，请联系管理员')
    }
  } catch (e) {
  } finally {
    logging.value = false
  }
}

async function confirmTenant() {
  if (!chosenTenant.value) {
    message.warning('请先选择一个租户')
    return
  }
  const current = tenants.value.find(t => t.id === chosenTenant.value)
  if (!current) {
    message.error('当前选择的租户无效，请重新登录后重试')
    tenantOpen.value = false
    return
  }
  try {
    const result = await chooseTenant({ 
      tenantId: chosenTenant.value,
      account: account.value
    })
    // 后端返回 true 表示成功，token 通过 cookie 传递
    if (result === true) {
      sessionStorage.setItem('account', account.value)
      sessionStorage.setItem('tenantId', chosenTenant.value)
      // 不需要手动设置 token，Sa-Token 会通过 cookie 自动管理
      
      // 获取路由时需要传递 account 和 tenantId
      const routesRes = await getRoutes({
        account: account.value,
        tenantId: chosenTenant.value
      })
      
      console.log('[Login] Routes response:', routesRes)
      
      await injectDynamicRoutes(router, routesRes)
      
      console.log('[Login] Dynamic routes injected, count:', dynamicRoutes.value.length)
      
      // 确保路由注入完成后再跳转
      await router.isReady()
      
      tenantOpen.value = false
      if (remember.value) {
        localStorage.setItem('fx-remember-account', account.value)
      } else {
        localStorage.removeItem('fx-remember-account')
      }
      
      // 使用 nextTick 确保 DOM 更新完成
      await nextTick()
      
      console.log('[Login] About to navigate to /workspace')
      router.replace('/workspace')
    } else {
      message.error('选择租户失败，请稍后重试')
    }
  } catch (e: any) {
    // http拦截器已经显示了错误，这里不再重复显示
    console.error('选择租户失败:', e)
  }
}

async function openSlider() {
  sliderOpen.value = true
}

async function initSlider() {
  if (!sliderBox.value) return
  try {
    const data = await captchaSlider()
    console.log('init slider', data)
  } catch (e) {}
}

function choose(t: { id: string }) {
  chosenTenant.value = t.id
}

function toggleSort() {
  showSort.value = !showSort.value
}

function moveUp(idx: number) {
  if (idx <= 0) return
  const arr = tenants.value.slice()
  const tmp = arr[idx - 1]
  arr[idx - 1] = arr[idx]
  arr[idx] = tmp
  tenants.value = arr
}

function moveDown(idx: number) {
  const arr = tenants.value.slice()
  if (idx >= arr.length - 1) return
  const tmp = arr[idx + 1]
  arr[idx + 1] = arr[idx]
  arr[idx] = tmp
  tenants.value = arr
}

async function toggleDefault(t: any) {
  try {
    const newVal = t.isDefault === 1 ? 0 : 1
    await updateTenantPreferences({
      account: account.value,
      ordered: tenants.value.map(t => t.id),
      defaultTenantId: newVal === 1 ? t.id : undefined
    })
    await reloadTenantIgnore()
    t.isDefault = newVal
    message.success('默认租户设置已更新')
  } catch (e) {
    message.error('更新默认租户失败')
  }
}

async function savePreferences() {
  try {
    await updateTenantPreferences({
      account: account.value,
      ordered: tenants.value.map(t => t.id)
    })
    await reloadTenantIgnore()
    message.success('排序已保存')
  } catch (e) {
    message.error('保存排序失败')
  }
}

onMounted(async () => {
  try {
    const remembered = localStorage.getItem('fx-remember-account')
    if (remembered) {
      account.value = remembered
      remember.value = true
    }
  } catch (_) {}
  await loadMode()
})

watch(sliderOpen, async open => {
  if (open) {
    await nextTick()
    await initSlider()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&display=swap');
.login-wrap {
  position: relative;
  height: 100%;
}
.bg-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
}
.grid {
  position: absolute;
  inset: 0;
  opacity: 0.28;
  background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxwYXR0ZXJuIGlkPSJwYXR0ZXJuX2JhY2tncm91bmQiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHBhdHRlcm5UcmFuc2Zvcm09InJvdGF0ZSgxMCkiIHBhdHRlcm5zY2FwZT0iMTAsMTAiI...');
}
.content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
}
.brand {
  font-family: 'Orbitron', sans-serif;
  font-weight: 700;
  color: #fff;
  margin-bottom: 24px;
  position: relative;
  animation: glitch 2s infinite;
  text-align: center;
}
.brand-blue {
  color: #05d9e8;
  text-shadow: 0 0 6px #05d9e8, 0 0 12px #05d9e8;
  font-size: clamp(24px, 5vw, 42px);
}
.brand-red {
  color: #ff2a6d;
  text-shadow: 0 0 6px #ff2a6d, 0 0 12px #ff2a6d;
  font-size: clamp(24px, 5vw, 42px);
  margin-left: 4px;
}
.brand-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: -6px;
  height: 2px;
  background: linear-gradient(90deg, #d300c5, #05d9e8);
  opacity: 0.7;
}
.brand-sub {
  color: #9ca3af;
  margin-top: 6px;
  margin-bottom: 18px;
  font-size: 14px;
}
.glass-card {
  width: 380px;
  padding: 24px;
  background: transparent;
  border-radius: 10px;
  border: 1px solid rgba(5, 217, 232, 0.45);
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.25),
    0 0 24px rgba(211, 0, 197, 0.12);
}
.cyber-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cyber-label {
  color: #cfd8dc;
  font-size: 14px;
}
.cyber-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  background: transparent;
  color: #e5e7eb;
  outline: none;
}
.cyber-input::placeholder {
  color: #94a3b8;
}
.cyber-input:hover {
  border-color: #05d9e8;
}
.cyber-input:focus {
  border-color: #05d9e8;
  box-shadow: 0 0 8px rgba(5, 217, 232, 0.45);
}
.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.captcha-input {
  flex: 1;
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  cursor: pointer;
  object-fit: cover;
  transition: border-color 0.2s ease;
}
.captcha-img:hover {
  border-color: #05d9e8;
}
.form-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #cfd8dc;
}
.remember {
  display: flex;
  align-items: center;
  gap: 8px;
}
.remember input {
  width: 16px;
  height: 16px;
  accent-color: #05d9e8;
}
.forgot {
  color: #05d9e8;
  text-decoration: none;
}
.forgot:hover {
  color: #d300c5;
}
.block-btn {
  width: 100%;
  margin-top: 12px;
  padding: 10px 0;
  border: none;
  border-radius: 999px;
  background: linear-gradient(90deg, #05d9e8, #d300c5);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.divider {
  margin: 16px 0 8px;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}
.oauth-row {
  display: flex;
  justify-content: center;
  gap: 16px;
}
.oauth-btn {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: rgba(15, 23, 42, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.oauth-btn img {
  width: 22px;
  height: 22px;
}
.copyright {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  text-align: center;
  color: #9ca3af;
  font-size: 12px;
}
.identity-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 2, 33, 0.7), rgba(13, 2, 33, 0.9));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 10;
}
.identity-container {
  max-width: 960px;
  width: 100%;
  position: relative;
}
.tenant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 200px));
  gap: 16px;
  justify-content: center;
}
.tenant-grid.single {
  max-width: 200px;
  margin: 0 auto;
}
.tenant-card {
  position: relative;
  padding: 24px 18px;
  border-radius: 16px;
  background: transparent;
  border: 1px solid rgba(5, 217, 232, 0.45);
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.25),
    0 0 24px rgba(211, 0, 197, 0.12);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.tenant-card.big {
  transform: scale(1.06);
}
.tenant-card:hover {
  transform: scale(1.02);
  border-color: #05d9e8;
}
.tenant-card.selected {
  border-color: #05d9e8;
  box-shadow:
    0 0 16px rgba(5, 217, 232, 0.45),
    0 0 24px rgba(211, 0, 197, 0.25);
  transform: scale(1.02);
}
.tenant-logo {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 999px;
  background: rgba(5, 217, 232, 0.1);
  border: 2px solid rgba(5, 217, 232, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.tenant-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-fallback {
  color: #e5e7eb;
  font-size: 24px;
  font-weight: 700;
}
.tenant-info {
  flex: 1;
  text-align: center;
  width: 100%;
}
.tenant-name {
  color: #e5e7eb;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}
.tenant-intro {
  color: #9ca3af;
  font-size: 13px;
  line-height: 1.4;
}
.tenant-tools {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  z-index: 10;
  position: relative;
}
.identity-actions {
  margin-top: 32px;
  text-align: center;
  z-index: 10;
  position: relative;
}
.action-btn {
  min-width: 140px;
  height: 40px;
  border-radius: 999px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(5, 217, 232, 0.3);
}
.action-btn.primary {
  margin-left: 12px;
  background: linear-gradient(90deg, #05d9e8, #d300c5);
  border: none;
  color: #fff;
}
.action-btn.primary:hover {
  box-shadow: 0 4px 16px rgba(5, 217, 232, 0.5);
}
.identity-global-tools {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 20;
}
.spinner {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid rgba(15, 23, 42, 0.2);
  border-top-color: #05d9e8;
  animation: spin 0.8s linear infinite;
}
.star {
  color: #fbbf24;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

