# Redis 工具使用方式

> 分类：后端 / 公共能力
> 版本：**V0.6.5**
> 更新时间：**2026-04-22**

本文重点讲"代码如何正确使用 RedisHelper"、"如何使用 Redisson 分布式能力"和"如何利用字典二级缓存"。

## 一、代码如何使用 RedisHelper

### 1.1 注入方式

```java
@Autowired
private RedisHelper redisHelper;
```

或使用构造注入：

```java
@RequiredArgsConstructor
public class MyService {
    private final RedisHelper redisHelper;
}
```

### 1.2 字符串操作

**写入字符串：**

```java
// 永久存储
redisHelper.setString("user:123", "张三", null);

// 设置过期时间（2小时）
redisHelper.setString("user:123", "张三", Duration.ofHours(2));

// 设置过期时间（24小时）
redisHelper.setString("config:theme", "dark", Duration.ofDays(1));
```

**读取字符串：**

```java
String name = redisHelper.getString("user:123");
if (name == null) {
    // 缓存不存在，从数据库加载
}
```

### 1.3 JSON 对象操作

**写入对象：**

```java
User user = new User();
user.setId(456L);
user.setName("李四");
redisHelper.setJson("user:456", user, Duration.ofDays(1));
```

**读取对象：**

```java
User cachedUser = redisHelper.getJson("user:456", User.class);
if (cachedUser == null) {
    // 缓存不存在或反序列化失败
    cachedUser = userMapper.selectById(456L);
    redisHelper.setJson("user:456", cachedUser, Duration.ofDays(1));
}
```

### 1.4 删除与存在检查

```java
// 删除 key
Boolean deleted = redisHelper.delete("user:123");
if (deleted) {
    log.info("缓存已清除");
}

// 判断 key 是否存在
Boolean exists = redisHelper.exists("user:123");
if (exists) {
    String value = redisHelper.getString("user:123");
}
```

### 1.5 缓存穿透处理

推荐使用"缓存不存在时返回 null"特性：

```java
public User getUserById(Long id) {
    String key = "user:" + id;
    User cached = redisHelper.getJson(key, User.class);
    if (cached != null) {
        return cached;
    }

    // 缓存不存在，查数据库
    User user = userMapper.selectById(id);
    if (user == null) {
        // 数据库也没有，缓存空值防止穿透
        redisHelper.setString(key, "NULL", Duration.ofMinutes(10));
        return null;
    }

    // 缓存有效数据
    redisHelper.setJson(key, user, Duration.ofDays(1));
    return user;
}
```

## 二、代码如何使用 Redisson

### 2.1 注入 RedissonClient

```java
@Autowired
private RedissonClient redissonClient;
```

### 2.2 原子计数器

适用于登录失败计数、编码规则序列号等场景：

```java
// 获取原子计数器
RAtomicLong counter = redissonClient.getAtomicLong("counter:order:seq");

// 原子递增
long nextSeq = counter.incrementAndGet();

// 设置过期时间
counter.expire(Duration.ofDays(30));

// 删除计数器
counter.delete();
```

### 2.3 分布式状态存储

适用于账号锁定状态等场景：

```java
// 设置锁定状态
RBucket<String> lockBucket = redissonClient.getBucket("lock:user:" + userId);
lockBucket.set("LOCKED", Duration.ofMinutes(30));

// 检查锁定状态
boolean isLocked = lockBucket.isExists();

// 删除锁定状态
lockBucket.delete();

// 获取剩余锁定时间
long ttlMillis = lockBucket.remainTimeToLive();
if (ttlMillis > 0) {
    long ttlSeconds = (ttlMillis + 999) / 1000;
    log.info("账号将在 {} 秒后解锁", ttlSeconds);
}
```

### 2.4 分布式锁（示例扩展）

如果需要分布式锁，可以参考以下用法：

```java
RLock lock = redissonClient.getLock("lock:order:create:" + orderId);
try {
    // 尝试获取锁，等待 5 秒，锁自动释放时间 30 秒
    boolean acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);
    if (acquired) {
        // 执行业务逻辑
        processOrder(orderId);
    } else {
        log.warn("获取分布式锁失败");
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

## 三、Key 命名规范

### 3.1 推荐格式

```text
模块:业务:标识
```

示例：

| 场景 | Key 格式 |
|---|---|
| 用户缓存 | `user:{userId}` |
| 字典缓存 | `dict:{tenantId}:{lang}:{type}:{code}` |
| 登录状态 | `fx:auth:login:lock:{account}` |
| 编码序列 | `encode:serial:{ruleCode}` |

### 3.2 避免的命名方式

- 不要使用纯数字：`123456`
- 不要使用中文：`用户:张三`
- 不要过长：`very:long:key:name:that:is:hard:to:read`

## 四、TTL 设置建议

| 数据类型 | 推荐 TTL | 原因 |
|---|---|---|
| Session 数据 | 按配置（通常 30min） | 安全策略要求 |
| 字典缓存 | 24h | 数据变化频率低 |
| 验证码 | 120s | 短时效安全要求 |
| 用户偏好 | 30d | 长期保存用户设置 |
| 临时数据 | 10min | 短时效业务数据 |

## 五、常见使用场景

### 5.1 登录失败计数

```java
public void recordLoginFailure(String account, LoginSecurityConfig config) {
    String failKey = "fx:auth:login:fail:" + account.toLowerCase();
    RAtomicLong failCounter = redissonClient.getAtomicLong(failKey);

    long currentCount = failCounter.incrementAndGet();
    failCounter.expire(Duration.ofMinutes(config.getFailWindowMinutes()));

    if (currentCount >= config.getMaxFailCount()) {
        // 达到阈值，锁定账号
        String lockKey = "fx:auth:login:lock:" + account.toLowerCase();
        redissonClient.getBucket(lockKey).set("1", Duration.ofMinutes(config.getLockMinutes()));
        failCounter.delete();
    }
}
```

### 5.2 验证码缓存

```java
// 存储验证码答案
redisHelper.setString("captcha:" + captchaId, answer, Duration.ofSeconds(120));

// 验证验证码
String cachedAnswer = redisHelper.getString("captcha:" + captchaId);
if (cachedAnswer == null) {
    throw new I18nBusinessException(CommonPrompt.CAPTCHA_EXPIRED);
}
if (!cachedAnswer.equals(userAnswer)) {
    throw new I18nBusinessException(CommonPrompt.CAPTCHA_ERROR);
}
// 验证成功后删除
redisHelper.delete("captcha:" + captchaId);
```

### 5.3 字典缓存查询

字典服务已内置二级缓存，业务代码直接调用即可：

```java
@Autowired
private DictI18nResolver dictI18nResolver;

// 获取字典标签
String label = dictI18nResolver.getLabel("status", "active");

// 获取字典项（包含样式）
DictItem item = dictI18nResolver.getDictItem("status", "active");
```

## 六、常见问题排查

| 现象 | 可能原因 | 解决方式 |
|---|---|---|
| setString 后 getString 返回 null | Key 为空、Redis 连接断开 | 检查 Key、检查 Redis 服务 |
| getJson 返回 null | 反序列化失败、类型不匹配 | 检查对象是否可序列化、Class 类型 |
| 缓存数据与数据库不一致 | 缓存未及时更新 | 写操作后主动 delete |
| 分布式锁获取失败 | 锁被其他线程持有 | 检查锁超时时间、是否有死锁 |
| 原子计数跳跃 | 并发请求、网络抖动 | 检查 Redisson 连接状态 |

## 七、推荐接入步骤

### 新增一个缓存场景

1. 确定 Key 格式和 TTL
2. 选择使用 RedisHelper 还是 Redisson
3. 写入时考虑缓存失效策略
4. 读取时处理缓存不存在的情况
5. 测试缓存穿透、缓存雪崩场景

### 新增一个分布式锁场景

1. 确定 Key 格式（锁的业务标识）
2. 设置合理的等待时间和锁持有时间
3. 在 finally 中释放锁
4. 测试锁超时、锁竞争场景

## 关联文档

- [Redis工具实现逻辑](./Redis工具实现逻辑.md)
- [数据字典与日志使用方式](../配置与审计/数据字典与日志使用方式.md)
- [认证授权使用方式](../身份与权限/认证授权使用方式.md)