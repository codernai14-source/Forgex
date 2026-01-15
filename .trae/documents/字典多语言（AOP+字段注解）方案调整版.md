## 你补充的 node_path 方案（用 dict_code 拼接）
同意，而且这比 pathIds 更适合做“唯一定位”。因为你已经约束：
- 顶级父 `dict_code` 一定不能重复；
- 同一个父节点下 `dict_code` 不能重复；
所以用 `dict_code` 拼接出来的路径天然唯一。

下面给出按这个约束修订后的完整方案。

## 1) 字典表结构升级（满足你图里的结构 + 唯一定位 + 多语言值）
当前 `sys_dict` 是树表但无 i18n 字段。升级为：
- `dict_code`：节点字典编号（你图里每个节点都有）
- `dict_key`：业务键（原 `dict_value` 的语义更适合做这个；用于叶子节点“取值”）
- `dict_value_i18n_json`：多语言显示值（JSON，例如 `{ "zh":"高", "en":"High" }`）
- `node_path`：**按 dict_code 自动计算的唯一路径**（字符串，用 `/` 分割）
  - 示例：`qualityLevel/envQuality/high`
- `level`：层级（root=1）
- `children_count`：直接子节点数（只统计子，不统计子子）
- （可选）`node_type`：GROUP/ITEM（便于后台校验与查询优化）

### 唯一性约束（严格落地）
- sibling 唯一（覆盖“同一父下 dict_code 不能重复”）：
  - 唯一索引：`(tenant_id, parent_id, dict_code, deleted)`
- root 唯一（覆盖“顶级父 dict_code 不重复”）：
  - 上面的 sibling 唯一已经覆盖（root 的 parent_id=0）
- `node_path` 唯一：
  - 推荐再加唯一索引：`(tenant_id, node_path, deleted)`（可做第二道保险）

## 2) node_path 自动计算规则（你要求的 dict_code 拼接）
### 2.1 新增节点
- 若 `parent_id=0`：
  - `node_path = dict_code`
  - `level = 1`
- 否则：
  - 查询父节点 `parent.node_path`、`parent.level`
  - `node_path = parent.node_path + "/" + dict_code`
  - `level = parent.level + 1`
- 同事务内维护：父节点 `children_count + 1`

### 2.2 修改 dict_code（最关键的维护点）
因为 node_path 由 dict_code 拼出来，所以改 dict_code 必须：
- 先计算新 `node_path`；
- 然后把该节点**整棵子树**的 `node_path` 前缀一起替换（批量 update）；
- 同时保持 sibling 唯一校验。

### 2.3 移动节点（换 parent）
- 重新计算新 `node_path/level`
- 子树前缀替换
- 旧父 `children_count -1`、新父 `children_count +1`

## 3) 字典接口（POST）升级：支持按 node_path 精确定位
保留兼容：
- `POST /sys/dict/items` 继续支持 `{ dictCode }`（仅用于“顶级父 dict_code 唯一”的简单字典）
新增推荐接口（用于你图这种复杂树）：
- `POST /sys/dict/itemsByPath`
  - 入参：`{ "nodePath": "qualityLevel/envQuality" }`
  - 返回：该节点的直接 children（label 来自 `dict_value_i18n_json` 按当前 lang 解析）

## 4) 字典翻译注解（字段级：你想放在 VO/实体字段上）
新增字段注解（示例名）：`@DictI18n`，核心参数：
- `nodePathField`：从对象哪个字段取 node_path（动态场景）
- `nodePathConst`：固定 node_path（静态场景）
- `valueField`：从哪个字段取 dict_key（默认当前字段）
- `targetField`：翻译后的文本写入哪个字段（例如 `statusText`）

这样可以解决你担心的歧义：
- 即使 dict_code 在树的不同分支出现重复，最终也靠 `node_path` 唯一定位。

## 5) ResponseBodyAdvice 统一翻译（你认可的实现方式 A）
在 Common 实现 `ResponseBodyAdvice`（类似 `RMessageI18nAdvice`），专门处理字典翻译。

### 5.1 必须覆盖的返回形态（按你要求全覆盖）
- `R.data` 可能是：
  - 单实体
  - List / 多种 List 混合（Map 里多 list）
  - MyBatisPlus `IPage`（records）
  - Map / 嵌套对象

### 5.2 遍历与安全
- 递归遍历：
  - `IPage` -> records
  - `Iterable` -> elements
  - `Map` -> values
  - POJO -> 反射字段
- 防止循环引用：visited（按对象引用）
- 跳过 java.*、基础类型、Enum、String

### 5.3 批量翻译（避免 N+1）
- 一次响应内先收集所有 `@DictI18n` 需求（tenantId+lang+nodePath+dictKey）
- 按 `nodePath` 分组批量取 children 映射，再一次性回填 `targetField`
- Redis 缓存 key 建议改为：`dict:{tenantId}:{lang}:{nodePath}`

## 6) 前端最小改动
- `useDict` 的缓存 key 需要加入 lang（否则切换语言不刷新）
- 对复杂字典用 `itemsByPath`，nodePath 由业务配置/表格配置/字段注解统一提供

## 7) 落地步骤（只做字典多语言闭环）
1) 升级 `sys_dict`（加 i18n_json、node_path、level、children_count、dict_key 等）+ 唯一索引
2) DictService 事务内维护 node_path/level/children_count（新增/改码/移动/删除）
3) 增加 `itemsByPath`（POST）
4) 增加 `@DictI18n` + 字典翻译 ResponseBodyAdvice（覆盖 entity/list/page/map/嵌套）
5) 前端 `useDict` 缓存按 lang 隔离

## 8) 验收标准
- 同一响应里混合实体/list/page/map 时都能翻译
- dict_code 重复但 node_path 不重复时，仍能精准翻译
- 改 dict_code 或移动节点后，子树 node_path 自动更新且不冲突

如果你认可这版“node_path 用 dict_code 拼接”的最终方案，我再退出计划模式并开始把上述改动落到仓库（含 SQL、后端服务、Advice、前端 useDict 修复）。