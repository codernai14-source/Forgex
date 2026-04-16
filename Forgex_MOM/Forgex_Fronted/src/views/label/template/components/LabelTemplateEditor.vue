
<template>
  <div class="template-editor">
    <a-row :gutter="16" style="height: 500px;">
      <!-- 左侧：组件面板 -->
      <a-col :span="6">
        <a-card title="组件库" size="small" style="height: 100%;">
          <a-collapse v-model:activeKey="activeKeys">
            <a-collapse-panel key="basic" header="基础组件">
              <div v-for="comp in basicComponents" :key="comp.type"
                   class="component-item"
                   draggable="true"
                   @dragstart="handleDragStart(comp)">
                <component :is="comp.icon" />
                <span>{{ comp.name }}</span>
              </div>
            </a-collapse-panel>

            <a-collapse-panel key="data" header="数据字段">
              <div v-for="field in availableFields" :key="field.key"
                   class="field-item"
                   draggable="true"
                   @dragstart="handleFieldDragStart(field)">
                <span>{{ field.label }}</span>
                <code>{{ field.key }}</code>
              </div>
            </a-collapse-panel>
          </a-collapse>
        </a-card>
      </a-col>

      <!-- 中间：画布区域 -->
      <a-col :span="12">
        <a-card title="画布" size="small" style="height: 100%;">
          <template #extra>
            <a-space>
              <a-select v-model:value="canvasConfig.paperSize" style="width: 120px">
                <a-select-option value="60x40">60mm x 40mm</a-select-option>
                <a-select-option value="80x60">80mm x 60mm</a-select-option>
                <a-select-option value="100x80">100mm x 80mm</a-select-option>
              </a-select>
              <a-button size="small" @click="handlePreview">预览</a-button>
            </a-space>
          </template>

          <div class="canvas-container"
               @drop="handleDrop"
               @dragover.prevent>
            <div class="label-preview" :style="labelSizeStyle">
              <div v-if="elements.length === 0" class="empty-tip">
                拖拽组件到此处
              </div>
              <div v-for="el in elements" :key="el.id"
                   class="canvas-element"
                   :class="{ active: selectedElement?.id === el.id }"
                   :style="getElementStyle(el)"
                   @click.stop="selectElement(el)">
                {{ el.content || el.type }}
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：属性面板 -->
      <a-col :span="6">
        <a-card title="属性配置" size="small" style="height: 100%;">
          <a-form v-if="selectedElement" layout="vertical" size="small">
            <a-form-item label="元素类型">
              <a-input :value="selectedElement.type" disabled />
            </a-form-item>
            <a-form-item label="文本内容">
              <a-input v-model:value="selectedElement.content" />
            </a-form-item>
            <a-form-item label="字体大小">
              <a-input-number v-model:value="selectedElement.fontSize" :min="8" :max="72" style="width: 100%" />
            </a-form-item>
            <a-form-item label="位置 X">
              <a-input-number v-model:value="selectedElement.x" style="width: 100%" />
            </a-form-item>
            <a-form-item label="位置 Y">
              <a-input-number v-model:value="selectedElement.y" style="width: 100%" />
            </a-form-item>
            <a-form-item label="宽度">
              <a-input-number v-model:value="selectedElement.width" :min="10" style="width: 100%" />
            </a-form-item>
            <a-form-item label="高度">
              <a-input-number v-model:value="selectedElement.height" :min="10" style="width: 100%" />
            </a-form-item>
            <a-button danger block @click="deleteElement">删除元素</a-button>
          </a-form>
          <a-empty v-else description="请选择一个元素" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">import { ref, computed, watch, type CSSProperties } from 'vue'
import { FontSizeOutlined, BarcodeOutlined, QrcodeOutlined, PictureOutlined } from '@ant-design/icons-vue'

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
  { key: 'supplierName', label: '供应商名称' }
]

const labelSizeStyle = computed(() => {
  const sizes: Record<string, string> = {
    '60x40': 'width: 227px; height: 151px',
    '80x60': 'width: 302px; height: 227px',
    '100x80': 'width: 378px; height: 302px'
  }
  return sizes[canvasConfig.value.paperSize] || sizes['60x40']
})

let draggedItem: any = null

function handleDragStart(comp: any) {
  draggedItem = { type: 'component', data: comp }
}

function handleFieldDragStart(field: any) {
  draggedItem = { type: 'field', data: field }
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  if (!draggedItem) return

  const rect = (e.target as HTMLElement).getBoundingClientRect()
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
      fontSize: 14
    }
    elements.value.push(newElement)
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
    border: '1px dashed #d9d9d9',
    cursor: 'move',
    padding: '4px',
    overflow: 'hidden'
  }
}

function deleteElement() {
  if (!selectedElement.value) return
  const index = elements.value.findIndex(el => el.id === selectedElement.value.id)
  if (index > -1) {
    elements.value.splice(index, 1)
    selectedElement.value = null
  }
}

function handlePreview() {
  // TODO: 实现预览功能
  console.log('预览数据:', JSON.stringify(elements.value))
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
      console.error('解析模板JSON失败', e)
    }
  }
}, { immediate: true })
</script>

<style scoped lang="less">
.template-editor {
  .component-item,
  .field-item {
    padding: 8px 12px;
    margin-bottom: 4px;
    background: #f5f5f5;
    border-radius: 4px;
    cursor: move;
    display: flex;
    align-items: center;
    gap: 8px;

    &:hover {
      background: #e6f7ff;
    }

    code {
      font-size: 12px;
      color: #999;
      margin-left: auto;
    }
  }

  .canvas-container {
    height: calc(100% - 50px);
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    border: 1px dashed #d9d9d9;

    .label-preview {
      background: white;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      position: relative;

      .empty-tip {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: #999;
      }

      .canvas-element {
        &:hover {
          border-color: #1890ff;
        }

        &.active {
          border-color: #1890ff;
          border-style: solid;
        }
      }
    }
  }
}
</style>
