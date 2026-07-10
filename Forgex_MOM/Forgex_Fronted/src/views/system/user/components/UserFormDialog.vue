<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('system.user.form.editUser') : t('system.user.form.addUser')"
    :loading="loading"
    width="900px"
    @submit="handleSubmit"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading" :tip="t('common.loading')" wrapper-class-name="sys-user-form-spin">
    <a-tabs v-model:activeKey="activeTab" type="card" class="sys-user-form-dialog">
      <a-tab-pane key="basic" :tab="t('system.user.tabs.basic')">
        <a-form
          ref="basicFormRef"
          :model="formData"
          :rules="basicRules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('common.login.accountLabel')" name="account">
                <a-input
                  v-model:value="formData.account"
                  :placeholder="t('system.user.form.account')"
                  :disabled="isEdit"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.username')" name="username">
                <a-input
                  v-model:value="formData.username"
                  :placeholder="t('system.user.form.username')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.email')" name="email">
                <a-input
                  v-model:value="formData.email"
                  :placeholder="t('system.user.form.email')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.phone')" name="phone">
                <a-input
                  v-model:value="formData.phone"
                  :placeholder="t('system.user.form.phone')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.user.gender')" name="gender">
                <a-radio-group v-model:value="formData.gender">
                  <a-radio v-for="option in genderOptions" :key="option.value" :value="Number(option.value)">
                    {{ option.label }}
                  </a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.entryDate')" name="entryDate">
                <a-date-picker
                  v-model:value="formData.entryDate"
                  :placeholder="t('system.user.form.entryDate')"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.user.department')" name="departmentId">
                <a-tree-select
                  v-model:value="formData.departmentId"
                  :placeholder="t('system.user.form.department')"
                  show-search
                  tree-default-expand-all
                  :tree-data="departmentList"
                  :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
                  :filter-tree-node="filterTreeNode"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.position')" name="positionId">
                <a-select
                  v-model:value="formData.positionId"
                  :placeholder="t('system.user.form.position')"
                  show-search
                  :filter-option="filterOption"
                >
                  <a-select-option v-for="pos in positionList" :key="pos.id" :value="pos.id">
                    {{ pos.positionName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.user.form.employeeId')" name="employeeId">
                <a-input
                  v-model:value="formData.employeeId"
                  :placeholder="t('system.user.form.employeeIdPlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.user.form.userSource')">
                <a-input :value="userSourceDisplayText" disabled />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.user.status')" name="status">
                <a-radio-group v-model:value="formData.status">
                  <a-radio v-for="option in normalizedStatusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="profile" :tab="t('system.user.tabs.profileOptional')">
        <a-form
          ref="profileFormRef"
          :model="profileData"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.user.profile.politicalStatus')">
                <a-select
                  v-model:value="profileData.politicalStatus"
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
            <a-col :span="12">
              <a-form-item :label="t('system.user.profile.birthPlace')">
                <a-input
                  v-model:value="profileData.birthPlace"
                  :placeholder="t('system.user.profile.birthPlacePlaceholder')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item :label="t('system.user.profile.intro')" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
                <a-textarea
                  v-model:value="profileData.intro"
                  :placeholder="t('system.user.profile.introPlaceholder')"
                  :rows="3"
                />
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
              <a-col :span="2" style="text-align: right">
                <a-button type="link" danger @click="removeWorkHistory(index)">
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
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="11">
                <a-form-item :label="t('system.user.profile.endDate')" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
                  <a-date-picker
                    v-model:value="history.endDate"
                    :placeholder="t('system.user.profile.endDatePlaceholder')"
                    style="width: 100%"
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
            <a-col :span="24" style="text-align: center">
              <a-button type="dashed" block @click="addWorkHistory">
                <template #icon><PlusOutlined /></template>
                {{ t('system.user.profile.addWorkHistory') }}
              </a-button>
            </a-col>
          </a-row>
        </a-form>
      </a-tab-pane>
    </a-tabs>
    </a-spin>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { userApi } from '@/api/system/user'
import { useDict } from '@/hooks/useDict'
import { normalizeBooleanValue } from '@/utils/user'
import type { Department, Position, User, UserProfile } from '../types'
interface Props {
  open: boolean
  isEdit: boolean
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  userId: undefined,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const visible = ref(props.open)
const loading = ref(false)
const activeTab = ref('basic')
const basicFormRef = ref()
const profileFormRef = ref()
const { t } = useI18n({ useScope: 'global' })

const formData = reactive<Partial<User>>({
  account: '',
  username: '',
  email: '',
  phone: '',
  gender: 1,
  entryDate: '',
  departmentId: '',
  positionId: '',
  employeeId: '',
  userSource: 1,
  userSourceText: t('system.user.userSource.siteCreate'),
  status: true,
})

const profileData = reactive<Partial<UserProfile>>({
  politicalStatus: '',
  homeAddress: '',
  emergencyContact: '',
  emergencyPhone: '',
  referrer: '',
  education: '',
  birthPlace: '',
  intro: '',
  workHistory: [],
})

const departmentList = ref<Department[]>([])
const positionList = ref<Position[]>([])

const { dictItems: genderOptions } = useDict('gender')
const { dictItems: politicalStatusOptions } = useDict('political_status')
const { dictItems: educationOptions } = useDict('education')
const { dictItems: statusOptions } = useDict('status')

/**
 * 鏂板鍦烘櫙榛樿鏄剧ず鈥滄湰绔欐柊澧炩€濓紝缂栬緫鍦烘櫙灞曠ず鍚庣杩斿洖鏉ユ簮鏂囨湰銆?
 */
const userSourceDisplayText = computed(() => formData.userSourceText || t('system.user.userSource.siteCreate'))

const normalizedStatusOptions = computed(() =>
  statusOptions.value.map(option => ({
    ...option,
    value: normalizeBooleanValue(option.value) ?? option.value,
  })),
)

const basicRules = {
  account: [
    { required: true, message: t('system.user.form.account'), trigger: 'blur' },
    { min: 3, max: 20, message: t('system.user.form.accountLength'), trigger: 'blur' },
  ],
  username: [
    { required: true, message: t('system.user.form.username'), trigger: 'blur' },
    { min: 2, max: 20, message: t('system.user.form.usernameLength'), trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: t('system.user.form.emailFormat'), trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('system.user.form.phoneFormat'), trigger: 'blur' },
  ],
  employeeId: [
    { pattern: /^\d*$/, message: t('system.user.form.employeeIdNumber'), trigger: 'blur' },
  ],
}

watch(() => props.open, async (val) => {
  visible.value = val
  if (!val) {
    return
  }
  activeTab.value = 'basic'
  await fetchDepartmentList()
  await fetchPositionList()
  if (props.isEdit && props.userId) {
    await loadUserData()
  } else {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:open', val)
})

async function loadUserData() {
  if (!props.userId) return

  loading.value = true
  try {
    const data = await userApi.getUserDetail(props.userId)
    Object.assign(formData, {
      id: data.id,
      account: data.account,
      username: data.username,
      email: data.email,
      phone: data.phone,
      gender: data.gender,
      entryDate: data.entryDate,
      departmentId: data.departmentId,
      positionId: data.positionId,
      employeeId: data.employeeId ? String(data.employeeId) : '',
      userSource: data.userSource,
      userSourceText: data.userSourceText || '',
      status: data.status,
    })

    const profile = data.profile || {}
    Object.assign(profileData, {
      politicalStatus: profile.politicalStatus || '',
      homeAddress: profile.homeAddress || '',
      emergencyContact: profile.emergencyContact || '',
      emergencyPhone: profile.emergencyPhone || '',
      referrer: profile.referrer || '',
      education: profile.education || '',
      birthPlace: profile.birthPlace || '',
      intro: profile.intro || '',
      workHistory: Array.isArray(profile.workHistory) ? profile.workHistory : [],
    })
  } catch {
    message.error(t('system.user.message.loadDetailFailed'))
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(formData, {
    id: undefined,
    account: '',
    username: '',
    email: '',
    phone: '',
    gender: 1,
    entryDate: '',
    departmentId: '',
    positionId: '',
    employeeId: '',
    userSource: 1,
    userSourceText: t('system.user.userSource.siteCreate'),
  })

  Object.assign(profileData, {
    politicalStatus: '',
    homeAddress: '',
    emergencyContact: '',
    emergencyPhone: '',
    referrer: '',
    education: '',
    birthPlace: '',
    intro: '',
    workHistory: [],
  })

  basicFormRef.value?.clearValidate?.()
  profileFormRef.value?.clearValidate?.()
}

async function fetchDepartmentList() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    departmentList.value = []
    return
  }
  const list = await userApi.getDepartmentTree({ tenantId })
  departmentList.value = Array.isArray(list) ? list : []
}

async function fetchPositionList() {
  const list = await userApi.getPositionList()
  positionList.value = Array.isArray(list) ? list : []
}

function addWorkHistory() {
  if (!profileData.workHistory) {
    profileData.workHistory = []
  }
  profileData.workHistory.push({
    company: '',
    position: '',
    startDate: '',
    endDate: '',
    description: '',
  })
}

function removeWorkHistory(index: number) {
  profileData.workHistory?.splice(index, 1)
}

function filterTreeNode(inputValue: string, treeNode: any) {
  return String(treeNode.deptName || '').toLowerCase().includes(inputValue.toLowerCase())
}

function filterOption(input: string, option: any) {
  const label = option?.children?.[0]?.children ?? option?.label ?? ''
  return String(label).toLowerCase().includes(input.toLowerCase())
}

async function handleSubmit() {
  try {
    await basicFormRef.value?.validate()
  } catch {
    message.warning(t('validation.required'))
    activeTab.value = 'basic'
    return
  }

  loading.value = true
  try {
    const normalizedStatus = normalizeBooleanValue(formData.status)
    const submitData = {
      ...formData,
      employeeId: formData.employeeId ? Number(formData.employeeId) : undefined,
      ...(normalizedStatus !== undefined ? { status: normalizedStatus } : {}),
      profile: profileData,
    }

    if (props.isEdit) {
      await userApi.updateUser(submitData as User)
    } else {
      await userApi.addUser(submitData as User)
    }

    visible.value = false
    emit('success')
  } catch {
    message.error(t('common.operationFailed'))
  } finally {
    loading.value = false
  }
}
function handleCancel() {
  visible.value = false
}
</script>

<style scoped lang="less" src="@/styles/views/system/user/components/user-form-dialog.less"></style>

