---
name: Android版本管理+Favicon
overview: 实现安卓版本管理页面（后端实体/服务/控制器 + 前端页面/API/头部改造），并配置浏览器 favicon 图标。
todos:
  - id: favicon
    content: 配置浏览器 favicon 图标（修改 index.html）
    status: completed
  - id: backend-entity
    content: 后端：创建 SysAndroidVersion 实体类、VO、DTO
    status: completed
  - id: backend-mapper
    content: 后端：创建 Mapper 接口和 XML
    status: completed
  - id: backend-service
    content: 后端：创建 Service 接口和实现类
    status: completed
  - id: backend-controller
    content: 后端：创建 Controller 控制器
    status: completed
  - id: frontend-api
    content: 前端：创建 AndroidVersion API 封装
    status: in_progress
  - id: frontend-page
    content: 前端：创建安卓版本管理列表页面
    status: pending
  - id: frontend-header
    content: 前端：改造 AppHeader 刷新按钮为安卓图标+弹窗
    status: pending
  - id: frontend-route
    content: 前端：注册动态路由组件
    status: pending
  - id: frontend-deps
    content: 前端：添加 qrcode 依赖
    status: pending
  - id: sql-script
    content: 数据库：建表+菜单+表格配置 SQL 脚本
    status: pending
isProject: false
---

## 一、配置浏览器 Favicon（简单）

**修改文件**：[index.html](file:///d:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/index.html)

在 `<head>` 中添加：
```html
<link rel="icon" href="/tubiao/forgex.png" />
```

---

## 二、后端：安卓版本管理模块

### 2.1 数据库表设计

新增表 `sys_android_version`：

```sql
CREATE TABLE sys_android_version (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  version_code  INT NOT NULL COMMENT '版本号(内部递增，如 101)',
  version_name  VARCHAR(50) NOT NULL COMMENT '版本名称(展示用，如 1.0.1)',
  changelog     TEXT COMMENT '更新日志',
  file_name     VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_url      VARCHAR(500) NOT NULL COMMENT '文件访问URL',
  file_size     BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  storage_type  VARCHAR(32) DEFAULT 'LOCAL' COMMENT '存储类型',
  status        TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
  download_url  VARCHAR(500) COMMENT '下载短链接(可选)',
  tenant_id     BIGINT DEFAULT 0 COMMENT '租户ID',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by     VARCHAR(50),
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by     VARCHAR(50),
  deleted       TINYINT DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_tenant_id(tenant_id)
) COMMENT '安卓版本管理表';
```

### 2.2 后端文件清单

| 层级 | 文件路径 | 说明 |
|------|---------|------|
| Entity | `Forgex_Sys/.../domain/entity/SysAndroidVersion.java` | 实体类 |
| VO | `Forgex_Sys/.../domain/vo/SysAndroidVersionVO.java` | 视图对象 |
| DTO | `Forgex_Sys/.../domain/dto/SysAndroidVersionDTO.java` | 新增/编辑参数 |
| QueryDTO | `Forgex_Sys/.../domain/dto/SysAndroidVersionQueryDTO.java` | 分页查询参数 |
| Mapper | `Forgex_Sys/.../mapper/SysAndroidVersionMapper.java` | Mapper接口 |
| Service接口 | `Forgex_Sys/.../service/ISysAndroidVersionService.java` | 服务接口 |
| Service实现 | `Forgex_Sys/.../service/impl/SysAndroidVersionServiceImpl.java` | 服务实现 |
| Controller | `Forgex_Sys/.../controller/SysAndroidVersionController.java` | 控制器 |
| XML | `Forgex_Sys/.../resources/mapper/SysAndroidVersionMapper.xml` | MyBatis XML |

### 2.3 API 接口设计

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/sys/android-version/page` | POST | `sys:androidVersion:view` | 分页查询 |
| `/sys/android-version/upload` | POST | `sys:androidVersion:add` | 上传APK+版本信息 |
| `/sys/android-version/update` | POST | `sys:androidVersion:edit` | 编辑版本信息 |
| `/sys/android-version/delete` | POST | `sys:androidVersion:delete` | 删除 |
| `/sys/android-version/latest` | POST | `sys:androidVersion:view` | 获取最新版本(用于二维码) |

---

## 三、前端：安卓版本管理页面

### 3.1 前端文件清单

| 文件 | 说明 |
|------|------|
| `src/api/system/androidVersion.ts` | API 接口封装 |
| `src/views/system/androidVersion/index.vue` | 列表页面(含上传弹窗) |

### 3.2 页面功能

1. **FxDynamicTable 列表**：版本号、版本名称、更新日志、文件大小、上传时间、上传人、状态、下载链接
2. **上传弹窗**：填写版本号/版本名称/更新日志 + 上传 APK 文件
3. **编辑弹窗**：修改版本信息/状态
4. **下载链接列**：点击可复制下载链接

### 3.3 动态路由注册

在 [router/index.ts](file:///d:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/src/router/index.ts) 的 `stableComponentMap` 中添加：
```typescript
SystemAndroidVersion: '../views/system/androidVersion/index.vue'
```

---

## 四、头部改造：刷新按钮替换为安卓图标

### 4.1 修改文件

[AppHeader.vue](file:///d:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/src/layouts/components/AppHeader.vue)

### 4.2 改造方案

将原来的刷新按钮（`SyncOutlined` 图标）替换为：
1. Android 图标按钮（使用 `AndroidOutlined` from `@ant-design/icons-vue`）
2. 点击弹出 Popover，展示：
   - 最新版本信息（版本号、更新日志）
   - 下载链接（可复制）
   - 二维码（使用 `qrcode` npm 包生成 Data URL 展示）
3. 移除原来的 `refresh` 事件发射，改为获取最新版本信息

### 4.3 依赖变更

在 [package.json](file:///d:/mine_product/forgex/Forgex_MOM/Forgex_Fronted/package.json) 中添加：
```json
"qrcode": "^1.5.4"
```

---

## 五、数据库菜单配置

在 `sys_menu` 表中添加菜单记录：
- 菜单名称：安卓版本管理
- 路径：androidVersion
- 组件名：SystemAndroidVersion
- 图标：AndroidOutlined
- 父级：系统管理模块
- 权限标识：sys:androidVersion:view

在 `fx_table_config` 和 `fx_table_column_config` 中添加表格配置。


### 六、文件变更汇总

| 文件 | 操作 | 说明 |
|------|------|------|
| `index.html` | 修改 | 添加 favicon link |
| `package.json` | 修改 | 添加 qrcode 依赖 |
| `AppHeader.vue` | 修改 | 刷新按钮改为安卓图标+弹窗 |
| `router/index.ts` | 修改 | 注册新路由组件 |
| `api/system/androidVersion.ts` | 新建 | API 封装 |
| `views/system/androidVersion/index.vue` | 新建 | 列表页面 |
| 后端 8 个 Java 文件 + 1 个 XML | 新建 | 完整 CRUD 模块 |
| SQL 脚本 | 新建 | 建表+菜单+表格配置 |