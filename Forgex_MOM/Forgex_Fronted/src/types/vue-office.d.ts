/**
 * vue-office 懒加载预览组件的模块声明。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
declare module '@vue-office/docx' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{ src: string }>
  export default component
}

declare module '@vue-office/excel' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{ src: string }>
  export default component
}

declare module '@vue-office/pdf' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{ src: string }>
  export default component
}

declare module '@vue-office/docx/lib/index.css'
declare module '@vue-office/excel/lib/index.css'
