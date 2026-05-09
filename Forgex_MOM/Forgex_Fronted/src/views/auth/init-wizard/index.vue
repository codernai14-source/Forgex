<template>
  <div class="init-wrap">
    <div class="carousel">
      <div class="slides" :style="{ transform: `translateY(${-step * 100}vh)` }">
        <section class="slide">
          <div class="slide-head">{{ t('auth.initWizard.securityTitle') }}</div>
          <div class="slide-hint">{{ t('auth.initWizard.securityHint') }}</div>
          <div class="form-grid">
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.passwordStrength') }}</div>
              <div class="item-hint">{{ strengthHint }}</div>
              <div class="radio-row">
                <label><input v-model="security.pwdStrength" type="radio" value="high" /> {{ t('auth.initWizard.strength.high') }}</label>
                <label><input v-model="security.pwdStrength" type="radio" value="normal" /> {{ t('auth.initWizard.strength.normal') }}</label>
                <label><input v-model="security.pwdStrength" type="radio" value="low" /> {{ t('auth.initWizard.strength.low') }}</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.initialPassword') }}</div>
              <div class="item-hint">{{ t('auth.initWizard.initialPasswordHint') }}</div>
              <input v-model="security.initialPassword" class="input" type="text" :placeholder="t('auth.initWizard.initialPasswordPlaceholder')" />
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.captcha') }}</div>
              <label class="ck"><input v-model="security.captchaEnabled" type="checkbox" /> {{ t('auth.initWizard.enableCaptcha') }}</label>
              <div class="item-hint">{{ captchaHint }}</div>
              <div v-if="security.captchaEnabled" class="radio-row">
                <label><input v-model="security.captchaMode" type="radio" value="image" /> {{ t('auth.initWizard.imageCaptcha') }}</label>
                <label><input v-model="security.captchaMode" type="radio" value="slider" /> {{ t('auth.initWizard.sliderCaptcha') }}</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.passwordStore') }}</div>
              <div class="item-hint">{{ storeHint }}</div>
              <div class="radio-row">
                <label><input v-model="security.passwordStore" type="radio" value="BCrypt" /> BCrypt</label>
                <label><input v-model="security.passwordStore" type="radio" value="Argon2" /> Argon2</label>
                <label><input v-model="security.passwordStore" type="radio" value="scrypt" /> scrypt</label>
                <label><input v-model="security.passwordStore" type="radio" value="PBKDF2" /> PBKDF2</label>
                <label><input v-model="security.passwordStore" type="radio" value="aes" /> AES-256-GCM</label>
                <label><input v-model="security.passwordStore" type="radio" value="rsa" /> RSA-2048</label>
              </div>
            </div>
          </div>
        </section>

        <section class="slide">
          <div class="slide-head">{{ t('auth.initWizard.accountTitle') }}</div>
          <div class="slide-hint">{{ t('auth.initWizard.accountHint') }}</div>
          <div class="form-grid">
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.users') }}</div>
              <div class="inline-row">
                <label class="ck"><input type="checkbox" disabled checked /> {{ t('auth.initWizard.userAdmin') }}</label>
                <label class="ck"><input v-model="form.addDev" type="checkbox" /> {{ t('auth.initWizard.userDev') }}</label>
                <label class="ck"><input v-model="form.addTest" type="checkbox" /> {{ t('auth.initWizard.userTest') }}</label>
                <label class="ck"><input v-model="form.addCustom" type="checkbox" /> {{ t('auth.initWizard.userCustom') }}</label>
              </div>
              <div class="item-hint">{{ t('auth.initWizard.customUserHint') }}</div>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.roles') }}</div>
              <div class="inline-row">
                <label class="ck"><input type="checkbox" disabled checked /> {{ t('auth.initWizard.roleAdmin') }}</label>
                <label class="ck"><input v-model="form.roleDeveloper" type="checkbox" /> {{ t('auth.initWizard.roleDeveloper') }}</label>
                <label class="ck"><input v-model="form.roleTester" type="checkbox" /> {{ t('auth.initWizard.roleTester') }}</label>
                <label class="ck"><input v-model="form.roleCustomer" type="checkbox" /> {{ t('auth.initWizard.roleCustomer') }}</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.tenants') }}</div>
              <div class="inline-row tenant-actions">
                <a-button size="small" type="default" @click="addForgex">{{ t('auth.initWizard.addForgexTenant') }}</a-button>
                <a-button size="small" type="default" @click="addCustomer">{{ t('auth.initWizard.addCustomerTenant') }}</a-button>
              </div>
              <a-table
                :columns="tenantColumns"
                :dataSource="tenantRows"
                size="small"
                :pagination="false"
                rowKey="key"
                :key="tenantTableKey"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'logo' || column.key === 'logo'">
                    <img v-if="record.logo" :src="record.logo" alt="logo" class="logo-sm" />
                    <span v-else style="color:#c3b37b">-</span>
                  </template>
                  <template v-if="column.key === 'ops'">
                    <a-button size="small" @click="openEdit(record.key)">{{ t('common.edit') }}</a-button>
                    <a-button
                      v-if="record.key !== 'default'"
                      size="small"
                      danger
                      style="margin-left:6px"
                      @click="removeTenant(record.key)"
                    >
                      {{ t('auth.initWizard.remove') }}
                    </a-button>
                  </template>
                </template>
              </a-table>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.userTenantBinding') }}</div>
              <div v-for="(b, i) in binds" :key="i" class="bind-row">
                <a-select :key="`user-${refreshKey}`" v-model:value="b.account" style="width:160px" :placeholder="t('auth.initWizard.selectUser')">
                  <a-select-option v-for="opt in userOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="`tenant-${refreshKey}`" v-model:value="b.tenants" mode="multiple" style="min-width:260px" :placeholder="t('auth.initWizard.selectTenant')">
                  <a-select-option v-for="opt in tenantOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-button size="small" @click="removeBind(i)">{{ t('common.delete') }}</a-button>
              </div>
              <a-button size="small" type="default" @click="addBind">{{ t('auth.initWizard.addBinding') }}</a-button>
            </div>
            <div class="block">
              <div class="block-title">{{ t('auth.initWizard.userRoleBinding') }}</div>
              <div v-for="(r, i) in roleBinds" :key="i" class="bind-row">
                <a-select :key="`role-user-${refreshKey}`" v-model:value="r.account" style="width:160px" :placeholder="t('auth.initWizard.selectUser')">
                  <a-select-option v-for="opt in userOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="`role-tenant-${refreshKey}`" v-model:value="r.tenant" style="width:160px" :placeholder="t('auth.initWizard.selectTenant')">
                  <a-select-option v-for="opt in tenantOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="`role-${refreshKey}`" v-model:value="r.roles" mode="multiple" style="min-width:260px" :placeholder="t('auth.initWizard.selectRole')">
                  <a-select-option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-button size="small" @click="removeRoleBind(i)">{{ t('common.delete') }}</a-button>
              </div>
              <a-button size="small" type="default" @click="addRoleBind">{{ t('auth.initWizard.addBinding') }}</a-button>
            </div>
          </div>
        </section>

        <section class="slide">
          <div class="slide-head">{{ t('auth.initWizard.menuTitle') }}</div>
          <div class="slide-hint">{{ t('auth.initWizard.menuHint') }}</div>
          <div class="footer-actions">
            <a-button type="primary" :loading="submitting" @click="submit">{{ t('auth.initWizard.submitInit') }}</a-button>
          </div>
        </section>
      </div>
      <a-modal v-model:open="modalOpen" :title="t('auth.initWizard.editTenant')" wrapClassName="dark-modal">
        <div class="grid-modal">
          <label>{{ t('auth.initWizard.name') }}</label>
          <input v-model="editModel.name" class="input" :placeholder="t('auth.initWizard.tenantNamePlaceholder')" />
          <label>{{ t('auth.initWizard.code') }}</label>
          <input v-model="editModel.code" class="input" :placeholder="t('auth.initWizard.tenantCodePlaceholder')" />
          <label>{{ t('auth.initWizard.intro') }}</label>
          <input v-model="editModel.intro" class="input" :placeholder="t('auth.initWizard.tenantIntroPlaceholder')" />
          <label>Logo</label>
          <a-upload :showUploadList="false" :beforeUpload="onLogoUpload">
            <a-button size="small">{{ t('auth.initWizard.chooseFile') }}</a-button>
          </a-upload>
          <div v-if="editModel.logo" class="logo-preview">
            <img :src="editModel.logo" class="logo-sm" alt="logo" />
          </div>
        </div>
        <template #footer>
          <div class="modal-actions">
            <a-button style="margin-right:8px" @click="modalOpen = false">{{ t('common.cancel') }}</a-button>
            <a-button type="primary" @click="saveEdit">{{ t('common.save') }}</a-button>
          </div>
        </template>
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { applyInit } from '../../../api/system/init'
import { uploadFile } from '../../../api/system/file'

type TenantKey = 'default' | 'forgex' | 'customer'

const { t, locale } = useI18n()
const submitting = ref(false)
const router = useRouter()
const step = ref(0)
const refreshKey = ref(0)

const security = reactive({
  pwdStrength: 'high',
  initialPassword: 'Aa123456',
  captchaEnabled: true,
  captchaMode: 'image',
  passwordStore: 'BCrypt',
})

const strengthHint = computed(() => t(`auth.initWizard.strengthHint.${security.pwdStrength}`))
const captchaHint = computed(() => {
  if (!security.captchaEnabled) return t('auth.initWizard.captchaDisabledHint')
  return security.captchaMode === 'slider' ? t('auth.initWizard.sliderCaptchaHint') : t('auth.initWizard.imageCaptchaHint')
})
const storeHint = computed(() => {
  const key = security.passwordStore.toLowerCase()
  if (key === 'bcrypt' || key === 'argon2' || key === 'scrypt') return t(`auth.initWizard.storeHint.${key}`)
  if (key === 'aes') return t('auth.initWizard.storeHint.aes')
  if (key === 'rsa') return t('auth.initWizard.storeHint.rsa')
  return t('auth.initWizard.storeHint.pbkdf2')
})

const form = reactive({
  addTest: false,
  addDev: false,
  addCustom: false,
  customUsername: '',
  roleTester: false,
  roleDeveloper: false,
  roleCustomer: false,
  tenantNames: [] as string[],
  defaultTenantName: 'default',
  defaultTenantCode: 'default',
  defaultTenantIntro: '',
  defaultTenantLogo: '',
  addForgexTenant: false,
  forgexTenantIntro: '',
  addCustomerTenant: false,
  customerTenantName: '',
  customerTenantCode: '',
  customerTenantIntro: '',
  customerTenantLogo: '',
  userTenantBinds: [] as Array<Record<string, unknown>>,
  userRoleBinds: [] as Array<Record<string, unknown>>,
})

const tenants = reactive({
  default: { selected: true, name: 'default', code: 'default', intro: '', logo: '' },
  forgex: { selected: false, name: 'Forgex', code: 'forgex', intro: '', logo: '' },
  customer: { selected: false, name: '', code: '', intro: '', logo: '' },
})

function resetLocalizedDefaults() {
  if (!form.defaultTenantIntro) form.defaultTenantIntro = t('auth.initWizard.defaultTenantIntro')
  if (!form.forgexTenantIntro) form.forgexTenantIntro = t('auth.initWizard.forgexTenantIntro')
  if (!tenants.default.intro) tenants.default.intro = t('auth.initWizard.defaultTenantIntro')
  if (!tenants.forgex.intro) tenants.forgex.intro = t('auth.initWizard.forgexTenantIntro')
}

resetLocalizedDefaults()

const tenantColumns = computed(() => [
  { title: t('auth.initWizard.tenantKey'), dataIndex: 'key', key: 'key' },
  { title: 'Logo', dataIndex: 'logo', key: 'logo' },
  { title: t('auth.initWizard.name'), dataIndex: 'name', key: 'name' },
  { title: t('auth.initWizard.code'), dataIndex: 'code', key: 'code' },
  { title: t('auth.initWizard.intro'), dataIndex: 'intro', key: 'intro' },
  { title: t('common.action'), key: 'ops', width: 150 },
])

const tenantRows = computed(() => {
  const rows: Array<Record<string, string>> = [
    { key: 'default', logo: tenants.default.logo, name: tenants.default.name, code: tenants.default.code, intro: tenants.default.intro },
  ]
  if (tenants.forgex.selected) rows.push({ key: 'forgex', logo: tenants.forgex.logo, name: tenants.forgex.name, code: tenants.forgex.code, intro: tenants.forgex.intro })
  if (tenants.customer.selected) rows.push({ key: 'customer', logo: tenants.customer.logo, name: tenants.customer.name || 'customer', code: tenants.customer.code, intro: tenants.customer.intro })
  return rows
})

const tenantTableKey = computed(() => `${locale.value}-${tenants.default.logo}${tenants.forgex.logo}${tenants.customer.logo}`)
const userOptions = computed(() => {
  const opts: Array<{ label: string; value: string }> = [{ label: 'admin', value: 'admin' }]
  if (form.addDev) opts.push({ label: 'dev', value: 'dev' })
  if (form.addTest) opts.push({ label: 'test', value: 'test' })
  if (form.addCustom) opts.push({ label: form.customUsername || 'custom', value: form.customUsername || 'custom' })
  return opts
})
const tenantOptions = computed(() => tenantRows.value.map((r) => ({ label: r.name, value: r.name, logo: r.logo })))
const roleOptions = computed(() => {
  const opts: Array<{ label: string; value: string }> = [{ label: 'admin', value: 'admin' }]
  if (form.roleDeveloper) opts.push({ label: 'developer', value: 'developer' })
  if (form.roleTester) opts.push({ label: 'tester', value: 'tester' })
  if (form.roleCustomer) opts.push({ label: 'customer', value: 'customer' })
  return opts
})

const modalOpen = ref(false)
const editTargetKey = ref<TenantKey | ''>('')
const editModel = reactive({ name: '', code: '', intro: '', logo: '' })

function addForgex() {
  tenants.forgex.selected = true
  form.addForgexTenant = true
}

function addCustomer() {
  tenants.customer.selected = true
  form.addCustomerTenant = true
  if (!tenants.customer.name) tenants.customer.name = 'customer'
}

function removeTenant(key: Exclude<TenantKey, 'default'>) {
  tenants[key].selected = false
  if (key === 'forgex') form.addForgexTenant = false
  if (key === 'customer') form.addCustomerTenant = false
}

function openEdit(key: TenantKey) {
  editTargetKey.value = key
  const target = tenants[key]
  editModel.name = target.name || (key === 'customer' ? 'customer' : key)
  editModel.code = target.code
  editModel.intro = target.intro
  editModel.logo = target.logo
  modalOpen.value = true
}

function saveEdit() {
  if (!editTargetKey.value) return
  const key = editTargetKey.value
  const target = tenants[key]
  target.name = editModel.name || (key === 'customer' ? 'customer' : key)
  target.code = editModel.code || key
  target.intro = editModel.intro || (key === 'default' ? t('auth.initWizard.defaultTenantIntro') : target.intro)
  target.logo = editModel.logo
  modalOpen.value = false
  refreshKey.value += 1
}

async function onLogoUpload(file: File) {
  try {
    const res = await uploadFile(file)
    if (typeof res === 'string') editModel.logo = res
  } catch {
    message.error(t('auth.initWizard.uploadFailed'))
  }
  return false
}

const binds = ref<Array<{ account: string; tenants: string[] }>>([])
const roleBinds = ref<Array<{ account: string; tenant: string; roles: string[] }>>([])

function addBind() {
  binds.value.push({ account: 'admin', tenants: ['default'] })
}

function removeBind(index: number) {
  binds.value.splice(index, 1)
}

function addRoleBind() {
  roleBinds.value.push({ account: 'admin', tenant: 'default', roles: ['admin'] })
}

function removeRoleBind(index: number) {
  roleBinds.value.splice(index, 1)
}

async function submit() {
  if (submitting.value) return
  try {
    submitting.value = true
    const payload = {
      security: { ...security },
      form: { ...form },
      tenants: {
        default: { ...tenants.default },
        forgex: tenants.forgex.selected ? { ...tenants.forgex } : null,
        customer: tenants.customer.selected ? { ...tenants.customer } : null,
      },
      binds: binds.value.slice(),
      roleBinds: roleBinds.value.slice(),
    }
    await applyInit(payload)
    message.success(t('auth.initWizard.initSuccess'))
    router.replace('/login')
  } catch {
    message.error(t('auth.initWizard.initFailed'))
  } finally {
    submitting.value = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') step.value = Math.min(step.value + 1, 2)
  if (e.key === 'ArrowUp') step.value = Math.max(step.value - 1, 0)
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})

watch(
  () => [form.addDev, form.addTest, form.addCustom],
  () => {
    form.userTenantBinds = binds.value
    form.userRoleBinds = roleBinds.value
  }
)
</script>

<style scoped>
.init-wrap {
  position: relative;
  height: 100vh;
  background: radial-gradient(circle at top, #0b1120, #020617);
  color: #e5e7eb;
  overflow: hidden;
}
.carousel {
  position: relative;
  width: 100%;
  height: 100%;
}
.slides {
  position: absolute;
  inset: 0;
  transition: transform 0.4s ease;
}
.slide {
  width: 100%;
  height: 100vh;
  padding: 40px 32px;
  overflow-y: auto;
}
.slide-head {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}
.slide-hint {
  color: #9ca3af;
  margin-bottom: 24px;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}
.block {
  padding: 16px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(148, 163, 184, 0.4);
}
.block-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.item-hint {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 10px;
}
.radio-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
}
.ck {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.input {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #4b5563;
  background: rgba(15, 23, 42, 0.7);
  color: #e5e7eb;
}
.inline-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tenant-actions {
  margin-bottom: 10px;
}
.bind-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}
.footer-actions {
  margin-top: 32px;
  text-align: center;
}
.grid-modal {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 8px 10px;
  align-items: center;
}
.logo-sm {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: contain;
}
.modal-actions {
  text-align: right;
}
</style>
