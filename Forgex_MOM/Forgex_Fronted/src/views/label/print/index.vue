<template>
  <div class="page-container">
    <a-card title="标签打印" :bordered="false">
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <!-- 打印类型 -->
        <a-form-item label="打印类型" required>
          <a-select v-model:value="formData.templateType" placeholder="请选择标签类型" @change="handleTypeChange">
            <a-select-option value="INCOMING">来料标签</a-select-option>
            <a-select-option value="PRODUCT">产品标签</a-select-option>
            <a-select-option value="LOT">LOT批次标签</a-select-option>
            <a-select-option value="CUSTOMER_MARK">客户唛头</a-select-option>
            <a-select-option value="SPQ_INNER">SPQ内箱标签</a-select-option>
            <a-select-option value="PQ_OUTER">PQ外箱标签</a-select-option>
            <a-select-option value="ENG_CARD_PACKAGE">工程卡包装标签</a-select-option>
            <a-select-option value="WORKSTATION">工位标识标签</a-select-option>
            <a-select-option value="EQUIPMENT">设备标识标签</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 模板选择（可选） -->
        <a-form-item label="选择模板">
          <a-select v-model:value="formData.templateId" placeholder="不选则自动匹配" allow-clear>
            <a-select-option :value="tpl.id" v-for="tpl in templates" :key="tpl.id">
              {{ tpl.templateName }}
              <a-tag v-if="tpl.isDefault" color="green">默认</a-tag>
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 打印张数 -->
        <a-form-item label="打印张数" required>
          <a-input-number v-model:value="formData.printCount" :min="1" :max="100" style="width: 200px" />
        </a-form-item>

        <!-- 工厂 -->
        <a-form-item label="工厂">
          <a-select v-model:value="formData.factoryId" placeholder="请选择工厂" allow-clear>
            <a-select-option :value="factory.id" v-for="factory in factories" :key="factory.id">
              {{ factory.factoryCode?.replace('', '') }} - {{ factory.factoryName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 绑定维度 -->
        <a-divider orientation="left">绑定维度（智能匹配使用）</a-divider>

        <a-form-item label="物料">
          <a-select v-model:value="formData.materialId" placeholder="请选择物料" show-search allow-clear
                    :filter-option="filterOption">
            <a-select-option :value="mat.id" v-for="mat in materials" :key="mat.id">
              {{ mat.materialCode }} - {{ mat.materialName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="供应商">
          <a-select v-model:value="formData.supplierId" placeholder="请选择供应商" allow-clear>
            <a-select-option :value="sup.id" v-for="sup in suppliers" :key="sup.id">
              {{ sup.supplierCode }} - {{ sup.supplierName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="客户">
          <a-select v-model:value="formData.customerId" placeholder="请选择客户" allow-clear>
            <a-select-option :value="cus.id" v-for="cus in customers" :key="cus.id">
              {{ cus.customerCode }} - {{ cus.customerName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 打印数据（JSON） -->
        <a-divider orientation="left">打印数据</a-divider>

        <a-form-item label="工程卡号">
          <a-input v-model:value="printData.engineeringCardNo" placeholder="输入工程卡号自动填充"
                   @blur="handleEngineeringCardBlur" />
        </a-form-item>

        <a-form-item label="打印数据">
          <a-textarea v-model:value="printDataJson" :rows="10" placeholder="打印数据 JSON 格式" />
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-space>
            <a-button type="primary" @click="handlePreview">
              <EyeOutlined /> 预览
            </a-button>
            <a-button type="primary" @click="handlePrint">
              <PrinterOutlined /> 打印
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 预览弹窗 -->
    <a-modal v-model:open="previewVisible" title="打印预览" width="800px" :footer="null">
      <div v-html="previewContent" class="preview-container"></div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { EyeOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { labelPrintApi } from '@/api/label/print'
import { labelTemplateApi } from '@/api/label/template'
import { factoryApi } from '@/api/basic/factory'
import { materialApi } from '@/api/basic/material'
import { supplierApi } from '@/api/basic/supplier'
import { customerApi } from '@/api/basic/customer'

// 表单数据
const formData = ref({
  templateType: '',
  templateId: undefined as number | undefined,
  printCount: 1,
  factoryId: undefined as number | undefined,
  materialId: undefined as number | undefined,
  supplierId: undefined as number | undefined,
  customerId: undefined as number | undefined
})

// 打印数据
const printData = ref({
  engineeringCardNo: '',
  materialCode: '',
  materialName: '',
  lotNo: '',
  batchNo: '',
  quantity: 0,
  unit: 'PCS'
})

// 打印数据 JSON
const printDataJson = computed({
  get: () => JSON.stringify(printData.value, null, 2),
  set: (val) => {
    try {
      printData.value = JSON.parse(val)
    } catch (e) {
      // 解析失败时保持原值
    }
  }
})

// 模板列表
const templates = ref<any[]>([])
const factories = ref<any[]>([])
const materials = ref<any[]>([])
const suppliers = ref<any[]>([])
const customers = ref<any[]>([])

// 预览
const previewVisible = ref(false)
const previewContent = ref('')

// 模板类型变化
function handleTypeChange() {
  formData.value.templateId = undefined
  loadTemplates()
}

// 加载模板列表
async function loadTemplates() {
  if (!formData.value.templateType) return

  try {
    const res = await labelTemplateApi.page({
      pageNum: 1,
      pageSize: 100,
      templateType: formData.value.templateType
    })
    templates.value = res.records || []
  } catch (e) {
    console.error('加载模板失败', e)
  }
}

// 工程卡号失焦，自动查询填充
function handleEngineeringCardBlur() {
  if (!printData.value.engineeringCardNo) return

  // TODO: 调用工程卡查询接口自动填充数据
  message.info('工程卡自动填充功能待实现')
}

// 筛选选项
function filterOption(input: string, option: any) {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 预览
async function handlePreview() {
  if (!formData.value.templateType) {
    message.warning('请选择打印类型')
    return
  }

  try {
    const params = {
      templateId: formData.value.templateId,
      templateType: formData.value.templateType,
      printCount: Math.min(formData.value.printCount, 10),
      factoryId: formData.value.factoryId,
      printData: printData.value,
      materialId: formData.value.materialId,
      supplierId: formData.value.supplierId,
      customerId: formData.value.customerId
    }

    const res = await labelPrintApi.preview(params)
    previewContent.value = res[0] || ''
    previewVisible.value = true
  } catch (e: any) {
    message.error(e.message || '预览失败')
  }
}

// 打印
async function handlePrint() {
  if (!formData.value.templateType) {
    message.warning('请选择打印类型')
    return
  }

  try {
    const params = {
      templateId: formData.value.templateId,
      templateType: formData.value.templateType,
      printCount: formData.value.printCount,
      factoryId: formData.value.factoryId,
      printData: printData.value,
      materialId: formData.value.materialId,
      supplierId: formData.value.supplierId,
      customerId: formData.value.customerId
    }

    await labelPrintApi.execute(params)
    message.success('打印成功')
  } catch (e: any) {
    message.error(e.message || '打印失败')
  }
}

// 重置
function handleReset() {
  formData.value = {
    templateType: '',
    templateId: undefined,
    printCount: 1,
    factoryId: undefined,
    materialId: undefined,
    supplierId: undefined,
    customerId: undefined
  }
  printData.value = {
    engineeringCardNo: '',
    materialCode: '',
    materialName: '',
    lotNo: '',
    batchNo: '',
    quantity: 0,
    unit: 'PCS'
  }
  templates.value = []
}

// 加载工厂列表
async function loadFactories() {
  try {
    const res = await factoryApi.list({ status: 1 })
    factories.value = res || []
  } catch (e) {
    console.error('加载工厂列表失败', e)
  }
}

// 加载物料列表
async function loadMaterials() {
  try {
    const res = await materialApi.list({ status: 1 })
    materials.value = res || []
  } catch (e) {
    console.error('加载物料列表失败', e)
  }
}

// 加载供应商列表
async function loadSuppliers() {
  try {
    const res = await supplierApi.list({ status: 1 })
    suppliers.value = res || []
  } catch (e) {
    console.error('加载供应商列表失败', e)
  }
}

// 加载客户列表
async function loadCustomers() {
  try {
    const res = await customerApi.list({ status: 1 })
    customers.value = res || []
  } catch (e) {
    console.error('加载客户列表失败', e)
  }
}

// 初始化
onMounted(() => {
  loadFactories()
  loadMaterials()
  loadSuppliers()
  loadCustomers()
})
</script>

<style scoped lang="less">
.page-container {
  padding: 16px;
  height: 100%;
}

.preview-container {
  border: 1px solid #d9d9d9;
  padding: 16px;
  min-height: 400px;
  background: #fafafa;
}
</style>
