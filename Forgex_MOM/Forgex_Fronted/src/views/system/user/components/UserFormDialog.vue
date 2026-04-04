<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    :loading="loading"
    width="900px"
    @submit="handleSubmit"
    @cancel="handleCancel"
  >
    <a-tabs v-model:activeKey="activeTab" type="card">
      <!-- 基础信息标签页 -->
      <a-tab-pane key="basic" tab="基础信息">
        <a-form
          ref="basicFormRef"
          :model="formData"
          :rules="basicRules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="账号" name="account">
                <a-input
                  v-model:value="formData.account"
                  placeholder="请输入账号"
                  :disabled="isEdit"
                />
              </a-form-item>
            </a-col>

            <a-col :span="12">
              <a-form-item label="用户名" name="username">
                <a-input
                  v-model:value="formData.username"
                  placeholder="请输入用户名"
                  :disabled="isEdit"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="邮箱" name="email">
                <a-input
                  v-model:value="formData.email"
                  placeholder="请输入邮箱"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="手机号" name="phone">
                <a-input
                  v-model:value="formData.phone"
                  placeholder="请输入手机号"
                />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="性别" name="gender">
                <a-radio-group v-model:value="formData.gender">
                  <a-radio v-for="option in genderOptions" :key="option.value" :value="Number(option.value)">
                    {{ option.label }}
                  </a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="入职时间" name="entryDate">
                <a-date-picker
                  v-model:value="formData.entryDate"
                  placeholder="请选择入职时间"
                  style="width: 100%;"
                  value-format="YYYY-MM-DD"
                />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="所属部门" name="departmentId">
                <a-tree-select
                  v-model:value="formData.departmentId"
                  placeholder="请选择部门"
                  show-search
                  tree-default-expand-all
                  :tree-data="departmentList"
                  :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
                  :filter-tree-node="filterTreeNode"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="职位" name="positionId">
                <a-select
                  v-model:value="formData.positionId"
                  placeholder="请选择职位"
                  show-search
                  :filter-option="filterOption"
                >
                  <a-select-option
                    v-for="pos in positionList"
                    :key="pos.id"
                    :value="pos.id"
                  >
                    {{ pos.positionName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="状态" name="status">
                <a-radio-group v-model:value="formData.status">
                  <a-radio v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-tab-pane>
      
      <!-- 附属信息标签页 -->
      <a-tab-pane key="profile" tab="附属信息（可选）">
        <a-form
          ref="profileFormRef"
          :model="profileData"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="政治面貌">
                <a-select
                  v-model:value="profileData.politicalStatus"
                  placeholder="请选择政治面貌"
                  allow-clear
                >
                  <a-select-option v-for="option in politicalStatusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </a-select-option>
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
                  <a-select-option v-for="option in educationOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="籍贯">
                <a-input
                  v-model:value="profileData.birthPlace"
                  placeholder="请输入籍贯"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item label="个人简介" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
                <a-textarea
                  v-model:value="profileData.intro"
                  placeholder="请输入个人简介"
                  :rows="3"
                />
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
              <a-form-item label="引荐人">
                <a-input
                  v-model:value="profileData.referrer"
                  placeholder="请输入引荐人"
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
      </a-tab-pane>
    </a-tabs>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { userApi } from '@/api/system/user'
import { useDict } from '@/hooks/useDict'
import type { User, UserProfile, WorkHistory, Department, Position } from '../types'

// Props
interface Props {
  /** 对话框是否打开，用于控制组件的显示/隐藏状态 */
  open: boolean
  /** 是否为编辑模式，true 表示编辑用户，false 表示新增用户 */
  isEdit: boolean
  /** 用户 ID，编辑模式下必填，用于加载用户详情数据 */
  userId?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  userId: undefined,
})

// Emits
const emit = defineEmits<{
  /**
   * 更新对话框打开状态
   * @param value 新的打开状态
   */
  'update:open': [value: boolean]
  /**
   * 操作成功事件
   * 触发时机：新增或编辑用户成功保存后触发
   */
  'success': []
}>()

// 响应式数据
const visible = ref(props.open)
const loading = ref(false)
const activeTab = ref('basic')
const basicFormRef = ref()
const profileFormRef = ref()

// 基础信息表单数据
const formData = reactive<Partial<User>>({
  account: '',
  username: '',
  email: '',
  phone: '',
  gender: 1,
  entryDate: '',
  departmentId: '',
  positionId: '',
  status: true,
})

// 用户详情数据（用于显示字典翻译后的文本）
const userDetail = ref<User | null>(null)

// 附属信息表单数据
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

// 部门列表
const departmentList = ref<Department[]>([])

// 职位列表
const positionList = ref<Position[]>([])

// 字典数据
const { dictItems: genderOptions } = useDict('gender')
const { dictItems: politicalStatusOptions } = useDict('political_status')
const { dictItems: educationOptions } = useDict('education')
const { dictItems: statusOptions } = useDict('status')

// 基础信息校验规则
const basicRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' },
  ],
  positionId: [
    { required: true, message: '请选择职位', trigger: 'change' },
  ],
}

// 监听 props.open 变化
watch(() => props.open, (val) => {
  visible.value = val
  if (val) {
    activeTab.value = 'basic'
    if (props.isEdit && props.userId) {
      loadUserData()
    } else {
      resetForm()
    }
    fetchDepartmentList()
    fetchPositionList()
  }
})

// 监听 visible 变化
watch(visible, (val) => {
  emit('update:open', val)
})

/**
 * 加载用户数据
 */
async function loadUserData() {
  if (!props.userId) return
  
  loading.value = true
  try {
    const data = await userApi.getUserDetail(props.userId)
    if (data) {
      // 保存完整的用户详情数据，包含字典翻译字段
      userDetail.value = data
      
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
        status: data.status,
      })
      
      if (data.profile) {
        Object.assign(profileData, {
          politicalStatus: data.profile.politicalStatus || '',
          homeAddress: data.profile.homeAddress || '',
          emergencyContact: data.profile.emergencyContact || '',
          emergencyPhone: data.profile.emergencyPhone || '',
          referrer: data.profile.referrer || '',
          education: data.profile.education || '',
          birthPlace: data.profile.birthPlace || '',
          intro: data.profile.intro || '',
          workHistory: data.profile.workHistory || [],
        })
      }
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    message.error('加载用户数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
function resetForm() {
  Object.assign(formData, {
    account: '',
    username: '',
    email: '',
    phone: '',
    gender: 1,
    entryDate: '',
    departmentId: '',
    positionId: '',
    status: true,
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
  
  basicFormRef.value?.clearValidate()
  profileFormRef.value?.clearValidate()
}

/**
 * 获取部门列表
 */
async function fetchDepartmentList() {
  try {
    const list = await userApi.getDepartmentTree({ tenantId: '1' })
    departmentList.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

/**
 * 获取职位列表
 */
async function fetchPositionList() {
  try {
    const list = await userApi.getPositionList()
    positionList.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('获取职位列表失败:', error)
  }
}

/**
 * 添加工作经历
 */
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

/**
 * 删除工作经历
 */
function removeWorkHistory(index: number) {
  profileData.workHistory?.splice(index, 1)
}

/**
 * 树形节点过滤
 */
function filterTreeNode(inputValue: string, treeNode: any) {
  return treeNode.deptName.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0
}

/**
 * 下拉框过滤
 */
function filterOption(input: string, option: any) {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

/**
 * 提交表单
 */
async function handleSubmit() {
  // 先校验基础信息
  try {
    await basicFormRef.value?.validate()
  } catch (error) {
    message.warning('请完善基础信息')
    activeTab.value = 'basic'
    return
  }
  
  loading.value = true
  try {
    const submitData = {
      ...formData,
      profile: profileData,
    }
    
    if (props.isEdit) {
      await userApi.updateUser(submitData as User)
      message.success('编辑成功')
    } else {
      await userApi.addUser(submitData as User)
      message.success('新增成功')
    }
    
    visible.value = false
    emit('success')
  } catch (error) {
    console.error('提交失败:', error)
    message.error('操作失败')
  } finally {
    loading.value = false
  }
}

/**
 * 取消
 */
function handleCancel() {
  visible.value = false
}
</script>

<style scoped lang="less">
.work-history-item {
  padding: 16px;
  margin-bottom: 16px;
  background-color: #fafafa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}
</style>
