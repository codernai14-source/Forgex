/**
 * パーソナルホーム翻訳 - 日本語
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
export default {
  // Hero セクション
  hero: {
    eyebrow: 'パーソナルワークスペース',
    title: '個人ホーム',
    titleManage: '個人ホーム初期設定',
    desc: 'システムログイン後の初期表示ページで、個人の好みに応じてコンポーネントのレイアウトを保存できます。',
    descManage: 'パブリックレベルとテナントレベルの初期ポータルレイアウトを一括管理し、すべてのユーザーがこれを基にシステムを利用します。',
    badge: {
      default: '初期エントリー',
      user: 'ユーザーレベル',
      tenant: 'テナントレベル',
      public: 'パブリックレベル',
    },
  },

  // ツールバー
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

  // 設定パネル
  panel: {
    title: 'ウィジェット設定',
    subtitle: '表示/非表示、件数、その他のオプション',
  },

  // ウィジェット設定フィールド
  widget: {
    limit: '表示件数',
    showMore: 'もっと見るリンクを表示',
    more: 'もっと見る',
  },

  // ウィジェットタイトル
  components: {
    commonMenus: {
      title: 'よく使うメニュー',
      subtitle: '頻繁に使用するエントリーと最近訪問したメニュー',
      empty: 'よく使うメニューはありません',
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
      title: '自分のメッセージ',
      subtitle: 'ユーザーから送信された站内メッセージ',
      empty: '未読メッセージはありません',
      systemSender: '站内メッセージ',
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

  // メッセージ
  message: {
    loadFailed: 'ホーム設定の読み込みに失敗しました',
    saveSuccess: 'ホーム設定を保存しました',
    saveFailed: 'ホーム設定の保存に失敗しました',
    resetSuccess: '初期レイアウトにリセットしました',
    resetFailed: '初期レイアウトへのリセットに失敗しました',
  },

  // サマリーカード
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