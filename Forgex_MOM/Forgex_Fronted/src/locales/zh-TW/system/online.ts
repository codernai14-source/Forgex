export default {
  action: {
    kickout: '強制下線',
    batchKickout: '批量強制下線',
    detail: '詳情',
  },
  terminal: {
    all: '全部',
    b: 'B端',
    c: 'C端',
    thirdParty: '第三方',
  },
  ttl: {
    longTerm: '長期',
    second: '{count}s',
    minuteSecond: '{minute}m {second}s',
  },
  confirm: {
    kickoutTitle: '確認強制下線',
    kickoutContent: '此操作會使對應會話立即失效。',
    batchKickoutTitle: '確認批量強制下線',
    batchKickoutContent: '將對選中的 {count} 個會話執行強制下線。',
    onlyKickout: '僅下線，保留用戶啟用狀態',
    kickoutAndDisable: '下線並停用用戶',
    batchTarget: '選中的 {count} 個用戶',
  },
  detail: {
    title: '在線會話詳情',
    account: '帳號',
    username: '用戶名稱',
    tenant: '租戶',
    sessionCount: '有效會話數',
    token: 'Token',
    terminal: '登入端',
    region: '登入地區',
    ttl: '剩餘會話時間',
  },
}
