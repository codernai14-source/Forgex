# FxDynamicTable 与列设置

> 分类：前端 / 配置驱动页面  
> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

## 一、概述

Forgex 动态表格核心组件位于：

- `Forgex_MOM/Forgex_Fronted/src/components/common/FxDynamicTable.vue`

它的目标是把后台管理页中最重复的 4 类逻辑统一起来：

1. 查询表单；
2. 表格列定义；
3. 分页与排序；
4. 用户列设置。

配合后端表格配置中心后，业务页面一般只需提供：

- `tableCode`
- `request`
- `dictOptions`（可选）
- 工具栏 / 自定义列插槽（可选）

---

## 二、核心组成

| 层级 | 名称 | 作用 |
|---|---|---|
| 前端主组件 | `FxDynamicTable.vue` | 渲染查询区、表格、分页 |
| 前端列设置 | `ColumnSettingButton.vue` | 用户列显隐、顺序、恢复默认 |
| 前端接口层 | `src/api/system/tableConfig.ts` | 获取表格配置 |
| 后端控制器 | `CommonTableController` | 提供表格配置与列设置接口 |
| 后端服务 | `FxTableConfigService`、`FxUserTableConfigService` | 公共配置 + 用户偏好 |

---

## 三、组件解决的问题

如果没有这套组件，业务页面通常会重复写：

- 查询表单布局
- 表格列配置
- 字典列渲染
- 分页/排序回调
- 用户偏好列显示逻辑

`FxDynamicTable` 让这些能力平台化后，页面实现会稳定很多，尤其适合：

- 用户、角色、租户、部门、消息模板等标准管理页；
- 列和查询条件常变的后台页面；
- 不同租户/不同用户展示偏好不同的页面。

---

## 四、最小接入方式

### 4.1 业务页面传入的关键属性

| 属性 | 必填 | 说明 |
|---|---|---|
| `tableCode` | 是 | 表格配置唯一编码 |
| `request` | 是 | 实际数据查询函数 |
| `dictOptions` | 否 | 下拉 / 字典标签数据 |
| `rowKey` | 否 | 行主键，默认读配置或 `id` |
| `showColumnSetting` | 否 | 是否显示列设置，默认 `true` |
| `showQueryForm` | 否 | 是否显示查询区 |
| `pagination` | 否 | 自定义分页配置；传 `false` 可关闭分页 |

### 4.2 `request` 函数签名

组件会把查询条件整理后按统一格式传入：

```ts
request(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => Promise<{ records: any[]; total: number }>
```

### 4.3 最小示例

```vue
<FxDynamicTable
  ref="tableRef"
  table-code="sys_user_table"
  :request="loadPage"
  :dict-options="dictOptions"
>
  <template #toolbar>
	<a-button type="primary" @click="openCreate">新增</a-button>
  </template>

  <template #action="{ record }">
	<a-space>
	  <a @click="editRow(record)">编辑</a>
	  <a @click="removeRow(record)">删除</a>
	</a-space>
  </template>
</FxDynamicTable>
```

---

## 五、配置加载机制

组件挂载后会先调用：

```text
getTableConfig({ tableCode })
```

然后拿到后端配置中的：

- `columns`
- `queryFields`
- `rowKey`
- `defaultPageSize`

### 5.1 配置来源优先级

组件内部支持把后端配置与前端降级配置合并：

```text
后端配置优先
  + dynamicTableConfig / 降级配置 兜底
```

适合场景：

- 后端配置尚未建全，但页面希望先可运行；
- 某些列前端需要补充展示细节；
- 需要逐步从硬编码表格迁移到配置驱动。

---

## 六、查询区行为

### 6.1 自动渲染的查询类型

当前组件会根据 `queryFields` 中的 `queryType` 自动渲染：

| `queryType` | 渲染组件 |
|---|---|
| `input` | `a-input` |
| `select` | `a-select` |
| `multiSelect` | `a-select` 多选 |
| `dateRange` | `a-range-picker` |
| `date` / `datetime` / `time` | `a-range-picker` |

### 6.2 展开收起规则

- 前 3 个查询项默认首行展示；
- 超过 3 个时显示“展开 / 收起”；
- 查询区展开收起后会重新计算表格可滚动高度。

### 6.3 查询参数标准化

组件会自动：

- 忽略 `undefined / null / ''` 空值；
- 忽略空数组；
- 把日期范围转成 `YYYY-MM-DD HH:mm:ss`。

---

## 七、表格列渲染规则

### 7.1 列基础信息

后端列配置通常会决定：

- `title`
- `field`
- `align`
- `width`
- `fixed`
- `ellipsis`
- `sortable`

### 7.2 操作列位置

如果存在 `field === 'action'` 的列，组件会自动把它放到最后，并默认固定到右侧。

### 7.3 字典列渲染

组件支持两类字典显示：

1. 配置了 `dictField`：直接读取记录上的翻译/样式字段；
2. 配置了 `dictCode`：结合 `dictOptions` 匹配字典项并渲染标签。

它还兼容后端直接返回 JSON 风格的标签对象，例如：

```json
{
  "label": "启用",
  "color": "green",
  "backgroundColor": "#f6ffed",
  "borderColor": "#b7eb8f"
}
```

这意味着前后端可以共同支撑：

- 字典值显示名
- 多语言字典名
- 标签颜色样式

---

## 八、列设置能力

当 `showColumnSetting !== false` 且当前表格存在列配置时，组件会显示右上角列设置按钮。

### 8.1 用户可以做什么

- 控制列显隐
- 调整列顺序
- 保存个人偏好
- 恢复默认列配置

### 8.2 生效机制

```text
ColumnSettingButton 修改列设置
  → 触发 change(columns)
  → FxDynamicTable 更新当前 columns
  → 重新渲染表格
```

用户再次进入页面时，后端用户列配置会继续生效。

---

## 九、分页、排序与滚动

### 9.1 分页

默认内置分页能力：

- `current`
- `pageSize`
- `total`
- `showSizeChanger`
- `showQuickJumper`

如果显式传 `pagination=false`，组件会关闭底部分页器。

### 9.2 排序

表格排序变化后，会把排序参数转换为：

```ts
{ field, order }
```

并重新调用 `request`。

### 9.3 自动滚动高度

组件会根据可用空间自动计算 `scroll.y`，并在以下场景重新计算：

- 窗口 resize
- 查询区展开 / 收起
- 数据量变化
- 配置切换
- DOM 结构变化

这也是它能更稳定适配后台页面高度的重要原因。

---

## 十、国际化与重载

组件会监听前端当前语言变化：

1. 重新加载表格配置；
2. 重新发起数据查询；
3. 让列标题、查询项、字典文案同步切换。

因此它非常适合与后端多语言表格配置中心配合使用。

---

## 十一、暴露给父组件的方法

组件通过 `defineExpose` 向外提供：

| 方法 | 说明 |
|---|---|
| `getQuery()` | 获取当前查询条件 |
| `getPage()` | 获取当前分页信息 |
| `reload()` | 重新加载数据 |
| `refresh()` | 刷新数据（等价于 reload） |

### 11.1 示例

```ts
tableRef.value?.reload()
```

常用于：

- 弹窗保存成功后刷新列表；
- 批量操作后刷新当前页；
- 外部筛选条件变更时触发表格刷新。

---

## 十二、推荐适用场景

最适合：

- 典型后台管理列表页；
- 配置中心、消息模板、导入导出配置页；
- 用户/角色/租户/组织等标准维护页；
- 需要个性化列设置的页面。

不建议强行使用在：

- 完全自由布局的数据看板；
- 强交互、非表格式页面；
- 非常规主从联动、行内复杂编辑页面。

---

## 十三、实践建议

1. **先定义稳定的 `tableCode`**；
2. **列表查询统一经由 `request`，不要在组件外重复做分页拼装**；
3. **字典列优先走后端字典与标签样式配置**；
4. **新增/编辑弹窗推荐配合 `BaseFormDialog`**；
5. **写操作成功后直接 `tableRef.reload()` 即可**。

---

## 十四、关联文档

- [HTTP 请求与消息提示](../请求与反馈/HTTP请求与消息提示.md)
- [公共弹窗](../请求与反馈/公共弹窗.md)
- [公共表格组件使用说明与实现逻辑](../../开发规范/规范文档/公共表格组件使用说明与实现逻辑.md)
- [后端公共能力与核心功能手册](../../后端/后端公共能力与核心功能手册.md)

