# HTTP 请求使用方式

> 分类：前端 / 请求与反馈
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文档说明页面和 API 文件如何正确使用 `http`、`httpSuccess`、`silentHttp`，以及在实际页面里该怎么组织代码。

## 代码位置

### 统一 HTTP 文件

```text
Forgex_MOM/Forgex_Fronted/src/api/http.ts
```

### 业务 API 组织位置

```text
Forgex_MOM/Forgex_Fronted/src/api/system/
Forgex_MOM/Forgex_Fronted/src/api/workflow/
Forgex_MOM/Forgex_Fronted/src/api/report/
```

典型文件：

```text
Forgex_MOM/Forgex_Fronted/src/api/system/integration.ts
Forgex_MOM/Forgex_Fronted/src/api/system/tableConfig.ts
Forgex_MOM/Forgex_Fronted/src/api/system/personalHomepage.ts
```

## 页面接入原则

### 1. 页面不要直接写 axios

错误写法：

```ts
import axios from 'axios'
await axios.post('/api/sys/user/page', params)
```

推荐写法：

```ts
import { getUserList } from '@/api/system/user'
await getUserList(params)
```

### 2. 页面优先调业务 API 文件

也就是：

```text
页面组件 -> src/api/xxx.ts -> http.ts
```

不要直接在页面里到处写字符串接口地址。

## 什么时候用哪一个客户端

| 场景 | 推荐客户端 | 原因 |
|---|---|---|
| 列表查询、详情查询、字典下拉、树加载 | `http` | 默认模式，带 loading，错误托管 |
| 新增、修改、删除、启停、授权、保存配置 | `httpSuccess` | 自动显示后端成功消息 |
| 轮询、未读角标、预加载、后台刷新 | `silentHttp` | 不打断用户，不显示全局 loading |

## API 文件如何写

## 1. 普通查询接口

```ts
import http from '../http'

export function getThirdSystemList(query) {
  return http.post('/integration/third-system/page', query)
}
```

适用于：

- 列表分页
- 详情查询
- 查询条件联动

## 2. 保存类接口

如果你希望页面保存后一定弹成功提示，可以这样封装：

```ts
import { httpSuccess } from '../http'

export function saveConfig(data) {
  return httpSuccess.post('/sys/common/table/config/update', data)
}
```

如果业务 API 文件已经统一用 `http`，也可以在调用时显式打开成功提示：

```ts
await http.post('/sys/user/save', payload, { showSuccessMessage: true })
```

## 3. 静默请求

```ts
import { silentHttp } from '../http'

export function getUnreadCount() {
  return silentHttp.get('/sys/message/unread')
}
```

这类请求适合：

- 首页角标刷新
- 自动恢复草稿
- 页面预取数据

## 页面如何使用

## 1. 列表页面

```ts
import { getThirdSystemList } from '@/api/system/integration'

async function loadPage(payload) {
  return getThirdSystemList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })
}
```

这种写法通常和 `FxDynamicTable` 配套使用。

## 2. 弹窗保存

```ts
import { addThirdSystem, updateThirdSystem } from '@/api/system/integration'

async function handleSubmit() {
  const values = await formRef.value?.validate()
  if (form.id) {
    await updateThirdSystem(values)
  } else {
    await addThirdSystem(values)
  }
  dialogVisible.value = false
  tableRef.value?.reload?.()
}
```

说明：

- 成功消息优先由 HTTP 层统一托管
- 页面层负责关闭弹窗和刷新列表

## 3. 文件上传自定义错误

```ts
await http.post('/sys/file/upload', formData, {
  customErrorMessage: '上传失败，请检查文件格式、大小或网络状态',
})
```

适合对用户可读性要求较高的场景。

## 常见控制参数

| 参数 | 用法 | 作用 |
|---|---|---|
| `showSuccessMessage` | `true / false` | 强制控制成功提示 |
| `silentError` | `true / false` | 静默错误消息 |
| `customErrorMessage` | 字符串 | 覆盖默认错误提示 |

示例：

```ts
await http.post('/sys/message/read', { id }, { showSuccessMessage: false })
await silentHttp.get('/sys/message/unread', { silentError: true })
```

## 页面层推荐写法

### 推荐

```text
src/views/... 页面
  -> 调 src/api/... 封装函数
  -> 由 http.ts 自动处理公共逻辑
```

### 不推荐

```text
页面里直接写 axios
页面里手动 message.success
页面里重复判断 res.code
```

## 常见排查

| 现象 | 处理建议 |
|---|---|
| 页面请求成功但没有提示 | 检查是否用了 `silentHttp`，或接口被识别为读操作 |
| 页面提示重复 | 删除页面里的手写 `message.success` |
| 提示语言不正确 | 检查当前语言切换和请求头传递 |
| 保存后页面没刷新 | 页面层是否主动调用 `reload` / `loadList` |

## 关联文档

- [HTTP 请求实现逻辑](./HTTP请求实现逻辑.md)
- [公共弹窗使用方式](./公共弹窗使用方式.md)
- [FxDynamicTable使用方式](../配置驱动页面/FxDynamicTable使用方式.md)
