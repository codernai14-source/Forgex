# DataStore 会话持久化

> 分类：安卓端 / 会话
> 版本：V1.0.0
> 关联模块：`core/datastore`

本文只说明当前 Android `SessionStore` 的 Preferences DataStore 读写。源码没有实现旧文档中描述的刷新令牌、用户资料字段或加密存储，因此这些内容不作为现状使用方式。

## 1. 存储实现

`SessionStore` 使用 `PreferenceDataStoreFactory.create` 创建单例 DataStore，文件名由源码常量确定：

```kotlin
private const val DATASTORE_NAME = "forgex_mobile_session.preferences_pb"

private val dataStore = PreferenceDataStoreFactory.create(
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
)
```

读取属性都是 `Flow`，写入方法都是 `suspend`。DataStore 读异常中的 `IOException` 会回退为空偏好，其它异常继续抛出。

## 2. 当前字段

以下字段和类型均来自 `SessionStore.kt`：

| Flow 属性 | 存储类型 | 用途 |
| --- | --- | --- |
| `token` | String? | 请求认证 Token |
| `tenantId` | String? | 当前租户 ID |
| `account` | String? | 登录账号 |
| `systemName` | String? | 当前系统名称 |
| `serverHost` | String? | 自定义服务主机 |
| `serverPort` | Int? | 自定义服务端口 |
| `serverScheme` | String? | 自定义协议，默认 `http` |
| `serverEndpoint` | `ServerEndpointConfig?` | host、port、scheme 组合配置 |
| `languageMode` | `LanguageMode` | 跟随系统或手动选择 |
| `languageTag` | String? | 手动语言 |
| `lastResolvedLanguageTag` | String? | 最近一次解析出的语言 |
| `i18nBundleJson` | String? | 已缓存的国际化 JSON |
| `i18nBundleLanguageTag` | String? | 国际化 JSON 对应语言 |

源码没有 `refresh_token`、`token_expire_at`、`tenant_code`、`user_id`、`user_name` 或 `user_avatar` 字段。

## 3. 保存和读取会话

登录成功后使用 `saveSession` 保存账号、租户和可选 Token。Token 为空时会主动移除旧 Token，避免沿用上一次登录状态。

```kotlin
class SessionRepository @Inject constructor(
    private val sessionStore: SessionStore,
) {
    suspend fun onLogin(account: String, tenantId: String, token: String?) {
        sessionStore.saveSession(account, tenantId, token)
    }

    val token: Flow<String?> = sessionStore.token
    val tenantId: Flow<String?> = sessionStore.tenantId
}
```

其它常用写操作：

```kotlin
suspend fun configure(sessionStore: SessionStore) {
    sessionStore.saveTenantId("tenant-001")
    sessionStore.saveSystemName("Forgex")
    sessionStore.saveServerEndpoint("10.0.2.2", 9000, "http")
    sessionStore.saveLanguageSelection(LanguageMode.MANUAL, "zh-CN")
    sessionStore.saveLastResolvedLanguageTag("zh-CN")
    sessionStore.saveI18nBundle("zh-CN", "{\"login.title\":\"登录\"}")
}
```

语言标签会经过 `AppLanguage.normalize` 规范化。`serverEndpoint` 在 host 为空时发射 `null`，否则以端口 `9000` 和协议 `http` 作为默认值。

## 4. 清理操作

`clearSession` 只移除 Token、租户 ID 和账号；服务器地址、系统名称、语言和国际化缓存不会被它删除。退出登录或切换服务器时应按实际意图调用对应方法：

```kotlin
suspend fun logout(sessionStore: SessionStore) {
    sessionStore.clearSession()
}

suspend fun resetServer(sessionStore: SessionStore) {
    sessionStore.clearServerEndpoint()
    sessionStore.clearI18nBundle()
}
```

当前 `SessionStore` 没有全量 `clear()` 方法，也没有 Token 刷新方法；调用方不能引用这些不存在的 API。

## 5. 网络层的读取方式

`AuthInterceptor` 和 `TenantInterceptor` 使用 `runBlocking { flow.first() }` 在 OkHttp 拦截器中读取 Token 和租户 ID；普通业务代码应收集 Flow，不应复制 DataStore 文件或直接操作 Preferences key。

```kotlin
val account: Flow<String?> = sessionStore.account
```

ViewModel 如需转换为状态，可在自己的 `viewModelScope` 中调用 `stateIn`，并提供合适的启动策略和初始值。

## 6. 安全现状与后续补充

当前源码直接把 Token 等值写入 Preferences DataStore，没有 Android Keystore、AES-GCM、`MasterKey` 或 `EncryptedSharedPreferences` 加密实现；日志脱敏和备份策略也未在 `SessionStore` 中实现。不能把会话存储描述为“已加密”。

建议后续按以下顺序补充：

1. 明确威胁模型和设备备份要求。
2. 设计 Keystore 管理密钥及加密字段格式，并添加迁移策略。
3. 在 `SessionStore` 增加加密读写和异常恢复测试。
4. 同步检查 `HttpLoggingInterceptor` 的发布构建级别，确保 Token 不进入日志。

## 7. 使用约定

1. feature 模块只依赖 `SessionStore` 的公开 Flow 和 suspend 方法。
2. 所有写操作在协程中执行，禁止主线程同步写文件。
3. 新增字段时同时增加 Preferences key、Flow、保存方法和清理策略。
4. 新增字段或安全实现后，必须以实际源码更新本文及网络层文档。
