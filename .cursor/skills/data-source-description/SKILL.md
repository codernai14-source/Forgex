# Forgex 项目数据源说明

## 一、数据源列表

Forgex 项目采用多数据源架构，使用 MyBatis-Plus 动态数据源（DynamicRoutingDataSource）实现数据隔离。

### 1.1 数据源清单

| 数据源名称 | 数据库名 | 用途 | 主要表 |
|-----------|---------|------|--------|
| **forgex_admin** | forgex_admin | 系统管理库 | 用户、角色、菜单、部门、岗位、租户等系统基础表 |
| **forgex_common** | forgex_common | 公共配置库 | 数据字典、表格配置、Excel 配置、用户个性化配置等 |
| **forgex_workflow** | forgex_workflow | 工作流库 | 审批任务配置、审批执行记录、我的任务等 |
| **forgex_history** | forgex_history | 历史数据库 | 历史归档数据（日志、历史记录等） |
| **forgex_scada** | forgex_scada | SCADA 数据库 | 数据采集与监控相关数据 |

## 二、数据源配置

### 2.1 Nacos 配置位置

数据源配置在 Nacos 配置中心，文件名为：`datasource-forgex-dev.yml`

### 2.2 配置示例

```yaml
spring:
  datasource:
    dynamic:
      primary: admin  # 默认数据源
      strict: false   # 未匹配数据源时是否报错
      datasource:
        admin:
          url: jdbc:mysql://localhost:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: ******
          driver-class-name: com.mysql.cj.jdbc.Driver
        common:
          url: jdbc:mysql://localhost:3306/forgex_common?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: ******
          driver-class-name: com.mysql.cj.jdbc.Driver
        workflow:
          url: jdbc:mysql://localhost:3306/forgex_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: ******
          driver-class-name: com.mysql.cj.jdbc.Driver
        history:
          url: jdbc:mysql://localhost:3306/forgex_history?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: ******
          driver-class-name: com.mysql.cj.jdbc.Driver
        scada:
          url: jdbc:mysql://localhost:3306/forgex_scada?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
          username: root
          password: ******
          driver-class-name: com.mysql.cj.jdbc.Driver
```

## 三、数据源使用方式

### 3.1 使用@DS 注解切换数据源

```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
@DS("admin")  // 默认使用 admin 数据源
public class UserServiceImpl implements IUserService {
    
    // 使用 common 数据源
    @DS("common")
    public void queryCommonData() {
        // ...
    }
    
    // 使用 workflow 数据源
    @DS("workflow")
    public void queryWorkflowData() {
        // ...
    }
}
```

### 3.2 数据源命名规范

- **admin** - 系统管理库（forgex_admin）
- **common** - 公共配置库（forgex_common）
- **workflow** - 工作流库（forgex_workflow）
- **history** - 历史数据库（forgex_history）
- **scada** - SCADA 数据库（forgex_scada）

## 四、各数据源详细表结构

### 4.1 forgex_admin（系统管理库）

**核心表：**

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_menu | 菜单表 |
| sys_module | 模块表 |
| sys_department | 部门表 |
| sys_position | 岗位表 |
| sys_tenant | 租户表 |
| sys_user_role | 用户角色关联表 |
| sys_role_menu | 角色菜单关联表 |
| sys_role_dept | 角色部门关联表 |
| sys_role_position | 角色岗位关联表 |
| sys_user_tenant | 用户租户关联表 |
| sys_dict | 数据字典表 |
| sys_login_log | 登录日志表 |
| sys_message | 系统消息表 |
| sys_message_template | 消息模板表 |
| sys_message_template_content | 消息模板内容表 |
| sys_message_template_receiver | 消息模板接收人表 |
| sys_tenant_message_whitelist | 租户消息白名单表 |
| sys_permission | 权限表 |
| sys_social_login | 社交登录表 |
| sys_file_storage | 文件存储表 |
| sys_job | 定时任务表 |

### 4.2 forgex_common（公共配置库）

**核心表：**

| 表名 | 说明 |
|------|------|
| sys_dict | 数据字典（公共） |
| fx_table_column_config | 动态表格列配置 |
| fx_user_table_config | 用户表格列设置 |
| fx_excel_import_config | Excel 导入配置 |
| fx_excel_export_config | Excel 导出配置 |
| sys_user_style_config | 用户个性化配置 |
| fx_config | 系统配置表 |

### 4.3 forgex_workflow（工作流库）

**核心表：**

| 表名 | 说明 |
|------|------|
| wf_task_config | 审批任务配置表 |
| wf_task_node_config | 审批节点配置表 |
| wf_task_execution | 审批执行记录表 |
| wf_task_execution_detail | 审批执行明细表 |
| wf_my_task | 我的任务表 |
| wf_cc_task | 抄送任务表 |

### 4.4 forgex_history（历史数据库）

**用途：** 存储历史归档数据，定期从主库迁移

### 4.5 forgex_scada（SCADA 数据库）

**用途：** 数据采集与监控系统相关数据

## 五、跨数据源查询注意事项

### 5.1 禁止跨库 JOIN

```java
// ❌ 错误：跨库 JOIN
@Select("SELECT u.*, r.role_name FROM sys_user u " +
        "JOIN sys_role r ON u.id = r.id")
List<UserRoleVO> selectUserRoles();

// ✅ 正确：分两次查询，在代码中组装
List<SysUser> users = userMapper.selectList(null);
List<SysRole> roles = roleMapper.selectList(null);
```

### 5.2 事务注意事项

```java
// ❌ 错误：跨数据源事务
@DSTransactional  // 不支持跨数据源事务
public void crossDataSourceOperation() {
    userMapper.insert(user);      // admin 库
    configMapper.insert(config);  // common 库
}

// ✅ 正确：分别处理
public void separateOperations() {
    // admin 库操作
    DS.set("admin");
    try {
        userMapper.insert(user);
    } finally {
        DS.clear();
    }
    
    // common 库操作
    DS.set("common");
    try {
        configMapper.insert(config);
    } finally {
        DS.clear();
    }
}
```

## 六、SQL 脚本执行规范

### 6.1 脚本命名规范

```
V{版本号}__{描述}.sql
例如：
- V2.0.3__审批管理菜单修复.sql
- V2.0.3__审批消息模板初始化.sql
```

### 6.2 脚本头部注释

```sql
-- ============================================
-- 脚本名称：审批管理菜单修复脚本
-- 版本号：V2.0.3
-- 目标数据库：forgex_admin
-- 执行日期：2026-04-06
-- 作者：XXX
-- ============================================
```

### 6.3 多数据源脚本执行顺序

1. **forgex_admin** - 基础数据（用户、角色、菜单）
2. **forgex_common** - 配置数据（字典、配置）
3. **forgex_workflow** - 工作流数据（审批配置）
4. **forgex_history** - 历史数据
5. **forgex_scada** - SCADA 数据

## 七、常见问题

### 7.1 如何查看当前使用的数据源？

```java
String currentDataSource = DynamicDataSourceContextHolder.peek();
log.info("当前数据源：{}", currentDataSource);
```

### 7.2 如何在代码中动态切换数据源？

```java
DynamicDataSourceContextHolder.push("common");
try {
    // 操作 common 库
} finally {
    DynamicDataSourceContextHolder.clear();
}
```

### 7.3 如何配置新的数据源？

1. 在 Nacos 添加数据源配置
2. 在代码中使用 `@DS("数据源名称")`
3. 重启服务

## 八、MCP 工具使用

项目已配置 MCP 工具连接各数据源：

| MCP Server | 数据源 | 工具 |
|-----------|--------|------|
| user-forgex_admin | forgex_admin | execute_sql |
| user-forgex_common | forgex_common | execute_sql |
| user-forgex_workflow | forgex_workflow | execute_sql |
| user-forgex_job | forgex_job | execute_sql |

**使用示例：**

```json
{
  "server": "user-forgex_admin",
  "toolName": "execute_sql",
  "arguments": {
    "query": "SELECT * FROM sys_user LIMIT 10"
  }
}
```

---

**文档版本**: 1.0  
**创建日期**: 2026-04-06  
**最后更新**: 2026-04-06