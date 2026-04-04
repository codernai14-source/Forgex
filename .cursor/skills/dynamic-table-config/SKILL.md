---
name: dynamic-table-config
description: Forgex 公共表格组件开发指导，包括表格配置、列配置、用户个性化配置、前后端字段链路一致性。当开发 FxDynamicTable 动态表格、补字段、修复列不显示、修复列设置保存异常、配置 fx_table_column_config 时使用。
---

# Forgex 公共表格配置开发指南

## 适用范围

- 前端公共组件 `FxDynamicTable`
- 前端列设置组件 `ColumnSettingButton`
- 后端表格配置接口 `/sys/common/table/config/*`
- 配置表 `fx_table_config`、`fx_table_column_config`、`fx_user_table_config`

## 核心原则

公共表格体系本质："后端配置定义结构，前端组件负责渲染，业务页面负责数据请求，用户配置负责个性化覆盖。"

**三个关键原则：**

1. **字段链路一致性**：`field` 必须贯穿数据库配置、后端返回、前端 slot 三端完全一致
2. **隐藏即不可见**：用户隐藏列只能是 `visible=false`，不能从配置集合删除
3. **展示字段补齐**：新增展示字段时，必须同时补 SQL、DTO/VO、Service 赋值逻辑

## 配置优先级

```
用户配置 > 租户配置 > 公共配置(tenant_id=0)
```

## 字段显示五层检查

表格字段能否显示，必须同时检查：

1. 数据库真实字段或后端返回字段是否存在
2. 后端 Entity / DTO / VO 是否已声明该字段
3. Service 转换逻辑是否把展示字段真正赋值
4. `fx_table_column_config.field` 是否与返回 JSON 字段完全一致
5. 前端动态表格 slot 名是否与字段名完全一致

## 前端接入模板

```vue
<FxDynamicTable
  ref="tableRef"
  :table-code="'YourTable'"
  :request="handleRequest"
  :dict-options="dictOptions"
  row-key="id"
/>
```

### request 方法约定

接收：
```ts
{ page: { current: number; pageSize: number }, query: Record<string, any>, sorter?: { field?: string; order?: string } }
```

返回：
```ts
{ records: any[]; total: number; }
```

### Slot 渲染规则

列字段是 `status`，slot 名必须是 `#status`。不能写语义相近但字段不一致的 slot 名。

## 表格配置 SQL 规范

1. **幂等**：使用 `INSERT ... SELECT ... WHERE NOT EXISTS (...)`
2. **顺序安全**：插入中间列时先 `UPDATE order_num = order_num + 1`
3. **JSON 合法**：`title_i18n_json` 必须是合法 JSON
4. **公共配置**：统一使用 `tenant_id = 0`

## 用户列配置注意事项

- 新增列后，用户配置合并逻辑需把新列补进去
- 列设置弹窗必须加载完整列配置，不是过滤后的可见列
- 保存列设置时，合并基准必须是"完整基础列"
- 隐藏列刷新后重新出现 → 检查合并逻辑是否正确

## 问题排查顺序

固定按此顺序排查：

1. 查接口返回里有没有该字段
2. 查 DTO/VO 有没有该字段
3. 查 Service 是否赋值
4. 查 `fx_table_column_config.field` 是否匹配
5. 查前端 slot 名是否匹配字段名
6. 查是否被用户列配置覆盖

## 更多资源

- 详细实现逻辑：见 [reference.md](reference.md)
- 问题排查清单：见 [troubleshooting.md](troubleshooting.md)