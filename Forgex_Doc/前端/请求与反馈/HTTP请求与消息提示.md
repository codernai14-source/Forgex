# HTTP 请求与消息提示

> 分类：前端 / 请求与反馈  
> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

## 一、概述

Forgex 前端统一 HTTP 层位于：

- `Forgex_MOM/Forgex_Fronted/src/api/http.ts`

它不是简单的 axios 二次封装，而是承接了整条前后端交互规范：

```text
页面 / API 模块
  → http / httpSuccess / silentHttp
  → 请求头注入语言
  → 后端 R<T> 返回 code / message / data
  → 自动成功 / 失败提示
  → 登录失效处理 / 租户恢复
  → 页面只拿 data，不再重复写消息逻辑
```

---

## 二、对外暴露的三个客户端

| 客户端 | 入口 | 默认行为 | 适用场景 |
|---|---|---|---|
| `http` | `export default httpClient` | 显示全局 loading、自动失败提示、按路径判断是否显示成功消息 | 大多数查询 / 写操作 |
| `httpSuccess` | `export { httpSuccess }` | 显示全局 loading、成功必弹后端 message | 保存、授权、提交等需要明确反馈的写操作 |
| `silentHttp` | `export { silentHttp }` | 不显示全局 loading、默认静默错误、不自动成功提示 | 轮询、预加载、后台刷新、角标更新 |

### 2.1 推荐选择方式

| 场景 | 推荐客户端 |
|---|---|
| 列表查询 / 详情查询 / 下拉联动 | `http` |
| 新增 / 修改 / 删除，且希望强制弹成功提示 | `httpSuccess` |
| 已经由页面自定义交互接管，不希望全局提示干扰 | `http` + 显式配置 |
| SSE 辅助轮询、未读数刷新、预加载 | `silentHttp` |

---

## 三、扩展请求配置 `FxRequestConfig`

除 axios 原生配置外，还支持 3 个高频控制项：

| 字段 | 类型 | 说明 |
|---|---|---|
| `showSuccessMessage` | `boolean` | 是否显示成功提示；未传时由请求路径自动判断 |
| `silentError` | `boolean` | 是否静默业务错误提示 |
| `customErrorMessage` | `string` | 覆盖后端返回 message 的错误提示 |

### 3.1 示例

```ts
import http, { httpSuccess, silentHttp } from '@/api/http'

// 普通查询
await http.post('/sys/user/page', form)

// 强制成功提示
await httpSuccess.post('/sys/user/save', payload)

// 静默轮询
await silentHttp.get('/sys/message/unread-count')

// 关闭成功提示
await http.post('/sys/message/read', { id }, { showSuccessMessage: false })

// 自定义失败提示
await http.post('/sys/file/upload', data, {
  customErrorMessage: '上传失败，请检查文件大小或格式',
})
```

---

## 四、请求阶段做了什么

### 4.1 统一注入语言头

请求发送前会从前端国际化状态中读取当前语言，并写入：

- `Accept-Language`
- `X-Lang`

这也是后端 `LangContext`、`RMessageI18nAdvice` 能正常翻译提示消息的前提。

### 4.2 全局 loading 控制

`http` 默认会：

1. 增加活跃请求计数；
2. 读取本地布局配置 `fx-layout-config`；
3. 如果允许显示 loading，则调用全局 loader；
4. 在所有请求完成后自动隐藏。

`silentHttp` 不参与这套 loading 逻辑。

---

## 五、响应阶段做了什么

## 5.1 标准响应模型

前端默认按后端统一结构处理：

```json
{
  "code": 200,
  "message": "保存成功",
  "data": { }
}
```

返回时：

- `code === 200`：默认返回 `data`
- `code !== 200`：统一抛错并显示错误消息
- `responseType === 'blob'`：按文件下载逻辑处理

### 5.2 自动成功提示

默认客户端 `http` 会根据 URL 关键词自动判断是否显示成功消息：

- **显示成功提示**：`/save`、`/update`、`/delete`、`/grant`、`/assign`、`/upload`、`/import`、`/register`、`/logout` 等
- **不显示成功提示**：`/page`、`/list`、`/detail`、`/tree`、`/download`、`/export`、`/routes`、`/captcha` 等

如果页面需要覆盖默认判断，可显式传：

- `showSuccessMessage: true`
- `showSuccessMessage: false`

### 5.3 自动失败提示

当 `code !== 200` 时：

1. 优先读取后端 `message` / `msg`；
2. 若页面传了 `customErrorMessage`，则优先使用自定义错误文案；
3. 若 `silentError !== true`，则通过全局消息组件统一提示；
4. 最终 `Promise.reject(data)`，调用方可继续捕获。

---

## 六、登录失效与租户恢复

### 6.1 登录失效码

当前前端把以下状态码视为需要重新登录：

- `602`：未登录或登录过期

### 6.2 自动租户恢复

如果后端提示和“租户未选择 / Tenant”相关，并且前端 session 中仍保留：

- `account`
- `tenantId`

则 HTTP 层会先尝试调用：

```text
POST /auth/choose-tenant
```

恢复成功后会自动重放原请求。

这对以下场景很有帮助：

- 页面刷新后租户上下文短暂丢失；
- Token 尚在，但租户绑定状态需要重新补齐；
- 某些跨模块跳转后首次接口调用需要恢复租户选择。

### 6.3 恢复失败后的行为

若恢复失败：

1. 清理 `sessionStorage` 中的登录态；
2. 清理本地动态路由缓存；
3. 弹出统一重新登录弹窗；
4. 跳转到 `/login`。

---

## 七、消息防重复机制

为了避免“后端自动提示 + 页面手动提示”重复弹两次，HTTP 层做了额外处理：

1. 对 `message.success` / `message.error` 做轻量包装；
2. 记录最近一次后端自动提示的时间与类型；
3. 在短时间窗口内抑制重复的同类前端提示。

这意味着：

- 页面若仍手写 `message.success('保存成功')`，很可能会被抑制；
- 正确姿势是**优先让后端返回 message，由 HTTP 层托管**。

---

## 八、推荐 API 模块写法

建议所有业务接口都集中在 `src/api/*` 下，不要在页面中直接写 axios。

### 8.1 示例

```ts
import http, { httpSuccess } from '@/api/http'

export function pageUsers(data: any) {
  return http.post('/sys/user/page', data)
}

export function saveUser(data: any) {
  return httpSuccess.post('/sys/user/save', data)
}

export function readMessage(id: string) {
  return http.post('/sys/message/read', { id }, { showSuccessMessage: false })
}
```

### 8.2 页面层写法

```ts
const records = await pageUsers(query)
await saveUser(formData)
await readMessage(id)
```

页面一般只关心：

- 数据结果；
- loading 状态；
- 个别特殊交互。

---

## 九、与后端约定

为保证这套机制稳定工作，后端接口建议遵守：

1. 统一返回 `R<T>`；
2. 写操作尽量返回明确 `message`；
3. 失败时返回明确 `message`，不要只给状态码；
4. 多语言提示优先由后端翻译后返回；
5. 下载接口明确使用 `blob`。

如果后端不返回 `message`，前端公共层仍可工作，但自动成功提示会明显变弱。

---

## 十、常见使用建议

### 10.1 新增列表页

- 查询接口：`http`
- 新增/修改/删除：`http` 或 `httpSuccess`
- 若后端已返回成功文案，页面不要再写 `message.success`

### 10.2 标记已读 / 静默更新

此类接口通常不希望频繁提示，可传：

```ts
{ showSuccessMessage: false }
```

### 10.3 后台轮询

统一使用：

```ts
silentHttp.get(...)
silentHttp.post(...)
```

避免 loading 和错误提示频繁打扰用户。

---

## 十一、排查清单

当出现“接口没有弹提示 / 总是重复弹 / 请求莫名跳登录”时，建议检查：

- [ ] 页面是否用了 `silentHttp`
- [ ] 是否显式设置了 `showSuccessMessage: false`
- [ ] 后端是否真的返回了 `message`
- [ ] 返回结构是否仍是 `R<T>`
- [ ] 是否命中了登录失效码 `602`
- [ ] session 中是否保留了 `account` 与 `tenantId`
- [ ] 页面是否又手动写了一次 `message.success`

---

## 十二、关联文档

- [公共弹窗](./公共弹窗.md)
- [FxDynamicTable 与列设置](../配置驱动页面/FxDynamicTable与列设置.md)
- [统一返回与国际化](../../后端/配置与审计/统一返回与国际化.md)
- [通用消息与提示](../../后端/配置与审计/通用消息与提示.md)

