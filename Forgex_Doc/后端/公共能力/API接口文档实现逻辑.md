# API 接口文档实现逻辑

> 版本：**V0.8.5**

后端根 `pom.xml` 和业务模块中已引入 Swagger annotations 与 springdoc-openapi UI 相关依赖，Controller 与领域对象通过注解生成 OpenAPI 描述。

## 一、依赖来源

项目使用以下能力：

- `io.swagger.core.v3:swagger-annotations`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `io.swagger.v3.oas.annotations.tags.Tag`
- `io.swagger.v3.oas.annotations.Operation`
- `io.swagger.v3.oas.annotations.media.Schema`

具体模块可按需继承根依赖或在模块 `pom.xml` 中显式声明。

## 二、生成链路

1. Spring Boot 启动时扫描 Controller。
2. springdoc 读取 `@RestController`、请求映射和 OpenAPI 注解。
3. `@Tag` 形成接口分组。
4. `@Operation` 形成接口说明。
5. `@Schema` 形成请求和响应模型说明。
6. UI 或 OpenAPI JSON 供前端和测试人员联调使用。

## 三、注解覆盖边界

- Controller 方法必须有清晰 summary。
- Param/DTO/VO 字段必须说明业务含义。
- 嵌套对象和列表字段需要说明元素结构。
- 分页接口应说明分页参数和排序口径。
- 工作流回调、内部调用、第三方集成接口应说明调用方和鉴权方式。

## 四、与统一返回的关系

项目 Controller 通常返回统一响应 `R<T>`。OpenAPI 注解应重点描述 `T` 的业务结构，同时在接口说明中明确异常和失败提示走统一返回与国际化链路。

## 五、质量检查

新增接口自检时至少确认：

- 文档中能看到接口分组。
- 接口路径和 HTTP 方法正确。
- 请求参数字段齐全。
- 响应对象字段可读。
- 权限、租户、文件、分页等特殊行为没有遗漏。

