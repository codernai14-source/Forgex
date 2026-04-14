# Forgex 接口平台模块 - 快速部署指南

## 一、数据库部署

### 1.1 创建数据库

接口平台模块使用独立的数据库 `forgex_integration`。

**方式一：使用独立脚本（推荐）**

```bash
mysql -u root -p < doc/sql/20260414_create_database_integration.sql
```

**方式二：手动创建**

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS forgex_integration DEFAULT CHARACTER SET utf8mb4;"

# 创建表结构
mysql -u root -p forgex_integration < doc/sql/20260414_create_integration_module.sql

# 初始化菜单和表格配置
mysql -u root -p forgex_admin < doc/sql/20260414_init_integration_module_menu.sql
```

### 1.2 验证数据库

```sql
-- 1. 查看数据库
SHOW DATABASES LIKE 'forgex_integration';

-- 2. 查看数据库字符集
SELECT 
    SCHEMA_NAME AS '数据库名',
    DEFAULT_CHARACTER_SET_NAME AS '字符集',
    DEFAULT_COLLATION_NAME AS '排序规则'
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'forgex_integration';

-- 3. 查看表（应在 forgex_integration 数据库中执行）
USE forgex_integration;
SHOW TABLES;

-- 应显示以下表：
-- +------------------------------------+
-- | Tables_in_forgex_integration       |
-- +------------------------------------+
-- | fx_third_system                    |
-- | fx_third_authorization             |
-- | fx_api_config                      |
-- | fx_api_param_config                |
-- | fx_api_param_mapping               |
-- | fx_api_call_log_202604             |
-- +------------------------------------+
```

---

## 二、后端部署

### 2.1 编译项目

```bash
# 1. 进入后端父目录
cd Forgex_MOM/Forgex_Backend

# 2. 编译所有模块
mvn clean install -DskipTests

# 或者只编译 Integration 模块
cd Forgex_Integration
mvn clean package -DskipTests
```

### 2.2 配置数据库连接

编辑配置文件：`Forgex_Integration/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forgex_integration?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password  # 修改为实际密码
```

### 2.3 启动服务

**方式一：使用 Maven**

```bash
cd Forgex_MOM/Forgex_Backend/Forgex_Integration
mvn spring-boot:run
```

**方式二：使用 JAR 包**

```bash
cd Forgex_MOM/Forgex_Backend/Forgex_Integration
mvn clean package -DskipTests
java -jar target/Forgex_Integration-1.0.0.jar
```

**方式三：在 IDEA 中启动**

1. 找到 `ForgexIntegrationApplication.java`
2. 右键 -> Run 'ForgexIntegrationApplication'

### 2.4 验证服务启动

```bash
# 检查服务是否启动
curl http://localhost:8084/actuator/health

# 查看 Swagger 文档
浏览器访问：http://localhost:8084/swagger-ui.html
```

---

## 三、前端部署

### 3.1 安装依赖

```bash
cd Forgex_MOM/Forgex_Fronted
npm install
```

### 3.2 配置 API 地址

编辑配置文件：`Forgex_Fronted/.env.development`

```env
VITE_APP_BASE_API=http://localhost:8084
```

### 3.3 启动前端

```bash
# 开发环境
npm run serve

# 生产环境构建
npm run build
```

### 3.4 验证前端

浏览器访问：`http://localhost:5173`（开发环境）

登录系统后，在菜单中找到：
- **系统管理** -> **接口平台**
  - 第三方系统管理
  - 接口配置管理
  - 调用记录查询

---

## 四、快速测试

### 4.1 创建第一个第三方系统

**使用 Swagger 测试**：

1. 访问：`http://localhost:8084/swagger-ui.html`
2. 找到 `第三方系统管理` -> `createThirdSystem`
3. 填写请求体：
```json
{
  "systemCode": "ERP_SYSTEM",
  "systemName": "ERP 系统",
  "ipAddress": "192.168.1.100",
  "contactInfo": "张三，13800138000",
  "remark": "测试用 ERP 系统",
  "status": 1
}
```
4. 点击 Execute 执行

**使用前端界面**：

1. 登录系统
2. 进入 **接口平台** -> **第三方系统管理**
3. 点击 **新增系统** 按钮
4. 填写表单并提交

### 4.2 创建授权

**生成 Token**：

```bash
curl -X POST http://localhost:8084/api/integration/third-authorization/generate-token/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_TOKEN"
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "tokenValue": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "tokenExpireTime": "2026-04-15 12:00:00"
  }
}
```

### 4.3 测试统一对外接口

```bash
curl -X POST http://localhost:8084/api/integration/invoke?processorBean=testProcessor \
  -H "Content-Type: application/json" \
  -H "Authorization: a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -d '{"name": "test", "value": "123"}'
```

---

## 五、常见问题

### Q1: 数据库创建失败？

**A**: 检查 MySQL 用户权限，确保有 CREATE DATABASE 权限。

```sql
-- 查看用户权限
SHOW GRANTS FOR 'root'@'localhost';

-- 授予权限（如果需要）
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

### Q2: 服务启动失败？

**A**: 检查以下几点：
1. 数据库连接是否正确
2. 端口 8084 是否被占用
3. 查看日志：`Forgex_Integration/logs/application.log`

```bash
# 检查端口占用
netstat -ano | findstr :8084

# 查看日志
tail -f Forgex_Integration/logs/application.log
```

### Q3: 前端无法访问后端？

**A**: 检查 CORS 配置和 API 地址配置。

```yaml
# application.yml 中添加 CORS 配置
server:
  servlet:
    cors:
      allowed-origins: "*"
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
```

### Q4: 菜单不显示？

**A**: 确保已执行菜单初始化脚本，并且使用 admin 账号登录。

```sql
-- 查看菜单是否创建
SELECT * FROM forgex_admin.sys_menu 
WHERE module_id = (SELECT id FROM forgex_admin.sys_module WHERE code = 'integration');
```

---

## 六、下一步

1. ✅ 数据库创建完成
2. ✅ 后端服务启动完成
3. ✅ 前端服务启动完成
4. ⏳ 配置第三方系统
5. ⏳ 配置接口和参数映射
6. ⏳ 测试统一对外接口

---

**文档版本**: 1.0  
**更新日期**: 2026-04-14  
**技术支持**: ForGexTeam
