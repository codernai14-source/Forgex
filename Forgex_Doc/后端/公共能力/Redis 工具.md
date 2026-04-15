# Redis 工具

> 分类：后端 / 公共能力  
> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

## 一、概述

Forgex 在 `Forgex_Common` 模块中提供了统一的 Redis 工具类 `RedisHelper`，封装了常用的 Redis 操作，包括字符串、Hash、List、Set、ZSet 等数据结构，以及分布式锁、缓存注解等高级功能。

## 二、核心组件

| 组件 | 包路径 | 作用 |
|---|---|---|
| `RedisHelper` | `com.forgex.common.util` | Redis 统一操作工具类 |
| `RedisConfig` | `com.forgex.common.config` | Redis 配置类 |
| `CacheAspect` | `com.forgex.common.cache` | 缓存注解切面 |
| `CacheEvictAspect` | `com.forgex.common.cache` | 缓存清除注解切面 |

## 三、RedisHelper 常用方法

### 3.1 字符串操作

```java
// 设置字符串
RedisHelper.set(String key, String value)
RedisHelper.set(String key, String value, long timeout, TimeUnit unit)  // 带过期时间

// 获取字符串
String RedisHelper.get(String key)

// 删除
Boolean RedisHelper.delete(String key)

// 判断是否存在
Boolean RedisHelper.hasKey(String key)

// 设置过期时间
Boolean RedisHelper.expire(String key, long timeout, TimeUnit unit)
```

### 3.2 Hash 操作

```java
// 设置 Hash 字段
void RedisHelper.hPut(String key, String hashKey, Object value)
void RedisHelper.hPutAll(String key, Map<String, Object> map)

// 获取 Hash 字段
Object RedisHelper.hGet(String key, String hashKey)
Map<Object, Object> RedisHelper.hEntries(String key)

// 删除 Hash 字段
Long RedisHelper.hDelete(String key, Object... hashKeys)

// 判断 Hash 字段是否存在
Boolean RedisHelper.hHasKey(String key, String hashKey)
```

### 3.3 List 操作

```java
// 左侧推入
Long RedisHelper.lLeftPush(String key, Object value)

// 右侧推入
Long RedisHelper.lRightPush(String key, Object value)

// 获取列表
List<Object> RedisHelper.lRange(String key, long start, long end)

// 获取列表长度
Long RedisHelper.lSize(String key)

// 删除元素
Long RedisHelper.lRemove(String key, long count, Object value)
```

### 3.4 Set 操作

```java
// 添加元素
Long RedisHelper.sAdd(String key, Object value)
Long RedisHelper.sAdd(String key, Object... values)

// 获取集合
Set<Object> RedisHelper.sMembers(String key)

// 判断是否包含
Boolean RedisHelper.sIsMember(String key, Object value)

// 删除元素
Long RedisHelper.sRemove(String key, Object... values)
```

### 3.5 ZSet 操作

```java
// 添加元素（带分数）
Boolean RedisHelper.zAdd(String key, Object value, double score)

// 获取指定排名范围的元素
Set<Object> RedisHelper.zRange(String key, long start, long end)

// 获取指定分数范围的元素
Set<Object> RedisHelper.zRangeByScore(String key, double min, double max)

// 获取元素的分数
Double RedisHelper.zScore(String key, Object value)

// 删除元素
Long RedisHelper.zRemove(String key, Object... values)
```

## 四、分布式锁

### 4.1 使用方式

```java
// 尝试获取锁
Boolean RedisHelper.tryLock(String key, long timeout, TimeUnit unit)

// 释放锁
void RedisHelper.unlock(String key)

// 带超时时间的锁
Boolean RedisHelper.tryLock(String key, long waitTime, long leaseTime, TimeUnit unit)
```

### 4.2 使用示例

```java
@Resource
private RedisHelper redisHelper;

public void doWithLock(String userId) {
    String lockKey = "lock:user:" + userId;
    boolean locked = false;
    try {
        locked = redisHelper.tryLock(lockKey, 10, TimeUnit.SECONDS);
        if (locked) {
            // 执行临界区代码
            // ...
        }
    } finally {
        if (locked) {
            redisHelper.unlock(lockKey);
        }
    }
}
```

## 五、缓存注解

### 5.1 @Cacheable 注解

自动缓存方法返回值：

```java
@Cacheable(value = "user", key = "#id", expire = 3600)
public SysUser getUserById(Long id) {
    return userMapper.selectById(id);
}
```

| 属性 | 说明 |
|---|---|
| `value` | 缓存名称（作为 key 的前缀） |
| `key` | SpEL 表达式，生成缓存 key |
| `expire` | 过期时间（秒） |
| `condition` | 条件表达式，满足条件才缓存 |

### 5.2 @CacheEvict 注解

清除缓存：

```java
@CacheEvict(value = "user", key = "#id")
public void updateUser(SysUser user) {
    userMapper.updateById(user);
}
```

| 属性 | 说明 |
|---|---|
| `value` | 缓存名称 |
| `key` | SpEL 表达式，生成缓存 key |
| `allEntries` | 是否清除所有条目 |
| `beforeInvocation` | 是否在方法执行前清除 |

## 六、Key 命名规范

### 6.1 命名格式

```
{业务模块}:{数据类型}:{唯一标识}
```

### 6.2 示例

| 用途 | Key 示例 |
|---|---|
| 用户信息 | `user:info:123456` |
| 用户 Token | `user:token:abc123` |
| 验证码 | `captcha:image:uuid123` |
| 登录失败次数 | `login:fail:count:admin` |
| 字典数据 | `dict:data:sys_status` |
| 配置信息 | `config:system:basic` |
| 分布式锁 | `lock:user:123456` |
| 导出文件 | `export:file:uuid123` |

## 七、常见使用场景

### 7.1 验证码存储

```java
// 生成验证码
String captchaId = UUID.randomUUID().toString();
String captchaCode = "ABCD";

// 存储到 Redis，5 分钟过期
redisHelper.set("captcha:image:" + captchaId, captchaCode, 5, TimeUnit.MINUTES);

// 校验验证码
String storedCode = redisHelper.get("captcha:image:" + captchaId);
if (!captchaCode.equals(storedCode)) {
    throw new BusinessException("验证码错误");
}
```

### 7.2 登录失败次数限制

```java
String failKey = "login:fail:count:" + account;
Integer failCount = redisHelper.get(failKey);

if (failCount != null && failCount >= maxFailCount) {
    throw new BusinessException("账号已被锁定，请稍后再试");
}

// 登录失败，累加失败次数
redisHelper.set(failKey, failCount == null ? 1 : failCount + 1, 30, TimeUnit.MINUTES);
```

### 7.3 Token 存储

```java
// 登录成功后存储 Token
String tokenKey = "user:token:" + token;
UserDTO userDTO = convertToDTO(user);
redisHelper.set(tokenKey, JSON.toJSONString(userDTO), 2, TimeUnit.HOURS);

// 登出时删除 Token
redisHelper.delete("user:token:" + token);
```

### 7.4 字典缓存

```java
// 获取字典数据（先从缓存读取）
String dictKey = "dict:data:" + dictCode;
String dictJson = redisHelper.get(dictKey);

if (dictJson == null) {
    // 缓存未命中，从数据库查询
    List<SysDict> dictList = dictMapper.selectByCode(dictCode);
    dictJson = JSON.toJSONString(dictList);
    redisHelper.set(dictKey, dictJson, 24, TimeUnit.HOURS);
}

return JSON.parseArray(dictJson, SysDict.class);
```

### 7.5 防止重复提交

```java
String submitKey = "submit:order:" + orderId + ":" + userId;
Boolean added = redisHelper.setIfAbsent(submitKey, "1", 5, TimeUnit.SECONDS);

if (Boolean.FALSE.equals(added)) {
    throw new BusinessException("请勿重复提交");
}

try {
    // 处理订单逻辑
    // ...
} finally {
    // 处理完成后删除（或者等待自动过期）
    redisHelper.delete(submitKey);
}
```

## 八、配置方式

### 8.1 application.yml 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000ms
```

### 8.2 Nacos 配置

```yaml
# redis.yml
spring:
  redis:
    host: ${redis.host:localhost}
    port: ${redis.port:6379}
    password: ${redis.password:}
    database: ${redis.database:0}
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

## 九、最佳实践

1. **设置合理的过期时间**：避免缓存永不过期导致内存泄漏
2. **使用统一的 Key 前缀**：便于管理和清理
3. **避免缓存大对象**：单个 key 的 value 建议不超过 10KB
4. **分布式锁必须释放**：使用 try-finally 确保锁被释放
5. **缓存穿透防护**：对不存在的数据也缓存空值
6. **缓存雪崩防护**：设置不同的过期时间，避免同时失效
7. **热点数据永驻内存**：使用 `@PostConstruct` 预热缓存

## 十、关联文档

- [后端公共能力与核心功能手册](../后端公共能力与核心功能手册.md)
- [数据字典与日志](../配置与审计/数据字典与日志.md)
- [认证授权](../身份与权限/认证授权.md)
