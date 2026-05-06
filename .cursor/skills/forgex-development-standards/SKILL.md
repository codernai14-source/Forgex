---
name: forgex-development-standards
description: Forgex 开发规范，涵盖项目架构、Git 提交规范、数据库字段规范、Java 注释规范、MyBatis-Plus 使用规范、公共表格组件规范、国际化提示消息规范、模块文档映射和一致性检查。当创建或修改 Forgex 代码、Schema、API、表结构、Java 注释、动态表格列、或文档时，在生成代码前自动加载此技能。
disable-model-invocation: true
---

# Forgex 开发规范

## 一、项目架构

### 1.1 微服务架构

```
forgex/
├── Forgex_MOM/
│   ├── Forgex_Backend/
│   │   ├── Forgex_Common/     # 公共模块
│   │   ├── Forgex_Gateway/    # 网关服务 (8080)
│   │   ├── Forgex_Auth/       # 认证服务 (8081)
│   │   ├── Forgex_Sys/        # 系统服务 (8082)
│   │   └── Forgex_Job/        # 任务调度服务 (8083)
│   └── Forgex_Fronted/        # 前端工程
└── Forgex_Doc/                # 文档目录
```

### 1.2 分层架构

**后端分层**：
```
controller/ → service/ → service.impl/ → mapper/ → domain.entity/
```

**前端分层**：
```
views/ → api/ → components/ → store/ → utils/
```

### 1.3 包命名规范

**基础包名**：`com.forgex.{模块名}`

| 模块 | 包名 | 示例 |
|------|------|------|
| 系统模块 | `com.forgex.sys` | `SysApplication` |
| 认证模块 | `com.forgex.auth` | `AuthApplication` |
| 任务调度 | `com.forgex.job` | `JobApplication` |
| 公共模块 | `com.forgex.common` | `CommonConstants` |

### 1.4 子包划分

```
com.forgex.sys/
├── controller/          # 控制器层
├── service/             # 服务层
│   └── impl/           # 服务实现层
├── mapper/              # 数据访问层
└── domain/              # 领域对象
    ├── entity/          # 实体类
    ├── param/           # 参数类
    ├── response/        # 响应 VO
    └── dto/             # 数据传输对象
```

## 二、命名规范

### 2.1 类命名

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
| 配置类 | 功能名 + `Config` | `RedisConfig` |
| 常量类 | 模块名 + `Constants` | `SysConstants` |
| 工具类 | 功能名 + `Utils` | `BeanUtils` |

### 2.2 方法命名

- 查询单个：`getById`、`getByName`
- 查询列表：`list`、`listByCondition`
- 创建：`create`、`add`
- 更新：`update`、`save`
- 删除：`delete`、`remove`
- 批量操作：`batchCreate`、`batchDelete`

### 2.3 字段命名

- Java 字段：驼峰命名（camelCase）
- 数据库字段：下划线命名（snake_case）
- 常量：大写 + 下划线（UPPER_SNAKE_CASE）

### 2.4 数据库字段规范

详见 `Forgex_Doc/开发规范/规范文档/数据库字段统一规范文档.md`

**通用审计字段**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键（雪花算法） |
| create_by | VARCHAR(64) | 创建人 |
| create_time | DATETIME | 创建时间 |
| update_by | VARCHAR(64) | 更新人 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除（0-未删除，1-已删除） |
| tenant_id | BIGINT | 租户 ID |

## 三、注释规范

详见 `Forgex_Doc/开发规范/规范文档/代码注释规范.md`

### 3.1 类注释

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
}
```

### 3.2 方法注释

```java
/**
 * 根据用户名查询用户信息
 * <p>
 * 通过用户名精确匹配查询用户，返回用户详细信息（包含角色、部门等关联数据）
 * </p>
 *
 * @param username 用户名，不能为空
 * @return 用户信息对象，如果不存在返回 null
 * @throws BusinessException 当查询失败时抛出业务异常
 * @see SysUser
 */
@GetMapping("/by-username")
public R<SysUser> getByUsername(@RequestParam String username) {
    return R.ok(userService.getByUsername(username));
}
```

### 3.3 字段注释

```java
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
```

### 3.4 前端组件注释

```typescript
/**
 * 用户管理页面
 * 
 * 功能：
 * 1. 用户列表展示
 * 2. 用户新增/编辑/删除
 * 3. 用户状态管理
 * 
 * @since 2026-03-28
 */
```

## 四、MyBatis-Plus 使用规范

详见 `Forgex_Doc/开发规范/规范文档/MyBatis-Plus 使用规范.md`

### 4.1 Mapper 接口

```java
/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);
}
```

### 4.2 Service 使用

```java
/**
 * 用户服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> 
        implements ISysUserService {
    
    @Override
    public SysUser getByUsername(String username) {
        // 使用 MPJ 联表查询
        MPJLambdaQueryWrapper<SysUser> wrapper = new MPJLambdaQueryWrapper<>();
        wrapper.selectAll(SysUser.class)
               .select(SysRole::getRoleName)
               .leftJoin(SysUserRole.class, SysUserRole::getUserId, SysUser::getId)
               .leftJoin(SysRole.class, SysRole::getId, SysUserRole::getRoleId)
               .eq(SysUser::getUsername, username);
        return baseMapper.selectJoinOne(SysUser.class, wrapper);
    }
}
```

### 4.3 XML 编写

- XML 文件放在 `src/main/resources/mapper/` 目录
- 命名与 Mapper 接口一致：`SysUserMapper.xml`
- namespace 指向 Mapper 接口全限定名

## 五、公共表格组件规范

详见 `Forgex_Doc/开发规范/规范文档/公共表格组件使用说明与实现逻辑.md`

### 5.1 前端使用

```vue
<FxDynamicTable
  :columns="columns"
  :api="getListApi"
  :search-form="searchForm"
  row-key="id"
/>
```

### 5.2 后端接口

```java
@GetMapping("/list")
public R<PageResult<SysUserResponse>> list(SysUserParam param) {
    return R.ok(userService.list(param));
}
```

## 六、国际化规范

详见 `Forgex_Doc/开发规范/规范文档/国际化提示消息使用指南.md`

### 6.1 后端国际化

```java
// 使用国际化消息
throw new BusinessException(I18nUtils.getMessage("user.not.found"));
```

### 6.2 前端国际化

```vue
<template>
  <a-input :placeholder="$t('user.placeholder.name')" />
</template>

<script setup>
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
</script>
```

## 七、Git 规范

详见 `Forgex_Doc/开发规范/规范文档/Git 提交与开发规范.md`

### 7.1 分支管理

```
master          # 主分支（生产）
├── develop     # 开发分支
│   ├── feature/xxx   # 功能分支
│   └── bugfix/xxx    # 修复分支
└── release/v1.0.0    # 发布分支
```

### 7.2 提交消息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

**type 类型**：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/配置

**示例**：
```
feat(user): 添加用户批量导入功能

- 支持 Excel 格式导入
- 添加数据校验逻辑

Closes #123
```

## 八、模块文档映射

详见 `Forgex_Doc/开发规范/模块文档映射/README.md`

### 8.1 文档组织原则

1. **架构变更先更新架构文档**
2. **通用规则写在开发规范中**
3. **模块差异写到各自专题文档**
4. **新的公共规范优先放在 `Forgex_Doc/开发规范`**

### 8.2 文档双轨制

每个功能模块维护两份文档：
- **实现逻辑**: 技术实现细节
- **使用方式**: 接入和使用方法

## 九、开发检查清单

### 9.1 代码结构

- [ ] 分层清晰（controller/service/mapper）
- [ ] 命名符合规范
- [ ] 包结构正确

### 9.2 注释质量

- [ ] 类注释完整（功能、作者、版本）
- [ ] 方法注释完整（功能、参数、返回值、异常）
- [ ] 字段注释清晰
- [ ] 使用中文注释

### 9.3 代码质量

- [ ] 单一职责原则
- [ ] 异常处理合理
- [ ] 事务使用正确
- [ ] 权限配置正确

### 9.4 数据库

- [ ] 表名/字段名符合规范
- [ ] 包含审计字段
- [ ] 索引设计合理

## 十、关联文档

- **前端规范**: 见 `forgex-frontend-guide` skill
- **后端规范**: 见 `forgex-backend-guide` skill
- **部署规范**: 见 `forgex-deploy-db-guide` skill
- **文档中心**: 见 `Forgex_Doc/README.md`
