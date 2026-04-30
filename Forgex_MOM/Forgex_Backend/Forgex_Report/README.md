# Forgex_Report 报表服务

## 模块概述

Forgex_Report 是独立的报表微服务模块，集成 UReport2 和 JimuReport 双报表引擎，提供统一的报表设计、管理、预览服务。

## 技术栈

- **Spring Boot 3.5.6**
- **MyBatis-Plus 3.5.14**
- **UReport2 2.2.10** - 轻量级报表引擎
- **JimuReport 1.9.0** - 积木报表引擎
- **Sa-Token** - 权限认证

## 模块结构

```
Forgex_Report/
├── src/main/java/com/forgex/report/
│   ├── ReportApplication.java          # 启动类
│   ├── config/                         # 配置类
│   │   ├── ReportConfig.java           # 报表配置
│   │   ├── UreportConfig.java          # UReport2 配置
│   │   └── JimuConfig.java             # JimuReport 配置
│   ├── controller/                     # 控制器层
│   │   ├── ReportTemplateController.java
│   │   ├── ReportCategoryController.java
│   │   └── ReportDatasourceController.java
│   ├── service/                        # 服务层
│   │   ├── IReportTemplateService.java
│   │   ├── IReportCategoryService.java
│   │   ├── IReportDatasourceService.java
│   │   └── impl/
│   │       ├── ReportTemplateServiceImpl.java
│   │       ├── ReportCategoryServiceImpl.java
│   │       └── ReportDatasourceServiceImpl.java
│   ├── mapper/                         # 数据访问层
│   │   ├── ReportTemplateMapper.java
│   │   ├── ReportCategoryMapper.java
│   │   └── ReportDatasourceMapper.java
│   ├── domain/                         # 领域对象
│   │   ├── entity/                     # 实体类
│   │   │   ├── ReportTemplate.java
│   │   │   ├── ReportCategory.java
│   │   │   └── ReportDatasource.java
│   │   ├── dto/                        # DTO
│   │   │   ├── ReportTemplateDTO.java
│   │   │   ├── ReportCategoryDTO.java
│   │   │   └── ReportDatasourceDTO.java
│   │   ├── param/                      # 参数类
│   │   │   ├── ReportTemplateParam.java
│   │   │   ├── ReportCategoryParam.java
│   │   │   └── ReportDatasourceParam.java
│   │   └── enums/                      # 枚举
│   │       └── ReportEngineType.java   # 报表引擎类型
│   └── config/                         # 配置类
└── src/main/resources/
    └── application.yml                 # 唯一启动配置，环境差异通过环境变量和 Nacos namespace 控制
```

## 启动说明

### 1. 数据库初始化

执行 SQL 脚本创建表结构：
- `fx_report_template` - 报表模板表
- `fx_report_category` - 报表分类表
- `fx_report_datasource` - 报表数据源表

### 2. 配置数据源

报表服务通过 Nacos 加载数据源配置，默认读取 `datasource-forgex-dev.yml`。如需切换环境，在启动参数或环境变量中覆盖：

```bash
FORGEX_DATASOURCE_CONFIG=datasource-forgex-prod.yml
FORGEX_NACOS_NAMESPACE=prod
```

### 3. 启动服务

在 Forgex_Backend 父工程下运行：

```bash
cd Forgex_Report
mvn spring-boot:run
```

或直接运行 `ReportApplication.java` 主类。

### 4. 访问服务

服务启动后，默认端口为 9006，可通过 `FORGEX_REPORT_PORT` 覆盖：

- **Swagger API 文档**: http://localhost:9006/swagger-ui.html
- **UReport2 设计器**: http://localhost:9006/ureport/designer
- **JimuReport 设计器**: http://localhost:9006/jmreport

## API 接口

### 报表模板管理

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/report/template/page` | GET | report:template:view | 分页查询报表模板 |
| `/api/report/template/{id}` | GET | report:template:view | 根据 ID 获取模板 |
| `/api/report/template/by-code/{code}` | GET | report:template:view | 根据编码获取模板 |
| `/api/report/template/save` | POST | report:template:add/edit | 保存模板 |
| `/api/report/template/update-content` | POST | report:template:design | 更新模板内容 |
| `/api/report/template/export/{id}` | POST | report:template:export | 导出模板文件 |
| `/api/report/template/import` | POST | report:template:import | 导入模板文件 |
| `/api/report/template/batch` | DELETE | report:template:delete | 批量删除模板 |

### 报表分类管理

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/report/category/page` | GET | report:category:view | 分页查询分类 |
| `/api/report/category/list` | GET | report:category:view | 查询所有分类 |
| `/api/report/category/{id}` | GET | report:category:view | 根据 ID 获取分类 |
| `/api/report/category/save` | POST | report:category:add/edit | 保存分类 |
| `/api/report/category/batch` | DELETE | report:category:delete | 批量删除分类 |

### 报表数据源管理

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/report/datasource/page` | GET | report:datasource:view | 分页查询数据源 |
| `/api/report/datasource/list` | GET | report:datasource:view | 查询启用的数据源 |
| `/api/report/datasource/{id}` | GET | report:datasource:view | 根据 ID 获取数据源 |
| `/api/report/datasource/by-code/{code}` | GET | report:datasource:view | 根据编码获取数据源 |
| `/api/report/datasource/save` | POST | report:datasource:add/edit | 保存数据源 |
| `/api/report/datasource/test` | POST | report:datasource:test | 测试数据源连接 |
| `/api/report/datasource/batch` | DELETE | report:datasource:delete | 批量删除数据源 |

## 报表引擎对比

| 特性 | UReport2 | JimuReport |
|------|----------|------------|
| 定位 | 轻量级报表引擎 | 低代码报表平台 |
| 适用场景 | 复杂中国式报表、票据打印 | 数据可视化大屏、BI 报表 |
| 设计器 | Web 在线设计器 | Web 在线设计器 |
| 导出格式 | PDF、Excel、Word | PDF、Excel、Word、图片 |
| 学习成本 | 低 | 中 |
| 推荐指数 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

## 开发规范

- 包名：`com.forgex.report`
- 类命名：遵循项目规范（Entity、DTO、Param、VO 等）
- 注释：完整的 Javadoc 注释
- 权限：使用 `@SaCheckPermission` 注解
- 日志：使用 `@OperationLog` 注解记录操作日志

## 注意事项

1. **数据库隔离**：Forgex_Report 使用独立的数据库 `forgex_report`
2. **租户隔离**：报表模块默认忽略租户隔离（公共报表）
3. **文件存储**：模板文件存储在 `templates/report` 目录
4. **跨域配置**：开发环境允许跨域，生产环境需关闭

## 常见问题

### Q1: UReport2 设计器无法打开？
A: 检查 UReport2 Starter 是否正确引入，确认 `/ureport/**` 路径未被拦截。

### Q2: JimuReport 保存失败？
A: 检查数据库表 `jimu_report` 是否创建，确认数据库连接配置正确。

### Q3: 报表预览乱码？
A: 检查数据库字符集配置，确保使用 UTF-8 编码。

## 版本历史

- **v1.0.0** (2026-04-09): 初始版本，集成 UReport2 和 JimuReport 双引擎

## 作者

- ForGexTeam

## 许可证

MIT License
