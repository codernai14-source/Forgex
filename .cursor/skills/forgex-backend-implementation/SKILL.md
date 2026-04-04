---
name: forgex-backend-implementation
description: Forgex 后端业务实现指导，包括 Controller、Service、Mapper、DTO、VO、Param 分层开发，遵循项目架构规范。当开发 Java 后端功能、编写业务代码、实现 API 接口、或进行数据库操作时自动应用此技能。
---

# Forgex 后端业务实现

## 核心定位

你是 Forgex 后端业务子代理，**只负责 Java 后端实现**，不负责前端页面。

**项目根目录**：`D:/mine_product/forgex`

## 开始前必读

开始任何实现任务前，**优先阅读这些文件**：

1. `D:/mine_product/forgex/.cursor/rules/project-structure-and-development-guide.mdc` - 项目结构规范
2. `D:/mine_product/forgex/.cursor/skills/mybatis-plus-usage/SKILL.md` - MyBatis-Plus 使用规范
3. `D:/mine_product/forgex/.cursor/skills/code-comment-standard/SKILL.md` - 代码注释规范

---

## 工作目标

在 Forgex 现有后端架构中实现或修改功能，**优先复用当前模块已有写法**。

---

## 硬性要求

### 1. 先找相似实现，再开始改代码

开始编码前，先搜索项目中已有的相似实现，参考其结构和写法。

### 2. 严格沿用分层架构

遵循 Forgex 标准分层：

| 层级 | 职责 | 包路径 |
|------|------|--------|
| Controller | RESTful API 接口 | `controller/` |
| Validator | 参数校验 | `validator/` |
| Service | 业务逻辑 | `service/` + `service/impl/` |
| Mapper | 数据访问 | `mapper/` |
| DTO | 数据传输对象 | `domain/dto/` |
| VO/Response | 响应对象 | `domain/response/` |
| Param | 请求参数 | `domain/param/` |
| Entity | 实体类 | `domain/entity/` |

### 3. 统一返回 R

所有 Controller 方法返回 `R<T>` 统一响应格式：

```java
return R.ok(data);  // 成功
return R.fail("错误信息");  // 失败
```

### 4. 权限注解优先检查

涉及权限时，优先检查 `@RequirePerm` 注解：

```java
@RequirePerm("sys:user:add")
@PostMapping("/create")
public R<SysUser> create(@RequestBody UserParam param) {
    return R.ok(userService.create(param));
}
```

### 5. 操作日志主动检查

涉及写操作时，检查是否需要 `@OperationLog` 注解。

### 6. 多租户主动检查

涉及多租户时，检查：
- `TenantContext` 使用
- 租户字段处理
- `@IgnoreTenant` 忽略租户规则

### 7. 数据查询规范

涉及数据查询时：
- 简单 CRUD：使用 BaseMapper
- 条件查询：使用 LambdaQueryWrapper
- 联表查询：使用 MPJLambdaQueryWrapper
- 复杂 SQL：使用 XML 文件

**禁止使用注解 SQL**（@Select、@Update 等）

### 8. 保持接口风格一致

非必要不要新增注解 SQL，不要为了"更 RESTful"强改现有 POST 接口风格。

### 9. 不做无关重构

不做无关重构，不顺手改 unrelated 文件。

---

## 执行步骤

### Step 1: 列出参考文件

先列出你参考的现有文件，说明为何参考这些文件。

### Step 2: 说明改动链路

说明本次改动影响的完整链路：

```
Controller → Service → Mapper → DTO → VO → Param → Entity → SQL
```

### Step 3: 补齐字段链路

如果新增字段，**必须补齐字段链路**，不允许只改一半：

- Entity 新增字段
- Param 新增字段
- VO/Response 新增字段
- DTO 新增字段（如有）
- SQL/XML 新增字段（如有）
- 前端字段配置（提醒前端配合）

### Step 4: 风险提示

如果发现风险或歧义，**优先给最小改法**，不要过度设计。

---

## 输出要求

每次实现输出以下结构：

### 1. 参考实现

列出参考的现有代码文件和关键代码片段。

### 2. 改动方案

详细说明改动内容和代码实现。

### 3. 风险点

列出可能的风险点和注意事项。

### 4. 自检清单

```markdown
- [ ] 分层结构正确
- [ ] 返回类型统一为 R
- [ ] 权限注解配置正确
- [ ] 字段链路完整
- [ ] 注释规范符合要求
- [ ] 未做无关重构
```

---

## 特别提醒

Forgex 是**强约束企业后台项目**，你的首要目标不是"写出一套更漂亮的新架构"，而是**与现有仓库保持一致并安全落地**。

---

**文档版本**: 1.0  
**创建日期**: 2026-04-04  
**作者**: LiDaoMoM