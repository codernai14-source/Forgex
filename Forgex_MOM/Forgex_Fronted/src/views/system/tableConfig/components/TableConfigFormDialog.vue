<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('system.tableConfig.edit') : t('system.tableConfig.add')"
    :loading="loading"
    width="1200px"
    @submit="handleSubmit"
    @cancel="handleCancel"
  >
    <a-tabs v-model:activeKey="activeTab" type="card">
      <a-tab-pane key="basic" :tab="t('system.tableConfig.basicInfo')">
        <a-form
          ref="basicFormRef"
          :model="formData"
          :rules="basicRules"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.tableCode')" name="tableCode">
                <a-input
                  v-model:value="formData.tableCode"
                  :placeholder="t('system.tableConfig.form.tableCode')"
                  :disabled="isEdit"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.tableName')" name="tableNameI18nJson">
                <I18nInput
                  v-model="formData.tableNameI18nJson"
                  mode="simple"
                  :placeholder="t('system.tableConfig.form.tableName')"
                />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.tableType')" name="tableType">
                <a-select
                  v-model:value="formData.tableType"
                  :placeholder="t('system.tableConfig.form.tableType')"
                >
                  <a-select-option value="NORMAL">{{ t('system.tableConfig.tableTypeNormal') }}</a-select-option>
                  <a-select-option value="LAZY">{{ t('system.tableConfig.tableTypeLazy') }}</a-select-option>
                  <a-select-option value="TREE">{{ t('system.tableConfig.tableTypeTree') }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.rowKey')" name="rowKey">
                <a-input
                  v-model:value="formData.rowKey"
                  :placeholder="t('system.tableConfig.form.rowKey')"
                />
              </a-form-item>
            </a-col>
          </a-row>
          
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.defaultPageSize')" name="defaultPageSize">
                <a-input-number
                  v-model:value="formData.defaultPageSize"
                  :min="1"
                  :max="100"
                  style="width: 100%;"
                />
              </a-form-item>
            </a-col>
            
            <a-col :span="12">
              <a-form-item :label="t('system.tableConfig.enabled')" name="enabled">
                <a-switch v-model:checked="formData.enabled" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-tab-pane>
      
      <a-tab-pane key="columns" :tab="t('system.tableConfig.columnConfig')">
        <div style="margin-bottom: 16px;">
          <a-button type="primary" @click="handleAddColumn">
            {{ t('system.tableConfig.addColumn') }}
          </a-button>
        </div>
        
        <a-table
          :columns="columnTableColumns"
          :data-source="formData.columns"
          :pagination="false"
          :scroll="{ x: 1500 }"
          row-key="tempId"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'field'">
              <a-input v-model:value="record.field" :placeholder="t('system.tableConfig.form.field')" />
            </template>
            
            <template v-else-if="column.key === 'title'">
              <I18nInput
                v-model="record.titleI18nJson"
                mode="simple"
                :placeholder="t('system.tableConfig.form.title')"
              />
            </template>
            
            <template v-else-if="column.key === 'align'">
              <a-select v-model:value="record.align" style="width: 100px;">
                <a-select-option value="left">left</a-select-option>
                <a-select-option value="center">center</a-select-option>
                <a-select-option value="right">right</a-select-option>
              </a-select>
            </template>
            
            <template v-else-if="column.key === 'width'">
              <a-input-number v-model:value="record.width" :min="1" style="width: 100px;" />
            </template>
            
            <template v-else-if="column.key === 'fixed'">
              <a-select v-model:value="record.fixed" allow-clear style="width: 100px;">
                <a-select-option value="left">left</a-select-option>
                <a-select-option value="right">right</a-select-option>
              </a-select>
            </template>
            
            <template v-else-if="column.key === 'ellipsis'">
              <a-checkbox v-model:checked="record.ellipsis" />
            </template>
            
            <template v-else-if="column.key === 'sortable'">
              <a-checkbox v-model:checked="record.sortable" />
            </template>
            
            <template v-else-if="column.key === 'queryable'">
              <a-checkbox v-model:checked="record.queryable" />
            </template>
            
            <template v-else-if="column.key === 'enabled'">
              <a-checkbox v-model:checked="record.enabled" />
            </template>
            
            <template v-else-if="column.key === 'orderNum'">
              <a-input-number v-model:value="record.orderNum" :min="0" style="width: 80px;" />
            </template>
            
            <template v-else-if="column.key === 'action'">
              <a-button type="link" danger @click="handleDeleteColumn(index)">
                {{ t('common.delete') }}
              </a-button>
            </template>
          </template>
        </a-table>
      </a-tab-pane>
    </a-tabs>
  </BaseFormDialog>
</template>

<script setup lang="ts">
/**
 * 琛ㄦ牸閰嶇疆琛ㄥ崟瀵硅瘽妗嗙粍浠?
 * 
 * 鍔熻兘锛?
 * 1. 琛ㄦ牸閰嶇疆鍩烘湰淇℃伅缂栬緫
 * 2. 琛ㄦ牸鍒楅厤缃鐞?
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, reactive, watch, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import {
  getTableConfigDetail,
  createTableConfig,
  updateTableConfig
} from '@/api/system/tableConfig'
import type { TableConfigDetail, TableColumnConfigItem } from '@/api/system/tableConfig'

interface Props {
  /** 瀵硅瘽妗嗘槸鍚︽墦寮€锛岀敤浜庢帶鍒剁粍浠剁殑鏄剧ず/闅愯棌鐘舵€?*/
  open: boolean
  /** 鏄惁涓虹紪杈戞ā寮忥紝true 琛ㄧず缂栬緫琛ㄦ牸閰嶇疆锛宖alse 琛ㄧず鏂板琛ㄦ牸閰嶇疆 */
  isEdit: boolean
  /** 琛ㄦ牸閰嶇疆 ID锛岀紪杈戞ā寮忎笅蹇呭～锛岀敤浜庡姞杞借〃鏍奸厤缃鎯?*/
  configId?: number
}

interface Emits {
  /**
   * 鏇存柊瀵硅瘽妗嗘墦寮€鐘舵€?
   * @param value 鏂扮殑鎵撳紑鐘舵€?
   */
  (e: 'update:open', value: boolean): void
  /**
   * 鎿嶄綔鎴愬姛浜嬩欢
   * 瑙﹀彂鏃舵満锛氭柊澧炴垨缂栬緫琛ㄦ牸閰嶇疆鎴愬姛淇濆瓨鍚庤Е鍙?
   */
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const loading = ref(false)
const activeTab = ref('basic')
const basicFormRef = ref()

const formData = reactive<TableConfigDetail>({
  tableCode: '',
  tableNameI18nJson: '',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  enabled: true,
  columns: []
})

const basicRules = {
  tableCode: [{ required: true, message: t('system.tableConfig.form.tableCode'), trigger: 'blur' }],
  tableNameI18nJson: [{ required: true, message: t('system.tableConfig.form.tableName'), trigger: 'blur' }],
  tableType: [{ required: true, message: t('system.tableConfig.form.tableType'), trigger: 'change' }],
  defaultPageSize: [{ required: true, message: t('system.tableConfig.form.defaultPageSize'), trigger: 'blur' }]
}

const columnTableColumns = [
  {
    title: t('system.tableConfig.field'),
    key: 'field',
    width: 150,
    fixed: 'left' as const
  },
  {
    title: t('system.tableConfig.title'),
    key: 'title',
    width: 150
  },
  {
    title: t('system.tableConfig.align'),
    key: 'align',
    width: 120
  },
  {
    title: t('system.tableConfig.width'),
    key: 'width',
    width: 120
  },
  {
    title: t('system.tableConfig.fixed'),
    key: 'fixed',
    width: 120
  },
  {
    title: t('system.tableConfig.ellipsis'),
    key: 'ellipsis',
    width: 80,
    align: 'center' as const
  },
  {
    title: t('system.tableConfig.sortable'),
    key: 'sortable',
    width: 80,
    align: 'center' as const
  },
  {
    title: t('system.tableConfig.queryable'),
    key: 'queryable',
    width: 80,
    align: 'center' as const
  },
  {
    title: t('system.tableConfig.enabled'),
    key: 'enabled',
    width: 80,
    align: 'center' as const
  },
  {
    title: t('system.tableConfig.orderNum'),
    key: 'orderNum',
    width: 100
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 80,
    fixed: 'right' as const
  }
]

const handleAddColumn = () => {
  const newColumn: TableColumnConfigItem = {
    tableCode: formData.tableCode,
    field: '',
    titleI18nJson: '',
    align: 'left',
    width: 120,
    ellipsis: false,
    sortable: false,
    queryable: false,
    orderNum: formData.columns.length,
    enabled: true,
    tempId: Date.now() + Math.random()
  }
  formData.columns.push(newColumn)
}

const handleDeleteColumn = (index: number) => {
  formData.columns.splice(index, 1)
}

const handleSubmit = async () => {
  try {
    await basicFormRef.value?.validate()

    if (formData.columns.length === 0) {
      message.warning(t('system.tableConfig.columnRequired'))
      return
    }
    
    loading.value = true
    
    const submitData = {
      ...formData,
      columns: formData.columns.map(({ tempId, ...col }) => ({
        ...col,
        tableCode: formData.tableCode
      }))
    }
    
    if (props.isEdit && props.configId) {
      await updateTableConfig(submitData)
      // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
    } else {
      await createTableConfig(submitData)
      // 鎴愬姛鎻愮ず鐢卞悗绔繑鍥烇紝鍦?http 鎷︽埅鍣ㄤ腑缁熶竴澶勭悊
    }
    
    emit('success')
  } catch (error) {
    console.error('淇濆瓨琛ㄦ牸閰嶇疆澶辫触:', error)
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('update:open', false)
}

const loadConfigDetail = async () => {
  console.log('loadConfigDetail called', { isEdit: props.isEdit, configId: props.configId })
  if (props.isEdit && props.configId) {
    loading.value = true
    try {
      console.log('寮€濮嬭皟鐢?getTableConfigDetail 鎺ュ彛锛孖D:', props.configId)
      const detail = await getTableConfigDetail(props.configId)
      console.log('鑾峰彇鍒扮殑璇︽儏鏁版嵁:', detail)
      Object.assign(formData, {
        ...detail,
        columns: detail.columns.map(col => ({
          ...col,
          tempId: col.id || Date.now() + Math.random()
        }))
      })
      console.log('琛ㄥ崟鏁版嵁宸叉洿鏂?', formData)
    } catch (error) {
      console.error('鍔犺浇琛ㄦ牸閰嶇疆璇︽儏澶辫触:', error)
      message.error(t('system.tableConfig.loadDetailFailed'))
    } finally {
      loading.value = false
    }
  } else {
    console.log('skip loading config detail', { isEdit: props.isEdit, configId: props.configId })
  }
}

const resetForm = () => {
  Object.assign(formData, {
    tableCode: '',
    tableNameI18nJson: '',
    tableType: 'NORMAL',
    rowKey: 'id',
    defaultPageSize: 20,
    enabled: true,
    columns: []
  })
  activeTab.value = 'basic'
}

// 鐩戝惉瀵硅瘽妗嗘墦寮€鐘舵€?
watch(() => props.open, (newOpen) => {
  console.log('=== watch 瑙﹀彂 ===')
  console.log('瀵硅瘽妗嗘墦寮€鐘舵€佸彉鍖?', newOpen)
  console.log('褰撳墠 props:', { open: props.open, isEdit: props.isEdit, configId: props.configId })
  
  if (newOpen) {
    // 浣跨敤 nextTick 纭繚 props 宸茬粡鍏ㄩ儴鏇存柊
    nextTick(() => {
      console.log('nextTick 鍚庣殑 props:', { open: props.open, isEdit: props.isEdit, configId: props.configId })
      if (props.isEdit) {
        console.log('鍑嗗鍔犺浇閰嶇疆璇︽儏...')
        loadConfigDetail()
      } else {
        console.log('鍑嗗閲嶇疆琛ㄥ崟...')
        resetForm()
      }
    })
  }
})

// 棰濆鐩戝惉 isEdit 鍜?configId 鐨勫彉鍖栵紙鐢ㄤ簬璋冭瘯锛?
watch(() => [props.isEdit, props.configId] as const, ([newIsEdit, newConfigId]) => {
  console.log('isEdit 鎴?configId 鍙樺寲:', { isEdit: newIsEdit, configId: newConfigId })
})
</script>

<style scoped>
:deep(.ant-tabs-content) {
  max-height: 600px;
  overflow-y: auto;
}
</style>
