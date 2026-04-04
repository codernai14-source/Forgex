# 表格配置问题排查清单

## 问题排查顺序（固定流程）

按照以下顺序依次排查：

1. 查接口返回里有没有该字段
2. 查 DTO/VO 有没有该字段
3. 查 Service 是否赋值
4. 查 `fx_table_column_config.field` 是否匹配
5. 查前端 slot 名是否匹配字段名
6. 查是否被用户列配置覆盖

## 常见问题与解决方案

### 1. 新增列后页面不显示

**排查点：**

- 接口返回里是否真的有这个字段
- `fx_table_column_config.field` 是否和返回 JSON 字段一致
- 该列是否 `enabled=true`
- 该列是否被用户列配置隐藏了

### 2. 自定义 slot 不生效

**常见原因：** slot 名不是字段名

**示例：**
- 字段是 `parentTenantName`
- 错误写法：`#parentTenant`
- 正确写法：`#parentTenantName`

### 3. 隐藏列刷新后又出现

**常见原因：**

- 用户配置保存成功，但读取配置时没有把 `column_config` 合并回来
- 保存列设置时错误地基于"已过滤隐藏列"的主表格做合并

**正确原则：**

- 隐藏列是 `visible=false`
- 隐藏列不是从配置数组删除

### 4. 列设置里找不到刚隐藏的列

**常见原因：** 列设置弹窗使用了主表格当前可见列作为数据源

**正确做法：** 列设置面板必须加载完整列配置

### 5. SQL 加了列，但页面还是空列

**常见原因：**

- 该字段是展示字段，不是原始表字段
- DTO 没声明
- Service 没赋值

**典型展示字段：**
- `parentTenantName`
- `departmentName`
- `positionName`

### 6. 新列永远不显示

**可能原因：**

- 用户配置合并逻辑没有把新列补进去
- 检查 `FxTableConfigServiceImpl` 的合并逻辑

### 7. 查询表单不显示某字段

**排查点：**

- `fx_table_column_config` 中该列的 `queryable` 是否为 `true`
- `query_type` 是否正确设置

## 衍生展示字段检查清单

衍生展示字段需要单独补齐：

- Entity 是否有该字段（可能不需要）
- DTO/VO 是否声明了该字段
- Service 转换逻辑是否赋值（`convertToDTO`、组装 VO、联表查询）

**典型衍生字段：**
- `parentTenantName`：父租户名称
- `departmentName`：部门名称
- `positionName`：岗位名称
- `tenantTypeDesc`：租户类型描述

## 表格配置 SQL 检查清单

新增列配置时必须满足：

- [ ] 幂等写法，避免重复执行重复插入
- [ ] 插入中间列时同步顺延后续 `order_num`
- [ ] `title_i18n_json` 必须是合法 JSON
- [ ] `field` 必须和接口字段完全一致
- [ ] 公共默认配置统一使用 `tenant_id = 0`

**推荐写法示例：**

```sql
-- 先顺延后续列的 order_num
UPDATE fx_table_column_config 
SET order_num = order_num + 1 
WHERE table_id = xxx AND order_num >= 5 AND tenant_id = 0;

-- 幂等插入
INSERT INTO fx_table_column_config (table_id, field, title_i18n_json, order_num, ...)
SELECT xxx, 'newField', '{"zh_CN":"新字段"}', 5, ...
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM fx_table_column_config 
    WHERE table_id = xxx AND field = 'newField' AND tenant_id = 0
);
```

## 用户列配置问题检查清单

- [ ] 用户配置合并逻辑会把新列补进去
- [ ] 隐藏列只是 `visible=false`，不是从数组里删掉
- [ ] 列设置弹窗加载的是完整列配置，不是过滤后的可见列
- [ ] 保存列设置时，合并基准必须是"完整基础列"

## 完整链路验证清单

新增列后必须验证四条链路：

1. **默认显示**：新列在未配置用户偏好时是否正常显示
2. **隐藏**：用户隐藏该列后是否真的不显示
3. **刷新**：隐藏后刷新页面是否仍然隐藏
4. **重置**：重置用户配置后是否恢复默认显示

## 关键文件位置

### 前端

- `FxDynamicTable.vue`：动态表格主组件
- `ColumnSettingButton.vue`：列设置组件
- `tableConfig.ts`：表格配置 API

### 后端

- `CommonTableController.java`：公共表格控制器
- `FxTableConfigServiceImpl.java`：表格配置服务
- `FxUserTableConfigServiceImpl.java`：用户表格配置服务

### 数据库表

- `fx_table_config`：表级配置
- `fx_table_column_config`：列级配置
- `fx_user_table_config`：用户个性化配置