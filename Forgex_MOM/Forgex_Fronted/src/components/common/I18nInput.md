# I18nInput 组件说明

## 概述

`I18nInput` 用于编辑多语言 JSON 字符串，适合菜单、字典、角色、部门等需要维护多语名称的场景。

组件支持三种模式：

- `simple`：单行输入，适合表单中快速录入。
- `table`：表格风格输入，适合同时维护多种语言。
- `modal`：弹窗编辑，适合复杂内容维护。

## 数据格式

组件的 `v-model` 值为 JSON 字符串，示例如下：

```json
{
  "zh-CN": "用户管理",
  "en-US": "User Management",
  "ja-JP": "ユーザー管理",
  "ko-KR": "사용자 관리"
}
```

## 基础用法

### simple 模式

```vue
<template>
  <a-form-item label="菜单名称">
    <I18nInput
      v-model="form.nameI18nJson"
      mode="simple"
      placeholder="请输入多语言名称"
    />
  </a-form-item>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import I18nInput from '@/components/common/I18nInput.vue'

const form = reactive({
  nameI18nJson: '',
})
</script>
```

### table 模式

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
  dictValueI18nJson: '',
})
</script>
```

## Props

| 属性 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `modelValue` | 组件绑定的多语言 JSON 字符串 | `string` | `''` |
| `mode` | 组件模式 | `'simple' \| 'table' \| 'modal'` | `'simple'` |
| `placeholder` | simple 模式下的占位文案 | `string` | `'请输入多语言内容'` |
| `defaultLang` | 默认语言 | `string` | `'zh-CN'` |

## Events

| 事件 | 说明 | 参数 |
|------|------|------|
| `update:modelValue` | 值变化时触发 | `(value: string) => void` |

## 典型场景

### 菜单维护

```vue
<a-form-item label="菜单名称" name="nameI18nJson">
  <I18nInput
    v-model="formData.nameI18nJson"
    mode="simple"
    placeholder="请输入菜单名称"
  />
</a-form-item>
```

### 字典值维护

```vue
<a-form-item label="字典值多语言">
  <I18nInput
    v-model="form.dictValueI18nJson"
    mode="table"
  />
</a-form-item>
```

### 角色名称维护

```vue
<a-form-item label="角色名称">
  <I18nInput
    v-model="roleForm.nameI18nJson"
    mode="simple"
    placeholder="请输入角色名称"
  />
</a-form-item>
```

### 部门名称维护

```vue
<a-form-item label="部门名称">
  <I18nInput
    v-model="deptForm.nameI18nJson"
    mode="simple"
    placeholder="请输入部门名称"
  />
</a-form-item>
```

## 注意事项

- 后端保存的字段应为合法 JSON 字符串。
- 若页面只展示单语言值，建议优先展示 `zh-CN`，并对空值做兜底。
- 如果接口直接返回对象而不是字符串，提交前应统一序列化。
