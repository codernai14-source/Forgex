# FxDynamicTable 使用方式

> 分类：前端 / 配置驱动页面
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文档说明页面开发时如何真正使用 `FxDynamicTable`，重点覆盖“代码如何接入”和“页面如何使用”两部分。

## 代码位置

### 组件与类型

```text
Forgex_MOM/Forgex_Fronted/src/components/common/FxDynamicTable.vue
Forgex_MOM/Forgex_Fronted/src/api/system/tableConfig.ts
```

### 参考页面

```text
Forgex_MOM/Forgex_Fronted/src/views/integrationPlatform/thirdSystem/index.vue
Forgex_MOM/Forgex_Fronted/src/views/system/messageTemplate/index.vue
Forgex_MOM/Forgex_Fronted/src/views/system/menu/index.vue
```

## 页面接入步骤

标准接入顺序建议如下：

1. 先确定 `tableCode`
2. 确认后端已经有对应表格配置
3. 页面实现 `request(payload)` 方法
4. 页面准备好 `dictOptions`
5. 需要自定义渲染的列再补同名插槽

## 最小接入示例

```vue
<template>
  <FxDynamicTable
    ref="tableRef"
    table-code="ThirdSystemTable"
    :request="handleRequest"
    :dict-options="dictOptions"
    row-key="id"
  >
    <template #toolbar>
      <a-button type="primary" @click="openAddDialog">新增</a-button>
    </template>

    <template #status="{ record }">
      <a-switch
        :checked="record.status === 1"
        @change="(checked) => handleStatusChange(record, checked)"
      />
    </template>

    <template #action="{ record }">
      <a-space>
        <a @click="openEditDialog(record)">编辑</a>
        <a @click="handleDelete(record.id)">删除</a>
      </a-space>
    </template>
  </FxDynamicTable>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getThirdSystemList } from '@/api/system/integration'

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()

const dictOptions = {
  thirdSystemStatus: [
    { label: '启用', value: 1 },
    { label: '停用', value: 0 },
  ],
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const res = await getThirdSystemList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  })

  return {
    records: res.records || [],
    total: Number(res.total || 0),
  }
}

function reloadTable() {
  tableRef.value?.refresh?.()
}
</script>
```

## request 怎么写

这是页面最核心的一段代码。
`FxDynamicTable` 不关心你的业务接口长什么样，所以页面要负责把统一 payload 转成后端需要的参数。

### 输入结构

```ts
payload = {
  page: { current, pageSize },
  query,
  sorter,
}
```

### 推荐转换方式

```ts
async function handleRequest(payload) {
  return api.page({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
    sortField: payload.sorter?.field,
    sortOrder: payload.sorter?.order,
  })
}
```

### 返回结构要求

页面最终必须返回：

```ts
{
  records: any[]
  total: number
}
```

如果你的业务接口返回的是：

```ts
{
  data: {
    records: [],
    total: 100
  }
}
```

那就需要在页面里自行拆出来后再返回。

## 页面如何使用查询区

查询区是自动生成的，不需要在页面再额外手写一套查询表单。
页面开发者真正要做的是：

- 在后端表格配置里把查询字段配出来
- 在页面把 `dictOptions` 准备好
- 在 `request` 中正确消费 `payload.query`

适合放到 `queryFields` 的条件通常有：

- 名称关键字
- 状态
- 类型
- 时间范围

## 页面如何使用工具栏

工具栏通过 `toolbar` 插槽扩展。

适合放在这里的按钮有：

- 新增
- 批量删除
- 导出
- 拉取公共配置

示例：

```vue
<template #toolbar>
  <a-space>
    <a-button type="primary" @click="handleAdd">新增</a-button>
    <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
      批量删除
    </a-button>
  </a-space>
</template>
```

## 页面如何使用自定义列

如果某列只是普通文本，不需要页面处理。
如果某列要显示：

- 开关
- 标签
- 操作按钮
- 自定义格式

就使用同名插槽。

例如列字段是 `action`：

```vue
<template #action="{ record }">
  <FxActionGroup :actions="getRowActions(record)" />
</template>
```

## 列设置与列宽

公共列设置入口默认显示在表格工具栏右侧。用户可以：

- 勾选或取消勾选列，控制显示 / 隐藏。
- 拖拽列名前的排序手柄调整显示顺序。
- 在列宽输入框中设置宽度，保存后刷新仍生效。
- 直接拖拽表头右侧手柄实时调整列宽，释放鼠标后自动持久化。
- 直接拖拽表头文字区域调整列顺序，释放到目标列后自动持久化。

列设置保存到用户个性化配置 `fx_user_table_config.column_config`，结构包含 `field`、`visible`、`order`、`width`。列宽允许范围为 `60px` 到 `800px`。`action` 操作列由组件固定到最右侧，不开放拖拽改宽或换位。

## 操作列按钮规则

表格 `action` 列统一使用 `src/components/common/FxActionGroup.vue` 表达行操作。页面传入 `ActionItem[]`，组件会先按 `permission`、`hidden` 过滤，再按传入顺序决定常用操作优先级。

- 权限过滤后操作数 `<= 3` 时，全部平铺显示。
- 权限过滤后操作数 `> 3` 时，只显示前 3 个操作 + 1 个“更多”下拉。
- 操作列最多只出现 4 个可见控件，避免按钮换行、挤压表格或影响固定列宽度。
- 删除等危险操作设置 `danger: true`，禁用态设置 `disabled: true`，不要在页面里额外写换行类样式。
- 如果行操作需要被页面引导直接定位，可以在 `ActionItem` 中传 `guideId`。被折叠到“更多”下拉里的操作默认不适合作为引导目标；必要时在页面上调大 `max-inline`。

```ts
import FxActionGroup, { type ActionItem } from '@/components/common/FxActionGroup.vue'

function getRowActions(record: any): ActionItem[] {
  return [
    { key: 'edit', label: '编辑', permission: 'xxx:edit', guideId: 'demo-row-edit', onClick: () => handleEdit(record) },
    { key: 'status', label: '启用/停用', permission: 'xxx:edit', onClick: () => handleStatus(record) },
    { key: 'detail', label: '详情', permission: 'xxx:view', onClick: () => handleDetail(record) },
    { key: 'delete', label: '删除', permission: 'xxx:delete', danger: true, onClick: () => handleDelete(record.id) },
  ]
}
```

## 页面引导锚点

`FxDynamicTable` 已内置系统页面引导需要的稳定锚点：

| 锚点 | 位置 |
|---|---|
| `fx-table-query` | 查询区 |
| `fx-table-toolbar` | 工具栏 |
| `fx-table-toolbar-left` | 工具栏左侧业务操作 |
| `fx-table-column-setting` | 列设置 |
| `fx-table-content` | 表格内容 |
| `fx-table-pagination` | 分页区 |

系统管理模块页面如果使用 `FxDynamicTable`，页面级引导可以直接复用这些公共锚点。页面自己的新增、导入、导出、批量删除等按钮，需要在业务页面上补 `data-guide-id`。

例如列字段是 `status`：

```vue
<template #status="{ record }">
  <a-tag :color="record.status ? 'green' : 'red'">
    {{ record.status ? '启用' : '停用' }}
  </a-tag>
</template>
```

## 页面如何使用列设置

默认情况下，`FxDynamicTable` 会自动显示列设置按钮。
页面通常不需要自己实现“列显隐、列顺序”的 UI。

页面开发时需要注意的只有两点：

1. 列字段名必须稳定，不要今天叫 `status`，明天改成 `statusText`
2. 同一个业务页面要固定使用同一个 `tableCode`

否则用户保存过的个性化配置会失效。

## 页面如何刷新数据

组件暴露了以下方法：

```ts
tableRef.value?.reload?.()
tableRef.value?.refresh?.()
tableRef.value?.getQuery?.()
tableRef.value?.getPage?.()
```

常见使用场景：

- 新增成功后刷新列表
- 编辑成功后刷新列表
- 删除成功后刷新列表
- 弹窗关闭后重新加载当前页数据

## 页面如何使用 dynamicTableConfig

如果后端配置还没补全，或者当前页面只想局部覆盖几列，可以传 `dynamicTableConfig`。

示例：

```ts
const dynamicTableConfig = {
  columns: [
    { field: 'systemCode', title: '系统编码', width: 180, visible: true },
    { field: 'action', title: '操作', width: 180, fixed: 'right', visible: true },
  ],
  queryFields: [
    { field: 'systemCode', label: '系统编码', queryType: 'input', queryOperator: 'like' },
  ],
}
```

使用方式：

```vue
<FxDynamicTable
  table-code="ThirdSystemTable"
  :request="handleRequest"
  :dynamic-table-config="dynamicTableConfig"
/>
```

注意：这更适合补充，不适合长期替代后端配置。

## 页面如何在左树右表场景使用

例如组织树筛选用户列表时，推荐写法如下：

```ts
const currentDeptId = ref('')

function handleDeptSelect(keys: string[], node: any) {
  currentDeptId.value = String(node?.id || '')
  tableRef.value?.refresh?.()
}

async function handleRequest(payload) {
  return getUserList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    deptId: currentDeptId.value || undefined,
    ...payload.query,
  })
}
```

这样筛选条件和列表能力仍然统一收口在 `FxDynamicTable`。

## 页面使用建议

- 列表页优先统一使用 `FxDynamicTable`
- 页面不要再自己维护一套重复分页状态
- 查询参数尽量通过 `payload.query` 统一传递
- 保存、删除后由页面主动调用 `refresh()`
- 自定义列尽量少而精，避免把所有列都改成插槽
- 页面外层如果还有标题、工具条、页签等固定区域，外层容器需要使用 `height: 100%`、`display: flex`、`flex-direction: column`、`overflow: hidden`，并让 `FxDynamicTable` 所在区域 `flex: 1`、`min-height: 0`。否则动态表格按父容器高度计算时，容易叠加固定区域高度导致底部出现大块空白或页面整体滚动。

## 常见排查

### 页面看不到新增按钮

优先检查：

1. 是否写了 `toolbar` 插槽
2. 是否被页面权限指令隐藏

### 列设置按钮不显示

优先检查：

1. `showColumnSetting` 是否被设成 `false`
2. 当前配置是否成功加载到 `columns`

### 列表数据为空

优先检查：

1. `request` 是否返回了 `{ records, total }`
2. 是否把 `payload.page.current/pageSize` 正确转给后端

### 查询后表格不刷新

优先检查：

1. `request` 是否真正使用了 `payload.query`
2. 是否在外部又维护了另一份分页和查询状态，导致混乱

## 关联文档

- [FxDynamicTable实现逻辑](./FxDynamicTable实现逻辑.md)
- [部门树与组织选择使用方式](../组件与页面/部门树与组织选择使用方式.md)
