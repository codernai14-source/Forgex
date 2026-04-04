---
name: frontend-design
description: 指导 Vue3+TypeScript 前端开发，包括组件开发、API 封装、状态管理、权限控制、国际化、样式规范。使用场景：创建 Vue 组件、编写页面、开发功能模块、实现业务逻辑。
---

# 前端设计规范

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| TypeScript | 5.x | 类型系统 |
| Vite | 4.x | 构建工具 |
| Ant Design Vue | 4.x | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |
| vue-i18n | 9.x | 国际化 |

## 项目结构

```
frontend/
├── src/
│   ├── api/                         # API 接口
│   │   ├── sys/                     # 系统模块
│   │   │   ├── user.ts              # 用户管理
│   │   │   ├── role.ts              # 角色管理
│   │   │   └── menu.ts              # 菜单管理
│   │   └── auth/                    # 认证模块
│   ├── assets/                      # 资源文件
│   │   └── styles/
│   │       ├── variables.scss       # SCSS 变量
│   │       ├── mixins.scss          # SCSS 混合
│   │       └── global.scss          # 全局样式
│   ├── components/                  # 公共组件
│   │   ├── common/                  # 通用组件
│   │   └── business/                # 业务组件
│   ├── composables/                 # 组合式函数
│   ├── directives/                  # 自定义指令
│   ├── layouts/                     # 布局组件
│   ├── locales/                     # 国际化
│   ├── router/                      # 路由配置
│   ├── store/                       # 状态管理
│   │   └── modules/                 # Store 模块
│   ├── types/                       # TypeScript 类型
│   ├── utils/                       # 工具函数
│   │   ├── request.ts               # Axios 封装
│   │   ├── auth.ts                  # 认证工具
│   │   ├── storage.ts               # 存储工具
│   │   └── permission.ts            # 权限工具
│   └── views/                       # 页面组件
│       ├── login/
│       ├── dashboard/
│       └── system/                  # 系统管理页面
│           ├── user/
│           ├── role/
│           └── menu/
```

---

## 组件开发规范

### 组件命名

- 使用 PascalCase 命名
- 文件名与组件名一致
- 多单词组件使用 PascalCase

```
✅ 正确：
- UserList.vue
- UserInfo.vue
- DepartmentTree.vue

❌ 错误：
- user-list.vue
- userinfo.vue
```

### 标准组件模板

```vue
<template>
  <div class="user-list">
    <!-- 组件内容 -->
  </div>
</template>

<!--
 * 用户列表组件
 * 
 * 功能描述：
 * 1. 展示用户分页列表，支持筛选和排序
 * 2. 提供用户新增、编辑、删除操作入口
 * 3. 支持批量操作和导出功能
 * 
 * 使用场景：
 * - 系统管理 > 用户管理页面
 * - 需要用户选择器的弹窗场景
 * 
 * @author Forgex
 * @version 1.0
 * @since 2026-03-28
-->
<script setup lang="ts">
// 导入依赖
import { ref, computed, onMounted } from 'vue'
import { getUserList } from '@/api/sys/user'
import type { User } from '@/types/user'

/**
 * 组件 Props 定义
 */
interface Props {
  /** 页面大小，默认 10，用于控制分页每页显示条数 */
  pageSize?: number
  /** 是否显示边框，默认 true，用于控制表格边框样式 */
  showBorder?: boolean
  /** 用户 ID，必填，用于查询指定用户信息 */
  userId: number
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 10,
  showBorder: true
})

/**
 * 组件事件定义
 */
interface Emits {
  /** 
   * 更新事件
   * 触发时机：点击编辑按钮时触发
   * @param data 用户数据对象，包含完整的用户信息
   */
  (e: 'update', data: User): void
  /** 
   * 删除事件
   * 触发时机：点击删除按钮确认后触发
   * @param id 用户 ID，已删除的用户标识
   */
  (e: 'delete', id: number): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const userList = ref<User[]>([])
const loading = ref(false)
const total = ref(0)

// 计算属性
const hasData = computed(() => userList.value.length > 0)

/**
 * 获取用户列表数据
 * 
 * 执行步骤：
 * 1. 设置加载状态为 true
 * 2. 调用 API 获取数据
 * 3. 更新响应式数据
 * 4. 设置加载状态为 false
 * 
 * @param params 查询参数
 * @returns 用户列表数据对象，包含 records 和 total
 */
const fetchData = async (params?: any) => {
  loading.value = true
  try {
    const { data } = await getUserList(params)
    userList.value = data.records
    total.value = data.total
    return data
  } finally {
    loading.value = false
  }
}

/**
 * 处理用户编辑操作
 * 
 * 打开编辑弹窗并加载用户详情数据
 * 
 * @param user 要编辑的用户对象
 * @see UserEditModal 编辑弹窗组件
 */
const handleEdit = (user: User): void => {
  emit('update', user)
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.user-list {
  padding: 20px;
  
  &--bordered {
    border: 1px solid #d9d9d9;
  }
}
</style>
```

### Props 规范

**必须使用 TypeScript 定义 Props 类型**：

```typescript
// ✅ 推荐：使用 interface 定义
interface Props {
  // 必填
  userId: number
  // 可选，带默认值
  pageSize?: number
  // 带默认值
  showBorder?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 10,
  showBorder: true
})

// ❌ 避免：使用 defineProps 的运行时声明
const props = defineProps({
  userId: Number,
  pageSize: {
    type: Number,
    default: 10
  }
})
```

### Emits 规范

**必须使用 TypeScript 定义 Emits 类型**：

```typescript
// ✅ 推荐：使用 interface 定义
interface Emits {
  (e: 'update', data: User): void
  (e: 'delete', id: number): void
  (e: 'change', value: string): void
}

const emit = defineEmits<Emits>()

// 使用
emit('update', userData)
emit('delete', userId)

// ❌ 避免：使用 defineEmits 的运行时声明
const emit = defineEmits(['update', 'delete', 'change'])
```

---

## API 接口规范

### API 模块划分

**按业务模块划分 API 文件**：

```
src/api/
├── sys/                    # 系统模块
│   ├── user.ts            # 用户管理
│   ├── role.ts            # 角色管理
│   ├── menu.ts            # 菜单管理
│   ├── dept.ts            # 部门管理
│   └── dict.ts            # 字典管理
├── auth/                   # 认证模块
│   ├── login.ts           # 登录
│   ├── captcha.ts         # 验证码
│   └── token.ts           # Token 管理
└── biz/                    # 业务模块
    ├── order.ts           # 订单管理
    └── product.ts         # 产品管理
```

### API 接口定义

```typescript
// src/api/sys/user.ts
import request from '@/utils/request'
import type { User, UserParam, PageResult } from '@/types/user'

/**
 * 分页查询用户列表
 * 
 * 接口路径：POST /api/sys/user/page
 * 需要权限：sys:user:query
 * 
 * @param params 查询参数
 *               - pageNum: 页码（必填）
 *               - pageSize: 每页大小（必填）
 *               - username: 用户名（可选）
 *               - status: 状态（可选）
 * @returns 分页结果，包含用户列表和总数
 */
export function getUserList(params: UserParam) {
  return request<PageResult<User>>({
    url: '/api/sys/user/page',
    method: 'post',
    data: params
  })
}

/**
 * 获取用户详情
 * 
 * 接口路径：GET /api/sys/user/get/{id}
 * 需要权限：sys:user:query
 * 
 * @param id 用户 ID
 * @returns 用户信息对象
 */
export function getUserDetail(id: number) {
  return request<User>({
    url: `/api/sys/user/get/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 * 
 * 接口路径：POST /api/sys/user/create
 * 需要权限：sys:user:add
 * 
 * @param data 用户创建参数
 *             - username: 用户名（必填）
 *             - password: 密码（必填）
 *             - realName: 真实姓名（可选）
 * @returns 新创建的用户 ID
 */
export function createUser(data: UserParam) {
  return request<number>({
    url: '/api/sys/user/create',
    method: 'post',
    data: data
  })
}

/**
 * 更新用户
 * 
 * 接口路径：POST /api/sys/user/update
 * 需要权限：sys:user:edit
 * 
 * @param data 用户更新参数
 *             - id: 用户 ID（必填）
 *             - username: 用户名（必填）
 *             - realName: 真实姓名（可选）
 * @returns 无返回值
 */
export function updateUser(data: UserParam) {
  return request<void>({
    url: '/api/sys/user/update',
    method: 'post',
    data: data
  })
}

/**
 * 删除用户
 * 
 * 接口路径：DELETE /api/sys/user/delete/{id}
 * 需要权限：sys:user:delete
 * 
 * @param id 用户 ID
 * @returns 无返回值
 */
export function deleteUser(id: number) {
  return request<void>({
    url: `/api/sys/user/delete/${id}`,
    method: 'delete'
  })
}
```

### 类型定义

```typescript
// src/types/user.ts
/**
 * 用户实体类型
 * 
 * 用于表示用户信息的完整数据结构，包含用户基本信息、
 * 部门信息、角色信息等。通常用于用户列表展示和详情查询。
 */
export interface User {
  /** 用户 ID，唯一标识 */
  id: number
  /** 用户名，用于登录 */
  username: string
  /** 真实姓名，用于显示 */
  realName: string
  /** 手机号，可选 */
  phone?: string
  /** 邮箱，可选 */
  email?: string
  /** 用户状态：0=禁用，1=启用 */
  status: number
  /** 部门 ID，关联部门表 */
  deptId?: number
  /** 部门名称，从部门表关联查询 */
  deptName?: string
  /** 角色 ID 列表 */
  roleIds?: number[]
  /** 角色名称列表，从角色表关联查询 */
  roleNames?: string[]
  /** 创建时间 */
  createTime: string
  /** 最后登录时间，可选 */
  lastLoginTime?: string
}

/**
 * 用户查询参数类型
 * 
 * 用于用户列表查询接口的请求参数
 */
export interface UserParam {
  /** 页码，从 1 开始 */
  pageNum: number
  /** 每页大小 */
  pageSize: number
  /** 用户名，模糊查询 */
  username?: string
  /** 真实姓名，模糊查询 */
  realName?: string
  /** 用户状态筛选：0=禁用，1=启用 */
  status?: number
  /** 部门 ID，查询指定部门下的用户 */
  deptId?: number
}

/**
 * 分页结果类型
 * 
 * 用于封装分页查询的返回数据，通用类型可复用
 * @template T 列表项数据类型
 */
export interface PageResult<T> {
  /** 数据记录列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  pageNum: number
  /** 每页大小 */
  pageSize: number
}
```

---

## 状态管理规范

### Pinia Store 定义

**使用 setup store 语法**：

```typescript
// src/store/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'
import { getUserInfo } from '@/api/auth/login'

/**
 * 用户状态管理模块
 * 
 * 功能描述：
 * 1. 存储用户登录状态和 Token
 * 2. 存储用户基本信息
 * 3. 存储用户权限列表
 * 4. 提供登录、登出、获取用户信息等方法
 * 
 * 使用场景：
 * - 登录后存储用户信息
 * - 权限判断时获取权限列表
 * - 页面刷新时恢复用户状态
 * 
 * @author Forgex
 * @version 1.0
 * @since 2026-03-28
 */
export const useUserStore = defineStore('user', () => {
  // ==================== 状态定义 ====================
  
  /** 用户登录 Token，用于 API 认证 */
  const token = ref<string>('')
  
  /** 用户信息对象，包含用户基本信息 */
  const userInfo = ref<User | null>(null)
  
  /** 用户权限列表，用于权限判断 */
  const permissions = ref<string[]>([])
  
  // ==================== 计算属性 ====================
  
  /**
   * 是否已登录
   * 
   * 判断逻辑：Token 不为空字符串时返回 true
   * @returns 登录状态布尔值
   */
  const isLoggedIn = computed(() => !!token.value)
  
  /**
   * 是否为管理员
   * 
   * 判断逻辑：用户名等于 'admin' 时返回 true
   * @returns 管理员状态布尔值
   */
  const isAdmin = computed(() => {
    return userInfo.value?.username === 'admin'
  })
  
  /**
   * 用户头像 URL
   * 
   * 如果用户没有头像，返回默认头像
   * @returns 头像 URL 字符串
   */
  const avatar = computed(() => {
    return userInfo.value?.avatar || '/default-avatar.png'
  })
  
  // ==================== 方法定义 ====================
  
  /**
   * 设置用户 Token
   * 
   * 将 Token 存储到状态和 localStorage
   * 
   * @param newToken 新的 Token 字符串
   */
  const setToken = (newToken: string): void => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  /**
   * 设置用户信息
   * 
   * 更新用户信息对象
   * 
   * @param info 用户信息对象
   */
  const setUserInfo = (info: User): void => {
    userInfo.value = info
  }
  
  /**
   * 设置权限列表
   * 
   * 更新用户权限列表
   * 
   * @param perms 权限标识列表
   */
  const setPermissions = (perms: string[]): void => {
    permissions.value = perms
  }
  
  /**
   * 获取用户信息
   * 
   * 调用 API 获取用户信息和权限列表，更新状态
   * 
   * @returns 用户信息对象
   * @throws API 请求失败时抛出错误
   */
  const fetchUserInfo = async (): Promise<User> => {
    const { data } = await getUserInfo()
    userInfo.value = data.userInfo
    permissions.value = data.permissions
    return data.userInfo
  }
  
  /**
   * 用户登出
   * 
   * 清空用户状态和本地存储，跳转到登录页
   */
  const logout = async (): Promise<void> => {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
  }
  
  // 暴露状态和方法
  return {
    token,
    userInfo,
    permissions,
    isLoggedIn,
    isAdmin,
    avatar,
    setToken,
    setUserInfo,
    setPermissions,
    fetchUserInfo,
    logout
  }
})
```

---

## 权限控制规范

### 权限指令

```typescript
// src/directives/permission.ts
import { useUserStore } from '@/store/modules/user'
import type { Directive, DirectiveBinding } from 'vue'

export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) => {
    const { value } = binding
    const userStore = useUserStore()
    const permissions = userStore.permissions
    
    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(perm => {
        return value.includes(perm) || value.includes('*:*:*')
      })
      
      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('权限标识不能为空')
    }
  }
}
```

### 权限判断函数

```typescript
// src/utils/permission.ts
import { useUserStore } from '@/store/modules/user'

/**
 * 判断是否有权限
 * 
 * @param permission 权限标识，可以是字符串或字符串数组
 * @returns 是否有权限
 */
export function hasPermission(permission: string | string[]): boolean {
  const userStore = useUserStore()
  const permissions = userStore.permissions
  
  if (typeof permission === 'string') {
    return permissions.includes(permission) || permissions.includes('*:*:*')
  }
  
  if (Array.isArray(permission)) {
    return permission.some(perm => 
      permissions.includes(perm) || permissions.includes('*:*:*')
    )
  }
  
  return false
}
```

### 权限使用示例

```vue
<template>
  <div>
    <!-- 按钮级权限 -->
    <a-button v-permission="['sys:user:add']">新增用户</a-button>
    <a-button v-permission="['sys:user:edit']">编辑用户</a-button>
    <a-button v-permission="['sys:user:delete']">删除用户</a-button>
    
    <!-- 使用函数判断 -->
    <a-button v-if="hasPermission('sys:user:export')">导出用户</a-button>
  </div>
</template>

<script setup lang="ts">
import { hasPermission } from '@/utils/permission'
</script>
```

---

## 国际化规范

### 语言包定义

```typescript
// src/locales/zh-CN.ts
export default {
  common: {
    submit: '提交',
    cancel: '取消',
    confirm: '确认',
    delete: '删除',
    edit: '编辑',
    search: '搜索',
    reset: '重置',
    loading: '加载中...',
    success: '成功',
    fail: '失败'
  },
  login: {
    title: '用户登录',
    username: '用户名',
    password: '密码',
    captcha: '验证码',
    rememberMe: '记住我',
    forgetPassword: '忘记密码',
    login: '登录',
    register: '注册'
  },
  user: {
    management: '用户管理',
    add: '新增用户',
    edit: '编辑用户',
    delete: '删除用户',
    username: '用户名',
    realName: '真实姓名',
    phone: '手机号',
    email: '邮箱',
    status: '状态',
    dept: '部门',
    role: '角色',
    createTime: '创建时间'
  }
}
```

### 使用国际化

```vue
<template>
  <div>
    <h1>{{ t('login.title') }}</h1>
    <a-input :placeholder="t('login.username')" />
    <a-input :placeholder="t('login.password')" type="password" />
    <a-button @click="handleLogin">{{ t('login.login') }}</a-button>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const handleLogin = () => {
  // 登录逻辑
}
</script>
```

---

## 样式规范

### 使用 SCSS

```vue
<style lang="scss" scoped>
// 使用变量
.user-list {
  padding: $spacing-md;
  background-color: $bg-color;
  
  // 使用嵌套
  .user-item {
    display: flex;
    align-items: center;
    
    &:hover {
      background-color: $hover-bg;
    }
  }
  
  // 使用 Mixin
  @include flex-center;
}
</style>
```

### 常用变量

```scss
// src/assets/styles/variables.scss

// 颜色
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$error-color: #f5222d;

// 背景色
$bg-color: #ffffff;
$bg-color-secondary: #f5f5f5;
$hover-bg: #e6f7ff;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 字体
$font-size-xs: 12px;
$font-size-sm: 14px;
$font-size-md: 16px;
$font-size-lg: 18px;
$font-size-xl: 20px;
```

### 常用 Mixin

```scss
// src/assets/styles/mixins.scss

// Flex 居中
@mixin flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

// Flex 垂直居中
@mixin flex-vertical-center {
  display: flex;
  align-items: center;
}

// 文本省略
@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// 多行文本省略
@mixin text-ellipsis-multi($lines: 2) {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: $lines;
  -webkit-box-orient: vertical;
}

// 清除浮动
@mixin clearfix {
  &::after {
    content: '';
    display: table;
    clear: both;
  }
}
```

---

## 工具函数规范

### Axios 封装

```typescript
// src/utils/request.ts
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/modules/user'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, data, message: msg } = response.data
    
    if (code === 200) {
      return response
    }
    
    message.error(msg || '请求失败')
    return Promise.reject(new Error(msg || '请求失败'))
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message.error('未登录或登录已过期')
          break
        case 403:
          message.error('无权限访问')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器错误')
          break
        default:
          message.error(error.response.data.message || '请求失败')
      }
    } else {
      message.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export function request<T>(config: AxiosRequestConfig): Promise<AxiosResponse<T>> {
  return service(config)
}

export default service
```

### 存储工具

```typescript
// src/utils/storage.ts
const STORAGE_KEY = 'forgex_'

/**
 * 本地存储工具
 * 
 * 提供 localStorage 的封装，支持 JSON 序列化
 */
export const storage = {
  /**
   * 设置存储
   * 
   * @param key 存储键
   * @param value 存储值
   */
  set(key: string, value: any): void {
    try {
      localStorage.setItem(STORAGE_KEY + key, JSON.stringify(value))
    } catch (error) {
      console.error('localStorage 设置失败', error)
    }
  },
  
  /**
   * 获取存储
   * 
   * @param key 存储键
   * @returns 存储值
   */
  get(key: string): any {
    try {
      const item = localStorage.getItem(STORAGE_KEY + key)
      return item ? JSON.parse(item) : null
    } catch (error) {
      console.error('localStorage 获取失败', error)
      return null
    }
  },
  
  /**
   * 移除存储
   * 
   * @param key 存储键
   */
  remove(key: string): void {
    try {
      localStorage.removeItem(STORAGE_KEY + key)
    } catch (error) {
      console.error('localStorage 移除失败', error)
    }
  },
  
  /**
   * 清空存储
   */
  clear(): void {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('localStorage 清空失败', error)
    }
  }
}
```

---

## 注释规范

### Vue 组件注释

每个 Vue 组件文件必须在 `<script>` 标签上方添加 HTML 格式的组件注释块：

```vue
<!--
 * 用户列表组件
 * 
 * 功能描述：
 * 1. 展示用户分页列表，支持筛选和排序
 * 2. 提供用户新增、编辑、删除操作入口
 * 3. 支持批量操作和导出功能
 * 
 * 使用场景：
 * - 系统管理 > 用户管理页面
 * - 需要用户选择器的弹窗场景
 * 
 * @author Forgex
 * @version 1.0
 * @since 2026-03-28
-->
<script setup lang="ts">
// 组件逻辑...
</script>
```

### Props 注释

```typescript
/**
 * 组件 Props 定义
 */
interface Props {
  /** 页面大小，默认 10，用于控制分页每页显示条数 */
  pageSize?: number
  /** 是否显示边框，默认 true，用于控制表格边框样式 */
  showBorder?: boolean
  /** 用户 ID，必填，用于查询指定用户信息 */
  userId: number
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 10,
  showBorder: true
})
```

### Emits 注释

```typescript
/**
 * 组件事件定义
 */
interface Emits {
  /** 
   * 更新事件
   * 触发时机：点击编辑按钮时触发
   * @param data 用户数据对象，包含完整的用户信息
   */
  (e: 'update', data: User): void
  /** 
   * 删除事件
   * 触发时机：点击删除按钮确认后触发
   * @param id 用户 ID，已删除的用户标识
   */
  (e: 'delete', id: number): void
}

const emit = defineEmits<Emits>()
```

### 方法注释

```typescript
/**
 * 获取用户列表数据
 * 
 * 执行步骤：
 * 1. 设置加载状态为 true
 * 2. 调用 API 获取数据
 * 3. 更新响应式数据
 * 4. 设置加载状态为 false
 * 
 * @param params 查询参数
 * @returns 用户列表数据对象，包含 records 和 total
 */
const fetchUserList = async (params: UserQueryParam): Promise<PageResult<User>> => {
  loading.value = true
  try {
    const { data } = await getUserList(params)
    userList.value = data.records
    total.value = data.total
    return data
  } finally {
    loading.value = false
  }
}
```

### 方法内部逻辑注释

对于包含多步骤的方法，每个步骤都需要添加行内注释：

```typescript
const handleSave = async (): Promise<void> => {
  // 1. 第一步：表单校验
  // 检查表单数据是否合法，为提交做准备
  if (!validateForm()) {
    message.error('表单填写不完整')
    return
  }

  // 2. 第二步：构建请求参数
  // 使用上一步校验通过的表单数据，构建 API 请求参数
  const requestData = {
    id: formData.value.id,
    name: formData.value.name,
    status: formData.value.status
  }

  // 3. 第三步：调用 API 保存数据
  // 使用上一步构建的请求参数，调用后端接口
  try {
    loading.value = true
    await saveUser(requestData)
    message.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败，请重试')
  } finally {
    loading.value = false
  }

  // 4. 第四步：关闭弹窗并刷新列表
  // 使用上一步保存成功的数据，刷新列表展示
  emit('refresh')
  emit('close')
}
```

### 类型注释

```typescript
/**
 * 用户实体类型
 * 
 * 用于表示用户信息的完整数据结构，包含用户基本信息、
 * 部门信息、角色信息等。通常用于用户列表展示和详情查询。
 */
export interface User {
  /** 用户 ID，唯一标识 */
  id: number
  /** 用户名，用于登录 */
  username: string
  /** 真实姓名，用于显示 */
  realName: string
  /** 手机号，可选 */
  phone?: string
  /** 邮箱，可选 */
  email?: string
  /** 用户状态：0=禁用，1=启用 */
  status: number
  /** 部门 ID，关联部门表 */
  deptId?: number
  /** 部门名称，从部门表关联查询 */
  deptName?: string
  /** 角色 ID 列表 */
  roleIds?: number[]
  /** 角色名称列表，从角色表关联查询 */
  roleNames?: string[]
  /** 创建时间 */
  createTime: string
  /** 最后登录时间，可选 */
  lastLoginTime?: string
}
```

---

## 代码质量检查清单

- [ ] 使用 TypeScript 定义类型
- [ ] 组件使用 setup 语法糖
- [ ] Props 和 Emits 使用 interface 定义
- [ ] API 接口统一封装
- [ ] 使用 Pinia 进行状态管理
- [ ] 权限控制使用指令和函数
- [ ] 所有文本使用国际化
- [ ] 样式使用 SCSS
- [ ] 组件添加文件注释（功能描述、@author、@version、@since）
- [ ] Props 添加字段注释说明含义、默认值、用途
- [ ] Emits 添加事件注释说明触发时机、参数含义
- [ ] 公共方法添加注释（功能描述、执行步骤、@param、@returns）
- [ ] Interface 添加类型注释和字段注释
- [ ] Store 添加文件注释、状态注释、方法注释
- [ ] API 方法添加注释（接口路径、权限、参数说明）
- [ ] 方法内部多步骤逻辑添加行内注释
- [ ] 重要业务逻辑添加行内注释
- [ ] 注释使用中文

---

## 常见错误

### 1. Props 使用运行时声明

❌ **错误**：

```typescript
const props = defineProps({
  userId: Number,
  pageSize: {
    type: Number,
    default: 10
  }
})
```

✅ **正确**：

```typescript
interface Props {
  userId: number
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  pageSize: 10
})
```

### 2. Emits 使用运行时声明

❌ **错误**：

```typescript
const emit = defineEmits(['update', 'delete', 'change'])
```

✅ **正确**：

```typescript
interface Emits {
  (e: 'update', data: User): void
  (e: 'delete', id: number): void
  (e: 'change', value: string): void
}

const emit = defineEmits<Emits>()
```

### 3. 组件缺少文件注释

❌ **错误**：

```vue
<template>
  <div class="user-list">
    <!-- 组件内容 -->
  </div>
</template>

<script setup lang="ts">
// 没有注释
</script>
```

✅ **正确**：

```vue
<!--
 * 用户列表组件
 * 
 * 功能描述：
 * 1. 展示用户分页列表，支持筛选和排序
 * 2. 提供用户新增、编辑、删除操作入口
 * 
 * @author Forgex
 * @version 1.0
 * @since 2026-03-28
-->
<template>
  <div class="user-list">
    <!-- 组件内容 -->
  </div>
</template>

<script setup lang="ts">
// 组件逻辑...
</script>
```

### 4. 方法缺少注释

❌ **错误**：

```typescript
const fetchData = async () => {
  loading.value = true
  const { data } = await getUserList()
  userList.value = data.records
  loading.value = false
}
```

✅ **正确**：

```typescript
/**
 * 获取用户列表数据
 * 
 * 执行步骤：
 * 1. 设置加载状态为 true
 * 2. 调用 API 获取数据
 * 3. 更新响应式数据
 * 4. 设置加载状态为 false
 * 
 * @returns 用户列表数据对象
 */
const fetchData = async (): Promise<void> => {
  loading.value = true
  try {
    const { data } = await getUserList()
    userList.value = data.records
  } finally {
    loading.value = false
  }
}
```

---

## 总结

本规范涵盖了 Vue3+TypeScript 前端开发的核心方面：

1. **组件开发**：使用 setup 语法糖，TypeScript 定义 Props 和 Emits
2. **API 封装**：按模块划分，统一类型定义，完整注释
3. **状态管理**：使用 Pinia setup store，规范状态和方法定义
4. **权限控制**：指令和函数结合，实现按钮级权限控制
5. **国际化**：统一语言包管理，使用 vue-i18n
6. **样式规范**：使用 SCSS，统一定义变量和 Mixin
7. **注释规范**：完整的文件注释、方法注释、类型注释、行内注释

遵循这些规范可以确保代码的一致性、可维护性和可读性。
