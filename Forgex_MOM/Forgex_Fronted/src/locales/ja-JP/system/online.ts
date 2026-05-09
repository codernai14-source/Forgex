export default {
  action: {
    kickout: '強制オフライン',
    batchKickout: '一括強制オフライン',
  },
  terminal: {
    all: 'すべて',
    b: 'B端末',
    c: 'C端末',
    thirdParty: 'サードパーティ',
  },
  ttl: {
    longTerm: '無期限',
    second: '{count}s',
    minuteSecond: '{minute}m {second}s',
  },
  confirm: {
    kickoutTitle: '強制オフラインの確認',
    kickoutContent: 'この操作により対象セッションは直ちに無効になります。',
    batchKickoutTitle: '一括強制オフラインの確認',
    batchKickoutContent: '選択した {count} 件のセッションを強制オフラインにします。',
  },
}
