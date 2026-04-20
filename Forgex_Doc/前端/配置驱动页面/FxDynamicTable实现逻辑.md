# FxDynamicTable 实现逻辑

> 分类：前端 / 配置驱动页面  
> 版本：**V0.6.0**  
> 更新时间：**2026-04-17**

本文档说明 `FxDynamicTable` 在 Forgex 中的真实实现方式，包括配置来源、前后端调用链路、列设置合并逻辑，以及实际代码所在位置。

## 功能定位

`FxDynamicTable.vue` 是 Forgex 后台列表页的公共表格容器。它解决的不是“如何查某一张业务表”，而是“如何把绝大多数列表页共性的查询区、表格区、分页区、列设置区统一实现”。

它主要承担以下职责：

- 根据 `tableCode` 拉取后端表格配置
- 根据配置自动生成查询表单和表格列
- 将页面的业务请求统一收口为 `request(payload)` 协议
- 处理分页、排序、字典列、插槽渲染
- 对接用户个性化列设置
- 在桌面端页面中自动计算表格滚动高度

页面层只需要关心两件事：

- 当前业务数据从哪个接口取
- 哪些列需要用插槽自定义展示

## 代码位置

### 前端核心组件

```text
Forgex_MOM/Forgex_Fronted/src/components/common/FxDynamicTable.vue
Forgex_MOM/Forgex_Fronted/src/components/common/ColumnSettingButton.vue
Forgex_MOM/Forgex_Fronted/src/api/system/tableConfig.ts
```

### 前端实际页面入口

```text
Forgex_MOM/Forgex_Fronted/src/views/integrationPlatform/thirdSystem/index.vue
Forgex_MOM/Forgex_Fronted/src/views/system/messageTemplate/index.vue
Forgex_MOM/Forgex_Fronted/src/views/system/menu/index.vue
```

### 后端配置服务

```text
Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/service/table/impl/FxTableConfigServiceImpl.java
Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/service/table/impl/FxUserTableConfigServiceImpl.java
```

## 核心对象与字段

前端表格配置类型定义在：

```text
Forgex_MOM/Forgex_Fronted/src/api/system/tableConfig.ts
```

最关键的结构有三个：

- `FxTableConfig`
  负责描述整张表的基础信息，例如 `tableCode`、`rowKey`、`defaultPageSize`
- `FxTableColumn`
  负责描述列本身，例如 `field`、`title`、`width`、`sortable`、`dictCode`
- `FxTableQueryField`
  负责描述查询条件，例如 `field`、`queryType`、`queryOperator`

这三组对象决定了页面最终能不能“自动长出一个列表页”。

## 前后端调用链路

完整链路如下：

```text
页面组件
  -> 传入 tableCode 和 request
  -> FxDynamicTable 加载表格配置
  -> getTableConfig('/sys/common/table/config/get')
  -> 后端 FxTableConfigServiceImpl 按 用户配置 > 租户配置 > 公共配置 查找
  -> 返回 columns / queryFields / rowKey / defaultPageSize
  -> FxDynamicTable 渲染查询区、表格区、分页区
  -> 页面提供的 request 再去请求真实业务数据
  -> 表格渲染 records
```

这也是 Forgex 列表页“配置驱动 + 页面请求解耦”的核心。

## 配置加载逻辑

`FxDynamicTable.vue` 挂载后会先执行 `loadConfig()`。

实现上做了几件事：

1. 根据 `props.tableCode` 调用 `getTableConfig({ tableCode })`
2. 如果页面传了 `dynamicTableConfig`，则执行前后端配置合并
3. 归一化列定义，确保每一列都有 `field`
4. 初始化分页大小与查询模型
5. 刷新 `configVersion`，强制依赖配置的计算属性重新渲染

这里有一个很关键的设计：页面传入的 `dynamicTableConfig` 不是替代后端配置，而是补充或覆盖。  
也就是说，系统允许：

- 后端维护公共默认配置
- 页面局部修补某几列
- 用户再在页面上保存自己的列偏好

## 配置合并逻辑

配置合并在 `mergeConfigs()` 中完成。

合并原则是：

- 表级字段以“后端配置优先，前端补充缺省”为主
- 列配置按 `field` 合并
- 查询配置按 `field` 合并
- 页面新增但后端未配置的列，也会被补进结果中

这样做的原因是：

- 后端配置是统一治理入口
- 页面仍然可以在特定场景下快速补列或补查询项
- 不会因为只改了一小段前端配置，就把整份后端配置覆盖掉

## 查询区生成逻辑

查询区不是写死的，而是根据 `config.queryFields` 动态渲染。

当前内置支持的查询控件类型包括：

- `input`
- `select`
- `multiSelect`
- `dateRange`
- `date`
- `datetime`
- `time`

对应的字典选项通过 `dictOptions` 注入。  
例如某个查询字段配置了 `dictCode: 'status'`，则组件会读取：

```ts
dictOptions.status
```

生成下拉选项。

提交查询时，组件会先执行 `normalizeQuery()`，把日期范围统一转换为字符串数组，再传给页面的 `request(payload)`。

## 表格列生成逻辑

表格列由 `tableColumns` 计算属性生成。

这里主要做了几件事：

- 将后端返回的 `FxTableColumn` 转为 Ant Design Vue `columns`
- 自动补上 `dataIndex`、`key`、`sorter`
- 当列配置里有 `dictField` 或 `dictCode` 时，自动生成字典渲染逻辑
- 强制把 `action` 列放到最后，且默认固定到右侧

这意味着页面只有在“默认字典展示不够用”时，才需要写插槽。

## 插槽渲染逻辑

组件内部表格渲染使用的是“列名同名插槽”策略：

```vue
<template #status="{ record }">
  ...
</template>
```

只要插槽名和列字段 `field` 一致，`FxDynamicTable` 就会优先使用页面插槽渲染该列。

这条规则很重要，因为很多“列不显示自定义内容”的问题，最终都出在这里：

- 后端字段叫 `status`
- 页面却写成了 `#statusText`

这种情况下插槽不会生效。

## request 协议设计

页面传给 `FxDynamicTable` 的不是 URL，而是一个函数：

```ts
request(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => Promise<{ records: any[]; total: number }>
```

组件负责把：

- 查询条件
- 当前页
- 每页条数
- 排序字段

整理成统一结构，再交给页面去调用业务接口。

这样设计的原因是：

- `FxDynamicTable` 不需要知道每个模块接口地址
- 页面可以自由适配不同后端分页参数，例如 `pageNum/pageSize`
- 表格能力与业务接口格式解耦

## 分页与排序逻辑

组件内部维护 `pagination.current`、`pagination.pageSize`、`pagination.total`。

运行流程如下：

1. 初始化后按照默认页大小请求第一页
2. 点击查询时重置到第一页
3. 切换页码或页大小时触发重新查询
4. 点击表头排序时把 sorter 一并传给 `request`

如果页面没有显式传 `pagination=false`，组件会自动渲染分页器。

## 列设置与用户个性化配置

列设置按钮来自：

```text
Forgex_MOM/Forgex_Fronted/src/components/common/ColumnSettingButton.vue
```

其本质是对接用户列偏好配置接口：

```text
/sys/common/table/config/user/columns
/sys/common/table/config/user/columns/save
/sys/common/table/config/user/columns/reset
```

后端在 `FxUserTableConfigServiceImpl.java` 中负责：

- 保存用户的列显隐和顺序
- 与租户级 / 公共级基础配置合并
- 过滤掉 `visible=false` 的列

优先级是：

```text
用户配置 > 租户配置 > 公共配置
```

这也是为什么同一个 `tableCode` 在不同用户界面上，列显示可能不完全一样。

## 自适应高度逻辑

`FxDynamicTable.vue` 除了表格配置能力，还做了一个比较重的 UI 能力：自动计算滚动高度。

核心思路是：

- 监听容器尺寸变化
- 监听表格 DOM 结构变化
- 计算表头、分页、工具栏之外剩余空间
- 自动给 `scroll.y` 赋值

这样页面不需要手动写每张表的高度，尤其适合：

- 卡片布局页面
- 左右分栏页面
- 高度撑满的管理后台页面

## 为什么这样设计

### 1. 避免每个列表页重复造轮子

如果每个页面都自己写查询区、表格列、分页、列设置，维护成本会很高。

### 2. 让表格结构可以被后端治理

列顺序、列标题、查询项、默认页大小都能通过后端统一配置，适合多租户平台持续演进。

### 3. 给页面保留足够的业务扩展点

通过 `request`、`dictOptions`、同名插槽、`dynamicTableConfig`，页面仍然能灵活扩展。

## 常见排查

### 页面没有显示列

优先检查：

1. `field` 是否和后端返回字段一致
2. 该列是否被用户列设置隐藏
3. 后端配置是否没有启用该列

### 插槽没有生效

优先检查：

1. 插槽名是否和列 `field` 完全一致
2. 当前列是否真的存在于 `config.columns`

### 查询条件不生效

优先检查：

1. 查询字段是否配置在 `queryFields`
2. 页面 `request` 是否把 `payload.query` 正确转给后端
3. 日期范围是否需要在后端按数组接收

### 列设置保存后无变化

优先检查：

1. `tableCode` 是否正确
2. 列字段名是否稳定
3. 是否切换了用户或租户，导致读取到不同层级配置

## 关联文档

- [FxDynamicTable使用方式](./FxDynamicTable使用方式.md)
- [公共弹窗使用方式](../请求与反馈/公共弹窗使用方式.md)
