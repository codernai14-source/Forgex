# Feign API 配置指南

## 1. 项目配置

### 1.1 依赖配置

Common 模块的 `pom.xml` 已添加以下依赖：

```xml
<!-- Spring Cloud OpenFeign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<!-- Feign 负载均衡 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

### 1.2 服务配置

在各个微服务模块（如 Sys、Auth）的 `application.yml` 中配置服务名称：

```yaml
spring:
  application:
    name: forgex-sys  # 或 forgex-auth
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
```

### 1.3 Feign 配置（可选）

如需自定义 Feign 配置，可在 `application.yml` 中添加：

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true
```

## 2. 使用步骤

### 2.1 在 Sys 模块添加 Feign 接口实现

已在 `UserController` 中添加以下接口：

```java
// 根据用户ID获取用户信息
@GetMapping("/info/{userId}")
public R<UserInfoDTO> getUserById(@PathVariable Long userId)

// 根据账号获取用户信息
@GetMapping("/info/account/{account}")
public R<UserInfoDTO> getUserByAccount(@PathVariable String account)

// 批量获取用户信息
@PostMapping("/info/batch")
public R<List<UserInfoDTO>> getUsersByIds(@RequestBody List<Long> userIds)

// 批量获取用户名映射
@PostMapping("/info/username-map")
public R<Map<Long, String>> getUsernameMap(@RequestBody List<Long> userIds)
```

### 2.2 在其他模块使用

#### 方式1：使用 UserInfoService（推荐）

```java
@Autowired
private UserInfoService userInfoService;

// 查询用户信息
UserInfoDTO userInfo = userInfoService.getUserById(userId);

// 查询用户名
String username = userInfoService.getUsernameById(userId);

// 批量查询
Map<Long, String> usernameMap = userInfoService.getUsernameMap(userIds);
```

#### 方式2：使用 @AutoFillUsername 注解（最简单）

```java
@Data
public class YourVO {
    private Long userId;
    
    @AutoFillUsername(userIdField = "userId")
    private String username;
}

// 在 Controller 中返回，用户名会自动填充
@GetMapping("/data")
public R<YourVO> getData() {
    YourVO vo = new YourVO();
    vo.setUserId(1L);
    return R.ok(vo);  // username 会自动填充
}
```

#### 方式3：直接使用 Feign 客户端

```java
@Autowired
private SysUserFeignClient sysUserFeignClient;

R<UserInfoDTO> response = sysUserFeignClient.getUserById(userId);
if (response.getCode() == 200) {
    UserInfoDTO userInfo = response.getData();
}
```

## 3. 注意事项

### 3.1 服务名称一致性

确保 Feign 客户端中的服务名称与 Nacos 注册的服务名称一致：

```java
@FeignClient(name = "forgex-sys")  // 必须与 spring.application.name 一致
```

### 3.2 接口路径一致性

Feign 接口的路径必须与实际 Controller 的路径完全一致：

```java
// Feign 客户端
@FeignClient(name = "forgex-sys", path = "/sys/user")
@GetMapping("/info/{userId}")

// 对应的 Controller
@RequestMapping("/sys/user")
@GetMapping("/info/{userId}")
```

### 3.3 租户隔离

如果使用了租户隔离，需要在 Feign 请求中传递租户信息。可以通过 Feign 拦截器实现：

```java
@Component
public class FeignTenantInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            template.header("X-Tenant-Id", String.valueOf(tenantId));
        }
    }
}
```

### 3.4 异常处理

Feign 调用失败时，`UserInfoService` 会记录日志并返回 null 或空集合，不会抛出异常。如需自定义异常处理，可以实现 Feign 的 ErrorDecoder。

### 3.5 性能优化

- 自动填充用户名使用批量查询，一次性查询所有需要的用户信息
- 建议在用户表上为 ID 和 account 字段建立索引
- 可以考虑添加 Redis 缓存减少数据库查询

## 4. 测试

### 4.1 测试 Feign 接口

```bash
# 测试根据用户ID获取用户信息
curl http://localhost:8080/sys/user/info/1

# 测试根据账号获取用户信息
curl http://localhost:8080/sys/user/info/account/admin

# 测试批量获取用户名映射
curl -X POST http://localhost:8080/sys/user/info/username-map \
  -H "Content-Type: application/json" \
  -d "[1,2,3]"
```

### 4.2 测试自动填充

```bash
# 访问带有 @AutoFillUsername 注解的接口
curl http://localhost:8080/example/message/1

# 返回结果中 senderName、receiverName、createByName 会自动填充
```

## 5. 故障排查

### 5.1 Feign 调用失败

**问题**：调用 Feign 接口时报错 "Service not found"

**解决方案**：
1. 检查服务是否已注册到 Nacos
2. 检查服务名称是否一致
3. 检查网络连接是否正常

### 5.2 用户名未自动填充

**问题**：返回的对象中用户名字段为空

**解决方案**：
1. 检查是否添加了 `@AutoFillUsername` 注解
2. 检查 `userIdField` 参数是否正确
3. 检查用户ID是否存在
4. 查看日志中是否有异常信息

### 5.3 循环依赖问题

**问题**：启动时报循环依赖错误

**解决方案**：
- Common 模块只通过 Feign 调用其他模块，不直接依赖
- 其他模块依赖 Common 模块，但不要在 Common 模块中依赖其他模块

## 6. 扩展

### 6.1 添加新的 Feign 客户端

1. 在 `com.forgex.common.api.feign` 包下创建新的 Feign 接口
2. 使用 `@FeignClient` 注解指定服务名称
3. 定义需要调用的接口方法

### 6.2 添加新的自动填充注解

参考 `@AutoFillUsername` 的实现，可以创建类似的注解，如：
- `@AutoFillDepartmentName` - 自动填充部门名称
- `@AutoFillRoleName` - 自动填充角色名称
- `@AutoFillDictLabel` - 自动填充字典标签

### 6.3 添加缓存

可以在 `UserInfoService` 中添加 Spring Cache 注解：

```java
@Cacheable(value = "userInfo", key = "#userId")
public UserInfoDTO getUserById(Long userId) {
    // ...
}
```

## 7. 最佳实践

1. **优先使用 @AutoFillUsername 注解**：最简单、最优雅的方式
2. **批量查询优于单个查询**：减少网络调用次数
3. **添加缓存**：对于频繁查询的用户信息，建议添加缓存
4. **异常处理**：合理处理 Feign 调用失败的情况
5. **日志记录**：记录 Feign 调用的关键信息，便于排查问题
6. **超时配置**：根据实际情况配置合理的超时时间
7. **服务降级**：可以考虑实现 Feign 的 Fallback 机制

