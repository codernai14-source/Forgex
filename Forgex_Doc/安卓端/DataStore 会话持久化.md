# DataStore 会话持久化

> 分类：安卓端 / 专题
> 版本：V0.8.5
> 关联模块：`core/datastore`

本文说明 Forgex Android 端如何使用 Preferences DataStore 持久化会话信息（Token、租户、用户基本信息），包括存储结构、加密策略与生命周期管理。

---

## 一、选型说明

| 方案 | 是否采用 | 原因 |
|---|---|---|
| SharedPreferences | 否 | 已废弃，存在阻塞主线程与数据一致性风险 |
| Preferences DataStore | **是** | 协程安全、支持 Flow 读取、轻量，适合键值型会话数据 |
| Proto DataStore | 否 | 会话字段少且多变，Proto schema 维护成本高，暂不需要 |
| Room | 否 | 会话数据无需复杂查询与关系，DataStore 足够 |

会话数据特点：字段少、读取频繁（每次请求注入拦截器）、写入低频（登录/登出时），Preferences DataStore 是最佳选择。

## 二、存储结构

`core/datastore` 模块通过 `SessionDataStore` 封装会话读写，存储文件为 `session.preferences_pb`。

| Key | 类型 | 说明 |
|---|---|---|
| `token` | String | 访问令牌（Access Token），网络层注入 `Authorization` 头 |
| `refresh_token` | String | 刷新令牌，用于 401 时静默续期 |
| `token_expire_at` | Long | Token 过期时间戳（毫秒） |
| `tenant_code` | String | 当前租户编码，网络层注入 `X-Tenant-Code` 头 |
| `tenant_name` | String | 当前租户名称（展示用） |
| `user_id` | Long | 当前用户 ID |
| `user_name` | String | 当前用户名 |
| `user_avatar` | String | 用户头像 URL |

## 三、核心实现

### 3.1 DataStore 实例提供

通过 Hilt 提供 DataStore 单例，作用域为 `SingletonComponent`：

```kotlin
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session"
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSessionDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.sessionDataStore
}
```

### 3.2 SessionDataStore 封装

`SessionDataStore` 对外暴露 Flow 读取与 suspend 写入，内部使用类型化 Key 定义：

```kotlin
class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_TOKEN_EXPIRE_AT = longPreferencesKey("token_expire_at")
        private val KEY_TENANT_CODE = stringPreferencesKey("tenant_code")
        private val KEY_TENANT_NAME = stringPreferencesKey("tenant_name")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_AVATAR = stringPreferencesKey("user_avatar")
    }

    /** 读取当前会话快照，首次发射为 null（未登录）。 */
    val sessionFlow: Flow<Session?> = dataStore.data.map { prefs ->
        val token = prefs[KEY_TOKEN] ?: return@map null
        Session(
            token = token,
            refreshToken = prefs[KEY_REFRESH_TOKEN].orEmpty(),
            tokenExpireAt = prefs[KEY_TOKEN_EXPIRE_AT] ?: 0L,
            tenantCode = prefs[KEY_TENANT_CODE].orEmpty(),
            tenantName = prefs[KEY_TENANT_NAME].orEmpty(),
            userId = prefs[KEY_USER_ID] ?: 0L,
            userName = prefs[KEY_USER_NAME].orEmpty(),
            userAvatar = prefs[KEY_USER_AVATAR].orEmpty(),
        )
    }

    /** 保存登录结果（登录成功 / 刷新 Token 后调用）。 */
    suspend fun saveSession(session: Session) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_REFRESH_TOKEN] = session.refreshToken
            prefs[KEY_TOKEN_EXPIRE_AT] = session.tokenExpireAt
            prefs[KEY_TENANT_CODE] = session.tenantCode
            prefs[KEY_TENANT_NAME] = session.tenantName
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_USER_NAME] = session.userName
            prefs[KEY_USER_AVATAR] = session.userAvatar
        }
    }

    /** 清除会话（登出 / Token 失效时调用）。 */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    /** 同步阻塞读取 Token（仅限 OkHttp 拦截器调用，禁止在主线程使用）。 */
    fun readTokenBlocking(): String? = runBlocking {
        dataStore.data.firstOrNull()?.get(KEY_TOKEN)
    }
}
```

## 四、会话生命周期

```
登录成功
   │ saveSession()
   ▼
SessionDataStore 持久化 ──► sessionFlow 发射 Session
   │
   ▼
AuthInterceptor 读取 Token 注入请求头
   │
   ▼（401 响应）
TokenRefresher 用 refresh_token 续期
   │ 成功 → saveSession() 更新
   │ 失败 → clear() + 跳转登录
```

- 登录成功后调用 `saveSession()`，之后网络请求通过 `AuthInterceptor` 自动注入 Token。
- 401 时由 `TokenRefresher` 尝试用 `refresh_token` 续期，成功则更新会话并重放请求，失败则 `clear()` 并通过 `AuthEventBus` 通知跳转登录。
- 主动登出调用 `clear()`，会话 Flow 发射 null，`app` 层监听后重置导航栈到登录页。

## 五、加密与安全

### 5.1 存储加密

Token 等敏感字段不应明文落盘。Forgex 采用 Android Keystore + AES-GCM 对敏感字段加密后写入 DataStore：

- 使用 `EncryptedSharedPreferences` 思路不可行（已选 DataStore），改为在 `SessionDataStore` 写入前对 `token` / `refresh_token` 字段做对称加密。
- 加密密钥通过 `MasterKey`（Android Keystore 管理，AES-256-GCM）派生，密钥不出 Keystore。
- `readTokenBlocking()` 读取后自动解密，对上层透明。

### 5.2 安全注意事项

| 项 | 要求 |
|---|---|
| Token 明文 | 禁止出现在日志（`HttpLoggingInterceptor` 在 release 构建仅 BASIC 级别） |
| 备份 | `android:allowBackup="false"`，防止会话文件被 adb 备份导出 |
| 多用户 | DataStore 按 Application 沙箱隔离，设备多用户天然隔离 |
| Root 设备 | 无法完全防御，文档提示用户避免在 Root 设备使用企业账号 |

## 六、规范约定

1. **会话字段统一走 `SessionDataStore`**，禁止 feature 模块自己用 SharedPreferences / DataStore 存 Token。
2. **写操作必须 suspend**，禁止在主线程同步写 DataStore。
3. **拦截器读取用 `readTokenBlocking()`**，其余场景一律用 Flow 异步读取。
4. **新增会话字段**：先在 `Session` data class 与 Key 中补齐，再在 `saveSession` / `sessionFlow` 中处理，避免字段遗漏。
5. **登出必须 `clear()` 全量清除**，不要残留部分字段导致下次登录状态不一致。
