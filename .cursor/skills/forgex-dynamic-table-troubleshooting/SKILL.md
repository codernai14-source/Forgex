---
name: forgex-dynamic-table-troubleshooting
description: Forgex 动态表格问题排查专家，专门处理 FxDynamicTable 列不显示、列设置保存异常、字段链路不一致、table-code 对应关系等问题。当用户报告表格列不显示、字段丢失、列设置异常、配置合并问题、或提到 "表格不显示"、"列设置" 时使用。
---

# Forgex 动态表格问题排查

## 快速诊断

在 Forgex 中，"表格不显示"大多数不是组件 bug，而是字段链路断了。**优先查链路**。

## 五层链路排查（固定顺序，不跳步）

```
接口返回字段 → DTO/VO 字段 → Service 赋值 → fx_table_column_config.field → 前端 slot 名
```

| 层级 | 检查点 | 排查方法 |
|------|--------|----------|
| 1. 接口返回 | JSON 中是否有该字段 | 查看浏览器 Network 响应 |
| 2. DTO/VO | 是否声明该字段 | 检查 Response/DTO 类 |
| 3. Service | 是否正确赋值 | 检查转换逻辑 |
| 4. 表配置 | field 是否匹配返回字段 | 查 `fx_table_column_config` |
| 5. 前端 slot | slot 名是否等于字段名 | 检查 `#字段名` |

## 执行步骤

1. **定位问题层级**：先判断问题落在哪一层
2. **明确缺失内容**：是字段、配置、映射还是合并逻辑
3. **最小修复方案**：只修复缺失部分，不重写公共逻辑
4. **给出验证步骤**：修复后如何验证

## 硬性要求

- 新增展示字段：必须同时检查 SQL、DTO/VO、Service、表配置、前端渲染
- 隐藏列处理：只能通过 `visible=false`，不删配置
- 列设置问题：检查完整配置合并逻辑，不只看当前页面
- 非必要不重写 FxDynamicTable 公共逻辑

## 输出格式

排查结果必须包含：

```
1. 问题所在层级：[层级名称]
2. 缺失字段或配置：[具体描述]
3. 最小修复方案：[代码或 SQL]
4. 修复后验证步骤：[操作步骤]
```

## 常见问题速查

### 列不显示

优先检查链路一致性：
- 后端返回字段名 → 前端 slot 名是否完全匹配
- `fx_table_column_config.field` 是否与 JSON 字段一致

### 列设置保存异常

检查配置合并逻辑：
- 新增列是否被合并进用户配置
- 隐藏列是否正确保存为 `visible=false`
- 基准配置是否完整（不是过滤后的可见列）

### table-code 对应关系

- 检查 `fx_table_config.table_code` 是否正确
- 确认前端 `table-code` prop 与配置一致

## 相关资源

- 开发指导：见 `dynamic-table-config` 技能
- 详细实现：见 [reference.md](../dynamic-table-config/reference.md)
- 问题清单：见 [troubleshooting.md](../dynamic-table-config/troubleshooting.md)
- 前端组件：`Forgex_Fronted/src/components/common/FxDynamicTable.vue`