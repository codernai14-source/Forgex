---
name: forgex-backend-guide
description: Forgex 后端开发指南，涵盖 Java 模块开发、Controller、Service、Mapper、DTO、VO、Param 分层开发，遵循项目架构规范。当开发 Forgex_MOM/Forgex_Backend 功能、编写业务代码、实现 API 接口、数据库操作、或修改 Java 代码时自动应用此技能。
disable-model-invocation: true
---

# Forgex 后端开发指南

## 一、项目架构

### 1.1 微服务划分

| 服务名 | 端口 | 职责 |
|--------|------|------|
| Forgex_Gateway | 8080 | API 网关、路由转发、鉴权 |
| Forgex_Auth | 8081 | 用户认证、Token 管理、登录注册 |
| Forgex_Sys | 8082 | 系统管理、用户管理、角色权限、数据字典 |
| Forgex_Job | 8083 | 定时任务、任务调度 |

### 1.2 标准服务目录结构

```
Forgex_Sys/
├── src/main/java/com/forgex/sys/
│   ├── SysApplication.java        # 启动类
│   ├── config/                    # 配置类
│   ├── controller/                # 控制器层
│   ├── service/                   # 服务层
│   │   └── impl/                  # 服务实现
│   ├── mapper/                    # 数据访问层
│   └── domain/                    # 领域对象
│       ├── entity/                # 实体类
│       ├── param/                 # 参数类
│       ├── response/              # 响应 VO
│       └── dto/                   # 数据传输对象
└── src/main/resources/
    ├── application.yml            # 配置文件
    └── mapper/                    # MyBatis XML
```

## 二、核心能力

### 2.1 统一返回与国际化

- **实现逻辑**: 见 `Forgex_Doc/后端/配置与审计/统一返回与国际化实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/配置与审计/统一返回与国际化使用方式.md`

**关键点**：
- 统一使用 `R<T>` 作为返回类型
- 状态码统一管理
- 业务异常使用 `BusinessException`
- 异常消息支持国际化

### 2.2 认证授权

- **实现逻辑**: 见 `Forgex_Doc/后端/身份与权限/认证授权实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/身份与权限/认证授权使用方式.md`

**关键点**：
- 使用 Sa-Token 进行认证
- 权限校验使用 `@RequirePerm` 注解
- 数据权限使用 `@DataScope` 注解
- 支持动态路由

### 2.3 用户与角色

- **实现逻辑**: 见 `Forgex_Doc/后端/身份与权限/用户与角色实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/身份与权限/用户与角色使用方式.md`

### 2.4 多租户

- **实现逻辑**: 见 `Forgex_Doc/后端/租户与上下文/多租户实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/租户与上下文/多租户使用方式.md`

**关键点**：
- 租户隔离通过行级实现
- 使用 `@IgnoreTenant` 注解忽略租户
- 租户上下文自动传递
- 支持公共配置回退

### 2.5 数据字典与日志

- **实现逻辑**: 见 `Forgex_Doc/后端/配置与审计/数据字典与日志实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/配置与审计/数据字典与日志使用方式.md`

### 2.6 消息模板与 SSE

- **实现逻辑**: 见 `Forgex_Doc/后端/配置与审计/消息模板与SSE实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/配置与审计/消息模板与SSE使用方式.md`

### 2.7 加密功能

- **实现逻辑**: 见 `Forgex_Doc/后端/模块专题/加密功能实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/模块专题/加密功能使用方式.md`

### 2.8 导入导出

- **实现逻辑**: 见 `Forgex_Doc/后端/模块专题/导入导出实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/模块专题/导入导出使用方式.md`

### 2.9 Redis 工具

- **实现逻辑**: 见 `Forgex_Doc/后端/公共能力/Redis 工具实现逻辑.md`
- **使用方式**: 见 `Forgex_Doc/后端/公共能力/Redis 工具使用方式.md`

### 2.10 其他模块

| 功能 | 实现逻辑 | 使用方式 |
|------|---------|---------|
| 文件上传 | [文档](Forgex_Doc/后端/模块专题/文件上传实现逻辑.md) | [文档](Forgex_Doc/后端/模块专题/文件上传使用方式.md) |
| 网关与路由 | [文档](Forgex_Doc/后端/模块专题/网关与路由实现逻辑.md) | [文档](Forgex_Doc/后端/模块专题/网关与路由使用方式.md) |
| 报表功能 | [文档](Forgex_Doc/后端/模块专题/报表功能实现逻辑.md) | [文档](Forgex_Doc/后端/模块专题/报表功能使用方式.md) |
| 代码生成 | [文档](Forgex_Doc/后端/模块专题/代码生成实现逻辑.md) | [文档](Forgex_Doc/后端/模块专题/代码生成使用方式.md) |

## 三、开发规范

### 3.1 命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Entity | `Sys` + 业务名 | `SysUser`、`SysRole` |
| Param | 业务名 + `Param` | `UserParam`、`RoleParam` |
| Response | 业务名 + `Response` | `UserResponse`、`RoleResponse` |
| DTO | 业务名 + `DTO` | `UserDTO`、`RoleDTO` |
| Controller | 业务名 + `Controller` | `UserController` |
| Service 接口 | `I` + 业务名 + `Service` | `IUserService` |
| Service 实现 | 业务名 + `ServiceImpl` | `UserServiceImpl` |
| Mapper | 业务名 + `Mapper` | `UserMapper` |

### 3.2 Controller 示例

```java
/**
 * 用户管理控制器
 * <p>
 * 处理用户相关的 HTTP 请求，包括用户创建、查询、更新、删除等操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-03-28
 */
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {

    @Autowired
    private ISysUserService userService;

    /**
     * 查询用户列表
     * <p>
     * 支持分页查询，根据条件过滤
     * </p>
     *
     * @param param 查询参数
     * @return 用户列表分页结果
     */
    @GetMapping("/list")
    public R<PageResult<SysUserResponse>> list(SysUserParam param) {
        return R.ok(userService.list(param));
    }

    /**
     * 创建用户
     * <p>
     * 需要 sys:user:add 权限
     * </p>
     *
     * @param param 用户参数
     * @return 创建的用户信息
     */
    @RequirePerm("sys:user:add")
    @PostMapping("/create")
    public R<SysUserResponse> create(@RequestBody SysUserParam param) {
        return R.ok(userService.create(param));
    }
}
```

### 3.3 Service 接口示例

```java
/**
 * 用户管理服务接口
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-03-28
 */
public interface ISysUserService {

    /**
     * 查询用户列表
     *
     * @param param 查询参数
     * @return 用户列表分页结果
     */
    PageResult<SysUserResponse> list(SysUserParam param);

    /**
     * 创建用户
     *
     * @param param 用户参数
     * @return 创建的用户信息
     */
    SysUserResponse create(SysUserParam param);
}
```

### 3.4 Service 实现示例

```java
/**
 * 用户管理服务实现类
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-03-28
 */
@Service
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public PageResult<SysUserResponse> list(SysUserParam param) {
        // 实现逻辑
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserResponse create(SysUserParam param) {
        // 实现逻辑
    }
}
```

### 3.5 Entity 示例

```java
/**
 * 用户实体类
 * <p>
 * 对应数据库表：sys_user
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-03-28
 */
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户 ID（主键，雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名（唯一，不能为空）
     */
    @TableField("username")
    private String username;

    /**
     * 用户状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;
}
```

## 四、MyBatis-Plus 使用

- **规范文档**: 见 `Forgex_Doc/开发规范/规范文档/MyBatis-Plus 使用规范.md`
- **Mapper XML**: 放在 `src/main/resources/mapper/` 目录下
- **基础包名**: `com.forgex.{模块名}`

## 五、权限体系

### 5.1 后端权限校验

```java
@RequirePerm("sys:user:add")  // 单权限
@RequirePerm({"sys:user:add", "sys:user:edit"})  // 多权限（满足一个即可）
```

### 5.2 数据权限

```java
@DataScope(deptAlias = "d", userAlias = "u")
List<SysUser> selectUserList(SysUserParam param);
```

## 六、开发流程

1. **创建实体类**: 在 `domain/entity/` 下创建 Entity
2. **创建 Mapper**: 在 `mapper/` 下创建 Mapper 接口
3. **创建 Param/Response**: 在 `domain/param/` 和 `domain/response/` 下创建
4. **创建 Service 接口**: 在 `service/` 下创建接口
5. **创建 Service 实现**: 在 `service/impl/` 下创建实现类
6. **创建 Controller**: 在 `controller/` 下创建控制器
7. **添加权限配置**: 使用 `@RequirePerm` 注解
8. **测试验证**: 使用 Postman 或浏览器测试

## 七、关联文档

- **模块专题文档**: 见 `Forgex_Doc/后端/模块专题/` 目录
- **公共能力文档**: 见 `Forgex_Doc/后端/公共能力/` 目录
- **开发规范**: 见 `forgex-development-standards` skill
- **数据库规范**: 见 `forgex-deploy-db-guide` skill
- **前端对接**: 见 `forgex-frontend-guide` skill
