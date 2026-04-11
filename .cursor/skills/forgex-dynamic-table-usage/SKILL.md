---
name: forgex-dynamic-table-usage
description: Forgex 项目 FxDynamicTable 动态表格组件使用指南，包括配置驱动、查询表单、列设置、字典翻译、分页排序等。当开发列表页面、使用 FxDynamicTable 组件、配置表格列、处理字典翻译或实现查询功能时使用。
---

# FxDynamicTable 动态表格组件使用指南

## 一、组件概述

### 1.1 核心特性

`FxDynamicTable` 是 Forgex 项目的**核心公共表格组件**，采用**配置驱动**设计理念：

- **配置驱动**：通过 `tableCode` 从后端获取表格配置，动态生成查询表单和表格列
- **内置查询表单**：支持多种查询类型（输入框、下拉选择、日期范围等）
- **智能布局**：查询条件自动换行，支持展开/收起更多搜索条件
- **列设置**：内置列显示/隐藏配置功能，支持用户个性化设置
- **字典翻译**：自动根据字典配置渲染 Tag 标签，支持自定义样式
- **分页排序**：内置分页和排序功能，支持自定义分页配置
- **响应式高度**：自动计算表格高度，适应不同屏幕尺寸
- **国际化支持**：支持多语言切换，自动重新加载配置

### 1.2 适用场景

- ✅ 标准 CRUD 列表页面
- ✅ 带复杂查询条件的数据列表
- ✅ 需要用户自定义列显示的场景
- ✅ 需要字典翻译和状态标签的场景

---

## 二、快速开始

### 2.1 基础用法

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="sys_user"
      :request="fetchData"
      :dict-options="dictOptions"
    />
  </div>
</template>

<script setup lang="ts">
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { ref } from 'vue'

/**
 * 获取表格数据
 */
const fetchData = async (payload: any) => {
  const { page, query, sorter } = payload
  // 调用后端 API
  const res = await getUserList({
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query,
    sortField: sorter?.field,
    sortOrder: sorter?.order === 'ascend' ? 'asc' : 'desc'
  })
  return {
    records: res.data.records,
    total: res.data.total
  }
}

/**
 * 字典选项配置
 */
const dictOptions = ref({
  userStatus: [
    { label: '正常', value: 1, color: 'green' },
    { label: '禁用', value: 0, color: 'red' }
  ]
})
</script>
```

### 2.2 核心 Props

| 属性名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `tableCode` | string | ✅ | - | 表格编码，用于从后端获取表格配置 |
| `request` | Function | ✅ | - | 数据请求函数，接收分页、查询、排序参数 |
| `dictOptions` | Record | ❌ | - | 字典选项配置，用于下拉选择和标签渲染 |
| `fallbackConfig` | Partial<FxTableConfig> | ❌ | - | 降级配置，当获取表格配置失败时使用 |
| `rowKey` | string \| Function | ❌ | 'id' | 行主键字段名或函数 |
| `loading` | boolean | ❌ | false | 加载状态（外部控制） |
| `pagination` | TableProps['pagination'] \| false | ❌ | - | 分页配置，设为 false 关闭分页 |
| `rowSelection` | TableProps['rowSelection'] | ❌ | - | 行选择配置 |
| `showQueryForm` | boolean | ❌ | true | 是否显示查询表单 |
| `showColumnSetting` | boolean | ❌ | true | 是否显示列设置按钮 |

---

## 三、后端配置规范

### 3.1 表格配置表结构

**fx_table_config（表格主配置表）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| table_code | varchar | 表格编码（主键） |
| table_name | varchar | 表格名称 |
| table_type | varchar | 表格类型（NORMAL/树形等） |
| row_key | varchar | 行主键字段名 |
| default_page_size | int | 默认每页条数 |
| version | int | 版本号 |

**fx_table_column_config（表格列配置表）**：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| table_code | varchar | 表格编码（外键） |
| field | varchar | 字段名 |
| title | varchar | 列标题 |
| width | int | 列宽度 |
| align | varchar | 对齐方式（left/center/right） |
| fixed | varchar | 固定列（left/right） |
| sortable | boolean | 是否可排序 |
| dict_code | varchar | 字典编码 |
| dict_field | varchar | 字典翻译字段 |
| ellipsis | boolean | 是否省略 |
| visible | boolean | 是否默认显示 |
| sort | int | 排序号 |

### 3.2 配置示例

```sql
-- 插入表格主配置
INSERT INTO fx_table_config (table_code, table_name, table_type, row_key, default_page_size, version)
VALUES ('sys_user', '系统用户表', 'NORMAL', 'id', 20, 1);

-- 插入表格列配置
INSERT INTO fx_table_column_config (table_code, field, title, width, align, sortable, dict_code, visible, sort)
VALUES 
('sys_user', 'username', '用户名', 120, 'left', false, null, true, 1),
('sys_user', 'nickname', '昵称', 120, 'left', false, null, true, 2),
('sys_user', 'email', '邮箱', 180, 'left', false, null, true, 3),
('sys_user', 'phone', '手机号', 120, 'left', false, null, true, 4),
('sys_user', 'status', '状态', 80, 'center', true, 'user_status', true, 5),
('sys_user', 'action', '操作', 200, 'center', false, null, true, 6);
```

### 3.3 查询字段配置

**fx_table_query_field（查询字段配置表）**：

| queryType | 组件类型 | 说明 |
|-----------|----------|------|
| `input` | 输入框 | 文本输入，支持模糊查询 |
| `select` | 下拉选择 | 从字典中选取，需配置 `dictCode` |
| `date` | 日期选择 | 选择单个日期 |
| `datetime` | 日期时间选择 | 选择单个日期时间 |
| `dateRange` | 日期范围选择 | 选择开始和结束日期 |
| `time` | 时间选择 | 选择单个时间 |

```sql
-- 插入查询字段配置
INSERT INTO fx_table_query_field (table_code, field, label, query_type, dict_code, sort)
VALUES 
('sys_user', 'username', '用户名', 'input', null, 1),
('sys_user', 'status', '状态', 'select', 'user_status', 2),
('sys_user', 'createTime', '创建时间', 'dateRange', null, 3),
('sys_user', 'email', '邮箱', 'input', null, 4);
```

---

## 四、详细使用说明

### 4.1 数据请求函数（request）

`request` 函数是组件与后端数据交互的核心接口，必须返回包含 `records` 和 `total` 的对象。

#### 4.1.1 函数签名

```typescript
type RequestFunction = (payload: {
  page: { 
    current: number;      // 当前页码
    pageSize: number;     // 每页条数
  }
  query: Record<string, any>;  // 查询条件
  sorter?: { 
    field?: string;       // 排序字段
    order?: string;       // 排序方向（ascend/descend）
  }
}) => Promise<{ 
  records: any[];         // 数据列表
  total: number;          // 总条数
}>
```

#### 4.1.2 完整示例

```typescript
import { getUserList } from '@/api/system/user'

const fetchData = async (payload: any) => {
  const { page, query, sorter } = payload
  
  // 构建请求参数
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  
  // 添加排序参数
  if (sorter?.field) {
    params.sortField = sorter.field
    params.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }
  
  // 调用后端 API
  const res = await getUserList(params)
  
  // 返回标准格式
  return {
    records: res.data.records,
    total: res.data.total
  }
}
```

#### 4.1.3 注意事项

1. **返回值格式**：必须返回 `{ records: [], total: 0 }` 格式，否则数据无法正确显示
2. **空值处理**：如果后端可能返回 null/undefined，需要做兼容处理
3. **错误处理**：建议在 API 层统一处理错误，组件层不处理具体错误

### 4.2 字典选项（dictOptions）

字典选项用于下拉选择框和状态标签的渲染，支持自定义颜色和样式。

#### 4.2.1 基本结构

```typescript
const dictOptions = ref({
  // key: 字典编码或字段名
  userStatus: [
    { 
      label: '正常',      // 显示文本
      value: 1,           // 实际值
      color: 'green',     // 标签颜色（可选）
      tagStyle: {         // 自定义样式（可选）
        color: '#52c41a',
        borderColor: '#52c41a',
        backgroundColor: '#f6ffed'
      }
    },
    { 
      label: '禁用', 
      value: 0, 
      color: 'red',
      tagStyle: {
        color: '#ff4d4f',
        borderColor: '#ff4d4f',
        backgroundColor: '#fff1f0'
      }
    }
  ]
})
```

#### 4.2.2 字典渲染优先级

组件会按以下优先级渲染字典标签：

1. **dictField 字段**：如果列配置了 `dictField`，优先使用该字段的值（支持 JSON 字符串或对象）
2. **自动翻译字段**：如果列配置了 `dictCode`，优先使用 `${field}Text` 字段的值
3. **原字段 JSON**：兼容后端直接返回 JSON 字符串的情况
4. **字典匹配**：根据值在字典选项中查找匹配的项
5. **默认值**：如果都未匹配，直接显示原字段值

### 4.3 列设置功能

#### 4.3.1 开启/关闭列设置

列设置功能默认开启，无需额外配置：

```vue
<!-- 默认开启 -->
<FxDynamicTable
  table-code="sys_user"
  :request="fetchData"
/>

<!-- 显式关闭 -->
<FxDynamicTable
  table-code="sys_user"
  :request="fetchData"
  :show-column-setting="false"
/>
```

#### 4.3.2 列配置存储

用户自定义的列配置（显示/隐藏、顺序、宽度）会保存到 `fx_table_column_config` 表中，按用户维度隔离。

### 4.4 分页配置

#### 4.4.1 默认分页

组件内置分页功能，默认配置：

```typescript
{
  current: 1,           // 当前页
  pageSize: 20,         // 每页条数（可从表格配置读取）
  total: 0,             // 总条数
  showSizeChanger: true,    // 显示每页条数切换
  showQuickJumper: true,    // 显示快速跳转
  showTotal: (total) => `共 ${total} 条`,  // 显示总数
  hideOnSinglePage: false   // 单页时不隐藏（固定为 false）
}
```

#### 4.4.2 自定义分页

```vue
<FxDynamicTable
  table-code="sys_user"
  :request="fetchData"
  :pagination="{
    pageSizeOptions: ['10', '20', '50', '100'],
    showTotal: (total) => `总共 ${total} 条数据`
  }"
/>
```

#### 4.4.3 关闭分页

```vue
<FxDynamicTable
  table-code="sys_user"
  :request="fetchData"
  :pagination="false"
/>
```

### 4.5 自定义单元格内容

#### 4.5.1 使用插槽自定义

```vue
<template>
  <FxDynamicTable
    table-code="sys_user"
    :request="fetchData"
  >
    <!-- 针对特定列的自定义插槽 -->
    <template #username="{ record }">
      <a>{{ record.username }}</a>
    </template>
    
    <!-- 操作列自定义 -->
    <template #action="{ record }">
      <a-button type="link">编辑</a-button>
      <a-button type="link" danger>删除</a-button>
    </template>
    
    <!-- 通用单元格插槽（兜底） -->
    <template #bodyCell="{ record, column }">
      {{ record[column.dataIndex] }}
    </template>
  </FxDynamicTable>
</template>
```

#### 4.5.2 插槽优先级

1. **列名插槽**：如 `#username`、`#action`，优先级最高
2. **通用插槽**：`#bodyCell`，兜底使用

### 4.6 工具栏插槽

```vue
<template>
  <FxDynamicTable
    table-code="sys_user"
    :request="fetchData"
  >
    <!-- 工具栏插槽（与列设置并排显示） -->
    <template #toolbar>
      <a-button type="primary" @click="handleAdd">
        <PlusOutlined /> 新增
      </a-button>
      <a-button @click="handleBatchDelete">
        批量删除
      </a-button>
      <a-button @click="handleExport">
        导出
      </a-button>
    </template>
  </FxDynamicTable>
</template>
```

---

## 五、进阶用法

### 5.1 降级配置（fallbackConfig）

当后端配置获取失败时，可以使用 `fallbackConfig` 提供降级配置。

```vue
<script setup lang="ts">
const fallbackConfig = ref({
  tableName: '用户列表',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'username', title: '用户名', width: 120 },
    { field: 'nickname', title: '昵称', width: 120 },
    { field: 'email', title: '邮箱', width: 180 },
    { field: 'status', title: '状态', width: 80, dictCode: 'user_status' },
    { field: 'action', title: '操作', width: 200, fixed: 'right' }
  ],
  queryFields: [
    { field: 'username', label: '用户名', queryType: 'input' },
    { field: 'status', label: '状态', queryType: 'select', dictCode: 'user_status' }
  ]
})
</script>

<template>
  <FxDynamicTable
    table-code="sys_user"
    :request="fetchData"
    :fallback-config="fallbackConfig"
  />
</template>
```

### 5.2 获取组件实例方法

通过 `ref` 获取组件实例，调用暴露的方法：

```vue
<template>
  <div>
    <FxDynamicTable
      ref="tableRef"
      table-code="sys_user"
      :request="fetchData"
    />
    <a-button @click="handleRefresh">刷新数据</a-button>
    <a-button @click="handleExport">导出当前查询条件</a-button>
  </div>
</template>

<script setup lang="ts">
const tableRef = ref<InstanceType<typeof FxDynamicTable>>()

const handleRefresh = () => {
  tableRef.value?.reload()
}

const handleExport = () => {
  const query = tableRef.value?.getQuery()
  const page = tableRef.value?.getPage()
  console.log('查询条件:', query)
  console.log('分页信息:', page)
  // 调用导出 API
}
</script>
```

#### 5.2.1 暴露的方法

| 方法名 | 返回值 | 说明 |
|--------|--------|------|
| `getQuery()` | Record<string, any> | 获取当前查询条件 |
| `getPage()` | { current, pageSize, total } | 获取当前分页信息 |
| `reload()` | Promise | 重新加载数据（保持当前分页和查询条件） |
| `refresh()` | Promise | 刷新数据（同 reload） |

### 5.3 行选择功能

```vue
<template>
  <FxDynamicTable
    table-code="sys_user"
    :request="fetchData"
    :row-selection="rowSelection"
  />
</template>

<script setup lang="ts">
const rowSelection = ref({
  selectedRowKeys: [],
  onChange: (selectedRowKeys: any[], selectedRows: any[]) => {
    console.log('选中了', selectedRows)
  }
})
</script>
```

### 5.4 固定列和表头滚动

```vue
<template>
  <FxDynamicTable
    table-code="sys_user"
    :request="fetchData"
    :scroll="{
      y: 500,  // 固定高度 500px（可选，不传则自动计算）
      x: 1200  // 固定宽度，超出显示横向滚动条
    }"
  />
</template>
```

### 5.5 树形表格

```vue
<template>
  <FxDynamicTable
    table-code="sys_menu"
    :request="fetchMenuData"
    :expandable="{
      childrenColumnName: 'children',
      indentSize: 20
    }"
    :default-expand-all-rows="false"
  />
</template>
```

---

## 六、最佳实践

### 6.1 性能优化建议

#### 6.1.1 避免频繁重新渲染

- 使用 `configVersion` 控制刷新时机，避免不必要的 computed 重新计算
- 列设置使用 computed 固定引用，避免 props 引用变化导致下拉层反复卸载

```typescript
const columnSettingColumns = computed(() => config.value?.columns ?? [])
```

#### 6.1.2 使用 requestAnimationFrame 优化滚动计算

```typescript
let computeScrollYRafPending = false

const scheduleComputeAutoScrollY = () => {
  if (computeScrollYRafPending) return
  computeScrollYRafPending = true
  requestAnimationFrame(() => {
    computeScrollYRafPending = false
    computeAutoScrollY()
  })
}
```

### 6.2 开发建议

#### 6.2.1 后端配置优先

- 优先使用后端配置表格和列，避免硬编码
- 使用 `fallbackConfig` 作为降级方案，而非主要配置方式

#### 6.2.2 字典翻译规范

- 后端返回数据时，优先使用 `${field}Text` 字段存储字典翻译结果
- 支持 JSON 字符串格式：`{"label": "正常", "color": "green"}`
- 前端 `dictOptions` 作为补充，用于兜底匹配

#### 6.2.3 查询条件设计

- 常用查询条件（3 个以内）放在第一行
- 不常用的查询条件放在展开行
- 日期范围查询使用 `dateRange` 类型

---

## 七、常见问题排查

### 7.1 表格列不显示

**原因**：
1. 后端未配置列信息
2. `tableCode` 配置错误
3. 降级配置未生效

**解决方案**：
- 检查 `fx_table_column_config` 表是否有对应配置
- 确认 `tableCode` 与后端一致
- 添加 `fallbackConfig` 降级配置

### 7.2 表格高度异常

**原因**：
1. 父容器未设置高度
2. 自动计算高度失败
3. 固定了 `scroll.y` 但未生效

**解决方案**：
- 确保父容器有明确高度（如 `height: 100%`）
- 检查 `tableWrapRef` 是否正确引用
- 显式传入 `scroll` 属性

### 7.3 字典标签不显示

**原因**：
1. `dictCode` 配置错误
2. `dictOptions` 未配置对应字典
3. 后端未返回字典翻译字段

**解决方案**：
- 检查列配置的 `dictCode` 是否正确
- 在 `dictOptions` 中补充字典配置
- 后端添加字典翻译逻辑

### 7.4 查询条件不生效

**原因**：
1. 查询字段未配置
2. 查询条件值为空被过滤
3. 日期格式不正确

**解决方案**：
- 检查 `fx_table_query_field` 表配置
- 确认查询条件有值
- 检查日期范围是否为数组格式

---

## 八、完整示例：用户管理列表

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="sys_user"
      :request="fetchUserData"
      :dict-options="dictOptions"
      :row-selection="rowSelection"
    >
      <!-- 工具栏 -->
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增用户
        </a-button>
        <a-button @click="handleBatchDelete" :disabled="selectedRowKeys.length === 0">
          批量删除
        </a-button>
        <a-button @click="handleExport">
          导出
        </a-button>
      </template>
      
      <!-- 操作列自定义 -->
      <template #action="{ record }">
        <a-button type="link" @click="handleEdit(record)">编辑</a-button>
        <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
      </template>
      
      <!-- 用户名自定义 -->
      <template #username="{ record }">
        <a>{{ record.username }}</a>
      </template>
    </FxDynamicTable>
    
    <!-- 新增/编辑弹窗 -->
    <UserModal
      v-model:open="modalVisible"
      :user="currentUser"
      @ok="handleModalOk"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import UserModal from './components/UserModal.vue'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/system/user'

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const modalVisible = ref(false)
const currentUser = ref<any>(null)
const selectedRowKeys = ref<any[]>([])

/**
 * 获取用户数据
 */
const fetchUserData = async (payload: any) => {
  const { page, query, sorter } = payload
  
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  
  if (sorter?.field) {
    params.sortField = sorter.field
    params.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }
  
  const res = await getUserList(params)
  
  return {
    records: res.data.records,
    total: res.data.total
  }
}

/**
 * 字典选项
 */
const dictOptions = ref({
  user_status: [
    { label: '正常', value: 1, color: 'green' },
    { label: '禁用', value: 0, color: 'red' }
  ],
  gender: [
    { label: '男', value: 1, color: 'blue' },
    { label: '女', value: 2, color: 'pink' }
  ]
})

/**
 * 行选择配置
 */
const rowSelection = ref({
  selectedRowKeys: [],
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  }
})

/**
 * 新增用户
 */
const handleAdd = () => {
  currentUser.value = null
  modalVisible.value = true
}

/**
 * 编辑用户
 */
const handleEdit = (record: any) => {
  currentUser.value = { ...record }
  modalVisible.value = true
}

/**
 * 删除用户
 */
const handleDelete = async (record: any) => {
  await deleteUser({ id: record.id })
  tableRef.value?.reload()
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  // 调用批量删除 API
  tableRef.value?.reload()
}

/**
 * 导出
 */
const handleExport = () => {
  const query = tableRef.value?.getQuery()
  console.log('导出查询条件:', query)
  // 调用导出 API
}

/**
 * 弹窗确认
 */
const handleModalOk = async () => {
  if (currentUser.value?.id) {
    await updateUser(currentUser.value)
  } else {
    await createUser(currentUser.value)
  }
  tableRef.value?.reload()
}
</script>

<style scoped>
.page-container {
  height: 100%;
  padding: 16px;
}
</style>
```

---

## 九、相关资源

- **组件源码**：`d:\mine_product\forgex\Forgex_MOM\Forgex_Fronted\src\components\common\FxDynamicTable.vue`
- **详细使用指南**：`d:\mine_product\forgex\doc\开发规范\使用指南\FxDynamicTable 动态表格组件使用指南.md`
- **配置开发指南**：见 [dynamic-table-config](../dynamic-table-config/SKILL.md)
- **问题排查专家**：见 [forgex-dynamic-table-troubleshooting](../forgex-dynamic-table-troubleshooting/SKILL.md)
