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
              <a-form-item label="用户名" name="username">
                <a-input
                  v-model:value="formData.username"
                  placeholder="请输入用户名"
                  :disabled="isEdit"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item label="真实姓名" name="realName">
                <a-input
                  v-model:value="formData.realName"
                  placeholder="请输入真实姓名"
                />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
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
                  <a-radio :value="1">男</a-radio>
                  <a-radio :value="2">女</a-radio>
                  <a-radio :value="0">未知</a-radio>
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
                <a-select
                  v-model:value="formData.departmentId"
                  placeholder="请选择部门"
                  show-search
                  :filter-option="filterOption"
                >
                  <a-select-option
                    v-for="dept in departmentList"
                    :key="dept.id"
                    :value="dept.id"
                  >
                    {{ dept.deptName }}
                  </a-select-option>
                </a-select>
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
                  <a-radio :value="1">启用</a-radio>
                  <a-radio :value="0">禁用</a-radio>
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
                  <a-select-option value="群众">群众</a-select-option>
                  <a-select-option value="共青团员">共青团员</a-select-option>
                  <a-select-option value="中共党员">中共党员</a-select-option>
                  <a-select-option value="民主党派">民主党派</a-select-option>
                  <a-select-option value="无党派人士">无党派人士</a-select-option>
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
                  <a-select-option value="小学">小学</a-select-option>
                  <a-select-option value="初中">初中</a-select-option>
                  <a-select-option value="高中">高中</a-select-option>
                  <a-select-option value="中专">中专</a-select-option>
                  <a-select-option value="大专">大专</a-select-option>
                  <a-select-option value="本科">本科</a-select-option>
                  <a-select-option value="硕士">硕士</a-select-option>
                  <a-select-option value="博士">博士</a-select-option>
                </a-select>
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
import type { User, UserProfile, WorkHistory, Department, Position } from '../types'

// Props
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

// Emits
const emit = defineEmits<{
  'update:open': [value: boolean]
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
  username: '',
  realName: '',
  email: '',
  phone: '',
  gender: 1,
  entryDate: '',
  departmentId: '',
  positionId: '',
  status: 1,
})

// 附属信息表单数据
const profileData = reactive<Partial<UserProfile>>({
  politicalStatus: '',
  homeAddress: '',
  emergencyContact: '',
  emergencyPhone: '',
  referrer: '',
  education: '',
  workHistory: [],
})

// 部门列表
const departmentList = ref<Department[]>([])

// 职位列表
const positionList = ref<Position[]>([])

// 基础信息校验规则
const basicRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
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
    const res = await userApi.getUserDetail(props.userId)
    if (res.code === 200 && res.data) {
      // 填充基础信息
      Object.assign(formData, {
        id: res.data.id,
        username: res.data.username,
        realName: res.data.realName,
        email: res.data.email,
        phone: res.data.phone,
        gender: res.data.gender,
        entryDate: res.data.entryDate,
        departmentId: res.data.departmentId,
        positionId: res.data.positionId,
        status: res.data.status,
      })
      
      // 填充附属信息
      if (res.data.profile) {
        Object.assign(profileData, {
          politicalStatus: res.data.profile.politicalStatus || '',
          homeAddress: res.data.profile.homeAddress || '',
          emergencyContact: res.data.profile.emergencyContact || '',
          emergencyPhone: res.data.profile.emergencyPhone || '',
          referrer: res.data.profile.referrer || '',
          education: res.data.profile.education || '',
          workHistory: res.data.profile.workHistory || [],
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
    username: '',
    realName: '',
    email: '',
    phone: '',
    gender: 1,
    entryDate: '',
    departmentId: '',
    positionId: '',
    status: 1,
  })
  
  Object.assign(profileData, {
    politicalStatus: '',
    homeAddress: '',
    emergencyContact: '',
    emergencyPhone: '',
    referrer: '',
    education: '',
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
    const res = await userApi.getDepartmentList()
    if (res.code === 200) {
      departmentList.value = res.data
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

/**
 * 获取职位列表
 */
async function fetchPositionList() {
  try {
    const res = await userApi.getPositionList()
    if (res.code === 200) {
      positionList.value = res.data
    }
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
    // 合并基础信息和附属信息
    const submitData = {
      ...formData,
      profile: profileData,
    }
    
    const res = props.isEdit
      ? await userApi.updateUser(submitData as User)
      : await userApi.addUser(submitData as User)
    
    if (res.code === 200) {
      message.success(props.isEdit ? '编辑成功' : '新增成功')
      visible.value = false
      emit('success')
    } else {
      message.error(res.message || '操作失败')
    }
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
