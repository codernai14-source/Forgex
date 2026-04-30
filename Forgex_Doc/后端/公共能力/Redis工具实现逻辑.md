# Redis 工具实现逻辑

> 分类：后端 / 公共能力  
> 版本：**V0.6.0**  
> 更新时间：**2026-04-22**

本文重点解释 Forgex 中 Redis 工具的实现逻辑，包括 `RedisHelper` 工具类、分布式锁机制、字典二级缓存架构、以及 Redis Pub/Sub 缓存失效机制。

## 代码入口

| 能力 | 代码位置 | 说明 |
|---|---|---|
| Redis 工具类 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/util/RedisHelper.java` | 基础字符串/JSON 操作封装 |
| Redisson 客户端 | `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/service/impl/AuthServiceImpl.java` | 分布式锁、原子计数器 |
| 字典缓存配置 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/config/DictCacheConfig.java` | Caffeine + Redis 二级缓存 |
| 缓存失效监听 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/dict/DictCacheInvalidationListener.java` | Redis Pub/Sub 消息监听 |
| 字典国际化解析 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/dict/DictI18nResolver.java` | 三级缓存查询链路 |

## 一、RedisHelper 工具类实现

### 1.1 核心依赖

`RedisHelper` 基于 `StringRedisTemplate` 和 `ObjectMapper` 封装：

```java
@Component
@RequiredArgsConstructor
public class RedisHelper {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
}
```

### 1.2 设计原则

| 设计点 | 实现方式 | 原因 |
|---|---|---|
| 统一字符串 Key | 使用 `StringRedisTemplate` | Key 可读性好，便于调试和跨语言兼容 |
| JSON 序列化 | 使用 Spring 默认 `ObjectMapper` | 与项目其他 JSON 处理保持一致 |
| 防空处理 | Key 为空时静默跳过 | 避免 Redis 操作异常影响主流程 |
| 异常容忍 | 序列化/反序列化失败返回 null | 缓存读取失败不应阻断业务 |

### 1.3 字符串操作实现

**写入逻辑：**

```text
setString(key, value, ttl)
  -> key 为空？直接返回
  -> ttl 为 null？调用 redisTemplate.opsForValue().set(key, value)
  -> ttl 不为 null？调用 redisTemplate.opsForValue().set(key, value, ttl)
```

**读取逻辑：**

```text
getString(key)
  -> key 为空？返回 null
  -> 调用 redisTemplate.opsForValue().get(key)
  -> 返回结果（不存在时返回 null）
```

### 1.4 JSON 对象操作实现

**写入逻辑：**

```text
setJson(key, value, ttl)
  -> key 或 value 为空？直接返回
  -> objectMapper.writeValueAsString(value) 序列化
  -> 序列化异常？静默跳过，不抛出
  -> 调用 setString(key, json, ttl)
```

**读取逻辑：**

```text
getJson(key, clazz)
  -> getString(key) 获取 JSON 字符串
  -> JSON 为空或 clazz 为 null？返回 null
  -> objectMapper.readValue(json, clazz) 反序列化
  -> 反序列化异常？返回 null
```

### 1.5 删除与存在检查

**删除逻辑：**

```text
delete(key)
  -> key 为空？返回 false
  -> redisTemplate.delete(key)
  -> 返回 Boolean（不存在时返回 false）
```

**存在检查逻辑：**

```text
exists(key)
  -> key 为空？返回 false
  -> redisTemplate.hasKey(key)
  -> 返回 Boolean
```

## 二、Redisson 分布式能力

### 2.1 Redisson 客户端引入

项目引入 Redisson 客户端用于高级分布式能力：

```java
@Autowired
private RedissonClient redissonClient;
```

### 2.2 原子计数器（登录失败计数）

**实现代码片段：**

```java
RAtomicLong failCounter = redissonClient.getAtomicLong(failKey);
long currentCount = failCounter.incrementAndGet();
failCounter.expire(Duration.ofMinutes(failWindowMinutes));
if (currentCount >= maxFailCount && lockMinutes > 0) {
    redissonClient.getBucket(lockKey).set("1", Duration.ofMinutes(lockMinutes));
    failCounter.delete();
}
```

**实现特点：**

| 特点 | 说明 |
|---|---|
| 原子递增 | `incrementAndGet()` 保证并发安全 |
| 自动过期 | `expire()` 设置窗口时间 |
| 联动锁定 | 达到阈值后设置锁定 Key 并清除计数 |

### 2.3 分布式锁使用场景

虽然 `RedisHelper` 未直接封装分布式锁方法，但项目在以下场景使用 Redisson 分布式锁：

| 场景 | 使用方式 | 说明 |
|---|---|
| 登录失败计数 | `RAtomicLong` | 原子计数，防并发问题 |
| 账号锁定状态 | `RBucket` | 分布式状态存储 |
| 编码规则序列号 | Redisson 原子操作 | 保证序列号唯一性 |

### 2.4 Key TTL 读取

```java
RBucket<Object> bucket = redissonClient.getBucket(key);
long ttlMillis = bucket.remainTimeToLive();
```

用于读取 Key 的剩余生存时间，例如账号锁定剩余时间。

## 三、字典二级缓存架构

### 3.1 三级缓存设计

```text
L1: 本地 Caffeine 缓存（30s TTL，最大 10000 条）
L2: Redis 缓存（24h TTL）
L3: 数据库
```

### 3.2 Caffeine 本地缓存配置

```java
@Bean("dictLabelCache")
public Cache<String, Map<String, String>> dictLabelCache() {
    return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats()
            .build();
}

@Bean("dictItemCache")
public Cache<String, Map<String, DictItem>> dictItemCache() {
    return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats()
            .build();
}
```

**配置说明：**

| 配置项 | 值 | 原因 |
|---|---|---|
| maximumSize | 10000 | 防止内存溢出，适应中小规模字典 |
| expireAfterWrite | 30s | 短过期保证数据新鲜度 |
| recordStats | 启用 | 监控命中率，便于调优 |

### 3.3 Redis 缓存 Key 规则

| 场景 | Key 格式 | TTL |
|---|---|---|
| 字典标签缓存 | `dict:{tenantId}:{lang}:{type}:{code}` | 24h |
| 字典路径缓存 | `dict:{tenantId}:{lang}:path:{nodePath}` | 24h |

### 3.4 查询链路

```text
查询字典标签
  -> 先查本地 Caffeine 缓存
  -> 未命中则查 Redis 缓存
  -> 未命中则查数据库
  -> 回写 Redis 缓存
  -> 回写本地缓存
  -> 返回结果
```

## 四、Redis Pub/Sub 缓存失效机制

### 4.1 设计目的

多节点部署时，本地缓存各自独立，需要通过 Redis Pub/Sub 机制保证缓存一致性：

```text
节点 A 更新字典
  -> 清除节点 A 本地缓存
  -> 发布 Redis 消息 (dict:cache:invalidate)
  -> 节点 B 收到消息
  -> 清除节点 B 本地缓存
```

### 4.2 消息监听配置

```java
@Bean
public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        DictCacheInvalidationListener listener) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listener, new PatternTopic("dict:cache:invalidate"));
    return container;
}
```

### 4.3 监听主题与消息格式

| 配置项 | 值 |
|---|---|
| 监听主题 | `dict:cache:invalidate` |
| 消息格式 | `tenantId:nodePath` |
| 处理逻辑 | 清除本地缓存和 Redis 缓存 |

## 五、项目中的 Redis 使用场景

### 5.1 认证与安全场景

| 场景 | Key 规则 | TTL | 数据结构 |
|---|---|---|---|
| 登录 Session | Sa-Token 管理 | 按配置 | String |
| 登录失败计数 | `fx:auth:login:fail:{account}` | 失败窗口时间 | RAtomicLong |
| 账号锁定状态 | `fx:auth:login:lock:{account}` | 锁定时间 | RBucket |
| 图片验证码 | `captcha:{captchaId}` | 120s | String |
| 滑块验证码 Token | `slider:{token}` | 按配置 | String |

### 5.2 业务缓存场景

| 场景 | Key 规则 | TTL | 数据结构 |
|---|---|---|---|
| 字典标签缓存 | `dict:{tenantId}:{lang}:{type}:{code}` | 24h | String (JSON) |
| 字典路径缓存 | `dict:{tenantId}:{lang}:path:{nodePath}` | 24h | String (JSON) |
| 国际化消息缓存 | `i18nmsg:{module}:{code}:{lang}` | 24h | String |
| 用户语言偏好 | `i18n:user:lang:{userId}:{tenantId}` | 30d | String |

### 5.3 编码规则场景

| 场景 | Key 规则 | TTL | 数据结构 |
|---|---|---|---|
| 编码序列号 | `encode:serial:{ruleCode}` | 按规则 | RAtomicLong |

## 六、Redis 配置与连接

### 6.1 Nacos 配置文件

```yaml
spring:
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
```

### 6.2 Redisson 配置

Redisson 客户端通过 Spring Boot Starter 自动配置，与 Redis 配置共用。

## 七、常见问题排查

### 7.1 RedisHelper 操作无效

| 现象 | 可能原因 | 排查方式 |
|---|---|---|
| setString 后 getString 返回 null | Key 格式问题、Redis 连接断开 | 检查 Key 是否为空、Redis 服务状态 |
| setJson 后 getJson 返回 null | 序列化失败、反序列化类型不匹配 | 检查对象是否可序列化、Class 类型是否正确 |

### 7.2 分布式计数不准确

| 现象 | 可能原因 | 排查方式 |
|---|---|---|
| 登录失败计数跳跃 | 并发请求、Redis 连接不稳定 | 检查 Redisson 连接、是否有网络抖动 |
| 计数器未过期 | expire 未调用 | 检查代码是否正确设置过期时间 |

### 7.3 字典缓存不一致

| 现象 | 可能原因 | 排查方式 |
|---|---|---|
| 节点 A 更新后节点 B 仍显示旧值 | Pub/Sub 消息未送达、监听器未启动 | 检查 Redis Pub/Sub 状态、监听器 Bean 是否加载 |
| 本地缓存命中率低 | 缓存容量不足、过期时间过短 | 检查 Caffeine 统计信息、调整缓存配置 |

## 八、扩展建议

### 8.1 RedisHelper 方法扩展

当前 `RedisHelper` 只提供基础能力，如需扩展可考虑：

| 方法 | 用途 | 实现方式 |
|---|---|---|
| `setex(key, value, seconds)` | 秒级过期设置 | Duration.ofSeconds(seconds) |
| `incr(key)` | 原子递增 | redisTemplate.opsForValue().increment(key) |
| `expire(key, ttl)` | 设置过期时间 | redisTemplate.expire(key, ttl) |
| `ttl(key)` | 获取剩余时间 | redisTemplate.getExpire(key) |

### 8.2 分布式锁封装

如需封装分布式锁，可参考：

```java
public boolean tryLock(String lockKey, Duration waitTime, Duration leaseTime) {
    RLock lock = redissonClient.getLock(lockKey);
    return lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
}

public void unlock(String lockKey) {
    RLock lock = redissonClient.getLock(lockKey);
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

## 关联文档

- [Redis 工具](./Redis工具.md)
- [Redis 工具使用方式](./Redis工具使用方式.md)（待补充）
- [数据字典与日志实现逻辑](../配置与审计/数据字典与日志实现逻辑.md)
- [认证授权实现逻辑](../身份与权限/认证授权实现逻辑.md)
- [后端公共能力与核心功能手册](../后端公共能力与核心功能手册.md)