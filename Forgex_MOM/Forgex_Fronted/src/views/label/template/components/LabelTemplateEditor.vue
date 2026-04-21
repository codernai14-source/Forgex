
<template>
  <div class="template-editor">
    <a-row :gutter="16" style="height: calc(100vh - 280px); min-height: 600px;">
      <!-- 左侧：组件面板 -->
      <a-col :span="5">
        <a-card title="组件库" size="small" class="panel-card">
          <template #title>
            <div class="panel-title">
              <AppstoreOutlined />
              <span>组件库</span>
            </div>
          </template>
          <a-collapse v-model:activeKey="activeKeys" :bordered="false">
            <a-collapse-panel key="basic" header="基础组件">
              <div v-for="comp in basicComponents" :key="comp.type"
                   class="component-item"
                   draggable="true"
                   @dragstart="handleDragStart(comp)">
                <div class="component-content">
                  <component :is="comp.icon" style="font-size: 18px;" />
                  <span class="component-name">{{ comp.name }}</span>
                </div>
              </div>
            </a-collapse-panel>

            <a-collapse-panel key="data" header="数据字段">
              <div v-for="field in availableFields" :key="field.key"
                   class="field-item"
                   draggable="true"
                   @dragstart="handleFieldDragStart(field)">
                <div class="field-content">
                  <div class="field-label">
                    <FieldStringOutlined />
                    <span>{{ field.label }}</span>
                  </div>
                  <code class="field-key">{{ field.key }}</code>
                </div>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </a-col>

      <!-- 中间：画布区域 -->
      <a-col :span="13">
        <a-card title="画布预览" size="small" class="panel-card">
          <template #title>
            <div class="panel-title">
              <EditOutlined />
              <span>画布预览</span>
            </div>
          </template>
          <template #extra>
            <a-space size="small">
              <a-tooltip title="纸张尺寸">
                <a-select v-model:value="canvasConfig.paperSize" style="width: 130px" size="small">
                  <a-select-option value="60x40">60×40 mm</a-select-option>
                  <a-select-option value="80x60">80×60 mm</a-select-option>
                  <a-select-option value="100x80">100×80 mm</a-select-option>
                  <a-select-option value="120x80">120×80 mm</a-select-option>
                </a-select>
              </a-tooltip>
              <a-button size="small" @click="handlePreview">
                <template #icon><EyeOutlined /></template>
                预览
              </a-button>
              <a-button size="small" danger @click="handleClearCanvas">
                <template #icon><DeleteOutlined /></template>
                清空
              </a-button>
            </a-space>
          </template>

          <div class="canvas-wrapper">
            <div class="canvas-container"
                 @drop="handleDrop"
                 @dragover.prevent
                 @click="selectElement(null)">
              <div class="label-canvas" :style="labelSizeStyle">
                <div v-if="elements.length === 0" class="empty-tip">
                  <InboxOutlined style="font-size: 48px; color: #d9d9d9; margin-bottom: 16px;" />
                  <div class="empty-text">拖拽组件到此处</div>
                  <div class="empty-hint">从左侧选择组件或数据字段，拖拽到画布中</div>
                </div>
                <div v-for="el in elements" :key="el.id"
                     class="canvas-element"
                     :class="{ active: selectedElement?.id === el.id }"
                     :style="getElementStyle(el)"
                     @click.stop="selectElement(el)"
                     @mousedown="startDrag($event, el)">
                  <div class="element-content">
                    <template v-if="el.type === 'text'">
                      <FontSizeOutlined />
                      <span>{{ el.content || '文本' }}</span>
                    </template>
                    <template v-else-if="el.type === 'barcode'">
                      <BarcodeOutlined />
                      <span>{{ el.content || '条码' }}</span>
                    </template>
                    <template v-else-if="el.type === 'qrcode'">
                      <QrcodeOutlined />
                      <span>{{ el.content || '二维码' }}</span>
                    </template>
                    <template v-else-if="el.type === 'image'">
                      <PictureOutlined />
                      <span>{{ el.content || '图片' }}</span>
                    </template>
                    <template v-else-if="el.type === 'field'">
                      <FieldStringOutlined />
                      <span class="field-tag">{{ el.fieldKey || el.content }}</span>
                    </template>
                  </div>
                  <div class="element-resize-handle" v-if="selectedElement?.id === el.id">
                    <div class="resize-handle resize-n" @mousedown.stop="startResize($event, el, 'n')"></div>
                    <div class="resize-handle resize-s" @mousedown.stop="startResize($event, el, 's')"></div>
                    <div class="resize-handle resize-e" @mousedown.stop="startResize($event, el, 'e')"></div>
                    <div class="resize-handle resize-w" @mousedown.stop="startResize($event, el, 'w')"></div>
                    <div class="resize-handle resize-ne" @mousedown.stop="startResize($event, el, 'ne')"></div>
                    <div class="resize-handle resize-nw" @mousedown.stop="startResize($event, el, 'nw')"></div>
                    <div class="resize-handle resize-se" @mousedown.stop="startResize($event, el, 'se')"></div>
                    <div class="resize-handle resize-sw" @mousedown.stop="startResize($event, el, 'sw')"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：属性面板 -->
      <a-col :span="6">
        <a-card title="属性配置" size="small" class="panel-card">
          <template #title>
            <div class="panel-title">
              <SettingOutlined />
              <span>属性配置</span>
            </div>
          </template>
          <a-form v-if="selectedElement" layout="vertical" size="small">
            <a-divider orientation="left" style="font-size: 12px;">基本信息</a-divider>
            <a-form-item label="元素类型">
              <a-tag :color="getTypeColor(selectedElement.type)">
                {{ getTypeName(selectedElement.type) }}
              </a-tag>
            </a-form-item>
            
            <template v-if="selectedElement.type === 'text' || selectedElement.type === 'field'">
              <a-form-item label="文本内容">
                <a-textarea 
                  v-model:value="selectedElement.content" 
                  placeholder="输入文本内容"
                  :rows="2"
                />
              </a-form-item>
              <a-form-item label="字体大小">
                <a-input-number 
                  v-model:value="selectedElement.fontSize" 
                  :min="8" 
                  :max="72" 
                  style="width: 100%"
                  addon-after="px"
                />
              </a-form-item>
              <a-form-item label="字体粗细">
                <a-select v-model:value="selectedElement.fontWeight" style="width: 100%">
                  <a-select-option :value="400">常规</a-select-option>
                  <a-select-option :value="600">加粗</a-select-option>
                  <a-select-option :value="700">粗体</a-select-option>
                </a-select>
              </a-form-item>
            </template>

            <a-divider orientation="left" style="font-size: 12px;">位置尺寸</a-divider>
            <a-row :gutter="8">
              <a-col :span="12">
                <a-form-item label="X 坐标">
                  <a-input-number 
                    v-model:value="selectedElement.x" 
                    style="width: 100%"
                    addon-after="px"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Y 坐标">
                  <a-input-number 
                    v-model:value="selectedElement.y" 
                    style="width: 100%"
                    addon-after="px"
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="8">
              <a-col :span="12">
                <a-form-item label="宽度">
                  <a-input-number 
                    v-model:value="selectedElement.width" 
                    :min="10" 
                    style="width: 100%"
                    addon-after="px"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="高度">
                  <a-input-number 
                    v-model:value="selectedElement.height" 
                    :min="10" 
                    style="width: 100%"
                    addon-after="px"
                  />
                </a-form-item>
              </a-col>
            </a-row>

            <a-divider orientation="left" style="font-size: 12px;">操作</a-divider>
            <a-button danger block @click="deleteElement">
              <template #icon><DeleteOutlined /></template>
              删除元素
            </a-button>
          </a-form>
          <a-empty v-else description="请选择一个元素">
            <template #image>
              <PictureOutlined style="font-size: 64px; color: #d9d9d9;" />
            </template>
          </a-empty>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, type CSSProperties } from 'vue'
import { 
  AppstoreOutlined, EditOutlined, SettingOutlined, 
  FontSizeOutlined, BarcodeOutlined, QrcodeOutlined, 
  PictureOutlined, EyeOutlined, DeleteOutlined,
  FieldStringOutlined, InboxOutlined
} from '@ant-design/icons-vue'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const activeKeys = ref(['basic', 'data'])
const elements = ref<any[]>([])
const selectedElement = ref<any>(null)
const canvasConfig = ref({
  paperSize: '60x40'
})

const basicComponents = [
  { type: 'text', name: '文本', icon: FontSizeOutlined },
  { type: 'barcode', name: '条码', icon: BarcodeOutlined },
  { type: 'qrcode', name: '二维码', icon: QrcodeOutlined },
  { type: 'image', name: '图片', icon: PictureOutlined }
]

const availableFields = [
  { key: 'materialCode', label: '物料编码' },
  { key: 'materialName', label: '物料名称' },
  { key: 'lotNo', label: '批次号' },
  { key: 'quantity', label: '数量' },
  { key: 'printTime', label: '打印时间' },
  { key: 'supplierName', label: '供应商名称' },
  { key: 'customerName', label: '客户名称' },
  { key: 'factoryName', label: '工厂名称' }
]

const labelSizeStyle = computed(() => {
  const sizes: Record<string, string> = {
    '60x40': 'width: 227px; height: 151px',
    '80x60': 'width: 302px; height: 227px',
    '100x80': 'width: 378px; height: 302px',
    '120x80': 'width: 453px; height: 302px'
  }
  return sizes[canvasConfig.value.paperSize] || sizes['60x40']
})

let draggedItem: any = null
let isDragging = false
let dragOffset = { x: 0, y: 0 }
let isResizing = false
let resizeData: any = null

function handleDragStart(comp: any) {
  draggedItem = { type: 'component', data: comp }
}

function handleFieldDragStart(field: any) {
  draggedItem = { type: 'field', data: field }
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  if (!draggedItem) return

  const canvas = document.querySelector('.label-canvas') as HTMLElement
  if (!canvas) return

  const rect = canvas.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  if (draggedItem.type === 'component') {
    const newElement = {
      id: Date.now().toString(),
      type: draggedItem.data.type,
      content: '',
      x: Math.max(0, x - 50),
      y: Math.max(0, y - 20),
      width: 100,
      height: 30,
      fontSize: 14,
      fontWeight: 400
    }
    elements.value.push(newElement)
    selectedElement.value = newElement
  } else if (draggedItem.type === 'field') {
    const newElement = {
      id: Date.now().toString(),
      type: 'field',
      fieldKey: draggedItem.data.key,
      content: `{${draggedItem.data.key}}`,
      x: Math.max(0, x - 50),
      y: Math.max(0, y - 20),
      width: 120,
      height: 30,
      fontSize: 14,
      fontWeight: 400
    }
    elements.value.push(newElement)
    selectedElement.value = newElement
  }

  draggedItem = null
}

function selectElement(el: any) {
  selectedElement.value = el
}

function getElementStyle(el: any): CSSProperties {
  return {
    position: 'absolute',
    left: el.x + 'px',
    top: el.y + 'px',
    width: el.width + 'px',
    height: el.height + 'px',
    fontSize: el.fontSize + 'px',
    fontWeight: el.fontWeight || 400,
    border: selectedElement?.id === el.id ? '2px solid #1890ff' : '1px dashed #d9d9d9',
    cursor: 'move',
    padding: '4px',
    overflow: 'hidden',
    backgroundColor: selectedElement?.id === el.id ? 'rgba(24, 144, 255, 0.05)' : 'transparent',
    zIndex: selectedElement?.id === el.id ? 10 : 1
  }
}

function startDrag(e: MouseEvent, el: any) {
  isDragging = true
  dragOffset.x = e.clientX - el.x
  dragOffset.y = e.clientY - el.y
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging) return
    const canvas = document.querySelector('.label-canvas') as HTMLElement
    if (!canvas) return
    
    const rect = canvas.getBoundingClientRect()
    const newX = e.clientX - rect.left - 50
    const newY = e.clientY - rect.top - 10
    
    el.x = Math.max(0, Math.min(newX, rect.width - el.width))
    el.y = Math.max(0, Math.min(newY, rect.height - el.height))
  }
  
  const handleMouseUp = () => {
    isDragging = false
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

function startResize(e: MouseEvent, el: any, direction: string) {
  isResizing = true
  resizeData = {
    element: el,
    direction,
    startX: e.clientX,
    startY: e.clientY,
    startWidth: el.width,
    startHeight: el.height,
    startXPos: el.x,
    startYPos: el.y
  }
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isResizing || !resizeData) return
    
    const dx = e.clientX - resizeData.startX
    const dy = e.clientY - resizeData.startY
    
    if (resizeData.direction.includes('e')) {
      el.width = Math.max(20, resizeData.startWidth + dx)
    }
    if (resizeData.direction.includes('w')) {
      const newWidth = resizeData.startWidth - dx
      if (newWidth >= 20) {
        el.width = newWidth
        el.x = resizeData.startXPos + dx
      }
    }
    if (resizeData.direction.includes('s')) {
      el.height = Math.max(20, resizeData.startHeight + dy)
    }
    if (resizeData.direction.includes('n')) {
      const newHeight = resizeData.startHeight - dy
      if (newHeight >= 20) {
        el.height = newHeight
        el.y = resizeData.startYPos + dy
      }
    }
  }
  
  const handleMouseUp = () => {
    isResizing = false
    resizeData = null
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

function deleteElement() {
  if (!selectedElement.value) return
  const index = elements.value.findIndex(el => el.id === selectedElement.value.id)
  if (index > -1) {
    elements.value.splice(index, 1)
    selectedElement.value = null
  }
}

function handleClearCanvas() {
  elements.value = []
  selectedElement.value = null
}

function handlePreview() {
  // TODO: 实现预览功能
  console.log('预览数据:', JSON.stringify(elements.value))
}

function getTypeName(type: string): string {
  const names: Record<string, string> = {
    'text': '文本',
    'barcode': '条码',
    'qrcode': '二维码',
    'image': '图片',
    'field': '数据字段'
  }
  return names[type] || type
}

function getTypeColor(type: string): string {
  const colors: Record<string, string> = {
    'text': 'blue',
    'barcode': 'purple',
    'qrcode': 'cyan',
    'image': 'orange',
    'field': 'green'
  }
  return colors[type] || 'default'
}

// 监听元素变化，更新 JSON
watch(elements, (val) => {
  const json = JSON.stringify({
    elements: val,
    config: canvasConfig.value
  })
  emit('update:modelValue', json)
  emit('change', json)
}, { deep: true })

// 初始化时解析 JSON
watch(() => props.modelValue, (val) => {
  if (val && val !== '{}') {
    try {
      const data = JSON.parse(val)
      if (data.elements) {
        elements.value = data.elements
      }
      if (data.config) {
        canvasConfig.value = data.config
      }
    } catch (e) {
      console.error('解析模板 JSON 失败', e)
    }
  }
}, { immediate: true })
</script>

<style scoped lang="less">
.template-editor {
  height: 100%;
  background: #f5f5f5;
  padding: 16px;

  .panel-card {
    height: 100%;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    :deep(.ant-card-head) {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 8px 8px 0 0;
      padding: 12px 16px;
      border-bottom: none;

      .ant-card-head-title {
        font-weight: 600;
        font-size: 14px;
      }
    }

    :deep(.ant-card-body) {
      padding: 12px;
      height: calc(100% - 57px);
      overflow-y: auto;
    }
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 8px;

    .anticon {
      font-size: 16px;
    }
  }

  .component-item {
    padding: 10px 12px;
    margin-bottom: 8px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 6px;
    cursor: move;
    transition: all 0.3s;
    border: 1px solid transparent;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-color: #667eea;
    }

    .component-content {
      display: flex;
      align-items: center;
      gap: 8px;

      .component-name {
        font-size: 13px;
        font-weight: 500;
      }
    }
  }

  .field-item {
    padding: 8px 10px;
    margin-bottom: 6px;
    background: #fff;
    border-radius: 4px;
    cursor: move;
    transition: all 0.3s;
    border: 1px solid #e8e8e8;

    &:hover {
      background: #e6f7ff;
      border-color: #1890ff;
      transform: translateX(4px);
    }

    .field-content {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .field-label {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #333;
      }

      .field-key {
        font-size: 11px;
        color: #888;
        background: #f5f5f5;
        padding: 2px 6px;
        border-radius: 3px;
        align-self: flex-start;
      }
    }
  }

  .canvas-wrapper {
    height: calc(100% - 50px);
    overflow: auto;
    background: #f0f2f5;
    border-radius: 4px;
    padding: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .canvas-container {
    min-width: 100%;
    min-height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .label-canvas {
      background: white;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      position: relative;
      transition: all 0.3s;
      border: 1px solid #d9d9d9;

      .empty-tip {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;
        color: #999;

        .empty-text {
          font-size: 16px;
          margin-bottom: 8px;
          color: #666;
        }

        .empty-hint {
          font-size: 13px;
          color: #999;
        }
      }

      .canvas-element {
        background: rgba(255, 255, 255, 0.9);
        transition: all 0.2s;

        .element-content {
          display: flex;
          align-items: center;
          gap: 6px;
          height: 100%;
          color: #333;

          .field-tag {
            font-family: 'Courier New', monospace;
            background: #e6f7ff;
            padding: 2px 6px;
            border-radius: 3px;
            border: 1px dashed #1890ff;
          }
        }

        &:hover {
          border-color: #40a9ff !important;
          background: rgba(24, 144, 255, 0.08);
        }

        &.active {
          border-color: #1890ff !important;
          background: rgba(24, 144, 255, 0.1);
          box-shadow: 0 2px 12px rgba(24, 144, 255, 0.3);
        }

        .element-resize-handle {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          pointer-events: none;

          .resize-handle {
            position: absolute;
            background: #1890ff;
            pointer-events: all;
            opacity: 0;
            transition: opacity 0.2s;

            &.resize-n, &.resize-s {
              left: 10%;
              right: 10%;
              height: 6px;
              cursor: ns-resize;
            }

            &.resize-e, &.resize-w {
              top: 10%;
              bottom: 10%;
              width: 6px;
              cursor: ew-resize;
            }

            &.resize-ne, &.resize-nw, &.resize-se, &.resize-sw {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              cursor: pointer;
            }

            &.resize-n { top: -3px; }
            &.resize-s { bottom: -3px; }
            &.resize-e { right: -3px; }
            &.resize-w { left: -3px; }
            &.resize-ne { top: -4px; right: -4px; }
            &.resize-nw { top: -4px; left: -4px; }
            &.resize-se { bottom: -4px; right: -4px; }
            &.resize-sw { bottom: -4px; left: -4px; }
          }

          &:hover .resize-handle {
            opacity: 1;
          }
        }
      }
    }
  }

  :deep(.ant-collapse) {
    border: none;
    background: transparent;

    .ant-collapse-item {
      border: none;
      margin-bottom: 8px;

      .ant-collapse-header {
        background: #fafafa;
        border-radius: 4px;
        padding: 8px 12px;
        font-weight: 500;
      }

      .ant-collapse-content {
        .ant-collapse-content-box {
          padding: 8px 4px;
        }
      }
    }
  }

  :deep(.ant-form-item) {
    margin-bottom: 12px;

    .ant-form-item-label > label {
      font-weight: 500;
      color: #333;
    }
  }

  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-divider) {
    margin: 12px 0;
    font-size: 12px;
    color: #888;
  }
}
</style>
