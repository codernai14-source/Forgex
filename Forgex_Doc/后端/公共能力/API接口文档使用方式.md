# API 接口文档使用方式

> 版本：**V0.8.5**

Forgex 后端使用 springdoc-openapi / Swagger 注解沉淀接口说明。新增或调整 Controller、DTO、Param、VO 时，应同步维护 OpenAPI 注解，保证前后端联调时能通过接口文档理解请求和响应结构。

## 一、Controller 注解

```java
@Tag(name = "供应商主数据", description = "供应商主数据管理接口")
@RestController
@RequestMapping("/basic/supplier")
public class SupplierController {

    @Operation(summary = "分页查询供应商")
    @PostMapping("/page")
    public R<PageResult<SupplierVO>> page(@RequestBody SupplierPageParam param) {
        ...
    }
}
```

## 二、参数与响应注解

DTO、Param、VO 字段使用 `@Schema` 描述：

```java
@Schema(description = "供应商名称")
private String supplierName;
```

枚举、状态字段、JSON 字段应在 description 中说明取值含义和结构，不只写字段中文名。

## 三、维护要求

- `@Tag` 按业务模块或资源命名，避免同一模块出现多个相近名称。
- `@Operation.summary` 使用动词 + 资源，例如“分页查询物料列表”“新增供应商”。
- 请求参数对象和响应对象字段都补 `@Schema`。
- 废弃接口应在描述中说明替代接口，不直接删除文档入口。
- 接口权限、租户隔离、分页、导入导出等特殊行为在 description 中补充。

## 四、联调建议

- 前端联调前先确认接口文档中的路径、方法、请求体、响应结构。
- 如果接口文档与实际返回不一致，以代码为准并立即修正文档注解。
- 对于统一返回 `R<T>`，业务字段说明应写在内部泛型对象上。
- 对文件上传、导出、流式响应等非 JSON 接口，单独说明 content type 和返回方式。

