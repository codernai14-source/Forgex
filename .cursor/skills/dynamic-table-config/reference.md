# 公共表格详细实现逻辑

## 1. 整体设计目标

公共表格体系主要解决 4 类问题：

1. 页面列定义重复，前后端字段不统一
2. 查询表单与表格列经常一起改，维护成本高
3. 同一个表格需要支持租户级和用户级差异化
4. 用户希望自行控制列显示、隐藏和排序

采用"配置驱动 UI，业务页面负责数据请求"的模式。

### 职责分工

- `FxDynamicTable`：根据配置渲染查询表单、表格列、分页、列设置入口
- 业务页面：提供 `request` 方法，调用业务接口拿数据
- `FxTableConfigService`：按优先级组装表格配置
- `FxUserTableConfigService`：保存和读取用户个性化列配置

## 2. 核心组成

### 前端组件

- `FxDynamicTable.vue`：动态表格主组件
- `ColumnSettingButton.vue`：列设置按钮组件
- `tableConfig.ts`：表格配置 API

### 后端接口与服务

- `CommonTableController.java`：公共表格控制器
- `FxTableConfigServiceImpl.java`：表格配置服务实现
- `FxUserTableConfigServiceImpl.java`：用户表格配置服务实现

### 数据表

- `fx_table_config`：表级配置
- `fx_table_column_config`：列级配置
- `fx_user_table_config`：用户级列配置

## 3. 表结构说明

### fx_table_config

描述表格整体信息：

- `table_code`：表格代码
- `table_name_i18n_json`：表格名称国际化
- `table_type`：表格类型
- `row_key`：行键
- `default_page_size`：默认分页大小
- `default_sort_json`：默认排序
- `enabled`：是否启用

### fx_table_column_config

描述单列的显示和查询能力：

- `field`：字段名
- `title_i18n_json`：标题国际化
- `align`：对齐方式
- `width`：列宽
- `fixed`：固定位置
- `ellipsis`：是否省略
- `sortable`：是否可排序
- `sorter_field`：排序字段
- `queryable`：是否可查询
- `query_type`：查询类型
- `query_operator`：查询操作符
- `dict_code`：字典代码
- `render_type`：渲染类型
- `perm_key`：权限键
- `order_num`：排序号
- `enabled`：是否启用

### fx_user_table_config

保存用户个性化偏好：

- `column_config`：列配置
- `query_config`：查询配置
- `sort_config`：排序配置
- `page_size`：分页大小
- `version`：版本号

## 4. 前端实现逻辑

### 启动流程

组件初始化时：

1. 调用 `getTableConfig({ tableCode })` 拉取表格配置
2. 调用页面传入的 `request` 拉取业务数据

### 查询表单渲染

查询区根据 `config.queryFields` 自动生成。内置支持：

- `input`
- `select`
- `dateRange`
- `date`
- `datetime`
- `time`

未识别 `queryType` 默认降级为 `a-input`。

### 表格列渲染

表格列来自 `config.columns`，映射为 Ant Design Vue 的 `columns`：

- `title -> title`
- `field -> dataIndex/key`
- `width/fixed/align/ellipsis`
- `sortable -> sorter`
- `dictField` 或 `dictCode` 的字典渲染
- `action` 列自动放到最后

### Slot 渲染规则

`FxDynamicTable` 在 `bodyCell` 中优先查找与字段同名的 slot。

### 对外暴露能力

- `getQuery()`：获取查询条件
- `getPage()`：获取分页信息
- `reload()`：重新加载
- `refresh()`：刷新数据

## 5. fallbackConfig 用途

- 后端配置缺失时临时显示
- 某些页面需要前端补充固定列
- 渐进迁移时前端兜底

合并原则：

- 后端配置优先作为主配置
- `fallbackConfig` 可覆盖同字段列定义
- `fallbackConfig` 没声明的后端列仍然保留

## 6. 列设置功能

### 打开列设置时

1. 先使用当前列初始化本地列状态
2. 调用 `/user/columns` 拉取用户列配置
3. 有用户配置则用完整用户列配置回填本地状态

**重点**：列设置面板必须使用完整列集合，隐藏列必须保留在配置数组里（`visible=false`）。

### 保存列设置时

前端只提交最小必要信息：

```ts
{ tableCode, columns: [{ field, visible, order }] }
```

### 保存后刷新逻辑

1. 用 `localColumns` 回写列的 `visible/order`
2. 过滤掉 `visible=false` 的列
3. 触发 `change` 事件通知 `FxDynamicTable`

## 7. 后端配置读取逻辑

### 获取表格配置

`FxTableConfigServiceImpl#getTableConfig()` 处理顺序：

1. 校验 `tableCode` 和 `tenantId`
2. 若有 `userId`，先读用户配置
3. 没有用户配置则读租户配置
4. 租户配置不存在则回退公共配置

### 组装列配置

从 `fx_table_column_config` 组装：

- `enabled=false` 的列不参与组装
- `order_num` 决定基础顺序
- `title_i18n_json` 通过当前语言解析成最终标题
- `queryable=true` 的列会同步生成查询字段

### 用户配置合并

关键原则：

- 基础列集合以租户/公共配置为准
- 用户配置只覆盖 `visible/order`
- 主表格返回时要过滤掉隐藏列
- 列设置接口返回时要保留完整列

## 8. 用户列配置接口

### 获取用户列配置

`POST /sys/common/table/config/user/columns`

### 保存用户列配置

`POST /sys/common/table/config/user/columns/save`

保存时必须基于完整基础列做合并，不能基于"已过滤隐藏列"的结果。

### 重置用户列配置

`POST /sys/common/table/config/user/columns/reset`

逻辑删除用户个性化列配置，恢复为默认。

## 9. 数据查询与配置的关系

公共表格体系只负责"结构配置"，不自动完成所有业务查询。

- `FxDynamicTable` 负责生成查询 UI 和表格 UI
- 业务页面负责把查询条件交给具体业务接口

**注意**：不要误以为"配置了表格列，就自动拥有通用数据接口"。

## 10. 开发新表格流程

### 后端准备

1. 确认业务接口已返回页面所需字段
2. 有衍生展示字段时补 DTO/VO/组装逻辑
3. 在 `fx_table_config` 中新增表定义
4. 在 `fx_table_column_config` 中新增列定义

### 前端接入

1. 页面引入 `FxDynamicTable`
2. 填写 `tableCode`
3. 提供 `request`
4. 有字典列时传入 `dictOptions`
5. 需自定义渲染时按字段名写 slot

### 联调检查

1. 查询表单是否按配置生成
2. 表头是否按 `title_i18n_json` 正确显示
3. 列顺序是否按 `order_num`
4. slot 是否与字段名一致
5. 列设置保存后刷新是否仍然正确