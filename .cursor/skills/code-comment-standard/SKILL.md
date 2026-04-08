---
name: code-comment-standard
description: Java 代码注释规范指导，包括类注释、方法注释、字段注释、行内注释等。当编写 Java 代码、添加注释、代码审查或重构时自动应用此技能。遵循 Javadoc 规范，使用中文注释。
---

# 代码注释规范技能

## 总则

### 注释原则

1. **注释应该解释代码的意图**，而不是代码本身
2. **注释应该简洁明了**，避免冗长
3. **注释应该与代码同步更新**，避免过时
4. **好的代码本身就是文档**，避免过度注释
5. **使用中文注释**，保持团队一致性

### 注释类型

| 注释类型 | 使用场景 | 格式 |
|----------|----------|------|
| 文件注释 | 文件头部说明 | `/** ... */` |
| 类注释 | 类的功能说明 | `/** ... */` |
| 方法注释 | 方法的功能、参数、返回值 | `/** ... */` |
| 字段注释 | 字段的含义说明 | `/** ... */` 或 `// ...` |
| 行内注释 | 解释复杂逻辑 | `// ...` |

---

## 类级注释规范

### 类注释格式

所有类必须在类定义上方添加类注释，包含以下信息：

```java
/**
 * 类的功能描述
 * <p>
 * 可以添加更详细的说明，包括：
 * 1. 类的主要职责
 * 2. 使用场景
 * 3. 注意事项
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 * @see 相关的类或接口
 */
public class ClassName {
    // ...
}
```

### 类注释标签

| 标签 | 说明 | 是否必需 |
|------|------|----------|
| `@author` | 作者 | 是 |
| `@version` | 版本号 | 是 |
| `@since` | 起始版本 | 推荐 |
| `@see` | 相关类/接口 | 可选 |
| `@param` | 泛型参数说明 | 可选（泛型类使用） |

### Entity 类注释示例

```java
/**
 * 用户实体类
 * <p>
 * 对应数据库表：sys_user
 * 用于存储系统用户信息，包括账号、密码、联系方式等
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    
    // ...
}
```

### Service 接口注释示例

```java
/**
 * 用户服务接口
 * <p>
 * 提供用户相关的业务逻辑处理，包括：
 * 1. 用户 CRUD 操作
 * 2. 用户角色分配
 * 3. 用户权限查询
 * 4. 用户状态管理
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 */
public interface ISysUserService extends IService<SysUser> {
    
    // ...
}
```

### Controller 类注释示例

```java
/**
 * 用户管理控制器
 * <p>
 * 提供用户管理的 RESTful API 接口，包括：
 * 1. 用户分页查询
 * 2. 用户详情查询
 * 3. 用户创建、更新、删除
 * 4. 用户角色分配
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 * @see ISysUserService
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class UserController {
    
    // ...
}
```

### Mapper 接口注释示例

```java
/**
 * 用户 Mapper 接口
 * <p>
 * 提供用户表的数据访问操作，继承 MPJBaseMapper 支持联表查询
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 * @see BaseMapper
 * @see MPJBaseMapper
 */
@Mapper
public interface SysUserMapper extends MPJBaseMapper<SysUser> {
    
    // ...
}
```

---

## 方法级注释规范

### 方法注释格式

所有公共方法必须在方法上方添加注释，包含以下信息：

```java
/**
 * 方法的功能描述
 * <p>
 * 可以添加更详细的说明，包括：
 * 1. 方法的执行逻辑
 * 2. 使用场景
 * 3. 注意事项
 * </p>
 *
 * @param paramName 参数说明
 * @param paramName2 参数说明 2
 * @return 返回值说明
 * @throws ExceptionType 异常说明（如果会抛出异常）
 */
public ReturnType methodName(ParamType paramName, ParamType paramName2) throws ExceptionType {
    // ...
}
```

### 方法注释标签

| 标签 | 说明 | 是否必需 |
|------|------|----------|
| `@param` | 参数说明 | 是（有参数时） |
| `@return` | 返回值说明 | 是（有返回值时） |
| `@throws` | 异常说明 | 可选（抛出异常时） |
| `@see` | 相关方法 | 可选 |

### Service 方法注释示例

```java
/**
 * 根据用户名查询用户
 * <p>
 * 查询未删除的用户，返回用户实体对象
 * </p>
 *
 * @param username 用户名，不能为空
 * @return 用户实体，如果不存在返回 null
 * @see SysUserMapper#selectByUsername(String)
 */
@Override
public SysUser getByUsername(String username) {
    return userMapper.selectByUsername(username);
}

/**
 * 分页查询用户列表
 * <p>
 * 根据查询条件分页返回用户信息，支持按用户名、状态筛选
 * </p>
 *
 * @param param 查询参数
 *              - pageNum: 页码，从 1 开始
 *              - pageSize: 每页大小
 *              - username: 用户名（可选）
 *              - status: 用户状态（可选）
 * @return 分页结果，包含用户列表和总数
 * @throws BusinessException 查询参数异常时抛出
 */
@Override
public Page<UserResponse> pageUsers(UserParam param) {
    // ...
}

/**
 * 创建用户
 * <p>
 * 创建新用户并分配角色，事务操作
 * </p>
 *
 * @param param 用户创建参数
 * @return 创建成功的用户 ID
 * @throws BusinessException 用户已存在或参数校验失败时抛出
 * @see SysUserMapper#insert(SysUser)
 * @see SysUserRoleMapper#insert(SysUserRole)
 */
@Override
@Transactional(rollbackFor = Exception.class)
public Long createUser(UserParam param) {
    // ...
}
```

### Controller 方法注释示例

```java
/**
 * 分页查询用户列表
 * <p>
 * 接口路径：POST /api/sys/user/page
 * 需要权限：sys:user:query
 * </p>
 *
 * @param param 查询参数
 * @return 分页结果
 *         - code: 状态码
 *         - data: 分页数据
 *         - message: 提示信息
 */
@Operation(summary = "分页查询用户列表")
@PostMapping("/page")
@RequirePerm("sys:user:query")
public R<PageResult<UserResponse>> page(@RequestBody UserParam param) {
    Page<UserResponse> page = userService.pageUsers(param);
    return R.ok(PageResult.of(page));
}

/**
 * 创建用户
 * <p>
 * 接口路径：POST /api/sys/user/create
 * 需要权限：sys:user:add
 * </p>
 *
 * @param param 用户创建参数
 *              - username: 用户名（必填）
 *              - password: 密码（必填）
 *              - realName: 真实姓名（可选）
 *              - roleIds: 角色 ID 列表（可选）
 * @return 创建结果
 *         - code: 状态码
 *         - data: 用户 ID
 *         - message: 提示信息
 * @throws BusinessException 用户已存在时抛出
 */
@Operation(summary = "创建用户")
@PostMapping("/create")
@RequirePerm("sys:user:add")
public R<Long> create(@RequestBody UserParam param) {
    Long userId = userService.createUser(param);
    return R.ok(userId);
}
```

---

## 字段注释规范

### 字段注释格式

Entity 类的所有字段必须添加注释：

```java
/**
 * 用户 ID（主键）
 */
@TableId(type = IdType.ASSIGN_ID)
private Long id;

/**
 * 用户名（登录账号）
 * 用于用户登录和唯一标识
 */
private String username;

/**
 * 密码（加密存储）
 * 使用 BCrypt 加密
 */
private String password;
```

### Entity 字段注释示例

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    
    /**
     * 用户 ID（主键）
     * 使用雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户名（登录账号）
     * 唯一，用于用户登录
     */
    private String username;
    
    /**
     * 密码（加密存储）
     * 使用 BCrypt 加密
     */
    private String password;
    
    /**
     * 真实姓名
     * 用于显示
     */
    private String realName;
    
    /**
     * 手机号
     * 用于接收通知
     */
    private String phone;
    
    /**
     * 邮箱
     * 用于接收通知
     */
    private String email;
    
    /**
     * 用户状态
     * 0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 部门 ID
     * 关联 sys_department 表
     */
    private Long deptId;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录 IP
     */
    private String lastLoginIp;
}
```

### Param 类字段注释示例

```java
@Data
public class UserParam extends BasePageParam {
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 真实姓名（模糊查询）
     */
    private String realName;
    
    /**
     * 用户状态
     * 0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 部门 ID
     * 查询指定部门的用户
     */
    private Long deptId;
    
    /**
     * 角色 ID 列表
     * 查询拥有指定角色的用户
     */
    private List<Long> roleIds;
}
```

### Response 类字段注释示例

```java
@Data
public class UserResponse {
    
    /**
     * 用户 ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号（脱敏）
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户状态
     * 0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 角色名称列表
     */
    private List<String> roleNames;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
```

---

## 行内注释规范

### 使用场景

行内注释用于解释复杂的业务逻辑、算法、正则表达式等：

```java
// 计算折扣价格：原价 * 折扣率
BigDecimal discountPrice = originalPrice.multiply(discountRate);

// 使用正则表达式验证邮箱格式
Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");

// 如果用户是管理员，跳过权限检查
if (user.isAdmin()) {
    return true;
}
```

### 注释位置

```java
// ✅ 推荐：写在上方
// 检查缓存是否过期
if (cache.isExpired()) {
    // 从数据库重新加载
    data = loadFromDatabase();
}

// ✅ 可以：写在右侧（简短注释）
if (cache.isExpired()) { // 检查缓存过期
    data = loadFromDatabase();
}
```

### 注释内容

行内注释应该解释**为什么这样做**，而不是**做了什么**：

```java
// ❌ 差的注释：重复代码
i++; // i 自增

// ✅ 好的注释：解释原因
i++; // 跳过第一个元素（哨兵节点）

// ❌ 差的注释：描述代码
if (user.getStatus() == 1) {
    // 如果用户状态等于 1
}

// ✅ 好的注释：解释业务逻辑
if (user.getStatus() == 1) {
    // 只处理启用状态的用户
}
```

### 方法内部逻辑注释

对于包含多段逻辑的方法，每段逻辑都需要添加行内注释说明。注释应包含以下信息：

1. **这一段代码做了什么** - 说明当前代码块的功能
2. **为下一步准备了什么** - 说明当前步骤为后续步骤准备的数据或条件
3. **使用了上一步的什么数据** - 说明当前步骤依赖的前置数据或结果
4. **多步骤逻辑使用数字编号** - 使用 (1. 2. 3.) 标识步骤顺序

**示例 1：Service 方法内部逻辑注释**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long createUser(UserCreateParam param) {
    // 1. 第一步：参数校验
    // 检查必要参数是否为空，为后续数据库查询做准备
    if (!StringUtils.hasText(param.getAccount())) {
        throw new BusinessException("账号不能为空");
    }

    // 2. 第二步：检查账号是否存在
    // 使用上一步校验后的账号参数，从数据库查询用户
    // 如果用户已存在，则抛出异常，避免重复创建
    SysUser existsUser = userMapper.selectByAccount(param.getAccount());
    if (existsUser != null) {
        throw new BusinessException("账号已存在");
    }

    // 3. 第三步：构建用户实体
    // 使用上一步校验通过的参数，填充用户实体字段
    // 为插入数据库做数据准备
    SysUser user = new SysUser();
    user.setAccount(param.getAccount());
    user.setPassword(passwordEncoder.encode(param.getPassword()));
    user.setRealName(param.getRealName());
    user.setPhone(param.getPhone());
    user.setEmail(param.getEmail());
    user.setStatus(SystemConstants.STATUS_NORMAL);

    // 4. 第四步：插入用户数据
    // 使用上一步构建的用户实体，执行数据库插入操作
    // 插入成功后返回用户 ID
    userMapper.insert(user);
    log.info("创建用户成功：userId={}, account={}", user.getId(), user.getAccount());
    
    return user.getId();
}
```

**示例 2：多步骤业务逻辑注释**

```java
@Override
public String getUserLangPreference(Long userId, Long tenantId) {
    // 1. 优先获取用户偏好（从 Redis）
    // 使用传入的 userId 和 tenantId 构建 Redis Key
    // 如果用户有自定义语言偏好，直接返回，为后续步骤提供快速路径
    if (userId != null) {
        String userLang = getUserLangFromRedis(userId, tenantId);
        if (userLang != null) {
            log.debug("获取用户语言偏好：userId={}, langCode={}", userId, userLang);
            return userLang;
        }
    }

    // 2. 获取租户默认语言（从 Redis）
    // 使用传入的 tenantId 查询租户级别的默认语言
    // 如果租户配置了默认语言，返回该语言，作为次优选择
    if (tenantId != null) {
        String tenantLang = getTenantLangFromRedis(tenantId);
        if (tenantLang != null) {
            log.debug("获取租户默认语言：tenantId={}, langCode={}", tenantId, tenantLang);
            return tenantLang;
        }
    }

    // 3. 返回系统默认语言（兜底策略）
    // 当用户偏好和租户默认都不存在时，使用系统默认语言
    // 确保始终有可用的语言设置，避免空指针异常
    log.debug("使用系统默认语言：{}", DEFAULT_LANG);
    return DEFAULT_LANG;
}
```

**示例 3：Controller 方法内部逻辑注释**

```java
@Override
@PostMapping("/switch")
public R<Void> switchLang(@RequestBody Map<String, String> request) {
    // 1. 获取请求参数
    // 从请求体中提取 langCode 参数，为后续校验和使用做准备
    String langCode = request.get("langCode");
    
    // 2. 参数校验
    // 检查上一步获取的 langCode 是否为空，确保后续操作的安全性
    if (langCode == null || langCode.isEmpty()) {
        return R.fail("语言编码不能为空");
    }

    // 3. 获取用户信息
    // 从登录上下文获取当前用户 ID 和租户 ID
    // 为设置语言偏好提供必要的用户标识
    Long userId = UserContextHolder.getUserId();
    Long tenantId = UserContextHolder.getTenantId();

    // 4. 设置语言偏好
    // 使用上一步获取的用户 ID 和租户 ID，将语言编码存储到 Redis
    // 更新用户的语言偏好设置
    langPreferenceService.setUserLangPreference(userId, tenantId, langCode);

    // 5. 记录操作日志
    // 使用前面步骤中的所有关键参数，记录操作日志便于审计
    log.info("用户切换语言：userId={}, tenantId={}, langCode={}", userId, tenantId, langCode);
    return R.ok();
}
```

### 注释要点总结

- 使用数字编号 (1. 2. 3.) 清晰标识步骤顺序
- 每段注释说明当前步骤的功能、输入数据、输出结果
- 说明步骤之间的数据流转和依赖关系
- 对于关键步骤，说明其在整体流程中的作用
- 避免简单的代码复述，重点解释业务意图和数据处理逻辑

---

## 特殊注释规范

### TODO 注释

用于标记待完成的任务：

```java
// TODO: 后续优化为异步处理
public void sendEmail(String to, String content) {
    emailService.send(to, content);
}

// TODO(li-da-mom): 需要添加缓存
// 日期：2026-03-28
public List<SysUser> getAllUsers() {
    return userMapper.selectList(null);
}
```

### FIXME 注释

用于标记需要修复的问题：

```java
// FIXME: 这里可能会抛出空指针异常
public String getUserName(Long userId) {
    SysUser user = userMapper.selectById(userId);
    return user.getUsername(); // 可能 NPE
}
```

### NOTE 注释

用于标记重要的注意事项：

```java
// NOTE: 这个方法必须在事务中调用
@Transactional
public void createUser(UserParam param) {
    // ...
}
```

### WARNING 注释

用于标记潜在的风险：

```java
// WARNING: 这个方法会删除所有数据，慎用
public void clearAllData() {
    // ...
}
```

---

## 国际化代码注释示例

### JSON 字段注释

对于存储 JSON 数据的字段，需要说明 JSON 的格式和用途：

```java
/**
 * 多语言翻译 JSON
 * <p>
 * MyBatis-Plus 会自动将 Map 转换为 JSON 存储。
 * Key 为语言编码（zh_CN/en_US 等），Value 为翻译内容。
 * 示例：{"zh_CN":"操作成功","en_US":"Success","zh_TW":"操作成功"}
 * </p>
 */
private Map<String, String> translations;
```

### Redis 操作注释

对于 Redis 操作的方法，需要说明执行步骤和 Redis Key 的构建规则：

```java
/**
 * 设置用户语言偏好（存储到 Redis）
 * <p>
 * 执行步骤：
 * 1. 参数校验：userId 和 langCode 不能为空
 * 2. 构建 Redis Key：i18n:user:lang:{userId}:{tenantId}
 * 3. 存储到 Redis，设置 30 天过期时间
 * 4. 记录调试日志
 * </p>
 *
 * @param userId   用户 ID
 * @param tenantId 租户 ID
 * @param langCode 语言编码（zh_CN/en_US 等）
 * @see RedisTemplate#opsForValue()
 */
@Override
public void setUserLangPreference(Long userId, Long tenantId, String langCode) {
    // 1. 参数校验
    if (userId == null || langCode == null) {
        return;
    }

    // 2. 构建 Redis Key
    String key = buildUserLangKey(userId, tenantId);
    
    // 3. 存储到 Redis
    redisTemplate.opsForValue().set(key, langCode, EXPIRE_DAYS, TimeUnit.DAYS);
    
    // 4. 记录日志
    log.debug("设置用户语言偏好：userId={}, tenantId={}, langCode={}", userId, tenantId, langCode);
}
```

---

## 总结

### 必须添加注释的地方

1. 所有类（类注释）
2. 所有公共方法（方法注释）
3. 所有 Entity 字段（字段注释）
4. 所有 Param/Response 字段（字段注释）
5. 复杂业务逻辑（行内注释）

### 注释质量检查清单

- [ ] 类注释包含功能描述、@author、@version
- [ ] 方法注释包含功能描述、@param、@return
- [ ] 字段注释清晰说明字段含义
- [ ] 行内注释解释复杂逻辑
- [ ] 注释与代码同步
- [ ] 注释使用中文
- [ ] 没有过时的注释
- [ ] 没有无意义的注释

---

**文档版本**: 1.3  
**创建日期**: 2026-03-28  
**更新日期**: 2026-03-30  
**作者**: LiDaoMoM
