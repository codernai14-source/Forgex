import { defineAsyncComponent } from 'vue'

export const workflowFormRegistry: Record<string, ReturnType<typeof defineAsyncComponent>> = {
  '/workflow/form/leave': defineAsyncComponent(() => import('@/views/workflow/form/LeaveForm.vue'))
}
