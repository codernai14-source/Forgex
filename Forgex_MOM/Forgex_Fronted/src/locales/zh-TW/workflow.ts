/**
 * 工作流模組翻譯 - 繁體中文
 */
export default {
  taskConfig: {
    addTask: '新增審批任務',
    editTask: '編輯審批任務',
    nodeConfig: '節點配置',
    customForm: '自定義表單',
    lowCodeForm: '低代碼表單',
    confirmDelete: '確定要刪除這個審批任務嗎？',
    taskName: '任務名稱',
    taskCode: '任務編碼',
    interpreterBean: '解釋器 Bean',
    formType: '表單類型',
    formPath: '表單路徑',
    formContent: '表單內容',
    status: '狀態',
    remark: '備註',
    form: {
      taskName: '請輸入任務名稱',
      taskCode: '請輸入任務編碼',
      interpreterBean: '請輸入解釋器 Bean（可選）',
      formType: '請選擇表單類型',
      formPath: '請輸入表單組件路徑',
      formContent: '請輸入低代碼表單 JSON',
      remark: '請輸入備註'
    }
  },
  execution: {
    startCenter: '發起中心',
    startTitle: '卡片化發起審批',
    startDesc: '先選擇流程卡片，再填寫對應表單並直接發起。這裡已經預置了一個「請假審批」示例，方便你查看用戶發起到 admin審核結束的完整實現。',
    enabledTasks: '已啟用流程',
    filteredTasks: '當前篩選',
    categoryTitle: '業務分類',
    recentTitle: '最近使用',
    allFlows: '全部流程',
    flowCount: '個流程',
    searchPlaceholder: '搜索流程名稱、編碼或備註',
    emptyTask: '暫無可發起的審批流程',
    currentSelection: '當前選中流程',
    defaultRemark: '這是一個標準審批流程，可直接進入表單發起。',
    launchTask: '立即發起',
    formNotRegistered: '當前流程尚未註冊前端表單組件',
    lowCodePlaceholder: '請輸入表單 JSON 內容',
    submitApproval: '提交審批',
    customForm: '自定義表單',
    lowCodeForm: '低代碼表單',
    lowCodeRenderer: '低代碼表單渲染器開發中',
    lowCodeDesc: '當前先以 JSON 方式提交表單內容',
    taskCode: '審批任務',
    taskName: '任務名稱',
    formType: '表單類型',
    formContent: '表單內容',
    form: {
      selectTask: '請選擇審批任務'
    }
  },
  myTask: {
    approve: '同意',
    reject: '駁回',
    detail: '詳情',
    history: '審批歷史',
    cancelApproval: '撤销',
    approveAgree: '審批同意',
    approveReject: '審批駁回'
  },
  dashboard: {
    title: '審批工作台',
    subtitle: '集中查看待辦、昨日已處理與抄送待閱，點擊條目可查看詳情。',
    pendingTitle: '待我處理',
    yesterdayTitle: '昨天處理的任務',
    ccTitle: '抄送給我的',
    more: '查看更多',
    ccHint: '去待辦處理',
    ccTag: '抄送',
    empty: '暫無數據',
    loadFailed: '加載工作台數據失敗',
    status: {
      pending: '未審批',
      processing: '審批中',
      done: '審批完成',
      rejected: '駁回'
    }
  }
}