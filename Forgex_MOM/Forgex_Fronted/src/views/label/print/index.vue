<template>
  <div class="page-container label-print-page">
    <a-card title="标签打印" :bordered="false">
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item label="模板编码" required>
              <a-input v-model:value="form.templateCode" placeholder="请输入模板编码" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="打印张数" required>
              <a-input-number v-model:value="form.printCount" :min="1" :max="1000" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="操作">
              <a-space>
                <a-button type="primary" @click="handleRender"><EyeOutlined /> 预览</a-button>
                <a-button :disabled="!rendered" @click="handlePrint"><PrinterOutlined /> 打印</a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="业务数据 JSON" required>
          <a-textarea v-model:value="dataJson" :rows="10" />
        </a-form-item>
      </a-form>
    </a-card>

    <div v-if="rendered" class="print-preview" ref="printRef">
      <div v-for="n in rendered.printCount" :key="n" class="print-page">
        <LabelPreview :template="rendered" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { EyeOutlined, PrinterOutlined } from '@ant-design/icons-vue'
import { labelPrintApi } from '@/api/label/print'
import LabelPreview from '@/views/label/template/components/LabelPreview.vue'

const form = ref({ templateCode: '', printCount: 1 })
const dataJson = ref(JSON.stringify({ matName: '测试物料', lotNo: 'LOT001' }, null, 2))
const rendered = ref<any>(null)
const printRef = ref<HTMLElement>()

async function handleRender() {
  if (!form.value.templateCode) {
    message.warning('请输入模板编码')
    return
  }
  let data: any
  try {
    data = JSON.parse(dataJson.value || '{}')
  } catch {
    message.warning('业务数据 JSON 格式不正确')
    return
  }
  rendered.value = await labelPrintApi.render({ ...form.value, data })
}

function handlePrint() {
  const html = printRef.value?.innerHTML || ''
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`<!doctype html><html><head><title>标签打印</title><style>body{margin:0}.print-page{page-break-after:always;display:flex;justify-content:center;padding:8mm}.label-sheet{position:relative;background:#fff;border:0!important;box-shadow:none!important}.label-component{position:absolute;overflow:hidden;display:flex;align-items:center}.label-content{width:100%;white-space:pre-wrap}img{width:100%;height:100%;object-fit:contain}</style></head><body>${html}</body></html>`)
  win.document.close()
  win.focus()
  win.print()
}
</script>

<style scoped>
.label-print-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.print-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  padding: 24px;
  background: #f5f5f5;
}
.print-page {
  background: #fff;
}
</style>
