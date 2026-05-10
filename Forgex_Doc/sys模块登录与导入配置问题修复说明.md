# sys 模块登录与导入配置问题修复说明

## 后端用户名自动填充

`AutoFillUsernameAspect` 只递归业务对象、集合、Map 和分页记录。`java.*`、`javax.*`、`jakarta.*`、`sun.*`、`jdk.*` 等 JDK/框架值对象，以及字符串、数字、日期时间、枚举等简单值会直接跳过，避免在 JDK 17 模块边界下反射访问 `Charset` 等内部字段导致 `InaccessibleObjectException`。

## 前端动态表格与国际化

动态表格标题解析优先使用后端 `titleI18nJson`、前端显式 i18n key 或历史文案兜底。普通列名如“账号”“用户名称”或 `Login Terminal` 不再直接调用 `t(...)`，避免 vue-i18n 对普通文本输出缺 key warning。

根语言包补充 `sys.title` 和 `approval.title`，用于模块首页、页签或菜单配置直接引用顶层模块标题。

## 导入配置编辑

字典树接口继续使用后端既有 `POST /sys/dict/tree`，前端 `getDictList` 与 `getDictTree` 均按 POST 调用，避免导入配置编辑弹窗加载字典时出现 `Request method 'GET' is not supported`。

JSON 数据源编辑区域扩大可用宽度，`JsonArrayEditor` 支持横向滚动与预览区滚动，避免选项值、选项标签和 JSON 预览被右侧操作列遮挡。

## 在线用户登录终端

在线用户页面统一使用 `terminalOptions` 解析 `B`、`C`、`THIRD_PARTY`，列表列、筛选项和顶部 tab 均显示当前语言包中的终端标签。未知终端值保留原值兜底，避免新增后端枚举时显示为空。
