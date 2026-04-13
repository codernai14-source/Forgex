# Redis 工具

> 分类：后端 / 公共能力  
> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

## 一、概述

Forgex 提供 `RedisHelper` 工具类作为 Redis 操作的统一入口，基于 `StringRedisTemplate` 封装，统一使用字符串 Key，值支持字符串或 JSON 序列化对象。

## 二、核心类

**类路径：** `com.forgex.common.util.RedisHelper`

**注入方式：** Spring `@Component`，直接 `@Autowired` 或构造注入使用。

```java
@Autowired
private RedisHelper redisHelper;
```

## 三、API 说明

### 3.1 字符串操作

```java
/**
 * 写入字符串值
 * @param key   Redis 键
 * @param value 字符串值
 * @param ttl   过期时间，null 表示永久
 */
redisHelper.setString("user:123", "张三", Duration.ofHours(2));

/**
 * 获取字符串值
 * @param key Redis 键
 * @return 值，不存在返回 null
 */
String name = redisHelper.getString("user:123");
```

### 3.2 JSON 对象操作

```java
/**
 * 写入对象（自动 JSON 序列化）
 * @param key   Redis 键
 * @param value 任意对象
 * @param ttl   过期时间
 */
User user = new User();
user.setName("李四");
redisHelper.setJson("user:456", user, Duration.ofDays(1));

/**
 * 获取对象（自动 JSON 反序列化）
 * @param key   Redis 键
 * @param clazz 目标类型
 * @return 对象实例，不存在或反序列化失败返回 null
 */
User cachedUser = redisHelper.getJson("user:456", User.class);
```

### 3.3 删除与存在检查

```java
/**
 * 删除 key
 * @return 是否删除成功
 */
Boolean deleted = redisHelper.delete("user:123");

/**
 * 判断 key 是否存在
 * @return 是否存在
 */
Boolean exists = redisHelper.exists("user:123");
```

## 四、方法一览

| 方法 | 参数 | 返回 | 说明 |
|---|---|---|---|
| `setString(key, value, ttl)` | key, value, Duration(可 null) | void | 写入字符串，ttl 为 null 永久存储 |
| `getString(key)` | key | String | 获取字符串，不存在返回 null |
| `setJson(key, value, ttl)` | key, Object, Duration(可 null) | void | 对象序列化后写入 |
| `getJson(key, clazz)` | key, Class | T | 反序列化为对象，失败返回 null |
| `delete(key)` | key | Boolean | 删除 key |
| `exists(key)` | key | Boolean | 判断 key 是否存在 |

## 五、安全约定

- **key 为空时所有操作静默跳过**，不抛异常
- **JSON 序列化/反序列化失败静默返回 null**，不抛异常
- 使用 `ObjectMapper` 进行 JSON 处理，与 Spring 默认配置一致

## 六、项目中的 Redis 使用场景

| 场景 | Key 规则 | TTL | 说明 |
|---|---|---|---|
| 登录 Session | Sa-Token 管理 | 按配置 | Token → 用户信息映射 |
| 登录失败计数 | `login:fail:count:{account}` | 失败窗口时间 | 防暴力破解锁定 |
| 图片验证码 | `captcha:{captchaId}` | 120s | 验证码答案缓存 |
| 滑块验证码 Token | `slider:{token}` | 按配置 | 滑块验证结果 |
| 字典缓存 | `dict:{tenantId}:{lang}:{type}:{code}` | 24h | 字典项缓存 |
| 字典路径缓存 | `dict:{tenantId}:{lang}:path:{nodePath}` | 24h | 字典路径查询缓存 |
| 编码规则序列号 | `encode:serial:{ruleCode}` | 按规则 | 编码序列号原子递增 |
| 国际化消息缓存 | `i18nmsg:{module}:{code}:{lang}` | 24h | 网关层消息缓存 |
| 用户语言偏好 | `i18n:user:lang:{userId}:{tenantId}` | 30d | 用户语言设置 |

## 七、字典缓存二级架构

Forgex 字典服务使用 **本地 Caffeine + Redis** 二级缓存：

```
查询字典
  → 先查本地 Caffeine 缓存（30s TTL, 最大 10000 条）
  → 未命中则查 Redis 缓存（24h TTL）
  → 未命中则查数据库
  → 回写 Redis 和本地缓存

字典变更
  → 清除本地缓存
  → 发布 Redis Pub/Sub 消息 (dict:cache:invalidate)
  → 其他节点收到消息后清除本地缓存
```

配置类：`com.forgex.common.config.DictCacheConfig`

## 八、Redis 配置

Nacos 配置文件 `redis.yml`：

```yaml
spring:
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
```

## 九、最佳实践

1. **优先使用 `RedisHelper`** 而非直接操作 `StringRedisTemplate`
2. **Key 命名遵循 `模块:业务:标识` 格式**，如 `dict:1:zh-CN:type:status`
3. **设置合理的 TTL**，避免缓存永久占用内存
4. **缓存失效策略**：写操作后主动清除相关缓存
5. **不要在 Redis 中存储大对象**，单个 value 建议不超过 1MB

## 十、关联文档

- [数据字典与日志](../配置与审计/数据字典与日志.md)
- [认证授权](../身份与权限/认证授权.md)
- [后端公共能力与核心功能手册](../后端公共能力与核心功能手册.md)

