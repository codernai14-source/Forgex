# FxDynamicTable 使用示例

## 示例 1：基础用户列表

最简单的用户列表页面，包含基础查询和字典翻译。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="sys_user"
      :request="fetchUserData"
      :dict-options="dictOptions"
    />
  </div>
</template>

<script setup lang="ts">
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getUserList } from '@/api/system/user'

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
  ]
})
</script>
```

---

## 示例 2：带工具栏和自定义操作列

包含新增、批量删除、导出功能的完整 CRUD 页面。

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
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> 新增
          </a-button>
          <a-button 
            @click="handleBatchDelete" 
            :disabled="selectedRowKeys.length === 0"
          >
            批量删除
          </a-button>
          <a-button @click="handleExport">
            导出
          </a-button>
        </a-space>
      </template>
      
      <!-- 操作列自定义 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" @click="handleEdit(record)">
            编辑
          </a-button>
          <a-popconfirm 
            title="确定删除该用户吗？" 
            @confirm="handleDelete(record)"
          >
            <a-button type="link" danger>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
      
      <!-- 用户名自定义链接 -->
      <template #username="{ record }">
        <a @click="handleView(record)">{{ record.username }}</a>
      </template>
      
      <!-- 手机号格式化 -->
      <template #phone="{ record }">
        <a-tag color="blue">{{ formatPhone(record.phone) }}</a-tag>
      </template>
    </FxDynamicTable>
    
    <!-- 用户详情弹窗 -->
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

const dictOptions = ref({
  user_status: [
    { label: '正常', value: 1, color: 'green' },
    { label: '禁用', value: 0, color: 'red' }
  ]
})

const rowSelection = ref({
  selectedRowKeys: [],
  onChange: (keys: any[]) => {
    selectedRowKeys.value = keys
  }
})

const handleAdd = () => {
  currentUser.value = null
  modalVisible.value = true
}

const handleView = (record: any) => {
  currentUser.value = { ...record }
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  currentUser.value = { ...record }
  modalVisible.value = true
}

const handleDelete = async (record: any) => {
  await deleteUser({ id: record.id })
  message.success('删除成功')
  tableRef.value?.reload()
}

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的用户')
    return
  }
  
  await deleteUserBatch({ ids: selectedRowKeys.value })
  message.success('批量删除成功')
  selectedRowKeys.value = []
  tableRef.value?.reload()
}

const handleExport = () => {
  const query = tableRef.value?.getQuery()
  exportUserExcel(query)
}

const handleModalOk = async () => {
  if (currentUser.value?.id) {
    await updateUser(currentUser.value)
    message.success('更新成功')
  } else {
    await createUser(currentUser.value)
    message.success('创建成功')
  }
  tableRef.value?.reload()
}

const formatPhone = (phone: string) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
</script>
```

---

## 示例 3：带复杂查询条件的订单列表

包含多个查询条件、日期范围查询、自定义单元格渲染。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="order_list"
      :request="fetchOrderData"
      :dict-options="dictOptions"
    >
      <!-- 工具栏 -->
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleCreateOrder">
            <PlusOutlined /> 创建订单
          </a-button>
          <a-button @click="handleExport">
            导出订单
          </a-button>
        </a-space>
      </template>
      
      <!-- 订单号自定义 -->
      <template #orderNo="{ record }">
        <a-copyable :text="record.orderNo">
          <a-tag color="blue">{{ record.orderNo }}</a-tag>
        </a-copyable>
      </template>
      
      <!-- 订单状态带图标 -->
      <template #orderStatus="{ record }">
        <a-tag :color="getStatusColor(record.orderStatus)">
          <template #icon>
            <CheckCircleOutlined v-if="record.orderStatus === 'COMPLETED'" />
            <ClockCircleOutlined v-else-if="record.orderStatus === 'PENDING'" />
            <CloseCircleOutlined v-else-if="record.orderStatus === 'CANCELLED'" />
          </template>
          {{ getDictLabel(dictOptions.order_status, record.orderStatus) }}
        </a-tag>
      </template>
      
      <!-- 金额格式化 -->
      <template #amount="{ record }">
        <span class="amount-text (record.amount) }}
      </template>
      
      <!-- 操作列 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" @click="handleViewDetail(record)">
            详情
          </a-button>
          <a-button 
            v-if="record.orderStatus === 'PENDING'" 
            type="link" 
            @click="handleCancel(record)"
          >
            取消
          </a-button>
        </a-space>
      </template>
    </FxDynamicTable>
    
    <!-- 订单详情弹窗 -->
    <OrderDetailModal
      v-model:open="detailVisible"
      :order-id="currentOrderId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  PlusOutlined, 
  CheckCircleOutlined, 
  ClockCircleOutlined, 
  CloseCircleOutlined 
} from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import OrderDetailModal from './components/OrderDetailModal.vue'
import { getOrderList, cancelOrder } from '@/api/order/order'

const detailVisible = ref(false)
const currentOrderId = ref<string>('')

const fetchOrderData = async (payload: any) => {
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
  const res = await getOrderList(params)
  return {
    records: res.data.records,
    total: res.data.total
  }
}

const dictOptions = ref({
  order_status: [
    { label: '待支付', value: 'PENDING', color: 'orange' },
    { label: '已完成', value: 'COMPLETED', color: 'green' },
    { label: '已取消', value: 'CANCELLED', color: 'red' },
    { label: '退款中', value: 'REFUNDING', color: 'blue' }
  ],
  payment_method: [
    { label: '支付宝', value: 'ALIPAY', color: 'blue' },
    { label: '微信支付', value: 'WECHAT', color: 'green' },
    { label: '银行卡', value: 'BANK', color: 'orange' }
  ]
})

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    PENDING: 'orange',
    COMPLETED: 'green',
    CANCELLED: 'red',
    REFUNDING: 'blue'
  }
  return colorMap[status] || 'default'
}

const getDictLabel = (dict: any[], value: any) => {
  const item = dict.find(d => d.value === value)
  return item ? item.label : value
}

const handleCreateOrder = () => {
  // 跳转到创建订单页面
  router.push('/order/create')
}

const handleViewDetail = (record: any) => {
  currentOrderId.value = record.id
  detailVisible.value = true
}

const handleCancel = async (record: any) => {
  await cancelOrder({ id: record.id })
  message.success('取消成功')
}

const handleExport = () => {
  // 调用导出 API
  exportOrderExcel()
}
</script>

<style scoped>
.amount {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 14px;
}
</style>
```

---

## 示例 4：树形表格（部门管理）

展示树形结构数据的部门管理页面。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="sys_dept"
      :request="fetchDeptData"
      :expandable="expandableConfig"
      :default-expand-all-rows="true"
      :pagination="false"
    >
      <!-- 工具栏 -->
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> 新增部门
          </a-button>
          <a-button @click="handleExpandAll">
            展开全部
          </a-button>
          <a-button @click="handleCollapseAll">
            收起全部
          </a-button>
        </a-space>
      </template>
      
      <!-- 部门名称带图标 -->
      <template #deptName="{ record }">
        <span>
          <template v-if="record.children && record.children.length > 0">
            <FolderOutlined style="color: #1890ff; margin-right: 8px;" />
          </template>
          <template v-else>
            <FileOutlined style="color: #52c41a; margin-right: 8px;" />
          </template>
          {{ record.deptName }}
        </span>
      </template>
      
      <!-- 操作列 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" @click="handleEdit(record)">
            编辑
          </a-button>
          <a-button type="link" @click="handleAddChild(record)">
            添加子部门
          </a-button>
          <a-popconfirm 
            title="确定删除该部门吗？" 
            @confirm="handleDelete(record)"
          >
            <a-button type="link" danger>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </FxDynamicTable>
    
    <!-- 部门编辑弹窗 -->
    <DeptModal
      v-model:open="modalVisible"
      :dept="currentDept"
      @ok="handleModalOk"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { 
  PlusOutlined, 
  FolderOutlined, 
  FileOutlined 
} from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import DeptModal from './components/DeptModal.vue'
import { getDeptList, createDept, updateDept, deleteDept } from '@/api/system/dept'

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const modalVisible = ref(false)
const currentDept = ref<any>(null)
const expandAll = ref(true)

const fetchDeptData = async (payload: any) => {
  const { query } = payload
  const res = await getDeptList(query)
  
  // 树形数据不需要分页
  return {
    records: res.data,
    total: res.data.length
  }
}

const expandableConfig = ref({
  childrenColumnName: 'children',
  indentSize: 20,
  expandedRowKeys: []
})

const handleAdd = () => {
  currentDept.value = { parentId: null }
  modalVisible.value = true
}

const handleEdit = (record: any) => {
  currentDept.value = { ...record }
  modalVisible.value = true
}

const handleAddChild = (record: any) => {
  currentDept.value = { parentId: record.id, parentName: record.deptName }
  modalVisible.value = true
}

const handleDelete = async (record: any) => {
  await deleteDept({ id: record.id })
  message.success('删除成功')
  tableRef.value?.reload()
}

const handleExpandAll = () => {
  expandAll.value = true
  // 设置所有节点为展开状态
  // 需要获取完整的树形数据并提取所有 ID
}

const handleCollapseAll = () => {
  expandAll.value = false
  expandableConfig.value.expandedRowKeys = []
}

const handleModalOk = async () => {
  if (currentDept.value?.id) {
    await updateDept(currentDept.value)
    message.success('更新成功')
  } else {
    await createDept(currentDept.value)
    message.success('创建成功')
  }
  tableRef.value?.reload()
}
</script>
```

---

## 示例 5：带降级配置的表格

在后端配置未就绪时使用降级配置。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="product_list"
      :request="fetchProductData"
      :fallback-config="fallbackConfig"
      :dict-options="dictOptions"
    >
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增产品
        </a-button>
      </template>
      
      <template #productImage="{ record }">
        <a-image 
          :width="60" 
          :src="record.productImage" 
          :preview="true"
        />
      </template>
      
      <template #price="{ record }">
        <span class="price">¥{{ record.price.toFixed(2) }}</span>
      </template>
      
      <template #stock="{ record }">
        <a-tag :color="record.stock > 100 ? 'green' : record.stock > 0 ? 'orange' : 'red'">
          {{ record.stock > 100 ? '充足' : record.stock > 0 ? '紧张' : '缺货' }}
          ({{ record.stock }})
        </a-tag>
      </template>
      
      <template #action="{ record }">
        <a-space>
          <a-button type="link" @click="handleEdit(record)">编辑</a-button>
          <a-button type="link" @click="handleStock(record)">补货</a-button>
        </a-space>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getProductList } from '@/api/product/product'

const fetchProductData = async (payload: any) => {
  const { page, query } = payload
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  const res = await getProductList(params)
  return {
    records: res.data.records,
    total: res.data.total
  }
}

/**
 * 降级配置（后端配置未就绪时使用）
 */
const fallbackConfig = ref({
  tableName: '产品列表',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'productCode', title: '产品编码', width: 120, fixed: 'left' },
    { field: 'productName', title: '产品名称', width: 200 },
    { field: 'productImage', title: '产品图片', width: 80 },
    { field: 'category', title: '分类', width: 100, dictCode: 'product_category' },
    { field: 'price', title: '价格', width: 100, sortable: true },
    { field: 'stock', title: '库存', width: 100, sortable: true },
    { field: 'status', title: '状态', width: 80, dictCode: 'product_status' },
    { field: 'action', title: '操作', width: 180, fixed: 'right' }
  ],
  queryFields: [
    { field: 'productCode', label: '产品编码', queryType: 'input' },
    { field: 'productName', label: '产品名称', queryType: 'input' },
    { field: 'category', label: '分类', queryType: 'select', dictCode: 'product_category' },
    { field: 'status', label: '状态', queryType: 'select', dictCode: 'product_status' },
    { field: 'createTime', label: '创建时间', queryType: 'dateRange' }
  ]
})

const dictOptions = ref({
  product_category: [
    { label: '电子产品', value: 'ELECTRONICS', color: 'blue' },
    { label: '服装鞋帽', value: 'CLOTHING', color: 'pink' },
    { label: '食品饮料', value: 'FOOD', color: 'green' },
    { label: '家居用品', value: 'HOME', color: 'orange' }
  ],
  product_status: [
    { label: '上架', value: 'ON_SALE', color: 'green' },
    { label: '下架', value: 'OFF_SALE', color: 'red' },
    { label: '预售', value: 'PRE_SALE', color: 'blue' }
  ]
})
</script>

<style scoped>
.price {
  color: #ff4d4f;
  font-weight: bold;
  font-size: 16px;
}
</style>
```

---

## 示例 6：外部控制加载状态

在复杂操作时外部控制表格加载状态。

```vue
<template>
  <div class="page-container">
    <a-space style="margin-bottom: 16px;">
      <a-button @click="handleBatchImport" :loading="importLoading">
        <ImportOutlined /> 批量导入
      </a-button>
      <a-button @click="handleSync" :loading="syncLoading">
        <SyncOutlined spin /> 同步数据
      </a-button>
    </a-space>
    
    <FxDynamicTable
      ref="tableRef"
      table-code="material_list"
      :request="fetchMaterialData"
      :loading="externalLoading"
    >
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增物料
        </a-button>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  PlusOutlined, 
  ImportOutlined, 
  SyncOutlined 
} from '@ant-design/icons-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getMaterialList, batchImport, syncData } from '@/api/material/material'

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const importLoading = ref(false)
const syncLoading = ref(false)

const fetchMaterialData = async (payload: any) => {
  const { page, query } = payload
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  const res = await getMaterialList(params)
  return {
    records: res.data.records,
    total: res.data.total
  }
}

/**
 * 外部控制的加载状态
 */
const externalLoading = computed(() => {
  return importLoading.value || syncLoading.value
})

const handleBatchImport = async () => {
  importLoading.value = true
  try {
    // 打开文件选择对话框
    const file = await selectFile()
    await batchImport({ file })
    message.success('导入成功')
    tableRef.value?.reload()
  } catch (error) {
    console.error('导入失败:', error)
    message.error('导入失败')
  } finally {
    importLoading.value = false
  }
}

const handleSync = async () => {
  syncLoading.value = true
  try {
    await syncData()
    message.success('同步成功')
    tableRef.value?.reload()
  } catch (error) {
    console.error('同步失败:', error)
    message.error('同步失败')
  } finally {
    syncLoading.value = false
  }
}

const handleAdd = () => {
  // 新增逻辑
}

const selectFile = (): Promise<File> => {
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = '.xlsx,.xls'
    input.onchange = (e: any) => {
      const file = e.target.files[0]
      if (file) {
        resolve(file)
      } else {
        reject(new Error('未选择文件'))
      }
    }
    input.click()
  })
}
</script>
```

---

## 示例 7：自定义行主键

使用复合主键或自定义主键的场景。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="order_item"
      :request="fetchOrderItemData"
      :row-key="(record) => `${record.orderId}-${record.skuCode}`"
      :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button @click="handleBatchUpdate">
          批量修改
        </a-button>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getOrderItemList, batchUpdateOrderItems } from '@/api/order/orderItem'

const selectedRows = ref<any[]>([])

const fetchOrderItemData = async (payload: any) => {
  const { page, query } = payload
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  const res = await getOrderItemList(params)
  return {
    records: res.data.records,
    total: res.data.total
  }
}

const rowSelection = ref({
  selectedRowKeys: [],
  onChange: (keys: any[], rows: any[]) => {
    selectedRows.value = rows
  }
})

const handleBatchUpdate = async () => {
  if (selectedRows.value.length === 0) {
    message.warning('请选择要修改的订单明细')
    return
  }
  
  // 批量修改逻辑
  await batchUpdateOrderItems({
    items: selectedRows.value.map(item => ({
      orderId: item.orderId,
      skuCode: item.skuCode,
      // ...其他字段
    }))
  })
  
  message.success('批量修改成功')
}
</script>
```

---

## 示例 8：服务端排序和筛选

完全由后端控制排序和筛选的场景。

```vue
<template>
  <div class="page-container">
    <FxDynamicTable
      table-code="sales_report"
      :request="fetchSalesData"
      :pagination="{
        pageSizeOptions: ['20', '50', '100'],
        showTotal: (total) => `共 ${total} 条记录`
      }"
    >
      <template #toolbar>
        <a-space>
          <a-button @click="handleExport">
            导出报表
          </a-button>
          <a-range-picker v-model:value="dateRange" @change="handleDateChange" />
        </a-space>
      </template>
      
      <!-- 金额列右对齐并带千分位 -->
      <template #amount="{ record }">
        <span style="text-align: right; display: block;">
          {{ formatCurrency(record.amount) }}
        </span>
      </template>
      
      <!-- 百分比列带颜色 -->
      <template #growthRate="{ record }">
        <span :style="{ color: record.growthRate > 0 ? '#52c41a' : '#ff4d4f' }">
          {{ record.growthRate > 0 ? '+' : '' }}{{ record.growthRate }}%
        </span>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getSalesReport } from '@/api/report/sales'

const dateRange = ref<[Dayjs, Dayjs]>()

const fetchSalesData = async (payload: any) => {
  const { page, query, sorter } = payload
  
  const params: any = {
    pageNum: page.current,
    pageSize: page.pageSize,
    ...query
  }
  
  // 添加日期范围
  if (dateRange.value) {
    params.startDate = dateRange.value[0].format('YYYY-MM-DD')
    params.endDate = dateRange.value[1].format('YYYY-MM-DD')
  }
  
  // 添加排序参数（服务端排序）
  if (sorter?.field) {
    params.sortField = sorter.field
    params.sortOrder = sorter.order === 'ascend' ? 'ASC' : 'DESC'
  }
  
  const res = await getSalesReport(params)
  return {
    records: res.data.records,
    total: res.data.total
  }
}

const handleDateChange = () => {
  // 日期变化时重新加载数据
  // FxDynamicTable 会自动触发查询
}

const handleExport = () => {
  const query = {
    startDate: dateRange.value?.[0].format('YYYY-MM-DD'),
    endDate: dateRange.value?.[1].format('YYYY-MM-DD')
  }
  exportSalesReport(query)
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}
</script>
```

---

## 示例选择指南

| 场景 | 推荐示例 |
|------|----------|
| 简单列表 | 示例 1：基础用户列表 |
| 完整 CRUD | 示例 2：带工具栏和自定义操作列 |
| 复杂查询 | 示例 3：带复杂查询条件的订单列表 |
| 树形结构 | 示例 4：树形表格（部门管理） |
| 后端配置未就绪 | 示例 5：带降级配置的表格 |
| 复杂操作 | 示例 6：外部控制加载状态 |
| 复合主键 | 示例 7：自定义行主键 |
| 数据报表 | 示例 8：服务端排序和筛选 |
