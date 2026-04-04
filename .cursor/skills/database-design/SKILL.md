---
name: database-design
description: 指导 MySQL 数据库设计，包括表命名、字段规范、索引设计、SQL 编写、数据类型选择。使用场景：创建表结构、编写 SQL 脚本、设计数据库 schema、优化查询性能。
---

# 数据库设计规范

## 命名规范

### 表命名规范

**基本规则**：
- 表名使用小写字母，单词间用下划线分隔
- 表名使用复数形式
- 必须添加模块前缀
- 避免使用 MySQL 保留字

**模块前缀**：

| 前缀 | 模块 | 示例 |
|------|------|------|
| `sys_` | 系统模块 | `sys_user`、`sys_role`、`sys_menu` |
| `fx_` | 公共模块 | `fx_i18n_message`、`fx_table_config` |
| `biz_` | 业务模块 | `biz_order`、`biz_product` |

**特殊表命名**：

| 表类型 | 命名规则 | 示例 |
|--------|----------|------|
| 主表 | `模块前缀_表名` | `sys_user` |
| 关联表 | `模块前缀_表名 1_表名 2` | `sys_user_role` |
| 中间表 | `模块前缀_表名_middle` | `biz_order_item` |
| 历史表 | `模块前缀_表名_history` | `sys_login_log_history` |
| 日志表 | `模块前缀_表名_log` | `sys_operation_log` |

### 字段命名规范

**基本规则**：
- 字段名使用小写字母，单词间用下划线分隔
- 使用有意义的英文单词或缩写
- 避免使用拼音
- 避免使用 MySQL 保留字

**字段类型前缀**：

| 前缀 | 类型 | 示例 |
|------|------|------|
| `is_` | boolean/tinyint | `is_deleted`、`is_enabled` |
| `has_` | boolean/tinyint | `has_permission` |
| `can_` | boolean/tinyint | `can_edit` |

### 索引命名规范

| 索引类型 | 命名规则 | 示例 |
|----------|----------|------|
| 主键索引 | `PRIMARY` | `PRIMARY` |
| 唯一索引 | `uk_字段名` | `uk_username` |
| 普通索引 | `idx_字段名` | `idx_create_time` |
| 联合索引 | `idx_字段名 1_字段名 2` | `idx_dept_id_status` |

### 约束命名规范

| 约束类型 | 命名规则 | 示例 |
|----------|----------|------|
| 外键约束 | `fk_表名_关联表名` | `fk_user_role` |
| 唯一约束 | `uk_表名_字段名` | `uk_user_username` |
| 检查约束 | `ck_表名_字段名` | `ck_user_status` |

---

## 通用字段规范

### 必须包含的字段

所有业务表必须包含以下字段：

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `id` | bigint | 主键，雪花算法 | `1234567890` |
| `create_time` | datetime | 创建时间 | `2026-03-28 10:00:00` |
| `update_time` | datetime | 更新时间 | `2026-03-28 12:00:00` |
| `create_by` | varchar(50) | 创建人 | `admin` |
| `update_by` | varchar(50) | 更新人 | `admin` |
| `deleted` | tinyint | 逻辑删除：0=未删除，1=已删除 | `0` |

### 多租户字段

需要租户隔离的表必须包含：

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `tenant_id` | bigint | 租户 ID：0=公共数据 | `1001` |

### 示例表结构

```sql
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '用户 ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（登录账号）',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(100) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint DEFAULT '1' COMMENT '用户状态：0=禁用，1=启用',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录 IP',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

## 数据类型规范

### 数值类型

| 类型 | 字节 | 范围 | 使用场景 |
|------|------|------|----------|
| `tinyint` | 1 | -128 ~ 127 | 状态、类型、标记 |
| `smallint` | 2 | -32768 ~ 32767 | 小整数 |
| `int` | 4 | -21 亿 ~ 21 亿 | 整数 |
| `bigint` | 8 | -922 亿亿 ~ 922 亿亿 | 主键、大整数 |
| `decimal(M,D)` | 变长 | 精确小数 | 金额、费率 |

**使用建议**：
- 主键统一使用 `bigint`
- 状态字段使用 `tinyint`
- 金额使用 `decimal(10,2)`
- 百分比使用 `decimal(5,2)`

### 字符串类型

| 类型 | 最大长度 | 使用场景 | 示例 |
|------|----------|----------|------|
| `char(N)` | 255 | 固定长度字符串 | 手机号、身份证号 |
| `varchar(N)` | 65535 | 可变长度字符串 | 用户名、邮箱 |
| `text` | 65535 | 长文本 | 描述、内容 |
| `json` | 4294967295 | JSON 数据 | 配置、扩展字段 |

**长度规范**：

| 字段类型 | 推荐长度 | 说明 |
|----------|----------|------|
| 用户名 | varchar(50) | 登录账号 |
| 真实姓名 | varchar(100) | 显示名称 |
| 手机号 | varchar(20) | 国际号码 |
| 邮箱 | varchar(100) | 邮箱地址 |
| 密码 | varchar(255) | 加密后的密码 |
| 地址 | varchar(500) | 详细地址 |
| URL | varchar(500) | 链接地址 |
| 备注 | text | 长文本内容 |

### 时间类型

| 类型 | 格式 | 使用场景 |
|------|------|----------|
| `datetime` | yyyy-MM-dd HH:mm:ss | 时间戳 |
| `date` | yyyy-MM-dd | 日期 |
| `time` | HH:mm:ss | 时间 |
| `timestamp` | yyyy-MM-dd HH:mm:ss | 时间戳（时区敏感） |

**使用建议**：
- 业务时间使用 `datetime`
- 创建/更新时间使用 `datetime`（自动填充）
- 避免使用 `timestamp`（时区问题）

### 布尔类型

MySQL 没有真正的布尔类型，使用 `tinyint(1)`：

| 值 | 含义 | 示例 |
|----|------|------|
| 0 | false/否 | `is_deleted = 0` |
| 1 | true/是 | `is_deleted = 1` |

---

## 索引设计规范

### 索引设计原则

1. **主键索引**：必须设置主键，推荐使用 `id`
2. **唯一索引**：业务上唯一的字段（用户名、手机号等）
3. **外键索引**：关联字段必须建立索引
4. **查询索引**：WHERE、ORDER BY、GROUP BY 字段
5. **联合索引**：多个字段组合查询时建立

### 索引优化建议

**最左前缀原则**：

```sql
-- ✅ 好的索引设计
CREATE INDEX idx_name_age ON sys_user(name, age);

-- ✅ 可以使用索引
SELECT * FROM sys_user WHERE name = '张三';
SELECT * FROM sys_user WHERE name = '张三' AND age = 20;

-- ❌ 不能使用索引（跳过最左列）
SELECT * FROM sys_user WHERE age = 20;
```

**覆盖索引**：

```sql
-- ✅ 使用覆盖索引（只查询索引字段）
CREATE INDEX idx_username_status ON sys_user(username, status);
SELECT username, status FROM sys_user WHERE username = 'admin';

-- ❌ 回表查询（需要查询非索引字段）
SELECT username, status, phone FROM sys_user WHERE username = 'admin';
```

**避免索引失效**：

```sql
-- ❌ 索引失效：对字段使用函数
SELECT * FROM sys_user WHERE DATE(create_time) = '2026-03-28';

-- ✅ 索引生效：使用范围查询
SELECT * FROM sys_user WHERE create_time >= '2026-03-28 00:00:00' 
                            AND create_time < '2026-03-29 00:00:00';

-- ❌ 索引失效：模糊查询以%开头
SELECT * FROM sys_user WHERE username LIKE '%admin%';

-- ✅ 索引生效：模糊查询以字符开头
SELECT * FROM sys_user WHERE username LIKE 'admin%';
```

### 索引数量控制

- 单表索引不超过 5 个
- 联合索引字段不超过 3 个
- 避免重复索引
- 定期清理无用索引

---

## SQL 编写规范

### SELECT 规范

**必须指定字段列表**：

```sql
-- ❌ 禁止：查询所有字段
SELECT * FROM sys_user;

-- ✅ 推荐：指定需要的字段
SELECT id, username, real_name, phone FROM sys_user;
```

**必须指定 WHERE 条件**：

```sql
-- ❌ 禁止：无条件查询
SELECT * FROM sys_user;

-- ✅ 推荐：添加条件（包括 deleted=0）
SELECT id, username FROM sys_user WHERE deleted = 0;
```

**分页查询必须 ORDER BY**：

```sql
-- ❌ 禁止：无排序的分页
SELECT * FROM sys_user LIMIT 0, 10;

-- ✅ 推荐：指定排序
SELECT * FROM sys_user WHERE deleted = 0 ORDER BY create_time DESC LIMIT 0, 10;
```

### INSERT 规范

**必须指定字段列表**：

```sql
-- ❌ 禁止：不指定字段
INSERT INTO sys_user VALUES (1, 'admin', ...);

-- ✅ 推荐：指定字段
INSERT INTO sys_user (id, username, password, create_time) 
VALUES (1, 'admin', 'password', NOW());
```

**批量插入**：

```sql
-- ✅ 推荐：批量插入（性能更好）
INSERT INTO sys_user (id, username, password) VALUES
(1, 'user1', 'password1'),
(2, 'user2', 'password2'),
(3, 'user3', 'password3');
```

### UPDATE 规范

**必须指定 WHERE 条件**：

```sql
-- ❌ 禁止：无条件更新
UPDATE sys_user SET status = 1;

-- ✅ 推荐：指定条件
UPDATE sys_user SET status = 1 WHERE id = 1 AND deleted = 0;
```

**批量更新**：

```sql
-- ✅ 推荐：CASE WHEN 批量更新
UPDATE sys_user 
SET status = CASE 
    WHEN id = 1 THEN 1
    WHEN id = 2 THEN 0
    ELSE status
END
WHERE id IN (1, 2) AND deleted = 0;
```

### DELETE 规范

**使用逻辑删除**：

```sql
-- ❌ 禁止：物理删除
DELETE FROM sys_user WHERE id = 1;

-- ✅ 推荐：逻辑删除
UPDATE sys_user SET deleted = 1, update_time = NOW() WHERE id = 1;
```

### JOIN 规范

**使用 INNER JOIN 明确连接类型**：

```sql
-- ❌ 禁止：隐式连接
SELECT * FROM sys_user u, sys_department d WHERE u.dept_id = d.id;

-- ✅ 推荐：显式连接
SELECT u.id, u.username, d.dept_name
FROM sys_user u
INNER JOIN sys_department d ON u.dept_id = d.id
WHERE u.deleted = 0 AND d.deleted = 0;
```

**LEFT JOIN 注意 NULL 值**：

```sql
-- ✅ 推荐：处理 NULL 值
SELECT u.id, u.username, COALESCE(d.dept_name, '未分配') AS dept_name
FROM sys_user u
LEFT JOIN sys_department d ON u.dept_id = d.id
WHERE u.deleted = 0;
```

---

## 表设计示例

### 用户表

```sql
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '用户 ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（登录账号）',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(100) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像 URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0=未知，1=男，2=女',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态：0=禁用，1=启用',
  `dept_id` bigint DEFAULT NULL COMMENT '部门 ID',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录 IP',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 角色表

```sql
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '角色 ID',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据范围：1=全部，2=自定义，3=本部门及下级，4=本部门，5=仅本人',
  `order_num` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '角色状态：0=禁用，1=启用',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_order_num` (`order_num`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

### 用户角色关联表

```sql
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

---

## 数据库配置规范

### 字符集配置

```sql
-- 数据库级别
CREATE DATABASE forgex 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_general_ci;

-- 表级别
CREATE TABLE sys_user (...) 
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COMMENT='用户表';
```

### 引擎配置

- 统一使用 `InnoDB` 引擎
- 支持事务、行级锁、外键
- 避免使用 `MyISAM`

### 存储过程/触发器

**原则上不使用**：
- 业务逻辑在代码层实现
- 避免使用存储过程
- 避免使用触发器
- 避免使用视图

**特殊情况**：
- 复杂的统计报表
- 数据归档
- 需要 DBA 审核

---

## 性能优化

### 表优化

**分区表**（大数据量）：

```sql
-- 按时间分区
CREATE TABLE sys_login_log (
  id bigint NOT NULL,
  login_time datetime NOT NULL,
  -- ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
PARTITION BY RANGE (YEAR(login_time)) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026)
);
```

**分表策略**（超大表）：
- 按租户分表：`sys_user_001`、`sys_user_002`
- 按时间分表：`sys_login_log_202401`、`sys_login_log_202402`

### 查询优化

**EXPLAIN 分析**：

```sql
EXPLAIN SELECT id, username FROM sys_user WHERE username = 'admin';

-- 关注字段：
-- type: system > const > eq_ref > ref > range > index > ALL
-- key: 实际使用的索引
-- rows: 扫描行数
-- Extra: 额外信息（Using index > Using where > Using temporary > Using filesort）
```

**慢查询日志**：

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- 超过 2 秒的查询
```

---

## 安全规范

### 权限控制

- 应用使用专用数据库账号
- 仅授予必要的权限（SELECT、INSERT、UPDATE、DELETE）
- 禁止授予 DROP、ALTER 权限
- 生产环境禁止使用 root 账号

### SQL 注入防护

- 使用参数化查询（预编译）
- 禁止拼接 SQL
- 使用 MyBatis-Plus 构造器
- 输入参数校验

### 数据脱敏

- 敏感数据加密存储（密码、手机号、身份证）
- 查询结果脱敏展示
- 日志中禁止输出敏感信息

---

## 数据库变更管理

### SQL 脚本管理

- 所有表结构变更必须编写 SQL 脚本
- SQL 脚本按版本编号：`V1.0.0__init.sql`、`V1.1.0__add_user_table.sql`
- SQL 脚本纳入版本控制
- 执行 SQL 脚本前必须备份数据

### 变更流程

1. 编写 SQL 变更脚本
2. 开发环境测试
3. 测试环境验证
4. DBA 审核
5. 生产环境执行
6. 验证变更结果

### 回滚方案

- 每个变更脚本必须配套回滚脚本
- 回滚脚本命名：`R1.0.0__rollback.sql`
- 回滚脚本必须在测试环境验证

---

## 常见错误

### 1. 表命名错误

❌ **错误**：

```sql
CREATE TABLE `User` (...);  -- 使用了大写字母
CREATE TABLE `sys_user_info` (...);  -- 缺少模块前缀
CREATE TABLE `select` (...);  -- 使用了保留字
```

✅ **正确**：

```sql
CREATE TABLE `sys_user` (...);
CREATE TABLE `sys_user_info` (...);
CREATE TABLE `sys_selection` (...);
```

### 2. 缺少通用字段

❌ **错误**：

```sql
CREATE TABLE `biz_order` (
  `id` bigint NOT NULL,
  `order_no` varchar(50) NOT NULL,
  -- 缺少 create_time, update_time, create_by, update_by, deleted
);
```

✅ **正确**：

```sql
CREATE TABLE `biz_order` (
  `id` bigint NOT NULL,
  `order_no` varchar(50) NOT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(50) DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  -- ...
);
```

### 3. 索引设计错误

❌ **错误**：

```sql
-- 没有唯一索引
CREATE TABLE `sys_user` (
  `username` varchar(50) NOT NULL,
  -- ...
);

-- 关联字段没有索引
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  -- 缺少 idx_user_id, idx_role_id
);
```

✅ **正确**：

```sql
CREATE TABLE `sys_user` (
  `username` varchar(50) NOT NULL,
  -- ...
  UNIQUE KEY `uk_username` (`username`)
);

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  -- ...
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
);
```

### 4. SQL 编写错误

❌ **错误**：

```sql
-- 查询所有字段
SELECT * FROM sys_user;

-- 无条件查询
SELECT * FROM sys_user;

-- 无排序分页
SELECT * FROM sys_user LIMIT 0, 10;

-- 物理删除
DELETE FROM sys_user WHERE id = 1;
```

✅ **正确**：

```sql
-- 指定字段
SELECT id, username, real_name FROM sys_user;

-- 添加条件
SELECT id, username FROM sys_user WHERE deleted = 0;

-- 指定排序
SELECT id, username FROM sys_user WHERE deleted = 0 ORDER BY create_time DESC LIMIT 0, 10;

-- 逻辑删除
UPDATE sys_user SET deleted = 1 WHERE id = 1;
```

### 5. 数据类型选择错误

❌ **错误**：

```sql
CREATE TABLE `biz_order` (
  `amount` float(10,2),  -- float 精度问题
  `status` int,  -- 状态使用 int 浪费
  `create_time` timestamp  -- timestamp 时区问题
);
```

✅ **正确**：

```sql
CREATE TABLE `biz_order` (
  `amount` decimal(10,2),  -- 使用 decimal 保证精度
  `status` tinyint,  -- 状态使用 tinyint
  `create_time` datetime  -- 使用 datetime 避免时区问题
);
```

---

## 设计检查清单

### 表结构设计

- [ ] 表名使用小写，带模块前缀
- [ ] 包含所有通用字段（id, create_time, update_time, create_by, update_by, deleted）
- [ ] 需要租户隔离的表包含 tenant_id 字段
- [ ] 所有字段都有 COMMENT 注释
- [ ] 主键使用 bigint 类型
- [ ] 状态字段使用 tinyint 类型
- [ ] 金额字段使用 decimal 类型
- [ ] 时间字段使用 datetime 类型
- [ ] 使用 InnoDB 引擎
- [ ] 使用 utf8mb4 字符集

### 索引设计

- [ ] 主键索引已设置
- [ ] 唯一字段设置唯一索引
- [ ] 关联字段设置索引
- [ ] 查询条件字段设置索引
- [ ] 索引命名符合规范（uk_, idx_）
- [ ] 单表索引不超过 5 个
- [ ] 联合索引字段不超过 3 个

### SQL 编写

- [ ] SELECT 指定字段列表
- [ ] SELECT 包含 WHERE 条件
- [ ] SELECT 包含 deleted=0 条件
- [ ] 分页查询包含 ORDER BY
- [ ] INSERT 指定字段列表
- [ ] UPDATE 包含 WHERE 条件
- [ ] 使用逻辑删除而非物理删除
- [ ] 使用显式 JOIN 而非隐式连接
- [ ] 使用参数化查询

### 安全规范

- [ ] 敏感数据加密存储
- [ ] 查询结果脱敏展示
- [ ] 日志中无敏感信息
- [ ] 无 SQL 拼接
- [ ] 输入参数校验

---

## 总结

本规范涵盖了 MySQL 数据库设计的核心方面：

1. **命名规范**：表名、字段名、索引名、约束名的统一命名规则
2. **通用字段**：必须包含的基础字段和多租户字段
3. **数据类型**：数值、字符串、时间、布尔类型的选择和使用
4. **索引设计**：索引原则、优化建议、数量控制
5. **SQL 编写**：SELECT、INSERT、UPDATE、DELETE、JOIN 的规范
6. **表设计示例**：用户表、角色表、关联表的完整示例
7. **数据库配置**：字符集、引擎、存储过程的使用规范
8. **性能优化**：分区表、分表策略、查询优化
9. **安全规范**：权限控制、SQL 注入防护、数据脱敏
10. **变更管理**：SQL 脚本管理、变更流程、回滚方案

遵循这些规范可以确保数据库设计的一致性、可维护性和高性能。
