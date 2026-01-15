# Design Document

## Overview

本设计文档描述了为Forgex后端Java项目添加规范Javadoc注释的实现方案。系统将采用模块化的方式，按照不同的Java文件类型（Controller、Service、Mapper、Entity等）应用相应的注释模板和规范，确保注释的一致性和完整性。

设计遵循以下原则：
- 注释应该清晰、准确、有价值
- 使用标准的Javadoc标签和HTML标签
- 方法内部注释位于代码行上方，而不是行尾
- 注释内容使用中文，专业且易懂
- 不同层次的类使用针对性的注释模板

## Architecture

系统采用分层处理架构：

```
注释处理系统
├── 文件扫描层 (File Scanner)
│   └── 扫描指定模块的所有Java文件
├── 文件分类层 (File Classifier)
│   └── 根据文件路径和类名识别文件类型
├── 注释生成层 (Comment Generator)
│   ├── 版权信息生成器
│   ├── 类注释生成器
│   ├── 方法注释生成器
│   ├── 字段注释生成器
│   └── 行注释生成器
├── 注释应用层 (Comment Applicator)
│   └── 将生成的注释插入到Java文件中
└── 验证层 (Validator)
    └── 检查注释的完整性和一致性
```

## Components and Interfaces

### 1. 文件扫描组件 (File Scanner)

**职责**: 扫描指定模块的所有Java文件

**接口**:
```java
List<File> scanJavaFiles(String modulePath)
```

**实现逻辑**:
- 递归遍历模块目录
- 过滤出所有.java文件
- 排除target、logs等构建目录
- 返回文件列表

### 2. 文件分类组件 (File Classifier)

**职责**: 识别Java文件的类型（Controller、Service、Mapper等）

**接口**:
```java
FileType classifyFile(File javaFile)
```

**分类规则**:
- 文件路径包含`/controller/` → Controller类
- 文件路径包含`/service/impl/` → ServiceImpl类
- 文件路径包含`/service/` 且不包含`/impl/` → Service接口
- 文件路径包含`/mapper/` → Mapper接口
- 文件路径包含`/domain/entity/` → Entity类
- 文件路径包含`/domain/dto/` → DTO类
- 文件路径包含`/domain/vo/` → VO类
- 文件路径包含`/domain/param/` → Param类
- 文件路径包含`/validator/` → Validator类
- 文件路径包含`/config/` → Config类
- 其他 → 通用类

### 3. 注释生成组件 (Comment Generator)

#### 3.1 版权信息生成器

**模板**:
```java
/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
```

#### 3.2 类注释生成器

**Controller类模板**:
```java
/**
 * {类名}控制器
 * <p>
 * 负责处理{业务领域}相关的HTTP请求，包括：
 * <ul>
 *   <li>接收和验证请求参数</li>
 *   <li>调用Service层业务逻辑</li>
 *   <li>返回统一格式的响应结果</li>
 * </ul>
 * <p>
 * 注意：Controller层不包含业务逻辑，仅负责请求转发和响应封装。
 * 
 * @author coder_nai@163.com
 * @date {当前日期}
 * @see {相关Service类}
 */
```

**Service实现类模板**:
```java
/**
 * {类名}Service实现类
 * <p>
 * 实现{业务领域}的核心业务逻辑，包括：
 * <ul>
 *   <li>{主要功能1}</li>
 *   <li>{主要功能2}</li>
 *   <li>{主要功能3}</li>
 * </ul>
 * <p>
 * 业务规则：
 * <ul>
 *   <li>{业务规则1}</li>
 *   <li>{业务规则2}</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date {当前日期}
 * @see {相关Mapper类}
 * @see {相关Entity类}
 */
```

**Mapper接口模板**:
```java
/**
 * {类名}Mapper接口
 * <p>
 * 负责{实体名}的数据访问操作，继承MyBatis-Plus的BaseMapper接口。
 * <p>
 * 提供的数据访问功能：
 * <ul>
 *   <li>基础CRUD操作（继承自BaseMapper）</li>
 *   <li>自定义查询方法</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date {当前日期}
 * @see {相关Entity类}
 */
```

**Entity类模板**:
```java
/**
 * {类名}实体类
 * <p>
 * 对应数据库表：{@code {表名}}
 * <p>
 * 字段说明：
 * <ul>
 *   <li>{@code id} - 主键ID</li>
 *   <li>{@code tenantId} - 租户ID</li>
 *   <li>{其他重要字段}</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date {当前日期}
 * @see BaseEntity
 */
```

**DTO类模板**:
```java
/**
 * {类名}数据传输对象
 * <p>
 * 用于{业务场景}的数据传输，包含以下信息：
 * <ul>
 *   <li>{字段1说明}</li>
 *   <li>{字段2说明}</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date {当前日期}
 */
```

#### 3.3 方法注释生成器

**Controller方法模板**:
```java
/**
 * {方法功能描述}
 * <p>
 * 请求路径：{@code POST /api/xxx}
 * <p>
 * 处理流程：
 * <ol>
 *   <li>接收并验证请求参数</li>
 *   <li>调用Service层方法处理业务逻辑</li>
 *   <li>返回统一格式的响应结果</li>
 * </ol>
 * 
 * @param {参数名} {参数说明}
 * @return 统一响应对象，包含{返回内容说明}
 * @see {相关Service方法}
 */
```

**Service方法模板**:
```java
/**
 * {方法功能描述}
 * <p>
 * 业务逻辑：
 * <ol>
 *   <li>{步骤1}</li>
 *   <li>{步骤2}</li>
 *   <li>{步骤3}</li>
 * </ol>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>{注意事项1}</li>
 *   <li>{注意事项2}</li>
 * </ul>
 * 
 * @param {参数名} {参数说明}
 * @return {返回值说明}
 * @throws BusinessException 当{异常条件}时抛出
 */
```

**Mapper方法模板**:
```java
/**
 * {方法功能描述}
 * <p>
 * SQL查询说明：{查询逻辑说明}
 * 
 * @param {参数名} {参数说明}
 * @return {返回值说明}
 */
```

#### 3.4 字段注释生成器

**字段注释模板**:
```java
/** {字段用途说明} */
private Type fieldName;
```

对于复杂字段：
```java
/**
 * {字段用途说明}
 * <p>
 * 取值范围：{取值说明}
 * <p>
 * 约束条件：{约束说明}
 */
private Type fieldName;
```

#### 3.5 行注释生成器

**行注释原则**:
- 注释位于代码行上方
- 使用`//`单行注释
- 简洁明了，说明代码意图
- 为代码逻辑分段添加注释

**示例**:
```java
// 1. 查询用户信息
SysUser user = userMapper.selectById(userId);

// 2. 验证用户状态
if (user == null || !user.getStatus()) {
    throw new BusinessException("用户不存在或已禁用");
}

// 3. 更新用户信息
user.setUpdateTime(LocalDateTime.now());
userMapper.updateById(user);
```

### 4. 注释应用组件 (Comment Applicator)

**职责**: 将生成的注释插入到Java文件的正确位置

**实现策略**:
1. 解析Java文件的AST（抽象语法树）
2. 识别类、方法、字段的位置
3. 检查是否已有注释
4. 如果没有注释，插入生成的注释
5. 如果有注释但不完整，补充缺失的部分
6. 保存修改后的文件

**注释插入位置**:
- 版权信息：文件开头，package声明之前
- 类注释：类声明之前
- 方法注释：方法声明之前
- 字段注释：字段声明之前
- 行注释：代码行上方

### 5. 验证组件 (Validator)

**职责**: 检查注释的完整性和一致性

**验证规则**:
1. 检查版权信息是否存在
2. 检查类注释是否包含@author和@date标签
3. 检查方法注释的@param标签是否与实际参数匹配
4. 检查方法注释的@return标签是否与返回类型匹配
5. 检查是否有未注释的public方法
6. 检查注释内容是否为空或无意义

## Data Models

### FileType枚举

```java
public enum FileType {
    CONTROLLER,      // Controller类
    SERVICE_IMPL,    // Service实现类
    SERVICE,         // Service接口
    MAPPER,          // Mapper接口
    ENTITY,          // Entity实体类
    DTO,             // DTO数据传输对象
    VO,              // VO视图对象
    PARAM,           // Param参数对象
    VALIDATOR,       // Validator校验类
    CONFIG,          // Config配置类
    UTIL,            // Util工具类
    COMMON           // 通用类
}
```

### CommentTemplate类

```java
public class CommentTemplate {
    private String copyrightHeader;      // 版权信息头
    private String classCommentTemplate; // 类注释模板
    private String methodCommentTemplate;// 方法注释模板
    private String fieldCommentTemplate; // 字段注释模板
}
```

### ProcessResult类

```java
public class ProcessResult {
    private int totalFiles;              // 总文件数
    private int processedFiles;          // 已处理文件数
    private int modifiedFiles;           // 修改的文件数
    private List<String> errors;         // 错误信息列表
    private Map<String, Integer> statistics; // 统计信息
}
```

## Correctness Properties

*属性是一个特征或行为，应该在系统的所有有效执行中保持为真——本质上是关于系统应该做什么的正式陈述。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### Property 1: 版权信息完整性

*对于任何*处理后的Java文件，文件开头应该包含完整的Apache License 2.0版权声明，且位于package声明之前

**Validates: Requirements 1.1, 1.2, 1.3, 1.4**

### Property 2: 类注释存在性

*对于任何*public类，应该存在类级别的Javadoc注释，且包含@author和@date标签

**Validates: Requirements 2.1, 2.7, 2.8**

### Property 3: 方法注释参数一致性

*对于任何*有参数的public方法，方法注释中的@param标签数量应该等于方法实际参数数量，且参数名称一一对应

**Validates: Requirements 3.4, 10.4**

### Property 4: 方法注释返回值一致性

*对于任何*有返回值（非void）的public方法，方法注释应该包含@return标签

**Validates: Requirements 3.5, 10.5**

### Property 5: 行注释位置正确性

*对于任何*方法内部的注释，注释应该位于代码行上方，而不是行尾

**Validates: Requirements 5.2**

### Property 6: Javadoc标签格式正确性

*对于任何*使用@see、@link、@code标签的注释，标签格式应该符合Javadoc规范

**Validates: Requirements 6.1, 6.2, 6.3**

### Property 7: 注释内容非空性

*对于任何*Javadoc注释，注释内容不应该为空或只包含标签而没有描述文本

**Validates: Requirements 7.3, 10.6**

### Property 8: 文件处理幂等性

*对于任何*Java文件，多次运行注释添加流程应该产生相同的结果，不会重复添加注释

**Validates: Requirements 9.5**

## Error Handling

### 错误类型

1. **文件读取错误**
   - 文件不存在
   - 文件权限不足
   - 文件编码问题
   - 处理方式：记录错误日志，跳过该文件，继续处理其他文件

2. **文件解析错误**
   - Java语法错误
   - 无法解析的代码结构
   - 处理方式：记录错误日志，跳过该文件，继续处理其他文件

3. **注释生成错误**
   - 模板加载失败
   - 模板变量替换失败
   - 处理方式：使用默认模板，记录警告日志

4. **文件写入错误**
   - 文件被锁定
   - 磁盘空间不足
   - 处理方式：记录错误日志，保留原文件，继续处理其他文件

### 错误恢复策略

1. **备份机制**
   - 在修改文件前创建备份
   - 如果处理失败，可以恢复原文件

2. **事务性处理**
   - 先将修改写入临时文件
   - 验证成功后再替换原文件

3. **错误报告**
   - 生成详细的错误报告
   - 包含文件路径、错误类型、错误信息
   - 提供修复建议

## Testing Strategy

### 单元测试

**测试范围**:
1. 文件扫描功能
   - 测试扫描指定目录的Java文件
   - 测试过滤构建目录
   - 测试空目录处理

2. 文件分类功能
   - 测试各种文件类型的识别
   - 测试边界情况（如文件路径包含多个关键字）

3. 注释生成功能
   - 测试各种类型的注释模板生成
   - 测试模板变量替换
   - 测试特殊字符处理

4. 注释应用功能
   - 测试注释插入位置
   - 测试已有注释的处理
   - 测试代码格式保持

5. 验证功能
   - 测试各种验证规则
   - 测试错误检测

### 集成测试

**测试场景**:
1. 完整流程测试
   - 从扫描文件到生成报告的完整流程
   - 验证处理结果的正确性

2. 批量处理测试
   - 测试处理多个模块
   - 测试处理大量文件
   - 验证性能和稳定性

3. 错误处理测试
   - 测试各种错误场景
   - 验证错误恢复机制
   - 验证错误报告生成

### 属性测试

**测试属性**:
1. 版权信息完整性属性
   - 生成随机的Java文件
   - 运行注释添加流程
   - 验证版权信息是否完整且位置正确

2. 类注释存在性属性
   - 生成随机的public类
   - 运行注释添加流程
   - 验证类注释是否存在且包含必要标签

3. 方法注释参数一致性属性
   - 生成随机参数数量和类型的方法
   - 运行注释添加流程
   - 验证@param标签与实际参数一致

4. 方法注释返回值一致性属性
   - 生成随机返回类型的方法
   - 运行注释添加流程
   - 验证@return标签的存在性

5. 行注释位置正确性属性
   - 生成包含行注释的方法
   - 运行注释添加流程
   - 验证注释位于代码行上方

6. 文件处理幂等性属性
   - 对同一文件运行多次注释添加流程
   - 验证结果一致且不重复添加注释

### 测试工具

- **单元测试框架**: JUnit 5
- **断言库**: AssertJ
- **Mock框架**: Mockito
- **属性测试库**: jqwik（Java的属性测试库）
- **代码覆盖率**: JaCoCo

### 测试配置

- 每个属性测试运行至少100次迭代
- 使用随机生成的测试数据
- 测试标签格式：`Feature: backend-javadoc-enhancement, Property {number}: {property_text}`
