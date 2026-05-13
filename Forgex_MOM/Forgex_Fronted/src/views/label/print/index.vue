<template>
  <div class="page-container">
    <a-card :title="t('label.print.title')" :bordered="false">
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <!-- 打印类型 -->
        <a-form-item :label="t('label.print.printType')" required>
          <a-select v-model:value="formData.templateType" :placeholder="t('label.print.selectLabelType')" @change="handleTypeChange">
            <a-select-option value="INCOMING">{{ t('label.templateTypes.INCOMING') }}</a-select-option>
            <a-select-option value="PRODUCT">{{ t('label.templateTypes.PRODUCT') }}</a-select-option>
            <a-select-option value="LOT">{{ t('label.templateTypes.LOT') }}</a-select-option>
            <a-select-option value="CUSTOMER_MARK">{{ t('label.templateTypes.CUSTOMER_MARK') }}</a-select-option>
            <a-select-option value="SPQ_INNER">{{ t('label.templateTypes.SPQ_INNER') }}</a-select-option>
            <a-select-option value="PQ_OUTER">{{ t('label.templateTypes.PQ_OUTER') }}</a-select-option>
            <a-select-option value="ENG_CARD_PACKAGE">{{ t('label.templateTypes.ENG_CARD_PACKAGE') }}</a-select-option>
            <a-select-option value="WORKSTATION">{{ t('label.templateTypes.WORKSTATION') }}</a-select-option>
            <a-select-option value="EQUIPMENT">{{ t('label.templateTypes.EQUIPMENT') }}</a-select-option>
          </a-select>
        </a-form-item>

        <!-- 模板选择（可选） -->
        <a-form-item :label="t('label.print.selectTemplate')">
          <a-select v-model:value="formData.templateId" :placeholder="t('label.print.autoMatchTemplate')" allow-clear>
            <a-select-option :value="tpl.id" v-for="tpl in templates" :key="tpl.id">
              {{ tpl.templateName }}
              <a-tag v-if="tpl.isDefault" color="green">{{ t('common.default') }}</a-tag>
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 打印张数 -->
        <a-form-item :label="t('label.print.printCount')" required>
          <a-input-number v-model:value="formData.printCount" :min="1" :max="100" style="width: 200px" />
        </a-form-item>

        <!-- 工厂 -->
        <a-form-item :label="t('label.print.factory')">
          <a-select v-model:value="formData.factoryId" :placeholder="t('label.print.selectFactory')" allow-clear>
            <a-select-option :value="factory.id" v-for="factory in factories" :key="factory.id">
              {{ factory.factoryCode?.replace('', '') }} - {{ factory.factoryName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 绑定维度 -->
        <a-divider orientation="left">{{ t('label.print.bindingDimension') }}</a-divider>

        <a-form-item :label="t('label.print.material')">
          <a-select v-model:value="formData.materialId" :placeholder="t('label.print.selectMaterial')" show-search allow-clear
                    :filter-option="filterOption">
            <a-select-option :value="mat.id" v-for="mat in materials" :key="mat.id">
              {{ mat.materialCode }} - {{ mat.materialName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('label.print.supplier')">
          <a-select v-model:value="formData.supplierId" :placeholder="t('label.print.selectSupplier')" allow-clear>
            <a-select-option :value="sup.id" v-for="sup in suppliers" :key="sup.id">
              {{ sup.supplierCode }} - {{ sup.supplierName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('label.print.customer')">
          <a-select v-model:value="formData.customerId" :placeholder="t('label.print.selectCustomer')" allow-clear>
            <a-select-option :value="cus.id" v-for="cus in customers" :key="cus.id">
              {{ cus.customerCode }} - {{ cus.customerName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 打印数据（JSON） -->
        <a-divider orientation="left">{{ t('label.print.printData') }}</a-divider>

        <a-form-item :label="t('label.print.engineeringCardNo')">
          <a-input v-model:value="printData.engineeringCardNo" :placeholder="t('label.print.engineeringCardAutoFill')"
                   @blur="handleEngineeringCardBlur" />
        </a-form-item>

        <a-form-item :label="t('label.print.printData')">
          <a-textarea v-model:value="printDataJson" :rows="10" :placeholder="t('label.print.printDataJsonPlaceholder')" />
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-space>
            <a-button type="primary" @click="handlePreview">
              <EyeOutlined /> {{ t('common.preview') }}
            </a-button>
            <a-button type="primary" @click="handlePrint">
              <PrinterOutlined /> {{ t('label.print.print') }}
            </a-button>
            <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 预览弹窗 -->
    <a-modal v-model:open="previewVisible" :title="t('label.print.previewTitle')" width="800px" :footer="null">
      <div v-html="previewContent" class="preview-container"></div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { EyeOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { labelPrintApi } from '@/api/label/print'
import { labelTemplateApi } from '@/api/label/template'
import { factoryApi } from '@/api/basic/factory'
import { materialApi } from '@/api/basic/material'
import { supplierApi } from '@/api/basic/supplier'
import { customerApi } from '@/api/basic/customer'

const { t } = useI18n()

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
    console.error('[LabelPrint] Failed to load templates', e)
  }
}

// 工程卡号失焦，自动查询填充
function handleEngineeringCardBlur() {
  if (!printData.value.engineeringCardNo) return

  // TODO: 调用工程卡查询接口自动填充数据
  message.info(t('label.print.engineeringCardAutoFillPending'))
}

// 筛选选项
function filterOption(input: string, option: any) {
  return option.children[0].children.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 预览
async function handlePreview() {
  if (!formData.value.templateType) {
    message.warning(t('label.print.selectPrintTypeWarning'))
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
    message.error(e.message || t('label.print.previewFailed'))
  }
}

// 打印
async function handlePrint() {
  if (!formData.value.templateType) {
    message.warning(t('label.print.selectPrintTypeWarning'))
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
    message.success(t('label.print.printSuccess'))
  } catch (e: any) {
    message.error(e.message || t('label.print.printFailed'))
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
    console.error('[LabelPrint] Failed to load factories', e)
  }
}

// 加载物料列表
async function loadMaterials() {
  try {
    const res = await materialApi.list({ status: 1 })
    materials.value = res || []
  } catch (e) {
    console.error('[LabelPrint] Failed to load materials', e)
  }
}

// 加载供应商列表
async function loadSuppliers() {
  try {
    const res = await supplierApi.list({ status: 1 })
    suppliers.value = res || []
  } catch (e) {
    console.error('[LabelPrint] Failed to load suppliers', e)
  }
}

// 加载客户列表
async function loadCustomers() {
  try {
    const res = await customerApi.list({ status: 1 })
    customers.value = res || []
  } catch (e) {
    console.error('[LabelPrint] Failed to load customers', e)
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

<style scoped lang="less" src="@/styles/views/label/print/index.less"></style>

