---
name: forgex-frontend-guide
description: Forgex 前端开发指南，涵盖 Vue3+TypeScript+Ant Design Vue 页面开发、FxDynamicTable 动态表格、BaseFormDialog 弹窗、HTTP 请求封装、国际化、布局与公共组件。当开发前端页面、实现组件、编写 API 对接、处理表格配置、国际化文本、或修改 Forgex_MOM/Forgex_Fronted 文件时自动应用此技能。
disable-model-invocation: true
---

# Forgex 前端开发指南

## 一、技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **UI 库**: Ant Design Vue
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios

## 二、项目结构

```
Forgex_Fronted/
├── src/
│   ├── api/              # API 接口封装
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── common/       # 通用组件（FxDynamicTable、BaseFormDialog 等）
│   │   ├── system/       # 系统组件（DeptTree 等）
│   │   └── excel/        # Excel 导入组件
│   ├── layouts/          # 布局组件
│   ├── locales/          # 国际化文件
│   ├── router/           # 路由配置
│   ├── store/            # Pinia 状态管理
│   ├── utils/            # 工具函数
│   └── views/            # 页面组件
├── public/               # 公共资源
├── package.json
└── vite.config.ts
```

## 三、核心组件使用

### 3.1 HTTP 请求

- **实现逻辑**: 见 `Forgex_Doc/前端/请求与反馈/HTTP请求实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/前端/请求与反馈/HTTP请求使用方式.md`

**关键点**：
- 统一使用 `src/api/http.ts` 封装的请求方法
- 自动处理 Token 注入、错误提示、国际化
- 请求/响应拦截器统一处理

### 3.2 FxDynamicTable 动态表格

- **实现逻辑**: 见 `Forgex_Doc/前端/配置驱动页面/FxDynamicTable实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/前端/配置驱动页面/FxDynamicTable使用方式.md`

**关键点**：
- 配置驱动的列表组件
- 支持列设置、用户个性化配置
- 集成字典翻译、权限按钮
- 自动处理分页、排序、查询

### 3.3 BaseFormDialog 公共弹窗

- **实现逻辑**: 见 `Forgex_Doc/前端/请求与反馈/公共弹窗实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/前端/请求与反馈/公共弹窗使用方式.md`

**关键点**：
- 统一的表单弹窗容器
- 支持新增/编辑模式
- 自动处理表单验证和提交

### 3.4 其他公共组件

| 组件 | 路径 | 实现逻辑 | 使用方式 |
|------|------|---------|---------|
| 部门树与组织选择 | `src/components/system/DeptTree.vue` | [文档](Forgex_Doc/前端/组件与页面/部门树与组织选择实现逻辑.md) | [文档](Forgex_Doc/前端/组件与页面/部门树与组织选择使用方式.md) |
| 图标选择器 | `src/components/common/IconPicker.vue` | [文档](Forgex_Doc/前端/组件与页面/图标选择器实现逻辑.md) | [文档](Forgex_Doc/前端/组件与页面/图标选择器使用方式.md) |
| 消息模板预览 | `TemplatePreview.vue` | [文档](Forgex_Doc/前端/组件与页面/消息模板预览与接收人选择实现逻辑.md) | [文档](Forgex_Doc/前端/组件与页面/消息模板预览与接收人选择使用方式.md) |
| 系统页面引导 | `FxGuideTour.vue` | [文档](Forgex_Doc/前端/组件与页面/系统页面引导实现逻辑.md) | [文档](Forgex_Doc/前端/组件与页面/系统页面引导使用方式.md) |
| 公共导入 | `src/components/excel/CommonImportDialog.vue` | [文档](Forgex_Doc/前端/组件与页面/公共导入组件实现逻辑.md) | [文档](Forgex_Doc/前端/组件与页面/公共导入组件使用方式.md) |
| 多语言输入 | `I18nInput.vue` | [文档](Forgex_Doc/前端/国际化与布局/多语言输入实现逻辑.md) | [文档](Forgex_Doc/前端/国际化与布局/多语言输入使用方式.md) |
| 个人首页 | `PersonalHomepageDesigner.vue` | [文档](Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局实现逻辑.md) | [文档](Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局使用方式.md) |

## 四、开发规范

### 4.1 组件开发

```vue
<template>
  <div class="component-name">
    <!-- 组件内容 -->
  </div>
</template>

<script setup lang="ts">
// 导入语句

// 类型定义
interface Props {
  // 属性定义
}

// Props 定义
const props = withDefaults(defineProps<Props>(), {
  // 默认值
});

// Emits 定义
const emit = defineEmits<{
  // 事件定义
}>();

// 响应式数据
const state = reactive({
  // 状态
});

// 方法
const handleAction = () => {
  // 逻辑
};

// 生命周期
onMounted(() => {
  // 初始化
});
</script>

<style scoped lang="less">
.component-name {
  // 样式
}
</style>
```

### 4.2 API 封装

```typescript
// src/api/xxx.ts
import { http } from '@/api/http';

export interface XxxParam {
  // 参数定义
}

export interface XxxResponse {
  // 响应定义
}

export const getXxxList = (params: XxxParam) => {
  return http.get<XxxResponse>('/api/xxx/list', { params });
};

export const createXxx = (data: XxxParam) => {
  return http.post<XxxResponse>('/api/xxx/create', data);
};
```

### 4.3 国际化

```typescript
// src/locales/zh-CN/xxx.ts
export default {
  xxx: {
    title: '标题',
    placeholder: '请输入',
    success: '操作成功',
    error: '操作失败',
  }
};

// 组件中使用
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const title = t('xxx.title');
```

### 4.4 权限按钮

```vue
<template>
  <a-button v-permission="'sys:user:add'">新增</a-button>
  <a-button v-permission="'sys:user:edit'">编辑</a-button>
  <a-button v-permission="'sys:user:delete'">删除</a-button>
</template>
```

## 五、页面开发流程

1. **创建页面文件**: 在 `src/views/模块名/` 下创建 `index.vue`
2. **定义 API 接口**: 在 `src/api/` 下创建对应的 API 文件
3. **配置路由**: 在路由配置中添加页面路由（或使用动态路由）
4. **实现页面逻辑**:
   - 使用 FxDynamicTable 展示列表
   - 使用 BaseFormDialog 处理表单
   - 使用权限按钮控制操作
   - 使用国际化文本
5. **测试验证**: 在浏览器中测试功能

## 六、注意事项

1. **统一使用公共组件**: 优先使用 FxDynamicTable、BaseFormDialog 等公共组件
2. **遵循国际化规范**: 所有用户可见文本必须国际化
3. **使用权限指令**: 按钮级权限使用 `v-permission` 指令
4. **保持代码风格一致**: 遵循 ESLint 和 Prettier 配置
5. **注释规范**: 组件添加功能描述注释

## 七、关联文档

- **实现逻辑与使用方式**: 见 `Forgex_Doc/前端/` 目录下各功能的双文档
- **开发规范**: 见 `forgex-development-standards` skill
- **后端接口**: 见 `forgex-backend-guide` skill
- **公共表格规范**: 见 `Forgex_Doc/开发规范/规范文档/公共表格组件使用说明与实现逻辑.md`
