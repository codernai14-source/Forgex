# Forgex 接口平台模块开发文档

## 一、模块概述

Forgex_Integration 是 Forgex 微服务架构下的接口平台模块，提供统一的对外接口管理能力。

### 1.1 核心功能

- **第三方系统管理**：管理第三方系统信息，包括系统编码、名称、IP 地址等
- **授权管理**：支持白名单和 Token 两种授权方式
- **接口配置**：配置接口的路径、处理器、调用方式等
- **参数映射**：配置双方系统的参数映射关系，支持 JSON 格式转换
- **调用记录**：记录接口调用详情，支持按月分表存储

### 1.2 技术栈

- **后端**：Spring Boot 3.5.6 + MyBatis-Plus + MPJ
- **前端**：Vue3 + TypeScript + Ant Design Vue
- **数据库**：MySQL 8.0
- **服务端口**：8084

---

## 二、已完成功能

### 2.1 数据库设计 ✅

已创建 6 张核心表：

1. **fx_third_system** - 第三方系统信息表
2. **fx_third_authorization** - 第三方授权表
3. **fx_api_config** - 接口配置主表
4. **fx_api_param_config** - 接口参数配置表
5. **fx_api_param_mapping** - 接口参数映射表
6. **fx_api_call_log_YYYYMM** - 接口调用记录表（按月分表）

**数据库**：`forgex_integration`（独立数据库）

**SQL 脚本**：
- `doc/sql/20260414_create_database_integration.sql` - 创建数据库
- `doc/sql/20260414_create_integration_module.sql` - 创建表结构
- `doc/sql/20260414_init_integration_module_menu.sql` - 菜单和表格配置

### 2.2 后端基础架构 ✅

**项目结构**：
```
Forgex_Integration/
├── src/main/java/com/forgex/integration/
│   ├── ForgexIntegrationApplication.java  # 启动类
│   ├── controller/                         # 控制器层
│   ├── service/                            # 服务层
│   ├── mapper/                             # 数据访问层
│   └── domain/
│       ├── entity/                         # 实体类（6 个）
│       ├── param/                          # 参数类（6 个）
│       └── dto/                            # DTO（3 个）
└── pom.xml
```

**已实现的实体类**：
- ThirdSystem - 第三方系统
- ThirdAuthorization - 第三方授权
- ApiConfig - 接口配置
- ApiParamConfig - 接口参数配置
- ApiParamMapping - 接口参数映射
- ApiCallLog - 接口调用记录

**已实现的 Mapper**：
- ThirdSystemMapper
- ThirdAuthorizationMapper
- ApiConfigMapper
- ApiParamConfigMapper
- ApiParamMappingMapper
- ApiCallLogMapper

**已实现的服务**：
- ThirdSystemService（完整实现）

**已实现的 Controller**：
- ThirdSystemController（完整实现）

### 2.3 前端基础架构 ✅

**已创建文件**：
- `src/api/system/integration.ts` - API 封装
- `src/views/system/integration/thirdSystem/index.vue` - 第三方系统列表页
- `src/views/system/integration/thirdSystem/types.ts` - 类型定义
- `src/views/system/integration/thirdSystem/components/ThirdSystemFormDialog.vue` - 表单弹窗
- `src/views/system/integration/thirdSystem/components/ThirdSystemAuthDialog.vue` - 授权弹窗

### 2.4 菜单和表格配置 ✅

SQL 脚本位置：`doc/sql/20260414_init_integration_module_menu.sql`

**已配置菜单**：
- 接口平台（一级菜单）
  - 第三方系统管理（二级菜单）
  - 接口配置管理（二级菜单）
  - 调用记录查询（二级菜单）

**已配置表格**：
- ThirdSystemTable
- ApiConfigTable

---

## 三、待完成功能

### 3.1 后端待开发

#### 1. 第三方授权服务（优先级：高）

**文件**：
- `service/IThirdAuthorizationService.java`
- `service/impl/ThirdAuthorizationServiceImpl.java`
- `controller/ThirdAuthorizationController.java`

**功能**：
- 授权的增删改查
- Token 生成和校验
- 白名单 IP 校验

#### 2. 接口配置服务（优先级：高）

**文件**：
- `service/IApiConfigService.java`
- `service/impl/ApiConfigServiceImpl.java`
- `controller/ApiConfigController.java`

**功能**：
- 接口配置的增删改查
- 启用/停用切换
- 接口编码唯一性校验

#### 3. 参数配置服务（优先级：中）

**文件**：
- `service/IApiParamConfigService.java`
- `service/impl/ApiParamConfigServiceImpl.java`
- `controller/ApiParamConfigController.java`

**功能**：
- 参数配置的增删改查
- 树形结构查询
- JSON 文件导入解析

#### 4. 参数映射服务（优先级：中）

**文件**：
- `service/IApiParamMappingService.java`
- `service/impl/ApiParamMappingServiceImpl.java`
- `controller/ApiParamMappingController.java`

**功能**：
- 参数映射的增删改查
- 批量保存映射关系

#### 5. 调用记录服务（优先级：低）

**文件**：
- `service/IApiCallLogService.java`
- `service/impl/ApiCallLogServiceImpl.java`
- `controller/ApiCallLogController.java`

**功能**：
- 调用记录的保存（异步）
- 分表查询
- 统计功能

#### 6. 核心路由服务（优先级：最高）

**文件**：
- `service/IApiRouterService.java`
- `service/impl/ApiRouterServiceImpl.java`
- `controller/IntegrationController.java`

**功能**：
- 统一对外接口入口
- 参数转换引擎
- 处理器路由
- 授权校验

### 3.2 前端待开发

#### 1. 接口配置管理页面

**文件**：
- `src/views/system/integration/apiConfig/index.vue`
- `src/views/system/integration/apiConfig/components/ApiConfigFormDialog.vue`

#### 2. 参数配置弹窗（核心）

**文件**：
- `src/components/system/integration/ApiParamConfigDialog.vue`
- `src/components/system/integration/JsonTreeViewer.vue`

**功能**：
- 树形表格展示 JSON 结构
- JSON 文件导入
- 字段编辑

#### 3. 参数映射配置弹窗

**文件**：
- `src/components/system/integration/ApiParamMappingDialog.vue`

#### 4. 调用记录查询页面

**文件**：
- `src/views/system/integration/apiCallLog/index.vue`

---

## 四、部署指南

### 4.1 数据库初始化

**重要**：接口平台模块使用独立的数据库 `forgex_integration`。

```bash
# 步骤 1：创建数据库
# 方法 1：使用命令行
mysql -u root -p < doc/sql/20260414_create_database_integration.sql

# 方法 2：使用 MySQL 客户端
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS forgex_integration DEFAULT CHARACTER SET utf8mb4;"

# 步骤 2：创建表结构
mysql -u root -p forgex_integration < doc/sql/20260414_create_integration_module.sql

# 步骤 3：初始化菜单和表格配置
mysql -u root -p forgex_admin < doc/sql/20260414_init_integration_module_menu.sql
```

**验证数据库**：
```sql
-- 查看数据库
SHOW DATABASES LIKE 'forgex_integration';

-- 查看数据库字符集
SELECT 
    SCHEMA_NAME AS '数据库名',
    DEFAULT_CHARACTER_SET_NAME AS '字符集',
    DEFAULT_COLLATION_NAME AS '排序规则'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'forgex_integration';

-- 查看表
USE forgex_integration;
SHOW TABLES;
```

### 4.2 后端部署

```bash
# 1. 更新父 POM（已完成）
# 父 POM 已添加 Forgex_Integration 模块

# 2. 编译项目
cd Forgex_MOM/Forgex_Backend
mvn clean install -DskipTests

# 3. 启动服务
cd Forgex_Integration
mvn spring-boot:run
```

**配置文件**：`src/main/resources/application.yml`

需要修改的配置：
- 数据库连接信息
- Nacos 地址
- Redis 配置

### 4.3 前端部署

```bash
# 1. 安装依赖
cd Forgex_MOM/Forgex_Fronted
npm install

# 2. 启动开发服务器
npm run serve

# 3. 构建生产版本
npm run build
```

---

## 五、核心实现指南

### 5.1 参数转换引擎实现

参数转换是接口平台的核心功能，负责将外部系统的 JSON 格式转换为内部系统格式。

**实现思路**：

```java
@Service
public class ApiParamMappingServiceImpl implements IApiParamMappingService {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 根据映射配置转换参数
     */
    @Override
    public Map<String, Object> transformParams(
        Object sourceData, 
        Long apiConfigId, 
        String direction
    ) {
        // 1. 查询该接口的所有映射配置
        List<ApiParamMapping> mappings = listByApiConfigIdAndDirection(apiConfigId, direction);
        
        // 2. 将源数据转换为 JSON 树
        JsonNode sourceTree = objectMapper.valueToTree(sourceData);
        
        // 3. 创建目标 JSON 树
        ObjectNode targetTree = objectMapper.createObjectNode();
        
        // 4. 遍历映射配置，逐个字段转换
        for (ApiParamMapping mapping : mappings) {
            // 从源树获取值
            JsonNode sourceValue = getNodeByPath(sourceTree, mapping.getSourceFieldPath());
            
            // 应用转换规则
            JsonNode transformedValue = applyTransformRule(
                sourceValue, 
                mapping.getTransformRule()
            );
            
            // 设置到目标树
            setNodeByPath(targetTree, mapping.getTargetFieldPath(), transformedValue);
        }
        
        // 5. 转换回 Map
        return objectMapper.convertValue(targetTree, Map.class);
    }
    
    /**
     * 根据路径获取 JSON 节点
     */
    private JsonNode getNodeByPath(JsonNode tree, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = tree;
        
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            current = current.get(part);
        }
        
        return current;
    }
    
    /**
     * 设置 JSON 节点到指定路径
     */
    private void setNodeByPath(ObjectNode tree, String path, JsonNode value) {
        String[] parts = path.split("\\.");
        ObjectNode current = tree;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part)) {
                current.putObject(part);
            }
            current = (ObjectNode) current.get(part);
        }
        
        String lastPart = parts[parts.length - 1];
        current.set(lastPart, value);
    }
    
    /**
     * 应用转换规则
     */
    private JsonNode applyTransformRule(JsonNode sourceValue, String rule) {
        if (rule == null || rule.isEmpty()) {
            return sourceValue;
        }
        
        // TODO: 解析并执行转换规则
        // 示例规则：
        // - toUpperCase()
        // - formatDate('YYYY-MM-DD')
        // - defaultIfEmpty('N/A')
        
        return sourceValue;
    }
}
```

### 5.2 统一对外接口实现

```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    
    @Autowired
    private IApiRouterService apiRouterService;
    
    /**
     * 统一对外接口
     * 外部系统调用此接口，传入 processor_bean 和参数
     */
    @PostMapping("/invoke")
    public R<Object> invoke(
        @RequestParam String processorBean,
        @RequestBody Map<String, Object> requestData,
        @RequestHeader(required = false) String Authorization
    ) {
        // 1. 校验授权（Token 或 IP 白名单）
        if (!apiRouterService.checkAuthorization(Authorization, getRequestIp())) {
            return R.error("未授权访问");
        }
        
        // 2. 路由请求
        return apiRouterService.routeRequest(processorBean, requestData);
    }
    
    /**
     * 获取请求 IP
     */
    private String getRequestIp() {
        // 从请求头获取真实 IP
        return "127.0.0.1";
    }
}
```

### 5.3 处理器接口定义

为了让各模块的服务能够被接口平台调用，需要定义统一的处理器接口：

```java
/**
 * API 处理器接口
 * 各模块的服务实现此接口，即可被接口平台路由调用
 */
public interface ApiProcessor<T, R> {
    
    /**
     * 处理请求
     * @param request 请求参数（已转换为内部格式）
     * @return 响应数据
     */
    R process(T request);
}

/**
 * 示例：用户管理模块的处理器实现
 */
@Service("userApiProcessor")
public class UserApiProcessor implements ApiProcessor<UserRequest, UserResponse> {
    
    @Autowired
    private ISysUserService userService;
    
    @Override
    public UserResponse process(UserRequest request) {
        // 调用业务服务
        SysUser user = userService.getByUsername(request.getUsername());
        
        // 转换为响应对象
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        // ...
        
        return response;
    }
}
```

---

## 六、开发建议

### 6.1 开发顺序

1. **第一步**：完成第三方授权服务（优先级高）
   - 实现授权的增删改查
   - 实现 Token 生成和校验逻辑

2. **第二步**：完成接口配置服务（优先级高）
   - 实现接口配置的增删改查
   - 实现启用/停用功能

3. **第三步**：完成核心路由服务（优先级最高）
   - 实现统一对外接口
   - 实现参数转换引擎
   - 实现处理器路由

4. **第四步**：完善前端页面
   - 接口配置管理页面
   - 参数配置弹窗
   - 参数映射弹窗

### 6.2 测试建议

1. **单元测试**：
   - 参数转换引擎测试
   - 授权校验测试
   - 处理器路由测试

2. **集成测试**：
   - 模拟第三方系统调用
   - 测试参数映射是否正确
   - 测试调用记录是否保存

3. **性能测试**：
   - 并发调用测试
   - 参数转换性能测试
   - 调用记录分表查询性能测试

---

## 七、常见问题

### Q1: 如何添加新的处理器？

**A**: 在业务模块中创建类，实现 `ApiProcessor` 接口，并添加`@Service("xxxProcessor")` 注解。

### Q2: 如何配置参数映射？

**A**: 通过前端的参数配置弹窗，导入双方的 JSON 示例，然后配置字段映射关系。

### Q3: 调用记录分表如何自动创建？

**A**: 可以创建定时任务，每月初自动创建当月的调用记录表。

### Q4: Token 如何生成？

**A**: 使用 UUID 或雪花算法生成，保存到数据库，并设置过期时间。

---

## 八、下一步计划

1. ✅ 完成数据库表设计
2. ✅ 搭建后端项目结构
3. ✅ 实现第三方系统管理
4. ⏳ 实现第三方授权管理
5. ⏳ 实现接口配置管理
6. ⏳ 实现参数配置管理
7. ⏳ 实现参数映射管理
8. ⏳ 实现核心路由服务
9. ⏳ 实现统一对外接口
10. ⏳ 完善前端页面

---

**文档版本**: 1.0  
**创建日期**: 2026-04-14  
**作者**: ForGexTeam  
**状态**: 开发中
