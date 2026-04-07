# Forgex 开发规范自检技能

## 一、技能概述

本技能用于自动检查新增文件的开发规范符合性，涵盖数据库、后端、前端三个维度，确保代码质量与项目规范一致。

## 二、检查范围

### 2.1 数据库检查

**检查对象**：新建的 SQL 表结构文件（`.sql`）

**检查依据**：
- @doc/开发规范/规范文档/数据库设计规范.md
- @doc/开发规范/规范文档/数据库字段统一规范文档.md

**检查项**：

1. **表命名规范**
   - [ ] 表名使用小写字母，单词间用下划线分隔
   - [ ] 必须添加模块前缀（`sys_`、`fx_`、`biz_`）
   - [ ] 避免使用 MySQL 保留字
   - [ ] 关联表命名：`模块前缀_表名 1_表名 2`

2. **通用字段检查**
   - [ ] 包含 `id` bigint 主键字段
   - [ ] 包含 `create_time` datetime 创建时间
   - [ ] 包含 `update_time` datetime 更新时间
   - [ ] 包含 `create_by` varchar(50/64) 创建人
   - [ ] 包含 `update_by` varchar(50/64) 更新人
   - [ ] 包含 `deleted` tinyint(1) 逻辑删除标记
   - [ ] 包含 `tenant_id` bigint 租户 ID（如需租户隔离）

3. **字段命名规范**
   - [ ] 字段名使用小写字母，单词间用下划线分隔
   - [ ] 使用有意义的英文单词或缩写
   - [ ] 避免使用拼音
   - [ ] 外键字段使用 `{表名单数}_id` 格式
   - [ ] 国际化字段使用 `xxx_i18n_json` 格式
   - [ ] JSON 配置字段使用 `xxx_json` 格式

4. **字段注释检查**
   - [ ] 所有字段必须有 `COMMENT` 注释
   - [ ] 注释清晰说明字段含义
   - [ ] 状态字段说明枚举值（如：0=禁用，1=启用）
   - [ ] 关联字段说明关联表

5. **索引规范**
   - [ ] 主键索引：`PRIMARY KEY (id)`
   - [ ] 租户索引：`KEY idx_tenant_id (tenant_id)`
   - [ ] 唯一字段建立唯一索引：`UNIQUE KEY uk_字段名`
   - [ ] 常用查询字段建立普通索引：`KEY idx_字段名`
   - [ ] 联合索引遵循最左前缀原则
   - [ ] 单表索引不超过 5 个

6. **数据类型规范**
   - [ ] 主键使用 `bigint`
   - [ ] 状态字段使用 `tinyint`
   - [ ] 金额使用 `decimal(10,2)`
   - [ ] 字符串类型长度合理（用户名 varchar(50)、手机号 varchar(20) 等）
   - [ ] 时间字段使用 `datetime`

7. **表和字段完整性**
   - [ ] 表有 `COMMENT` 说明
   - [ ] 字符集使用 `utf8mb4`
   - [ ] 引擎使用 `InnoDB`

### 2.2 后端检查

**检查对象**：新增的 Java 文件（`.java`）

**检查依据**：
- @doc/开发规范/规范文档/MyBatis-Plus 使用规范.md
- @doc/开发规范/规范文档/代码注释规范.md
- @doc/开发规范/架构设计/系统架构设计文档.md
- @doc/开发规范/架构设计/项目结构规范.md

**检查项**：

1. **项目结构规范**
   - [ ] 包路径正确：`com.forgex.{模块名}`
   - [ ] 分层清晰：controller、service、mapper、domain
   - [ ] domain 下 entity、param、response、dto 分离
   - [ ] 启动类在根包下

2. **类命名规范**
   - [ ] Entity：`Sys` + 业务名（如 `SysUser`）
   - [ ] Param：业务名 + `Param`（如 `UserParam`）
   - [ ] Response：业务名 + `Response`（如 `UserResponse`）
   - [ ] DTO：业务名 + `DTO`（如 `UserDTO`）
   - [ ] Controller：业务名 + `Controller`（如 `UserController`）
   - [ ] Service 接口：`I` + 业务名 + `Service`（如 `IUserService`）
   - [ ] Service 实现：业务名 + `ServiceImpl`（如 `UserServiceImpl`）
   - [ ] Mapper：业务名 + `Mapper`（如 `UserMapper`）

3. **类注释检查**
   - [ ] 所有类必须有类注释
   - [ ] 包含功能描述
   - [ ] 包含 `@author` 标签
   - [ ] 包含 `@version` 标签
   - [ ] 包含 `@since` 标签（推荐）
   - [ ] 使用 Javadoc 格式（`/** ... */`）

4. **方法注释检查**
   - [ ] 所有公共方法必须有方法注释
   - [ ] 包含功能描述
   - [ ] 包含 `@param` 标签（有参数时）
   - [ ] 包含 `@return` 标签（有返回值时）
   - [ ] 包含 `@throws` 标签（抛出异常时）
   - [ ] 多步骤方法内部有行内注释说明步骤

5. **字段注释检查**
   - [ ] Entity 类所有字段必须有注释
   - [ ] Param/Response 类所有字段必须有注释
   - [ ] 注释说明字段含义和用途
   - [ ] 状态字段说明枚举值
   - [ ] 关联字段说明关联表

6. **MyBatis-Plus 使用规范**
   - [ ] Mapper 接口继承 `BaseMapper` 或 `MPJBaseMapper`
   - [ ] 禁止使用 `@Select`、`@Update`、`@Insert`、`@Delete` 注解 SQL
   - [ ] 简单查询使用 `LambdaQueryWrapper` 或 `MPJLambdaQueryWrapper`
   - [ ] 复杂 SQL 在 XML 文件中编写
   - [ ] XML 文件在 `src/main/resources/mapper/` 目录
   - [ ] 使用逻辑删除（`deleted` 字段）
   - [ ] 使用自动填充（`create_time`、`update_time` 等）

7. **代码格式规范**
   - [ ] 使用空行分隔不同逻辑块
   - [ ] 代码缩进一致（4 个空格）
   - [ ] 一行代码不超过 120 字符
   - [ ] 方法长度不超过 50 行
   - [ ] 类长度不超过 500 行

8. **权限注解使用**
   - [ ] Controller 方法使用 `@RequirePerm` 注解
   - [ ] 数据权限使用 `@DataScope` 注解
   - [ ] 忽略租户使用 `@IgnoreTenant` 注解

9. **事务注解使用**
   - [ ] Service 层写操作添加 `@Transactional` 注解
   - [ ] 设置 `rollbackFor = Exception.class`

### 2.3 前端检查

**检查对象**：新增的 Vue/TS 文件（`.vue`、`.ts`）

**检查依据**：
- @doc/开发规范/规范文档/前端设计规范.md
- @doc/开发规范/使用指南/开发使用指南.md

**检查项**：

1. **Vue 组件注释**
   - [ ] 组件文件必须有 HTML 格式的组件注释块（在 `<script>` 标签上方）
   - [ ] 包含组件名称和功能描述
   - [ ] 包含使用场景说明
   - [ ] 包含 `@author`、`@version`、`@since` 标签
   - [ ] Props interface 每个字段有 Javadoc 注释
   - [ ] Emits interface 每个事件有注释（说明触发时机、参数含义）

2. **TypeScript 类型注释**
   - [ ] Interface 添加类型注释说明用途
   - [ ] Interface 每个字段有注释
   - [ ] Type 定义有注释
   - [ ] 复杂类型说明数据结构

3. **方法注释**
   - [ ] 公共方法有 Javadoc 风格注释
   - [ ] 包含功能描述
   - [ ] 包含执行步骤（多步骤方法）
   - [ ] 包含 `@param` 标签
   - [ ] 包含 `@returns` 标签
   - [ ] 方法内部多步骤逻辑有行内注释

4. **组件结构规范**
   - [ ] 使用 `<script setup lang="ts">` 语法糖
   - [ ] Props 使用 `interface` + `withDefaults` 定义
   - [ ] Emits 使用 `interface` 定义
   - [ ] 响应式数据使用 `ref`、`reactive`
   - [ ] 计算属性使用 `computed`

5. **API 接口规范**
   - [ ] API 文件有模块注释
   - [ ] API 方法有注释（接口路径、权限、参数说明）
   - [ ] 使用统一的 `request` 封装
   - [ ] 返回类型使用泛型

6. **Pinia Store 规范**
   - [ ] Store 文件有模块注释
   - [ ] 状态定义有注释
   - [ ] 计算属性有注释
   - [ ] 方法有注释

7. **权限控制**
   - [ ] 按钮级权限使用 `v-permission` 指令
   - [ ] 条件渲染使用 `hasPermission` 函数
   - [ ] 路由权限配置 `meta.permission`

8. **国际化**
   - [ ] 所有文本使用 `$t()` 或 `t()` 函数
   - [ ] 语言包在 `src/locales/` 目录
   - [ ] 避免硬编码中文文本

9. **样式规范**
   - [ ] 使用 `<style lang="scss" scoped>`
   - [ ] 使用项目定义的 SCSS 变量
   - [ ] 使用 Mixin 复用样式
   - [ ] 类名使用 BEM 或短横线命名

10. **代码质量**
    - [ ] 使用 TypeScript 定义类型
    - [ ] 避免使用 `any` 类型
    - [ ] 代码通过 ESLint 和 Prettier 检查
    - [ ] 组件添加完整的注释

## 三、使用流程

### 3.1 触发时机

- 创建新文件后
- 提交代码前
- Code Review 前

### 3.2 检查流程

1. **识别文件类型**
   - SQL 文件 → 数据库检查
   - Java 文件 → 后端检查
   - Vue/TS 文件 → 前端检查

2. **执行对应检查项**
   - 逐项检查规范要求
   - 记录不符合项

3. **生成检查报告**
   - 列出所有问题
   - 提供修改建议
   - 引用相关规范文档

### 3.3 检查报告格式

```markdown
## 开发规范检查报告

### 文件信息
- 文件路径：xxx
- 文件类型：xxx
- 检查时间：xxx

### 检查结果

#### ✅ 符合项
- [列出符合的规范项]

#### ❌ 不符合项
1. **问题描述**：xxx
   - **规范要求**：xxx
   - **修改建议**：xxx
   - **参考文档**：xxx

2. **问题描述**：xxx
   - **规范要求**：xxx
   - **修改建议**：xxx
   - **参考文档**：xxx

### 总结
- 符合项：xx 项
- 不符合项：xx 项
- 建议：xxx
```

## 四、检查清单模板

### 4.1 数据库检查清单

```markdown
## 数据库表规范检查

### 表命名
- [ ] 表名小写 + 下划线
- [ ] 包含模块前缀
- [ ] 避免保留字

### 通用字段
- [ ] id (bigint)
- [ ] create_time (datetime)
- [ ] update_time (datetime)
- [ ] create_by (varchar)
- [ ] update_by (varchar)
- [ ] deleted (tinyint(1))
- [ ] tenant_id (bigint)

### 字段注释
- [ ] 所有字段有 COMMENT
- [ ] 注释清晰完整
- [ ] 状态字段说明枚举值

### 索引
- [ ] 主键索引
- [ ] 租户索引
- [ ] 唯一索引（需要时）
- [ ] 普通索引（查询字段）

### 数据类型
- [ ] 类型选择合理
- [ ] 长度符合规范
```

### 4.2 后端检查清单

```markdown
## 后端代码规范检查

### 项目结构
- [ ] 包路径正确
- [ ] 分层清晰
- [ ] 类命名规范

### 注释规范
- [ ] 类注释完整
- [ ] 方法注释完整
- [ ] 字段注释完整
- [ ] 行内注释清晰

### MyBatis-Plus
- [ ] 继承 BaseMapper/MPJBaseMapper
- [ ] 无注解 SQL
- [ ] XML 位置正确

### 代码质量
- [ ] 代码格式规范
- [ ] 方法长度合理
- [ ] 类长度合理
```

### 4.3 前端检查清单

```markdown
## 前端代码规范检查

### Vue 组件
- [ ] 组件注释完整
- [ ] Props 注释完整
- [ ] Emits 注释完整
- [ ] 使用 setup 语法糖

### TypeScript
- [ ] 类型定义完整
- [ ] 类型注释清晰
- [ ] 避免使用 any

### 方法注释
- [ ] 公共方法有注释
- [ ] 执行步骤清晰
- [ ] 参数返回值说明

### 权限国际化
- [ ] 权限控制正确
- [ ] 使用国际化
- [ ] 无硬编码文本

### 样式规范
- [ ] 使用 SCSS
- [ ] 使用变量和 Mixin
- [ ] 样式 scoped
```

## 五、常见问题

### 5.1 数据库常见问题

1. **缺少通用字段**
   - 问题：表缺少 `create_time`、`update_time`、`deleted` 等字段
   - 解决：添加缺失的通用字段

2. **字段缺少注释**
   - 问题：字段没有 `COMMENT`
   - 解决：为所有字段添加清晰的注释

3. **索引缺失**
   - 问题：缺少租户索引、查询字段索引
   - 解决：根据查询需求添加索引

### 5.2 后端常见问题

1. **缺少类注释**
   - 问题：类没有 Javadoc 注释
   - 解决：添加包含 `@author`、`@version` 的类注释

2. **方法缺少注释**
   - 问题：公共方法没有注释
   - 解决：添加包含 `@param`、`@return` 的方法注释

3. **使用注解 SQL**
   - 问题：Mapper 使用 `@Select` 等注解
   - 解决：改为使用 XML 或 MPJLambdaQueryWrapper

### 5.3 前端常见问题

1. **组件缺少注释**
   - 问题：Vue 组件没有 HTML 注释块
   - 解决：在 `<script>` 上方添加组件注释

2. **Props/Emits 缺少注释**
   - 问题：Props 和 Emits 接口没有字段注释
   - 解决：为每个字段添加 Javadoc 注释

3. **硬编码中文文本**
   - 问题：模板中直接写中文
   - 解决：使用国际化 `$t()` 函数

## 六、持续改进

### 6.1 规范更新

- 定期检查规范文档是否更新
- 同步更新检查项
- 收集团队反馈优化检查项

### 6.2 自动化检查

- 集成 ESLint、Checkstyle 等工具
- 配置 Git Hook 自动检查
- CI/CD 流程中加入规范检查

### 6.3 培训与宣导

- 新成员培训时讲解规范
- 定期分享规范最佳实践
- Code Review 时强调规范

---

**文档版本**: 1.0  
**创建日期**: 2026-04-07  
**作者**: LiDaoMoM  
**适用项目**: Forgex
