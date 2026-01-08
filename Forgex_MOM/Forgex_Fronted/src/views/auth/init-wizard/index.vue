<template>
  <div class="init-wrap">
    <div class="carousel">
      <div class="slides" :style="{ transform: 'translateY(' + (-step*100) + 'vh)' }">
        <section class="slide">
          <div class="slide-head">密码与验证码安全策略初始化</div>
          <div class="slide-hint">请选择密码强度、初始密码、验证码与加密方式</div>
          <div class="form-grid">
            <div class="block">
              <div class="block-title">密码强度</div>
              <div class="item-hint">{{ strengthHint }}</div>
              <div class="radio-row">
                <label><input type="radio" value="high" v-model="security.pwdStrength" /> 高</label>
                <label><input type="radio" value="normal" v-model="security.pwdStrength" /> 普通</label>
                <label><input type="radio" value="low" v-model="security.pwdStrength" /> 低</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">初始密码</div>
              <div class="item-hint">此值用于所有用户初始密码，重置密码将恢复为此值</div>
              <input class="input" type="text" v-model="security.initialPassword" placeholder="默认 Aa123456@" />
            </div>
            <div class="block">
              <div class="block-title">验证码</div>
              <label class="ck"><input type="checkbox" v-model="security.captchaEnabled" /> 开启验证码</label>
              <div class="item-hint">{{ captchaHint }}</div>
              <div class="radio-row" v-if="security.captchaEnabled">
                <label><input type="radio" value="image" v-model="security.captchaMode" /> 图片验证码</label>
                <label><input type="radio" value="slider" v-model="security.captchaMode" /> 滑块验证码</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">密码加密方式</div>
              <div class="item-hint">{{ storeHint }}</div>
              <div class="radio-row">
                <label><input type="radio" value="BCrypt" v-model="security.passwordStore" /> BCrypt</label>
                <label><input type="radio" value="Argon2" v-model="security.passwordStore" /> Argon2</label>
                <label><input type="radio" value="scrypt" v-model="security.passwordStore" /> scrypt</label>
                <label><input type="radio" value="PBKDF2" v-model="security.passwordStore" /> PBKDF2</label>
              </div>
            </div>
          </div>
        </section>

        <section class="slide">
          <div class="slide-head">用户、角色、租户初始化</div>
          <div class="slide-hint">选择用户与角色，配置租户与关联关系</div>
          <div class="form-grid">
            <div class="block">
              <div class="block-title">用户</div>
              <div class="inline-row">
                <label class="ck"><input type="checkbox" disabled checked /> admin（默认）</label>
                <label class="ck"><input type="checkbox" v-model="form.addDev" /> dev 系统开发者</label>
                <label class="ck"><input type="checkbox" v-model="form.addTest" /> test 系统测试员</label>
                <label class="ck"><input type="checkbox" v-model="form.addCustom" /> custom 客户账号</label>
              </div>
              <div class="item-hint">客户账号默认为 custom，不支持在此创建更多客户用户</div>
            </div>
            <div class="block">
              <div class="block-title">角色</div>
              <div class="inline-row">
                <label class="ck"><input type="checkbox" disabled checked /> 系统管理员</label>
                <label class="ck"><input type="checkbox" v-model="form.roleDeveloper" /> 开发者</label>
                <label class="ck"><input type="checkbox" v-model="form.roleTester" /> 测试</label>
                <label class="ck"><input type="checkbox" v-model="form.roleCustomer" /> 客户</label>
              </div>
            </div>
            <div class="block">
              <div class="block-title">租户</div>
              <div class="inline-row tenant-actions">
                <a-button size="small" type="default" @click="addForgex">添加 Forgex 租户</a-button>
                <a-button size="small" type="default" @click="addCustomer">添加 客户 租户</a-button>
              </div>
              <a-table :columns="tenantColumns" :dataSource="tenantRows" size="small" :pagination="false" rowKey="key" :key="(tenants.default.logo||'')+(tenants.forgex.logo||'')+(tenants.customer.logo||'')">
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex==='logo' || column.key==='logo'">
                    <img v-if="record.logo" :src="record.logo" alt="logo" class="logo-sm" />
                    <span v-else style="color:#c3b37b">无</span>
                  </template>
                  <template v-if="column.key==='ops'">
                    <a-button size="small" @click="openEdit(record.key)">编辑</a-button>
                    <a-button size="small" danger style="margin-left:6px" v-if="record.key!=='default'" @click="removeTenant(record.key)">移除</a-button>
                  </template>
                </template>
              </a-table>
            </div>
            <div class="block">
              <div class="block-title">用户租户关联</div>
              <div v-for="(b,i) in binds" :key="i" class="bind-row">
                <a-select :key="'user-'+refreshKey" style="width:160px" v-model:value="b.account" placeholder="选择用户">
                  <a-select-option v-for="opt in userOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="'tenant-'+refreshKey" mode="multiple" style="min-width:260px" v-model:value="b.tenants" placeholder="选择租户">
                  <a-select-option v-for="opt in tenantOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-button size="small" @click="removeBind(i)">删除</a-button>
              </div>
              <a-button size="small" type="default" @click="addBind">添加关联</a-button>
            </div>
            <div class="block">
              <div class="block-title">用户角色关联</div>
              <div v-for="(r,i) in roleBinds" :key="i" class="bind-row">
                <a-select :key="'user-'+refreshKey" style="width:160px" v-model:value="r.account" placeholder="选择用户">
                  <a-select-option v-for="opt in userOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="'tenant-'+refreshKey" style="width:160px" v-model:value="r.tenant" placeholder="选择租户">
                  <a-select-option v-for="opt in tenantOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-select :key="'role-'+refreshKey" mode="multiple" style="min-width:260px" v-model:value="r.roles" placeholder="选择角色">
                  <a-select-option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                </a-select>
                <a-button size="small" @click="removeRoleBind(i)">删除</a-button>
              </div>
              <a-button size="small" type="default" @click="addRoleBind">添加关联</a-button>
            </div>
          </div>
        </section>

        <section class="slide">
          <div class="slide-head">菜单初始化</div>
          <div class="slide-hint">预留，暂不做实现</div>
          <div class="footer-actions">
            <a-button type="primary" :loading="submitting" @click="submit">提交初始化</a-button>
          </div>
        </section>
      </div>
      <a-modal v-model:open="modalOpen" title="编辑租户" wrapClassName="dark-modal">
        <div class="grid-modal">
          <label>名称</label>
          <input class="input" v-model="editModel.name" placeholder="租户名称" />
          <label>编码</label>
          <input class="input" v-model="editModel.code" placeholder="租户编码" />
          <label>简介</label>
          <input class="input" v-model="editModel.intro" placeholder="租户简介" />
          <label>Logo</label>
          <a-upload :showUploadList="false" :beforeUpload="onLogoUpload">
            <a-button size="small">选择文件</a-button>
          </a-upload>
          <div class="logo-preview" v-if="editModel.logo">
            <img :src="editModel.logo" class="logo-sm" />
          </div>
        </div>
        <template #footer>
          <div class="modal-actions">
            <a-button style="margin-right:8px" @click="modalOpen=false">取消</a-button>
            <a-button type="primary" @click="saveEdit">保存</a-button>
          </div>
        </template>
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { applyInit } from '../../../api/sys/init'
import { uploadFile } from '../../../api/sys/file'
import { useRouter } from 'vue-router'

const submitting = ref(false)
const router = useRouter()
const step = ref(0)
const refreshKey = ref(0)

const security = reactive({
  pwdStrength: 'high',
  initialPassword: 'Aa123456@',
  captchaEnabled: true,
  captchaMode: 'image',
  passwordStore: 'BCrypt'
})

const strengthHint = computed(() => {
  if (security.pwdStrength === 'high') return '高：必须包含字母、数字、符号'
  if (security.pwdStrength === 'normal') return '普通：必须包含数字和字母'
  return '低：仅数字'
})
const captchaHint = computed(() => {
  if (!security.captchaEnabled) return '验证码已关闭'
  return security.captchaMode === 'slider' ? '滑块验证码：提升人机识别，降低误拦' : '图片验证码：拦截暴力尝试'
})
const storeHint = computed(() => {
  const v = security.passwordStore.toLowerCase()
  if (v === 'bcrypt') return 'BCrypt：不可逆哈希，安全与性能平衡'
  if (v === 'argon2') return 'Argon2：内存硬耗型不可逆哈希（Argon2id）'
  if (v === 'scrypt') return 'scrypt：抗GPU/ASIC的不可逆哈希'
  return 'PBKDF2：迭代不可逆哈希（HmacSHA256）'
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
  defaultTenantIntro: '默认租户',
  defaultTenantLogo: '' as string,
  addForgexTenant: false,
  forgexTenantIntro: '您的系统开发供应商，专注MOM系统开发与交付，是您可以信赖的专业合作伙伴！',
  addCustomerTenant: false,
  customerTenantName: '',
  customerTenantCode: '',
  customerTenantIntro: '',
  customerTenantLogo: '' as string,
  userTenantBinds: [] as any[],
  userRoleBinds: [] as any[]
})

const tenants = reactive({
  default: { selected: true, name: 'default', code: 'default', intro: '默认租户', logo: '' },
  forgex: { selected: false, code: 'forgex', intro: '您的系统开发供应商，专注MOM系统开发与交付，是您可以信赖的专业合作伙伴！', logo: '' },
  customer: { selected: false, name: '', code: '', intro: '', logo: '' }
})

const tenantColumns = [
  { title: '租户标识', dataIndex: 'key', key: 'key' },
  { title: 'Logo', dataIndex: 'logo', key: 'logo' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '简介', dataIndex: 'intro', key: 'intro' },
  { title: '操作', key: 'ops' }
]
const tenantRows = computed(() => {
  const rows: any[] = []
  rows.push({ key: 'default', logo: tenants.default.logo, name: tenants.default.name, code: tenants.default.code, intro: tenants.default.intro })
  if (tenants.forgex.selected) rows.push({ key: 'forgex', logo: tenants.forgex.logo, name: 'Forgex', code: tenants.forgex.code, intro: tenants.forgex.intro })
  if (tenants.customer.selected) rows.push({ key: 'customer', logo: tenants.customer.logo, name: tenants.customer.name || 'customer', code: tenants.customer.code, intro: tenants.customer.intro })
  return rows
})
const userOptions = computed(() => {
  const opts: any[] = [{ label: 'admin', value: 'admin' }]
  if (form.addDev) opts.push({ label: 'dev', value: 'dev' })
  if (form.addTest) opts.push({ label: 'test', value: 'test' })
  if (form.addCustom) opts.push({ label: form.customUsername || 'custom', value: form.customUsername || 'custom' })
  return opts
})
const tenantOptions = computed(() => tenantRows.value.map(r => ({ label: r.name, value: r.name, logo: r.logo })))
const roleOptions = computed(() => {
  const opts: any[] = [{ label: 'admin', value: 'admin' }]
  if (form.roleDeveloper) opts.push({ label: 'developer', value: 'developer' })
  if (form.roleTester) opts.push({ label: 'tester', value: 'tester' })
  if (form.roleCustomer) opts.push({ label: 'customer', value: 'customer' })
  return opts
})
const modalOpen = ref(false)
const editTargetKey = ref<'default'|'forgex'|'customer' | ''>('')
const editModel = reactive({ name: '', code: '', intro: '', logo: '' })
function addForgex() { tenants.forgex.selected = true }
function addCustomer() { tenants.customer.selected = true; if (!tenants.customer.name) tenants.customer.name = 'customer' }
function removeTenant(key: 'forgex'|'customer') { if (key==='forgex') tenants.forgex.selected=false; else tenants.customer.selected=false }
function openEdit(key: 'default'|'forgex'|'customer') {
  editTargetKey.value = key
  if (key === 'default') { editModel.name = tenants.default.name; editModel.code = tenants.default.code; editModel.intro = tenants.default.intro; editModel.logo = tenants.default.logo }
  if (key === 'forgex') { editModel.name = 'Forgex'; editModel.code = tenants.forgex.code; editModel.intro = tenants.forgex.intro; editModel.logo = tenants.forgex.logo }
  if (key === 'customer') { editModel.name = tenants.customer.name || 'customer'; editModel.code = tenants.customer.code; editModel.intro = tenants.customer.intro; editModel.logo = tenants.customer.logo }
  modalOpen.value = true
}
function saveEdit() {
  if (editTargetKey.value === 'default') {
    tenants.default.name = editModel.name || 'default'
    tenants.default.code = editModel.code || 'default'
    tenants.default.intro = editModel.intro || '默认租户'
    tenants.default.logo = editModel.logo
  }
  if (editTargetKey.value === 'forgex') {
    tenants.forgex.code = editModel.code || 'forgex'
    tenants.forgex.intro = editModel.intro || tenants.forgex.intro
    tenants.forgex.logo = editModel.logo
  }
  if (editTargetKey.value === 'customer') {
    tenants.customer.name = editModel.name || 'customer'
    tenants.customer.code = editModel.code || tenants.customer.code
    tenants.customer.intro = editModel.intro || tenants.customer.intro
    tenants.customer.logo = editModel.logo
  }
  modalOpen.value = false
  refreshKey.value++
}
async function onLogoUpload(file: any) {
  try {
    const res = await uploadFile(file)
    if (res && res.url) {
      editModel.logo = res.url
    }
  } catch (e) {
    message.error('上传失败')
  }
  return false
}
const binds = ref<{ account: string; tenants: string[] }[]>([])
const roleBinds = ref<{ account: string; tenant: string; roles: string[] }[]>([])
function addBind() {
  binds.value.push({ account: 'admin', tenants: ['default'] })
}
function removeBind(i: number) {
  binds.value.splice(i, 1)
}
function addRoleBind() {
  roleBinds.value.push({ account: 'admin', tenant: 'default', roles: ['admin'] })
}
function removeRoleBind(i: number) {
  roleBinds.value.splice(i, 1)
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
        customer: tenants.customer.selected ? { ...tenants.customer } : null
      },
      binds: binds.value.slice(),
      roleBinds: roleBinds.value.slice()
    }
    await applyInit(payload)
    message.success('初始化完成')
    router.replace('/login')
  } catch (e) {
    message.error('初始化失败')
  } finally {
    submitting.value = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    step.value = Math.min(step.value + 1, 2)
  }
  if (e.key === 'ArrowUp') {
    step.value = Math.max(step.value - 1, 0)
  }
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
  border-radius: 12px;
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
}
.modal-actions {
  text-align: right;
}
</style>

