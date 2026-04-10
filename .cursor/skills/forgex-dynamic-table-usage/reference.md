# FxDynamicTable 实现原理与 API 参考

## 一、组件架构

### 1.1 组件结构

```
FxDynamicTable
├── 查询区域卡片（a-card）
│   └── 查询表单（a-form）
│       ├── 第一行查询条件（最多 3 个）
│       ├── 展开行查询条件（超过 3 个时显示）
│       └── 操作按钮区（搜索/重置/展开收起）
└── 表格区域卡片（a-card）
    ├── 工具栏（toolbar 插槽 + 列设置按钮）
    ├── 表格内容区（a-table）
    │   ├── 表头
    │   ├── 表体
    │   └── 自定义单元格插槽
    └── 分页器（a-pagination）
```

### 1.2 响应式数据

```typescript
// 表格配置
const config = ref<FxTableConfig>()

// 配置版本号（用于强制刷新 computed）
const configVersion = ref(0)

// 表格数据
const tableData = ref<any[]>([])

// 分页信息
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

// 查询模型
const queryModel = reactive<Record<string, any>>({})

// 搜索区域展开状态
const isQueryExpanded = ref(false)

// 加载状态
const loading = ref(false)

// 表格容器引用
const tableWrapRef = ref<HTMLElement | null>(null)

// 自动计算的滚动高度
const autoScrollY = ref<number | undefined>(undefined)
```

---

## 二、核心 Computed

### 2.1 表格列配置（tableColumns）

```typescript
const tableColumns = computed(() => {
  // 依赖 configVersion 强制刷新
  const _ = configVersion.value
  const cols: FxTableColumn[] = config.value?.columns || []
  
  // 将操作列移到最后
  const actionCol = cols.find(c => c.field === 'action')
  const otherCols = cols.filter(c => c.field !== 'action')
  const finalCols = actionCol ? [...otherCols, actionCol] : otherCols
  
  return finalCols.map(c => {
    const column: any = {
      title: c.title,
      dataIndex: c.field,
      key: c.field,
      align: c.align,
      width: c.width,
      fixed: c.field === 'action' ? (c.fixed || 'right') : c.fixed,
      ellipsis: c.ellipsis,
      sorter: !!c.sortable,
    }
    
    // 字典字段渲染
    if (c.dictField) {
      column.customRender = ({ record }: any) => {
        const dictText = record?.[c.dictField]
        return renderTagByDictText(dictText, record?.[c.field])
      }
    }
    // 字典代码渲染
    else if (c.dictCode) {
      column.customRender = ({ record }: any) => {
        const autoDictField = `${c.field}Text`
        const fromTextField = renderTagByDictText(record?.[autoDictField], undefined)
        if (fromTextField !== undefined) {
          return fromTextField
        }
        
        const value = record?.[c.field]
        const fromValueJson = tryRenderTagFromDictJson(value)
        if (fromValueJson !== undefined) {
          return fromValueJson
        }
        
        const dictItems = props.dictOptions?.[c.dictCode] || []
        const dictItem = dictItems.find((item: any) => String(item.value) === String(value))
        
        if (dictItem) {
          return h(ATag, { color: dictItem.tagStyle?.color || dictItem.color || 'blue' }, dictItem.label)
        }
        return value
      }
    }
    
    return column
  })
})
```

**关键点**：
- 依赖 `configVersion` 实现强制刷新
- 自动将操作列移到最后一列
- 根据字典配置自动添加 `customRender` 函数
- 支持多种字典翻译格式（JSON 字符串、对象、字段映射）

### 2.2 表格滚动配置（resolvedScroll）

```typescript
const resolvedScroll = computed(() => {
  const baseScroll = props.scroll || {}
  const columns = tableColumns.value || []
  
  // 计算所有列的总宽度
  let totalWidth = 0
  for (const col of columns as any[]) {
    const w = col?.width
    if (typeof w === 'number' && Number.isFinite(w)) {
      totalWidth += w
      continue
    }
    if (typeof w === 'string') {
      const parsed = Number.parseFloat(w)
      if (Number.isFinite(parsed)) {
        totalWidth += parsed
        continue
      }
    }
    // 默认宽度 160px
    totalWidth += 160
  }
  
  // X 轴滚动：使用列总宽度
  const x = (baseScroll as any)?.x ?? totalWidth
  
  // Y 轴滚动：使用自动计算高度或用户指定值
  const baseY = (baseScroll as any)?.y
  const y = baseY === undefined ? autoScrollY.value : baseY
  
  return { ...(baseScroll as any), x, y }
})
```

### 2.3 分页配置（resolvedPaginationConfig）

```typescript
const resolvedPaginationConfig = computed(() => {
  // 检查是否显式传入 pagination
  if (isPaginationPropPassed.value && props.pagination === false) return undefined
  
  const base = {
    current: pagination.current,
    pageSize: pagination.pageSize,
    total: pagination.total,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => String(t('common.total', { total })),
    hideOnSinglePage: false,  // 固定为 false，单页也显示分页
  }
  
  // 合并用户配置
  if (!isPaginationPropPassed.value || props.pagination === undefined) return base as any
  if (typeof props.pagination === 'object') {
    return { ...base, ...(props.pagination as any), hideOnSinglePage: false } as any
  }
  return base as any
})
```

---

## 三、核心方法

### 3.1 加载配置（loadConfig）

```typescript
async function loadConfig() {
  try {
    // 从后端获取表格配置
    const backendConfig = await getTableConfig({ tableCode: props.tableCode })
    
    // 合并后端配置和降级配置
    const mergedConfig = mergeConfigs(backendConfig, props.fallbackConfig)
    
    // 创建新对象，确保响应式更新
    config.value = {
      ...mergedConfig,
      columns: mergedConfig.columns ? [...mergedConfig.columns] : [],
      queryFields: mergedConfig.queryFields ? [...mergedConfig.queryFields] : []
    }
    
    // 增加版本号，强制刷新 computed
    configVersion.value++
    
  } catch (e) {
    console.error('[FxDynamicTable] 获取表格配置失败:', e)
    // 使用降级配置或默认空配置
    if (props.fallbackConfig) {
      config.value = { ...props.fallbackConfig } as any
    } else {
      config.value = {
        tableCode: props.tableCode,
        tableName: '默认表格',
        tableType: 'NORMAL',
        rowKey: 'id',
        defaultPageSize: 20,
        columns: [],
        queryFields: []
      }
    }
    configVersion.value++
  }
  
  // 标准化列配置
  if (config.value?.columns?.length) {
    config.value.columns = normalizeColumns(config.value.columns as any)
  }
  
  // 设置默认分页大小
  pagination.pageSize = config.value?.defaultPageSize || pagination.pageSize
  
  // 初始化查询模型
  for (const q of config.value?.queryFields || []) {
    if (!(q.field in queryModel)) {
      queryModel[q.field] = undefined
    }
  }
}
```

### 3.2 数据查询（handleQuery）

```typescript
async function handleQuery(sorter?: any) {
  loading.value = true
  try {
    const res = await props.request({
      page: { current: pagination.current, pageSize: pagination.pageSize },
      query: normalizeQuery(),
      sorter: normalizeSorter(sorter),
    })
    
    // 兼容不同响应格式
    const anyRes: any = res as any
    const records = anyRes?.records ?? anyRes?.data ?? []
    const total = anyRes?.total ?? 0
    
    // 更新表格数据
    tableData.value = Array.isArray(records) ? records : []
    pagination.total = typeof total === 'number' ? total : parseInt(String(total) || '0', 10)
    
  } finally {
    loading.value = false
    await nextTick()
    // 重新计算表格高度
    scheduleComputeAutoScrollY()
  }
}
```

### 3.3 格式化查询条件（normalizeQuery）

```typescript
function normalizeQuery() {
  const out: Record<string, any> = {}
  if (!config.value) return out
  
  for (const q of config.value.queryFields || []) {
    const v = queryModel[q.field]
    
    // 跳过空值
    if (v === undefined || v === null || v === '') continue
    
    // 处理日期范围
    if ((q.queryType === 'dateRange' || q.queryType === 'date' || q.queryType === 'datetime' || q.queryType === 'time') 
        && Array.isArray(v) && v.length === 2) {
      out[q.field] = [
        dayjs(v[0]).format('YYYY-MM-DD HH:mm:ss'),
        dayjs(v[1]).format('YYYY-MM-DD HH:mm:ss')
      ]
      continue
    }
    
    out[q.field] = v
  }
  return out
}
```

### 3.4 计算自动滚动高度（computeAutoScrollY）

```typescript
function computeAutoScrollY() {
  const baseScroll = props.scroll as any
  // 如果用户指定了 y 值，则清除自动计算值
  if (baseScroll && baseScroll.y !== undefined) {
    if (autoScrollY.value !== undefined) {
      autoScrollY.value = undefined
    }
    return
  }
  
  const wrapEl = tableWrapRef.value
  if (!wrapEl) {
    autoScrollY.value = undefined
    return
  }
  
  // 获取容器高度
  const rect = wrapEl.getBoundingClientRect()
  if (!Number.isFinite(rect.height)) {
    autoScrollY.value = undefined
    return
  }
  
  const available = rect.height
  
  // 获取表格头部高度
  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0
  
  // 计算可用高度（容器高度 - 表头高度 - 缓冲）
  const buffer = 2
  const y = Math.floor(available - headerHeight - buffer)
  
  const nextY = y > 100 ? y : undefined  // 最小高度 100px
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}
```

---

## 四、配置合并逻辑

### 4.1 配置合并策略

```typescript
function mergeConfigs(
  backendConfig: FxTableConfig | undefined, 
  fallbackConfig: Partial<FxTableConfig> | undefined
): FxTableConfig {
  if (!backendConfig) return fallbackConfig as any
  if (!fallbackConfig) return backendConfig
  
  const merged: FxTableConfig = { ...(backendConfig as any), ...(fallbackConfig as any) }
  
  // 合并列配置：降级配置优先，后端配置补充
  const backendColumns = normalizeColumns(backendConfig.columns || [])
  const fallbackColumns = normalizeColumns((fallbackConfig.columns || []) as any[])
  
  if (fallbackColumns.length) {
    const backendMap = new Map<string, FxTableColumn>()
    for (const bc of backendColumns) {
      backendMap.set(bc.field, bc)
    }
    
    // 以降级的列为基础，合并后端配置
    const mergedColumns: FxTableColumn[] = fallbackColumns.map(fc => {
      const bc = backendMap.get(fc.field)
      const mergedCol: any = { ...(bc as any), ...(fc as any) }
      
      // fixed 属性以降级配置为准
      if ('fixed' in (fc as any)) {
        mergedCol.fixed = (fc as any).fixed
      } else {
        mergedCol.fixed = undefined
      }
      
      return mergedCol
    })
    
    // 添加后端独有的列
    const fallbackFieldSet = new Set(fallbackColumns.map(c => c.field))
    for (const bc of backendColumns) {
      if (!fallbackFieldSet.has(bc.field)) {
        mergedColumns.push(bc)
      }
    }
    
    merged.columns = mergedColumns
  } else {
    merged.columns = backendColumns
  }
  
  // 合并查询字段：逻辑同列配置
  // ...
  
  return merged
}
```

**合并原则**：
1. **降级配置优先**：同名字段以降级的配置为准
2. **后端配置补充**：降级没有的字段使用后端配置
3. **fixed 属性特殊处理**：以降级配置为准，避免后端固定列配置干扰

---

## 五、响应式监听

### 5.1 监听 tableCode 变化

```typescript
watch(
  () => props.tableCode,
  async () => {
    pagination.current = 1
    await loadConfig()
    await handleQuery(lastSorter.value)
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)
```

### 5.2 监听语言变化

```typescript
watch(
  () => locale.value,
  async (newLocale, oldLocale) => {
    console.log('[FxDynamicTable] 语言切换，重新加载配置:', newLocale)
    // 重新加载配置（后端返回多语言配置）
    await loadConfig()
    // 重新加载数据
    await handleQuery(lastSorter.value)
  },
)
```

### 5.3 其他监听器

```typescript
// 监听分页信息变化，重新计算表格高度
watch(
  pagination,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
  { deep: true }
)

// 监听查询区域展开状态，重新计算表格高度
watch(
  () => isQueryExpanded.value,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

// 监听数据变化，重新计算表格高度
watch(
  () => tableData.value.length,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)
```

---

## 六、生命周期

### 6.1 挂载时（onMounted）

```typescript
onMounted(async () => {
  // 监听窗口大小变化
  window.addEventListener('resize', onResizeOrScroll, { passive: true })
  
  // 加载配置
  await loadConfig()
  
  // 加载数据
  await handleQuery()
  
  // 监听 DOM 变化（确保表格高度正确计算）
  const wrapEl = tableWrapRef.value
  if (wrapEl) {
    const observer = new MutationObserver(() => {
      scheduleComputeAutoScrollY()
    })
    
    observer.observe(wrapEl, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
    
    onBeforeUnmount(() => {
      observer.disconnect()
    })
  }
  
  // 延迟计算，确保 DOM 完全渲染
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 100)
  
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 300)
})
```

### 6.2 卸载前（onBeforeUnmount）

```typescript
onBeforeUnmount(() => {
  window.removeEventListener('resize', onResizeOrScroll as any)
})
```

---

## 七、样式说明

### 7.1 组件布局结构

```css
.fx-dynamic-table {
  display: flex;
  flex-direction: column;
  gap: 16px;              /* 查询区域和表格区域间距 */
  height: 100%;           /* 占满父容器高度 */
  min-height: 0;          /* 允许 flex 子项缩小 */
}
```

### 7.2 查询区域样式

```css
.fx-query-row {
  display: flex;
  justify-content: space-between;  /* 查询条件和按钮区左右分布 */
  align-items: center;
  flex-wrap: nowrap;
}

.fx-query-row-content {
  display: flex;
  flex-wrap: wrap;                 /* 查询条件自动换行 */
  gap: 16px;                       /* 查询条件间距 */
  flex: 1;
  max-width: calc(100% - 200px);   /* 为按钮区预留空间 */
}

.fx-query-actions-row {
  display: flex;
  align-items: center;
  gap: 16px;                       /* 按钮间距 */
  white-space: nowrap;
}
```

### 7.3 表格区域样式

```css
.fx-table-card {
  flex: 1 1 auto;          /* 占满剩余空间 */
  min-height: 0;           /* 允许缩小 */
}

.fx-table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1 1 auto;
  height: 100%;
}

.fx-table-toolbar-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
  padding: 12px 16px 0 16px;
  margin-bottom: 16px;
  flex-shrink: 0;          /* 不允许缩小 */
  overflow-x: auto;        /* 工具栏内容过多时横向滚动 */
}

.fx-table-toolbar-left {
  flex: 1;                 /* 左侧工具栏占满剩余空间 */
  min-width: 0;
}

.fx-table-toolbar-right {
  flex-shrink: 0;
  margin-left: auto;       /* 列设置按钮靠右对齐 */
}

.fx-table-content {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;          /* 占满剩余空间 */
  min-height: 0;
  height: 100%;
  padding: 0 16px 16px 16px;
  box-sizing: border-box;
}
```

### 7.4 表格容器样式

```css
.fx-dynamic-table-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;      /* 纵向滚动由 a-table 的 scroll.y 控制 */
  min-width: 0;
  min-height: 0;
}

/* 确保表格内部元素也支持 flex 布局 */
.fx-dynamic-table-wrap :deep(.ant-spin-nested-loading),
.fx-dynamic-table-wrap :deep(.ant-spin-container),
.fx-dynamic-table-wrap :deep(.ant-table),
.fx-dynamic-table-wrap :deep(.ant-table-container) {
  flex: 1 1 auto;
  min-height: 0;
}
```

### 7.5 分页器样式

```css
.fx-table-pagination {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-end;
  padding: 12px 0 0 0;     /* 顶部 12px 间距 */
}

.fx-table-pagination :deep(.ant-pagination) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;  /* 分页器右对齐 */
  gap: 16px;
}
```

---

## 八、类型定义

### 8.1 FxTableConfig

```typescript
interface FxTableConfig {
  tableCode: string;           // 表格编码
  tableName: string;           // 表格名称
  tableType: string;           // 表格类型（NORMAL/树形等）
  rowKey: string;              // 行主键字段
  defaultPageSize: number;     // 默认每页条数
  columns: FxTableColumn[];    // 列配置
  queryFields: FxQueryField[]; // 查询字段配置
  version: number;             // 版本号
}
```

### 8.2 FxTableColumn

```typescript
interface FxTableColumn {
  field: string;               // 字段名
  title: string;               // 列标题
  width?: number;              // 列宽度
  align?: 'left' | 'center' | 'right';  // 对齐方式
  fixed?: 'left' | 'right';    // 固定列
  sortable?: boolean;          // 是否可排序
  dictCode?: string;           // 字典编码
  dictField?: string;          // 字典翻译字段
  ellipsis?: boolean;          // 是否省略
  visible?: boolean;           // 是否默认显示
  sort?: number;               // 排序号
}
```

### 8.3 FxQueryField

```typescript
interface FxQueryField {
  field: string;               // 字段名
  label: string;               // 查询标签
  queryType: 'input' | 'select' | 'date' | 'datetime' | 'dateRange' | 'time';
  dictCode?: string;           // 字典编码（select 类型需要）
  sort?: number;               // 排序号
}
```

---

## 九、后端 API

### 9.1 获取表格配置

```typescript
// src/api/system/tableConfig.ts
export function getTableConfig(params: { tableCode: string }): Promise<FxTableConfig>
```

**请求示例**：

```typescript
const config = await getTableConfig({ tableCode: 'sys_user' })
```

**响应示例**：

```json
{
  "tableCode": "sys_user",
  "tableName": "系统用户表",
  "tableType": "NORMAL",
  "rowKey": "id",
  "defaultPageSize": 20,
  "columns": [
    {
      "field": "username",
      "title": "用户名",
      "width": 120,
      "align": "left"
    }
  ],
  "queryFields": [
    {
      "field": "username",
      "label": "用户名",
      "queryType": "input"
    }
  ],
  "version": 1
}
```
