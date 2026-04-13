/**
 * 个人首页翻译 - 日文注释
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // 头部区域
  hero: {
    eyebrow: 'パーソナルワークスペース',
    title: '個人ホーム',
    titleManage: '個人ホーム初期設定',
    desc: 'システム日志イン後の初期表示ページで、個人の好みに応じてコンポーネントのレイアウトを保存できます。',
    descManage: 'パブリックレベルとテナントレベルの初期ポータルレイアウトを一括管理し、すべてのユーザーがこれを基にシステムを利用します。',
    badge: {
      default: '初期エントリー',
      user: 'ユーザーレベル',
      tenant: 'テナントレベル',
      public: 'パブリックレベル',
    },
  },

  // 工具栏
  toolbar: {
    editMode: 'レイアウト編集',
    exitMode: '編集終了',
    refresh: '更新',
    resetDefault: '初期値にリセット',
    saveLayout: 'レイアウト保存',
    hint: {
      edit: 'コンポーネントをドラッグしてサイズ変更後、直接保存できます',
      view: '編集モードに切り替えてコンポーネントのレイアウトを調整できます',
    },
  },

  // 设置面板
  panel: {
    title: 'ウィジェット設定',
    subtitle: '表示/非表示、件数、其他のオプション',
  },

  // 组件配置字段
  widget: {
    limit: '表示件数',
    showMore: 'もっと見るリンクを表示',
    more: 'もっと見る',
  },

  // 组件标题
  components: {
    commonMenus: {
      title: 'よく使うメニュー',
      subtitle: 'システム集計による固定 Top 6 メニュー',
      empty: 'よく使うメニューはありません',
    },
    myFavorites: {
      title: 'お気に入り',
      subtitle: '自分で登録したショートカット',
      empty: 'お気に入りメニューはありません',
      add: 'お気に入りに追加',
      remove: 'お気に入りを解除',
    },
    pendingApprovals: {
      title: '承認待ち',
      subtitle: '自分に割り当てられた承認タスク',
      empty: '承認待ちのタスクはありません',
    },
    calendar: {
      title: 'カレンダー',
      subtitle: 'ローカルカレンダービュー',
    },
    messages: {
      title: '自分の消息',
      subtitle: 'ユーザーから送信された站内消息',
      empty: '未読消息はありません',
      systemSender: '站内消息',
    },
    notices: {
      title: 'システム通知',
      subtitle: '承認およびシステム通知',
      empty: 'システム通知はありません',
      systemType: 'システム通知',
    },
    currentTime: {
      title: '現在の時刻',
      subtitle: '現在の日時',
    },
  },

  // 空状態
  empty: '有効なホームウィジェットはありません',

  // 消息
  message: {
    loadFailed: 'ホーム設定の読み込みに失敗しました',
    saveSuccess: 'ホーム設定を保存しました',
    saveFailed: 'ホーム設定の保存に失敗しました',
    resetSuccess: '初期レイアウトにリセットしました',
    resetFailed: '初期レイアウトへのリセットに失敗しました',
  },

  // お気に入り管理ページ
  management: {
    title: 'お気に入り管理',
    desc: 'お気に入りメニューをまとめて管理し、表示順の調整や一括解除を行えます。',
    alert: '「お気に入り」カードはここで保存した順序で表示されます。「よく使うメニュー」はシステム集計の Top 6 を固定表示します。',
    empty: 'お気に入りメニューがありません。ホームまたはメニューから先に追加してください。',
    stats: {
      count: '{count} 件を登録済み',
    },
    table: {
      order: '順序',
      menu: 'メニュー',
      path: 'ルートパス',
      action: '操作',
    },
    action: {
      refresh: '再読み込み',
      batchCancel: '一括解除',
      saveSort: '順序を保存',
      moveUp: '上へ',
      moveDown: '下へ',
      open: '開く',
      remove: '解除',
    },
    confirm: {
      batchCancelTitle: '一括解除の確認',
      batchCancelContent: '選択した {count} 件のお気に入りを解除しますか？',
      singleCancelTitle: '解除の確認',
      singleCancelContent: '「{title}」をお気に入りから解除しますか？',
    },
    message: {
      loadFailed: 'お気に入り一覧の読み込みに失敗しました',
      batchCancelSuccess: '一括解除が完了しました',
      batchCancelFailed: '一括解除に失敗しました',
      singleCancelSuccess: 'お気に入りを解除しました',
      singleCancelFailed: 'お気に入りの解除に失敗しました',
      sortSaveSuccess: 'お気に入り順序を保存しました',
      sortSaveFailed: 'お気に入り順序の保存に失敗しました',
    },
  },

  // 摘要卡片
  summary: {
    greeting: {
      honorificMale: '様',
      honorificFemale: '様',
      lead: {
        morning: 'おはようございます',
        afternoon: 'こんにちは',
        evening: 'こんばんは',
      },
      closing: {
        morning: '元気いっぱいで、生産性のある一日をスタートしましょう。',
        afternoon: '調子を維持しましょう。適度に休憩して深呼吸してください。',
        evening: '充実した一日でしたね。今夜はゆっくり休んで充電してください。',
      },
      lineJaMale: '{name} 様、{lead} — {closing}',
      lineJaFemale: '{name} 様、{lead} — {closing}',
      lineJaNeutral: '{name} さん、{lead} — {closing}',
    },
    weekday: {
      0: '日曜日',
      1: '月曜日',
      2: '火曜日',
      3: '水曜日',
      4: '木曜日',
      5: '金曜日',
      6: '土曜日',
    },
    todayLineJa: '今日は{month}月{day}日 {weekday}',
    onlineDuration: 'オンライン時間',
  },
}