<!--
  英文文本显示测试页面
    ????????????????????????
-->
<template>
  <div class="english-test-page">
    <a-card title="English Text Display Test" class="test-card">
      <a-space direction="vertical" :size="24" style="width: 100%">
        
        <!-- 娴嬭瘯璇存槑 -->
        <a-alert
          message="Test Purpose"
          description="This page tests how English text (long words, long sentences) displays in different container widths and components."
          type="info"
          show-icon
        />
        
        <!-- 闀垮崟璇嶆祴璇?-->
        <div class="test-section">
          <h3>1. Long Words Test</h3>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-card title="Narrow Container (200px)" size="small">
                <div style="width: 200px">
                  <p>Internationalization</p>
                  <p>Telecommunications</p>
                  <p>Responsibilities</p>
                  <p>Acknowledgement</p>
                  <p>Incomprehensibilities</p>
                </div>
              </a-card>
            </a-col>
            
            <a-col :span="8">
              <a-card title="Medium Container (300px)" size="small">
                <div style="width: 300px">
                  <p>Internationalization</p>
                  <p>Telecommunications</p>
                  <p>Responsibilities</p>
                  <p>Acknowledgement</p>
                  <p>Incomprehensibilities</p>
                </div>
              </a-card>
            </a-col>
            
            <a-col :span="8">
              <a-card title="Wide Container (400px)" size="small">
                <div style="width: 400px">
                  <p>Internationalization</p>
                  <p>Telecommunications</p>
                  <p>Responsibilities</p>
                  <p>Acknowledgement</p>
                  <p>Incomprehensibilities</p>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>
        
        <!-- 闀垮彞瀛愭祴璇?-->
        <div class="test-section">
          <h3>2. Long Sentences Test</h3>
          <a-card size="small">
            <p>
              The quick brown fox jumps over the lazy dog. This is a very long sentence that contains multiple words and should wrap properly in different container widths without causing layout issues or text overflow problems.
            </p>
            <p>
              Manufacturing Execution System (MES) is a computerized system used in manufacturing to track and document the transformation of raw materials to finished goods. It provides information that helps manufacturing decision makers understand how current conditions on the plant floor can be optimized to improve production output.
            </p>
          </a-card>
        </div>
        
        <!-- 鑿滃崟娴嬭瘯 -->
        <div class="test-section">
          <h3>3. Menu Items Test</h3>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-card title="Narrow Menu (200px)" size="small">
                <a-menu mode="inline" style="width: 200px">
                  <a-menu-item key="1">
                    <template #icon><UserOutlined /></template>
                    User Management
                  </a-menu-item>
                  <a-menu-item key="2">
                    <template #icon><SettingOutlined /></template>
                    System Configuration
                  </a-menu-item>
                  <a-menu-item key="3">
                    <template #icon><DatabaseOutlined /></template>
                    Database Administration
                  </a-menu-item>
                  <a-sub-menu key="sub1">
                    <template #icon><AppstoreOutlined /></template>
                    <template #title>Production Management</template>
                    <a-menu-item key="4">Manufacturing Orders</a-menu-item>
                    <a-menu-item key="5">Quality Inspection</a-menu-item>
                  </a-sub-menu>
                </a-menu>
              </a-card>
            </a-col>
            
            <a-col :span="12">
              <a-card title="Wide Menu (300px)" size="small">
                <a-menu mode="inline" style="width: 300px">
                  <a-menu-item key="1">
                    <template #icon><UserOutlined /></template>
                    User Management
                  </a-menu-item>
                  <a-menu-item key="2">
                    <template #icon><SettingOutlined /></template>
                    System Configuration
                  </a-menu-item>
                  <a-menu-item key="3">
                    <template #icon><DatabaseOutlined /></template>
                    Database Administration
                  </a-menu-item>
                  <a-sub-menu key="sub1">
                    <template #icon><AppstoreOutlined /></template>
                    <template #title>Production Management</template>
                    <a-menu-item key="4">Manufacturing Orders</a-menu-item>
                    <a-menu-item key="5">Quality Inspection</a-menu-item>
                  </a-sub-menu>
                </a-menu>
              </a-card>
            </a-col>
          </a-row>
        </div>
        
        <!-- 琛ㄦ牸娴嬭瘯 -->
        <div class="test-section">
          <h3>4. Table Test</h3>
          <a-table
            :columns="tableColumns"
            :data-source="tableData"
            :pagination="false"
            size="small"
          />
        </div>
        
        <!-- 鎸夐挳娴嬭瘯 -->
        <div class="test-section">
          <h3>5. Button Test</h3>
          <a-space wrap>
            <a-button type="primary">Submit</a-button>
            <a-button>Cancel</a-button>
            <a-button type="primary">Save Configuration</a-button>
            <a-button>Reset to Default</a-button>
            <a-button type="primary">Export Data</a-button>
            <a-button>Import from File</a-button>
            <a-button type="primary">Generate Report</a-button>
            <a-button>Download Documentation</a-button>
          </a-space>
        </div>
        
        <!-- 琛ㄥ崟娴嬭瘯 -->
        <div class="test-section">
          <h3>6. 表单 Test</h3>
          <a-form
            :model="formData"
            :label-col="{ span: 8 }"
            :wrapper-col="{ span: 16 }"
          >
            <a-form-item label="Username">
              <a-input v-model:value="formData.username" placeholder="Enter your username" />
            </a-form-item>
            
            <a-form-item label="Email Address">
              <a-input v-model:value="formData.email" placeholder="Enter your email address" />
            </a-form-item>
            
            <a-form-item label="Department">
              <a-select v-model:value="formData.department" placeholder="Select your department">
                <a-select-option value="1">Manufacturing Department</a-select-option>
                <a-select-option value="2">Quality Assurance</a-select-option>
                <a-select-option value="3">Production Planning</a-select-option>
                <a-select-option value="4">Warehouse Management</a-select-option>
              </a-select>
            </a-form-item>
            
            <a-form-item label="Description">
              <a-textarea
                v-model:value="formData.description"
                placeholder="Enter a detailed description of your requirements and expectations"
                :rows="4"
              />
            </a-form-item>
          </a-form>
        </div>
        
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import {
  UserOutlined,
  SettingOutlined,
  DatabaseOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'

// 琛ㄦ牸鍒楀畾涔?
const tableColumns = [
  {
    title: 'Order Number',
    dataIndex: 'orderNumber',
    key: 'orderNumber',
    width: 150,
  },
  {
    title: 'Product Name',
    dataIndex: 'productName',
    key: 'productName',
    width: 200,
  },
  {
    title: 'Manufacturing 状态',
    dataIndex: 'status',
    key: 'status',
    width: 180,
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
  },
]

// 琛ㄦ牸鏁版嵁
const tableData = [
  {
    key: '1',
    orderNumber: 'MO-2025-001',
    productName: 'High-Performance Manufacturing Component',
    status: 'In Progress',
    description: 'This is a very long description that contains multiple words and should wrap properly without causing layout issues.',
  },
  {
    key: '2',
    orderNumber: 'MO-2025-002',
    productName: 'Telecommunications Equipment',
    status: 'Quality Inspection',
    description: 'Manufacturing Execution System integration for telecommunications equipment production line.',
  },
  {
    key: '3',
    orderNumber: 'MO-2025-003',
    productName: 'Internationalization Module',
    status: 'Completed',
    description: 'Internationalization and localization support for multi-language manufacturing systems.',
  },
]

// 琛ㄥ崟鏁版嵁
const formData = reactive({
  username: '',
  email: '',
  department: undefined,
  description: '',
})
</script>

<style scoped lang="less">
.english-test-page {
  padding: 24px;
  
  .test-card {
    max-width: 1400px;
    margin: 0 auto;
  }
  
  .test-section {
    padding: 16px;
    background: var(--fx-bg-container, #ffffff);
    border: 1px solid var(--fx-border-color, #e5e7eb);
    border-radius: var(--fx-radius, 6px);
    
    h3 {
      margin-top: 0;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 600;
      color: var(--fx-text-primary, #111827);
    }
    
    p {
      margin-bottom: 8px;
      line-height: 1.6;
    }
  }
}
</style>
