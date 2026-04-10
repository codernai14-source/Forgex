# Forgex 发布说明

## 版本信息
- **发布日期**: 2026-04-07
- **分支**: develop
- **提交范围**: b8a65f0..f025f1e

---

## 新增功能

### 1. 系统管理仪表盘优化

#### 后端改进
- 新增 JVM 内存池监控接口 `/sys/dashboard/moduleMemoryUsage`，支持查看各内存分区占用（MB）
- 服务器信息增加 `jvmHeapMaxGb` 字段，区分物理内存与 JVM 堆内存
- CPU 使用率改为优先读取整机占用（与 Windows 任务管理器一致），避免仅显示进程占用

#### 前端展示优化
- **CPU 仪表盘**: 优化布局，避免百分比数字遮盖指针
- **内存饼图**: 
  - 放大环形图表（容器高度 300px → 380px，环形半径 32%/56% → 38%/66%）
  - 图例改为底部横向布局
  - 扇区内直接显示占用百分比（{d}%）
- **内存池柱状图**: 改为显示 JVM 各内存池占用（Metaspace、G1 Eden Space 等）
- **服务器信息**: 增加 JVM 堆上限展示，去掉重复的物理内存行

#### 统计数据准确性
- 用户/角色/菜单数量统计增加租户过滤，仅显示当前租户数据
- 逻辑删除字段统一改为 `false` 过滤

### 2. 国际化文件补充

#### 新增多语言文件（48 个）
- **语言**: zh-CN, zh-TW, ja-JP, ko-KR, en-US
- **模块**: layout, message, operationLog, personalHomepage, profile
- **系统模块**: config, department, module, position, tableConfig, tenant, user, role 等

#### Prompt 枚举扩展
- **Common 模块**: 新增 CodeGen, Dashboard, Excel, File, Message, Profile 等场景的提示枚举
- **Auth 模块**: 扩展登录认证、Token 管理、权限校验、第三方登录相关提示
- **Workflow 模块**: 新增工作流相关提示枚举

### 3. 前端交互优化
- 移除 API 响应成功时的自动消息提示，避免不必要的弹窗干扰
- 多个页面组件细节优化（profile, loginLog, operationLog, role, tenant 等）

---

## 技术改进

### 后端
- `DashboardServiceImpl.resolveCpuUsagePercent()`: CPU 计算逻辑优化，优先使用 `getSystemCpuLoad()`（整机）
- `I18nMessageServiceImpl`: 国际化消息服务优化
- `application.yml`: 新增仪表盘配置项（地图标注、产品版本号）

### 前端
- `http.ts`: 简化响应处理逻辑
- `FxDynamicTable.vue`: 动态表格组件优化
- `MainLayout.vue`: 主布局组件优化
- `I18nInput.vue`: 国际化输入组件优化

---

## 文件统计
- **总变更**: 107 个文件
- **新增代码**: 6561 行
- **删除代码**: 436 行

---

## 注意事项

1. **仪表盘数据刷新**: 重启 Sys 服务后生效，前端无需刷新缓存
2. **国际化**: 新增的翻译键需要配合数据库 `fx_i18n_message` 表数据使用
3. **CPU 显示**: 若任务管理器显示 9% 而页面显示较低，属正常现象（采样时间窗口差异）

---

## 测试建议

1. 访问系统管理仪表盘，验证：
   - CPU 仪表盘数值与任务管理器趋势一致
   - 内存饼图显示扇区占比百分比
   - JVM 内存池柱状图显示各分区数据
   - 服务器信息显示物理内存与 JVM 堆上限
   - 统计数据仅显示当前租户

2. 切换语言（zh-CN/en-US/ja-JP/ko-KR/zh-TW），验证国际化文案显示正确

---

**发布负责人**: Forgex Team  
**审核人**: LiDaoMoM
