# HTTP 请求实现逻辑

> 分类：前端 / 请求与反馈  
> 版本：**V0.6.0**  
> 更新时间：**2026-04-17**

本文档说明 Forgex 前端统一 HTTP 能力的实现链路、关键代码位置、客户端分层设计和消息提示机制。

## 功能定位

Forgex 前端所有常规接口请求都应统一经过 `http.ts`，由它负责把以下公共能力串成一条链路：

- Axios 实例创建
- 请求头注入
- 语言透传
- 租户透传
- 全局 loading 控制
- 后端成功/失败消息托管
- 登录失效处理
- 租户恢复尝试
- 文件下载响应处理

这层的目标是让页面层只关心“调哪个接口、传什么参数、拿什么数据”，不要在每个页面重复写错误提示、登录过期判断和 loading 管理。

## 代码位置

### 核心文件

```text
Forgex_MOM/Forgex_Fronted/src/api/http.ts
```

### 相关依赖文件

```text
Forgex_MOM/Forgex_Fronted/src/locales/index.ts
Forgex_MOM/Forgex_Fronted/src/api/system/*.ts
```

`src/api/system/*.ts`、`src/api/workflow/*.ts`、`src/api/report/*.ts` 等业务 API 文件最终都会依赖 `http.ts` 导出的客户端。

## 对外客户端设计

`http.ts` 当前对外导出 3 个客户端：

| 客户端 | 代码位置 | 作用 |
|---|---|---|
| `http` | `src/api/http.ts` 默认导出 | 普通请求，列表、详情、下拉、联动查询优先使用 |
| `httpSuccess` | `src/api/http.ts` 命名导出 | 强制显示成功提示，保存、提交、授权类写操作优先使用 |
| `silentHttp` | `src/api/http.ts` 命名导出 | 静默请求，不显示全局 loading，默认静默错误 |

实现上这 3 个客户端底层共用 Axios 实例能力，但通过默认配置区分行为：

- `http`
  默认参与全局 loading，自动处理业务失败提示，成功提示按路径和配置判断
- `httpSuccess`
  在 `http` 基础上默认 `showSuccessMessage=true`
- `silentHttp`
  使用独立静默实例，不显示全局 loading，默认 `silentError=true`

## 请求阶段实现链路

### 1. 创建实例

`http.ts` 内部创建了 3 个 Axios 实例：

```text
http
silentHttp
rawHttp
```

其中：

- `http`
  用于普通请求，带全局 loading 管理
- `silentHttp`
  用于静默请求，不走全局 loading
- `rawHttp`
  用于特殊内部恢复逻辑，例如租户恢复时避免再次进入当前拦截器链路造成循环调用

### 2. 注入请求头

请求拦截器会统一注入：

- `Accept-Language`
- `X-Lang`
- `X-Tenant-Id`

其中语言来自：

```ts
getLocale()
```

租户来自：

```ts
sessionStorage.getItem('tenantId')
```

这一步是前后端国际化链路与多租户链路打通的关键。

### 3. 管理全局 loading

`http` 实例会通过 `activeReq` 计数器管理全局 loading：

```text
请求发起 -> activeReq++
请求结束 -> activeReq--
activeReq 为 0 -> 关闭全局 loading
```

实际加载器来自：

```ts
(window as any).__globalLoader
```

这意味着所有页面不需要自己单独写“开始请求显示 loading、结束请求关闭 loading”的公共逻辑。

### 4. 扩展配置

`FxRequestConfig` 在 `AxiosRequestConfig` 基础上补充了几个前端统一控制字段：

| 字段 | 作用 |
|---|---|
| `showSuccessMessage` | 是否显示后端成功消息 |
| `silentError` | 是否静默错误提示 |
| `customErrorMessage` | 自定义错误提示文案 |

这些字段会在响应拦截器阶段决定最终提示策略。

## 响应阶段实现链路

## 1. 统一返回模型

`http.ts` 约定后端统一返回结构为：

```json
{
  "code": 200,
  "message": "保存成功",
  "data": {}
}
```

因此页面侧拿到的不是原始 AxiosResponse，而是处理后的 `data.data`。

## 2. 文件下载特殊处理

当 `responseType === 'blob'` 时，`http.ts` 会绕过普通业务码处理逻辑，直接返回原始响应，用于文件下载。

这保证导出、模板下载、附件下载这类能力不会被统一返回解包逻辑破坏。

## 3. 业务失败处理

当后端返回 `code !== 200` 时：

1. 先提取后端消息 `message` 或 `msg`
2. 若配置了 `customErrorMessage`，优先用自定义错误文案
3. 若 `silentError !== true`，则统一弹错误提示
4. 返回 `Promise.reject`

这样页面层无需再重复写：

```ts
if (res.code !== 200) {
  message.error(res.message)
}
```

## 4. 登录失效与租户恢复

当前重新登录错误码定义为：

```ts
const reloadCodes = [602]
```

当命中登录失效错误码时，`http.ts` 会执行：

```text
判断是否有 account / tenantId
  -> 判断当前错误是否与租户选择失效相关
  -> 若满足条件，先用 rawHttp 调 /auth/choose-tenant 尝试恢复租户
  -> 恢复成功则自动重放原请求
  -> 恢复失败则弹出登录失效弹窗并跳登录页
```

这段逻辑是 Forgex 比较关键的一个体验优化点，避免用户因为租户态暂时丢失而被直接踢回登录页。

## 5. 成功消息抑制机制

为了避免“后端已经自动弹成功提示，页面又手动弹一次”出现双重提示，`http.ts` 做了消息抑制补丁：

- 缓存最近一次后端提示
- 在短时间窗口内抑制前端重复调用 `message.success` / `message.error`
- 对存量硬编码提示先走 `legacyI18n` 翻译兜底，再交给 Ant Design Vue 的消息组件显示

相关变量包括：

```ts
recentBackendToast
backendToastDepth
backendToastSuppressWindow
```

存量硬编码提示的兼容入口：

```text
Forgex_MOM/Forgex_Fronted/src/utils/legacyI18n.ts
Forgex_MOM/Forgex_Fronted/src/locales/legacyText.ts
```

这层只用于历史页面过渡。新增页面仍应优先使用 `useI18n().t(...)` 或后端统一返回消息，避免继续新增散落的中文提示。

## 6. 成功消息判定

`http.ts` 并不是所有成功请求都自动提示，而是按路径关键字和配置综合判断。

核心判定函数：

```ts
shouldShowSuccessByPath(url)
shouldShowSuccessMessage(resp, httpInstance, backendMessage)
```

典型规则：

- `/create`、`/save`、`/update`、`/delete`
  通常显示成功消息
- `/page`、`/list`、`/detail`、`/download`
  通常不显示成功消息

这样能减少列表、详情类接口的无意义提示，又能保证写操作有明确反馈。

## 页面调用链路

业务页面的标准链路通常如下：

```text
页面组件
  -> 调用 src/api/system/*.ts / src/api/workflow/*.ts
  -> 业务 API 文件内部调用 http / httpSuccess / silentHttp
  -> http.ts 注入公共请求头、loading、统一提示
  -> 返回 data 给页面
```

例如集成平台 API 文件：

```text
Forgex_MOM/Forgex_Fronted/src/api/system/integration.ts
```

例如动态表格配置 API 文件：

```text
Forgex_MOM/Forgex_Fronted/src/api/system/tableConfig.ts
```

## 为什么这样设计

### 1. 页面减负

页面只写业务参数和业务结果，不写一堆重复样板：

- loading
- message.success
- message.error
- 登录失效处理
- 语言头注入

### 2. 前后端统一提示口径

后端统一返回 `message` 后，前端能直接托管提示，减少一套页面一套提示文案的问题。

### 3. 国际化链路统一

前端统一传 `X-Lang`，后端统一返回多语言消息，整个提示链路才稳定。

### 4. 多租户与会话恢复更集中

租户恢复逻辑统一放在 HTTP 层，而不是每个页面自己判断。

## 常见排查

| 现象 | 优先检查 |
|---|---|
| 页面请求成功但不弹提示 | 是否用了 `silentHttp`，或接口路径被判定为读操作 |
| 页面重复弹两次成功 | 页面里是否又手写了 `message.success` |
| 提示语言不对 | `X-Lang`、`Accept-Language` 是否正确注入 |
| 请求报未登录 | 是否命中 `602`，租户恢复是否执行成功 |
| 下载接口异常 | `responseType='blob'` 是否正确设置 |

## 关联文档

- [HTTP 请求使用方式](./HTTP请求使用方式.md)
- [公共弹窗实现逻辑](./公共弹窗实现逻辑.md)
- [统一返回与国际化实现逻辑](../../后端/配置与审计/统一返回与国际化实现逻辑.md)
