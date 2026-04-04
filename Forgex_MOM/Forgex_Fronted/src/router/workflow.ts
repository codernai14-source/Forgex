/**
 * 工作流模块路由配置
 */
import type { RouteRecordRaw } from 'vue-router'

const workflowRoutes: RouteRecordRaw[] = [
  {
    path: '/workflow',
    name: 'Workflow',
    redirect: '/workflow/taskConfig',
    children: [
      {
        path: 'taskConfig',
        name: 'WorkflowTaskConfig',
        component: () => import('@/views/workflow/taskConfig/index.vue'),
        meta: {
          title: '审批任务配置',
          icon: 'SettingOutlined'
        }
      },
      {
        path: 'taskConfig/:taskCode/nodes',
        name: 'WorkflowTaskConfigNodes',
        component: () => import('@/views/workflow/taskConfig/nodes.vue'),
        meta: {
          title: '审批节点配置',
          hidden: true
        }
      },
      {
        path: 'execution/start',
        name: 'WorkflowExecutionStart',
        component: () => import('@/views/workflow/execution/start.vue'),
        meta: {
          title: '发起审批',
          icon: 'PlusOutlined'
        }
      },
      {
        path: 'execution/start/:taskCode',
        name: 'WorkflowExecutionStartForm',
        component: () => import('@/views/workflow/execution/startForm.vue'),
        meta: {
          title: '填写审批表单',
          hidden: true
        }
      },
      {
        path: 'my/pending',
        name: 'WorkflowMyPending',
        component: () => import('@/views/workflow/myTask/pending.vue'),
        meta: {
          title: '我的待办',
          icon: 'BellOutlined'
        }
      },
      {
        path: 'my/processed',
        name: 'WorkflowMyProcessed',
        component: () => import('@/views/workflow/myTask/processed.vue'),
        meta: {
          title: '我已处理',
          icon: 'CheckCircleOutlined'
        }
      },
      {
        path: 'my/initiated',
        name: 'WorkflowMyInitiated',
        component: () => import('@/views/workflow/myTask/initiated.vue'),
        meta: {
          title: '我发起的',
          icon: 'SendOutlined'
        }
      }
    ]
  }
]

export default workflowRoutes
