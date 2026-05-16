<template>
  <div class="designer">
    <aside class="designer__left">
      <section>
        <h3>模板属性</h3>
        <a-form layout="vertical" size="small">
          <a-form-item label="模板编码"><a-input :value="template.templateCode" disabled /></a-form-item>
          <a-form-item label="模板名称"><a-input :value="template.templateName" disabled /></a-form-item>
          <a-row :gutter="8">
            <a-col :span="12"><a-form-item label="宽(mm)"><a-input-number :value="template.paperWidth" disabled style="width:100%" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="高(mm)"><a-input-number :value="template.paperHeight" disabled style="width:100%" /></a-form-item></a-col>
          </a-row>
        </a-form>
      </section>
      <section>
        <h3>组件库</h3>
        <button v-for="item in componentTypes" :key="item.type" class="component-button" type="button" @click="addComponent(item.type)">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </button>
      </section>
      <a-button type="primary" block :loading="saving" @click="handleSave">保存设计</a-button>
    </aside>

    <main class="designer__stage">
      <div class="canvas-scroll">
        <div class="label-canvas" :style="canvasStyle" @click="selectedId = ''">
          <div
            v-for="item in details"
            :key="item.clientId"
            class="design-item"
            :class="{ active: item.clientId === selectedId }"
            :style="itemStyle(item)"
            @mousedown.stop="startMove($event, item)"
            @click.stop="selectedId = item.clientId"
          >
            <span v-if="isLine(item)"></span>
            <img v-else-if="item.componentType === 'IMAGE' && item.componentContent" :src="item.componentContent" />
            <span v-else>{{ itemText(item) }}</span>
            <i class="resize-handle" @mousedown.stop="startResize($event, item)"></i>
          </div>
        </div>
      </div>
    </main>

    <aside class="designer__right">
      <template v-if="selected">
        <h3>组件属性</h3>
        <a-form layout="vertical" size="small">
          <a-form-item label="组件类型"><a-input :value="selected.componentType" disabled /></a-form-item>
          <a-form-item v-if="selected.componentType === 'TEXT'" label="数据来源">
            <a-segmented v-model:value="selected.dataSource" :options="dataSourceOptions" />
          </a-form-item>
          <a-form-item v-if="selected.dataSource === 'FIELD'" label="业务字段">
            <a-select v-model:value="selected.fieldCode" :options="fieldOptions" show-search />
          </a-form-item>
          <a-form-item v-else-if="!isLine(selected)" label="固定内容">
            <a-textarea v-model:value="selected.componentContent" :rows="3" />
          </a-form-item>
          <a-row :gutter="8">
            <a-col :span="12"><a-form-item label="X(mm)"><a-input-number v-model:value="selected.positionX" style="width:100%" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="Y(mm)"><a-input-number v-model:value="selected.positionY" style="width:100%" /></a-form-item></a-col>
          </a-row>
          <a-row :gutter="8">
            <a-col :span="12"><a-form-item label="宽(mm)"><a-input-number v-model:value="selected.componentWidth" :min="1" style="width:100%" /></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="高(mm)"><a-input-number v-model:value="selected.componentHeight" :min="1" style="width:100%" /></a-form-item></a-col>
          </a-row>
          <template v-if="selected.componentType === 'TEXT'">
            <a-row :gutter="8">
              <a-col :span="12"><a-form-item label="字号"><a-input-number v-model:value="styleModel.fontSize" :min="6" :max="72" style="width:100%" /></a-form-item></a-col>
              <a-col :span="12"><a-form-item label="字重"><a-select v-model:value="styleModel.fontWeight" :options="weightOptions" /></a-form-item></a-col>
            </a-row>
            <a-form-item label="对齐"><a-segmented v-model:value="styleModel.textAlign" :options="alignOptions" /></a-form-item>
          </template>
          <a-button danger block @click="deleteSelected">删除组件</a-button>
        </a-form>
      </template>
      <a-empty v-else description="请选择组件" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { BarcodeOutlined, FontSizeOutlined, PictureOutlined, QrcodeOutlined, MinusOutlined, ColumnHeightOutlined } from '@ant-design/icons-vue'
import { labelFieldApi } from '@/api/label/field'
import { labelTemplateApi } from '@/api/label/template'

const props = defineProps<{ template: any }>()
const emit = defineEmits<{ (e: 'saved'): void }>()
const scale = 4
const saving = ref(false)
const selectedId = ref('')
const fieldOptions = ref<any[]>([])
const details = ref<any[]>([])
const dataSourceOptions = [{ label: '固定值', value: 'FIXED' }, { label: '业务字段', value: 'FIELD' }]
const weightOptions = [{ label: '常规', value: 400 }, { label: '加粗', value: 700 }]
const alignOptions = [{ label: '左', value: 'left' }, { label: '中', value: 'center' }, { label: '右', value: 'right' }]
const componentTypes = [
  { type: 'TEXT', label: '文本框', icon: FontSizeOutlined },
  { type: 'BARCODE', label: '条形码', icon: BarcodeOutlined },
  { type: 'QRCODE', label: '二维码', icon: QrcodeOutlined },
  { type: 'IMAGE', label: '图片', icon: PictureOutlined },
  { type: 'HORIZONTAL_LINE', label: '横线', icon: MinusOutlined },
  { type: 'VERTICAL_LINE', label: '竖线', icon: ColumnHeightOutlined }
]

const selected = computed(() => details.value.find(item => item.clientId === selectedId.value))
const canvasStyle = computed(() => ({ width: `${props.template.paperWidth * scale}px`, height: `${props.template.paperHeight * scale}px` }))
const styleModel = computed({
  get() {
    return selected.value?.styleJson ? JSON.parse(selected.value.styleJson) : { fontSize: 12, fontWeight: 400, textAlign: 'left' }
  },
  set(value) {
    if (selected.value) selected.value.styleJson = JSON.stringify(value)
  }
})

watch(styleModel, value => {
  if (selected.value) selected.value.styleJson = JSON.stringify(value)
}, { deep: true })

function normalizeDetail(item: any, index: number) {
  return {
    clientId: item.id ? `id-${item.id}` : `tmp-${Date.now()}-${index}`,
    componentType: item.componentType || 'TEXT',
    positionX: item.positionX ?? 5,
    positionY: item.positionY ?? 5,
    componentWidth: item.componentWidth ?? 30,
    componentHeight: item.componentHeight ?? 10,
    componentContent: item.content ?? item.componentContent ?? '',
    dataSource: item.dataSource || 'FIXED',
    fieldCode: item.fieldCode,
    styleJson: item.styleJson || JSON.stringify({ fontSize: 12, fontWeight: 400, textAlign: 'left' }),
    sortNo: item.sortNo ?? index
  }
}

function addComponent(type: string) {
  const detail = normalizeDetail({ componentType: type }, details.value.length)
  if (type === 'HORIZONTAL_LINE') detail.componentHeight = 1
  if (type === 'VERTICAL_LINE') detail.componentWidth = 1
  if (type === 'TEXT') detail.componentContent = '文本'
  details.value.push(detail)
  selectedId.value = detail.clientId
}

function isLine(item: any) {
  return item.componentType === 'HORIZONTAL_LINE' || item.componentType === 'VERTICAL_LINE'
}

function itemText(item: any) {
  if (item.dataSource === 'FIELD') return `{${item.fieldCode || 'field'}}`
  if (item.componentType === 'BARCODE') return item.componentContent || 'BARCODE'
  if (item.componentType === 'QRCODE') return item.componentContent || 'QRCODE'
  return item.componentContent || item.componentType
}

function itemStyle(item: any) {
  const style = item.styleJson ? JSON.parse(item.styleJson) : {}
  return {
    left: `${item.positionX * scale}px`,
    top: `${item.positionY * scale}px`,
    width: `${item.componentWidth * scale}px`,
    height: `${item.componentHeight * scale}px`,
    fontSize: `${style.fontSize || 12}px`,
    fontWeight: style.fontWeight || 400,
    textAlign: style.textAlign || 'left',
    background: isLine(item) ? '#111' : '#fff',
    border: item.clientId === selectedId.value ? '1px solid #1677ff' : '1px dashed #bfbfbf'
  }
}

function startMove(e: MouseEvent, item: any) {
  selectedId.value = item.clientId
  const startX = e.clientX
  const startY = e.clientY
  const originX = item.positionX
  const originY = item.positionY
  const move = (event: MouseEvent) => {
    item.positionX = Math.max(0, Math.round(originX + (event.clientX - startX) / scale))
    item.positionY = Math.max(0, Math.round(originY + (event.clientY - startY) / scale))
  }
  const up = () => {
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
  }
  window.addEventListener('mousemove', move)
  window.addEventListener('mouseup', up)
}

function startResize(e: MouseEvent, item: any) {
  const startX = e.clientX
  const startY = e.clientY
  const originW = item.componentWidth
  const originH = item.componentHeight
  const move = (event: MouseEvent) => {
    item.componentWidth = Math.max(1, Math.round(originW + (event.clientX - startX) / scale))
    item.componentHeight = Math.max(1, Math.round(originH + (event.clientY - startY) / scale))
  }
  const up = () => {
    window.removeEventListener('mousemove', move)
    window.removeEventListener('mouseup', up)
  }
  window.addEventListener('mousemove', move)
  window.addEventListener('mouseup', up)
}

function deleteSelected() {
  details.value = details.value.filter(item => item.clientId !== selectedId.value)
  selectedId.value = ''
}

async function handleSave() {
  saving.value = true
  try {
    await labelTemplateApi.saveDesign({
      templateId: props.template.id,
      templateCode: props.template.templateCode,
      details: details.value.map((item, index) => ({ ...item, sortNo: index }))
    })
    message.success('保存成功')
    emit('saved')
  } finally {
    saving.value = false
  }
}

async function loadFields() {
  const res = await labelFieldApi.options()
  fieldOptions.value = (res || []).map((item: any) => ({ label: item.label, value: item.value }))
}

onMounted(() => {
  details.value = (props.template.components || []).map(normalizeDetail)
  loadFields()
})
</script>

<style scoped>
.designer {
  display: grid;
  grid-template-columns: 280px 1fr 320px;
  height: calc(100vh - 56px);
  background: #f5f7fb;
}
.designer__left,
.designer__right {
  padding: 16px;
  background: #fff;
  overflow: auto;
  border-right: 1px solid #e8e8e8;
}
.designer__right {
  border-right: 0;
  border-left: 1px solid #e8e8e8;
}
h3 {
  margin: 0 0 12px;
  font-size: 14px;
}
section {
  margin-bottom: 18px;
}
.component-button {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #d9d9d9;
  background: #fff;
  padding: 8px 10px;
  margin-bottom: 8px;
  cursor: pointer;
}
.designer__stage {
  overflow: hidden;
  padding: 24px;
}
.canvas-scroll {
  width: 100%;
  height: 100%;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}
.label-canvas {
  position: relative;
  background: #fff;
  border: 1px solid #d9d9d9;
  box-shadow: 0 12px 32px rgba(0,0,0,.08);
}
.design-item {
  position: absolute;
  overflow: hidden;
  display: flex;
  align-items: center;
  cursor: move;
  user-select: none;
}
.design-item span {
  width: 100%;
  padding: 1px 3px;
  white-space: pre-wrap;
}
.design-item img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.resize-handle {
  position: absolute;
  width: 8px;
  height: 8px;
  right: -4px;
  bottom: -4px;
  background: #1677ff;
  cursor: nwse-resize;
}
</style>
