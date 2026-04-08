# I18nInput 多语言输入组件使用文档

## 📖 概述

`I18nInput` 是一个通用的多语言配置组件，用于简化前端多语言配置的用户体验。它可以自动从后端获取支持的语言列表，并提供友好的表格界面让用户填写各语言的翻译内容。

## ✨ 特性

- ✅ **自动获取语言列表**：从后端 API 自动加载系统支持的语言
- ✅ **三种显示模式**：simple（简单输入框）、table（表格）、modal（仅弹窗）
- ✅ **双向绑定**：支持 v-model 绑定 JSON 字符串
- ✅ **用户友好**：无需手写 JSON，通过表格可视化配置
- ✅ **数据驱动**：新增语言无需修改代码
- ✅ **类型安全**：完整的 TypeScript 类型定义

## 🎯 解决的问题

### 传统方式的痛点

```json
// 用户需要手写这样的 JSON，容易出错
{
  "zh-CN": "用户管理",
  "en-US": "User Management",
  "ja-JP": "ユーザー管理",
  "ko-KR": "사용자 관리"
}
```

### 新方式的优势

使用 `I18nInput` 组件，用户只需在表格中填写：

| 语言 | 翻译内容 |
|------|---------|
| 🇨🇳 简体中文 | 用户管理 |
| 🇺🇸 English | User Management |
| 🇯🇵 日本語 | ユーザー管理 |
| 🇰🇷 한국어 | 사용자 관리 |

组件会自动生成 JSON 并提交给后端。

## 📦 安装

组件已创建在 `src/components/common/I18nInput.vue`，无需额外安装。

## 🚀 快速开始

### 1. 基础用法（Simple 模式）

适合在表单中使用，提供一个输入框和多语言配置按钮：

```vue
<template>
  <a-form-item label="菜单名称">
    <I18nInput 
      v-model="form.nameI18nJson" 
      mode="simple" 
      placeholder="请输入菜单名称"
    />
  </a-form-item>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import I18nInput from '@/components/common/I18nInput.vue'

const form = reactive({
  nameI18nJson: ''
})
</script>
```

### 2. 表格模式（Table 模式）

直接显示表格，适合在独立的多语言配置页面：

```vue
<template>
  <I18nInput 
    v-model="form.dictValueI18nJson" 
    mode="table"
  />
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import I18nInput from '@/components/common/I18nInput.vue'

const form = reactive({
  dictValueI18nJson: ''
})
</script>
```

## 📋 API 文档

### Props

| 参数 | 说明 | 类型 | 默认值 | 必填 |
|------|------|------|--------|------|
| modelValue | v-model 绑定的值（JSON 字符串） | string | '' | 否 |
| mode | 显示模式 | 'simple' \| 'table' \| 'modal' | 'simple' | 否 |
| placeholder | 占位符（仅 simple 模式） | string | '请输入' | 否 |
| defaultLang | 默认语言代码 | string | 'zh-CN' | 否 |

### Events

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| update:modelValue | 值变化时触发 | (value: string) => void |

### 数据格式

组件输出的 JSON 格式：

```json
{
  "zh-CN": "简体中文翻译",
  "en-US": "English Translation",
  "ja-JP": "日本語翻訳",
  "ko-KR": "한국어 번역"
}
```

## 🎨 实际应用场景

### 场景1：菜单管理

在菜单管理的新增/编辑表单中使用：

```vue
<template>
  <a-form :model="formData" :rules="rules">
    <!-- 菜单名称 - 使用多语言输入 -->
    <a-form-item label="菜单名称" name="nameI18nJson">
      <I18nInput 
        v-model="formData.nameI18nJson" 
        mode="simple" 
        placeholder="请输入菜单名称"
      />
    </a-form-item>
    
    <!-- 其他字段 -->
    <a-form-item label="菜单路径" name="path">
      <a-input v-model:value="formData.path" />
    </a-form-item>
    
    <a-form-item>
      <a-button type="primary" @click="handleSubmit">提交</a-button>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { createMenu } from '@/api/system/menu'

const formData = reactive({
  nameI18nJson: '',
  path: '',
  icon: '',
  orderNum: 0
})

const handleSubmit = async () => {
  try {
    // 提交时，nameI18nJson 已经是 JSON 字符串，直接传给后端
    await createMenu(formData)
    message.success('创建成功')
  } catch (error) {
    message.error('创建失败')
  }
}
</script>
```

### 场景2：字典管理

在字典项的编辑中使用：

```vue
<template>
  <a-modal v-model:open="visible" title="编辑字典项" @ok="handleSubmit">
    <a-form :model="form">
      <a-form-item label="字典名称">
        <a-input v-model:value="form.dictName" />
      </a-form-item>
      
      <!-- 字典值多语言配置 -->
      <a-form-item label="字典值翻译">
        <I18nInput 
          v-model="form.dictValueI18nJson" 
          mode="table"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { updateDict } from '@/api/system/dict'

const visible = ref(false)
const form = reactive({
  id: null,
  dictName: '',
  dictValueI18nJson: ''
})

const handleSubmit = async () => {
  try {
    await updateDict(form)
    message.success('更新成功')
    visible.value = false
  } catch (error) {
    message.error('更新失败')
  }
}
</script>
```

### 场景3：角色管理

```vue
<template>
  <a-form-item label="角色名称">
    <I18nInput 
      v-model="roleForm.nameI18nJson" 
      mode="simple" 
      placeholder="请输入角色名称"
    />
  </a-form-item>
</template>
```

### 场景4：部门管理

```vue
<template>
  <a-form-item label="部门名称">
    <I18nInput 
      v-model="deptForm.nameI18nJson" 
      mode="simple" 
      placeholder="请输入部门名称"
    />
  </a-form-item>
</template>
```

## 🔧 后端集成

### 1. 实体类字段

确保实体类有对应的 JSON 字段：

```java
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    /** 菜单名称 */
    private String name;
    
    /** 菜单名称多语言JSON */
    private String nameI18nJson;
    
    // 其他字段...
}
```

### 2. 数据库字段

```sql
ALTER TABLE sys_menu ADD COLUMN name_i18n_json VARCHAR(1000) COMMENT '菜单名称多语言JSON';
ALTER TABLE sys_dict ADD COLUMN dict_value_i18n_json VARCHAR(1000) COMMENT '字典值多语言JSON';
```

### 3. 前端显示逻辑

在列表页面显示时，根据当前语言从 JSON 中取值：

```typescript
// 工具函数：从 I18n JSON 中获取当前语言的值
export function getI18nValue(i18nJson: string, fallback: string = ''): string {
  if (!i18nJson) return fallback
  
  try {
    const obj = JSON.parse(i18nJson)
    const currentLang = localStorage.getItem('fx-locale') || 'zh-CN'
    return obj[currentLang] || obj['zh-CN'] || fallback
  } catch (error) {
    return fallback
  }
}

// 使用示例
const menuName = getI18nValue(menu.nameI18nJson, menu.name)
```

## 🎯 需要配置多语言的地方

根据你的项目分析，以下地方需要配置多语言：

### 1. ✅ 菜单管理（SysMenu）
- **字段**：`name`（菜单名称）
- **存储字段**：`nameI18nJson`
- **使用位置**：`/views/system/menu/index.vue`

### 2. ✅ 字典管理（SysDict）
- **字段**：`dictValue`（字典值显示名称）
- **存储字段**：`dictValueI18nJson`
- **使用位置**：`/views/system/dict/index.vue`

### 3. 🔄 角色管理（SysRole）
- **字段**：`roleName`（角色名称）
- **建议添加**：`roleNameI18nJson`

### 4. 🔄 部门管理（SysDepartment）
- **字段**：`deptName`（部门名称）
- **建议添加**：`deptNameI18nJson`

### 5. 🔄 岗位管理（SysPosition）
- **字段**：`positionName`（岗位名称）
- **建议添加**：`positionNameI18nJson`

### 6. 🔄 模块管理（SysModule）
- **字段**：`moduleName`（模块名称）
- **建议添加**：`moduleNameI18nJson`

## 📝 迁移指南

### 步骤1：更新数据库

```sql
-- 菜单表（已有）
-- ALTER TABLE sys_menu ADD COLUMN name_i18n_json VARCHAR(1000);

-- 字典表（已有）
-- ALTER TABLE sys_dict ADD COLUMN dict_value_i18n_json VARCHAR(1000);

-- 角色表
ALTER TABLE sys_role ADD COLUMN role_name_i18n_json VARCHAR(1000) COMMENT '角色名称多语言JSON';

-- 部门表
ALTER TABLE sys_department ADD COLUMN dept_name_i18n_json VARCHAR(1000) COMMENT '部门名称多语言JSON';

-- 岗位表
ALTER TABLE sys_position ADD COLUMN position_name_i18n_json VARCHAR(1000) COMMENT '岗位名称多语言JSON';

-- 模块表
ALTER TABLE sys_module ADD COLUMN module_name_i18n_json VARCHAR(1000) COMMENT '模块名称多语言JSON';
```

### 步骤2：更新实体类

在对应的实体类中添加字段：

```java
/** 角色名称多语言JSON */
private String roleNameI18nJson;
```

### 步骤3：更新前端表单

将原来的输入框替换为 `I18nInput` 组件：

```vue
<!-- 原来 -->
<a-input v-model:value="form.roleName" placeholder="请输入角色名称" />

<!-- 现在 -->
<I18nInput v-model="form.roleNameI18nJson" mode="simple" placeholder="请输入角色名称" />
```

### 步骤4：更新显示逻辑

在列表页面使用工具函数显示：

```vue
<template>
  <a-table :columns="columns" :data-source="dataSource">
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'roleName'">
        {{ getI18nValue(record.roleNameI18nJson, record.roleName) }}
      </template>
    </template>
  </a-table>
</template>
```

## 🐛 常见问题

### Q1: 如何设置初始值？

```vue
<script setup>
const form = reactive({
  // 直接设置 JSON 字符串
  nameI18nJson: '{"zh-CN":"用户管理","en-US":"User Management"}'
})
</script>
```

### Q2: 如何验证必填？

```vue
<script setup>
const rules = {
  nameI18nJson: [
    { 
      required: true, 
      message: '请配置多语言', 
      trigger: 'change',
      validator: (rule, value) => {
        if (!value || value === '{}' || value === '') {
          return Promise.reject('请至少配置一种语言')
        }
        return Promise.resolve()
      }
    }
  ]
}
</script>
```

### Q3: 如何获取当前语言的值？

```typescript
import { getI18nValue } from '@/utils/i18n'

const displayName = getI18nValue(menu.nameI18nJson, menu.name)
```

## 🎉 总结

使用 `I18nInput` 组件后：

1. ✅ **用户体验提升**：不需要手写 JSON，通过表格可视化配置
2. ✅ **开发效率提升**：统一的组件，减少重复代码
3. ✅ **维护成本降低**：新增语言只需在后端配置，前端自动适配
4. ✅ **数据一致性**：统一的数据格式，减少错误

## 📚 相关文档

- [Vue I18n 官方文档](https://vue-i18n.intlify.dev/)
- [Ant Design Vue 表单组件](https://antdv.com/components/form-cn)
- [项目多语言配置文档](../locales/README.md)

