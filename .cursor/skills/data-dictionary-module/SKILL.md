---
name: data-dictionary-module
description: 实现数据字典模块的开发指导，包括实体类、服务层、控制器、API 接口等。当开发数据字典相关功能、管理系统枚举值、或实现下拉框选项时自动应用此技能。
---

# 数据字典模块开发技能

## 模块概述

数据字典模块用于管理系统中的各类枚举值和下拉选项，支持树形结构和国际化。

### 核心特性

- **树形结构**：字典类型和字典值存储在同一张表，通过 `parent_id` 区分
- **国际化支持**：字典名称支持多语言
- **缓存支持**：字典数据自动缓存，提高查询效率
- **租户隔离**：支持公共字典和租户私有字典

### 使用场景

1. 下拉框选项（如：性别、状态、类型等）
2. 系统配置项（如：开关、选项等）
3. 业务枚举值（如：订单状态、审批状态等）
4. 国际化文本（如：多语言标签等）

---

## 数据库设计

### 表结构

```sql
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父 ID（0 表示字典类型，>0 表示字典值）',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `dict_value` varchar(255) DEFAULT NULL COMMENT '字典值',
  `dict_label` varchar(255) DEFAULT NULL COMMENT '字典标签',
  `order_num` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0=禁用，1=启用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID（0=公共字典）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0=未删除，1=已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_dict_code` (`dict_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';
```

### 字段说明

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `id` | bigint | 主键 ID | 1234567890 |
| `parent_id` | bigint | 父 ID | 0（字典类型）或类型 ID（字典值） |
| `dict_name` | varchar(100) | 字典名称 | "性别" |
| `dict_code` | varchar(100) | 字典编码 | "user_gender" |
| `dict_value` | varchar(255) | 字典值 | "1" |
| `dict_label` | varchar(255) | 字典标签 | "男" |
| `order_num` | int | 排序号 | 1 |
| `status` | tinyint | 状态 | 0=禁用，1=启用 |
| `tenant_id` | bigint | 租户 ID | 0=公共字典，>0=租户私有 |

---

## 代码实现

### 目录结构

```
Forgex_Sys/src/main/java/com/forgex/sys/
├── domain/
│   ├── entity/
│   │   └── SysDict.java           # 字典实体
│   ├── param/
│   │   └── DictQueryParam.java    # 字典查询参数
│   └── vo/
│       └── DictVO.java            # 字典 VO
├── mapper/
│   └── SysDictMapper.java         # 字典 Mapper
├── service/
│   ├── ISysDictService.java       # 字典服务接口
│   └── impl/
│       └── SysDictServiceImpl.java # 字典服务实现
└── controller/
    └── SysDictController.java     # 字典控制器
```

### 实体类

```java
/**
 * 数据字典实体类
 * <p>
 * 对应数据库表：sys_dict
 * 用于存储系统字典信息，支持树形结构和租户隔离
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    
    /**
     * 父 ID
     * 0 表示字典类型，>0 表示字典值
     */
    private Long parentId;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典编码
     */
    private String dictCode;
    
    /**
     * 字典值
     */
    private String dictValue;
    
    /**
     * 字典标签
     */
    private String dictLabel;
    
    /**
     * 排序号
     */
    private Integer orderNum;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}
```

### 服务接口

```java
/**
 * 数据字典服务接口
 * <p>
 * 提供字典相关的业务逻辑处理，包括：
 * 1. 字典 CRUD 操作
 * 2. 字典树查询
 * 3. 字典值查询
 * 4. 字典缓存管理
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 */
public interface ISysDictService extends IService<SysDict> {
    
    /**
     * 根据字典编码查询字典值列表
     * <p>
     * 查询指定字典编码下的所有启用状态的字典值
     * </p>
     *
     * @param dictCode 字典编码
     * @return 字典值列表
     */
    List<SysDict> getDictValuesByCode(String dictCode);
    
    /**
     * 获取字典树
     * <p>
     * 查询指定父 ID 下的字典树
     * </p>
     *
     * @param parentId 父 ID，0 表示查询所有字典类型
     * @return 字典树列表
     */
    List<SysDict> getDictTree(Long parentId);
    
    /**
     * 刷新字典缓存
     * <p>
     * 清除指定字典编码的缓存
     * </p>
     *
     * @param dictCode 字典编码
     */
    void refreshCache(String dictCode);
}
```

### 控制器

```java
/**
 * 数据字典控制器
 * <p>
 * 提供字典管理的 RESTful API 接口，包括：
 * 1. 字典分页查询
 * 2. 字典详情查询
 * 3. 字典创建、更新、删除
 * 4. 字典树查询
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-03-28
 * @see ISysDictService
 */
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDictController {
    
    private final ISysDictService dictService;
    
    /**
     * 分页查询字典列表
     * <p>
     * 接口路径：POST /sys/dict/page
     * 需要权限：sys:dict:query
     * </p>
     *
     * @param query 查询参数
     *              - pageNum: 页码（必填）
     *              - pageSize: 每页大小（必填）
     *              - dictCode: 字典编码（可选）
     *              - dictName: 字典名称（可选）
     *              - status: 状态（可选）
     * @return 字典分页列表
     */
    @RequirePerm("sys:dict:query")
    @PostMapping("/page")
    public R<Page<SysDictVO>> page(@RequestBody DictQueryParam query) {
        return R.ok(dictService.pageDicts(query));
    }
    
    /**
     * 根据字典编码查询字典值
     * <p>
     * 接口路径：POST /sys/dict/values/{dictCode}
     * 无需权限（公共接口）
     * </p>
     *
     * @param dictCode 字典编码
     * @return 字典值列表
     */
    @PostMapping("/values/{dictCode}")
    public R<List<SysDictVO>> getDictValues(@PathVariable String dictCode) {
        return R.ok(dictService.getDictValuesByCode(dictCode));
    }
    
    /**
     * 获取字典树
     * <p>
     * 接口路径：POST /sys/dict/tree
     * 需要权限：sys:dict:query
     * </p>
     *
     * @param parentId 父 ID，0 表示查询所有字典类型
     * @return 字典树列表
     */
    @RequirePerm("sys:dict:query")
    @PostMapping("/tree")
    public R<List<SysDictVO>> getDictTree(@RequestParam(defaultValue = "0") Long parentId) {
        return R.ok(dictService.getDictTree(parentId));
    }
}
```

---

## API 接口

### 字典管理接口

#### 分页查询字典列表

**请求**：`POST /sys/dict/page`

```json
{
    "pageNum": 1,
    "pageSize": 10,
    "dictCode": "user_gender",
    "dictName": "性别",
    "status": 1
}
```

**响应**：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "parentId": 0,
                "dictName": "性别",
                "dictCode": "user_gender",
                "orderNum": 1,
                "status": 1
            }
        ],
        "total": 1
    }
}
```

#### 根据字典编码查询字典值

**请求**：`POST /sys/dict/values/{dictCode}`

**响应**：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 2,
            "dictValue": "1",
            "dictLabel": "男",
            "orderNum": 1
        },
        {
            "id": 3,
            "dictValue": "2",
            "dictLabel": "女",
            "orderNum": 2
        }
    ]
}
```

#### 获取字典树

**请求**：`POST /sys/dict/tree`

```json
{
    "parentId": 0
}
```

**响应**：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "parentId": 0,
            "dictName": "性别",
            "dictCode": "user_gender",
            "children": [
                {
                    "id": 2,
                    "parentId": 1,
                    "dictValue": "1",
                    "dictLabel": "男"
                },
                {
                    "id": 3,
                    "parentId": 1,
                    "dictValue": "2",
                    "dictLabel": "女"
                }
            ]
        }
    ]
}
```

---

## 使用指南

### 后端使用

#### 查询字典值

```java
@Service
public class OrderServiceImpl {
    
    @Autowired
    private ISysDictService dictService;
    
    public List<String> getOrderStatusOptions() {
        // 查询订单状态字典值
        List<SysDict> dicts = dictService.getDictValuesByCode("order_status");
        
        // 提取字典标签
        return dicts.stream()
                   .map(SysDict::getDictLabel)
                   .collect(Collectors.toList());
    }
}
```

#### 使用字典缓存

```java
@Service
public class UserServiceImpl {
    
    @Autowired
    private ISysDictService dictService;
    
    @Cacheable(value = "dict:user_gender", key = "#root.methodName")
    public List<SysDict> getGenderOptions() {
        // 从缓存获取，如果没有则查询数据库
        return dictService.getDictValuesByCode("user_gender");
    }
}
```

### 前端使用

#### Vue 组件中使用

```vue
<template>
  <div>
    <!-- 使用字典数据 -->
    <a-select v-model:value="gender" :options="genderOptions" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getDictValues } from '@/api/sys/dict';

const gender = ref('');
const genderOptions = ref([]);

onMounted(async () => {
  const result = await getDictValues('user_gender');
  genderOptions.value = result.data.map(item => ({
    label: item.dictLabel,
    value: item.dictValue
  }));
});
</script>
```

---

## 缓存机制

### 缓存策略

- **缓存内容**：字典值列表
- **缓存 Key**：`dict:{dictCode}`
- **过期时间**：30 分钟
- **刷新方式**：
  - 字典数据变更时自动刷新
  - 手动调用 `refreshCache` 方法

### 缓存更新

```java
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDict entity) {
        boolean success = super.save(entity);
        if (success) {
            // 清除缓存
            refreshCache(entity.getDictCode());
        }
        return success;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDict entity) {
        boolean success = super.updateById(entity);
        if (success) {
            // 清除缓存
            refreshCache(entity.getDictCode());
        }
        return success;
    }
    
    @Override
    public void refreshCache(String dictCode) {
        // 清除 Redis 缓存
        String cacheKey = "dict:" + dictCode;
        redisTemplate.delete(cacheKey);
    }
}
```

---

## 注意事项

### 字典编码规范

1. **命名格式**：`模块_业务_类型`
   - 示例：`user_gender`（用户模块 - 性别）
   - 示例：`order_status`（订单模块 - 状态）
   - 示例：`biz_audit_status`（业务模块 - 审核状态）

2. **唯一性**：字典编码在租户内必须唯一

3. **公共字典**：`tenant_id = 0` 的字典所有租户可用

### 性能优化

1. **批量查询**：避免循环调用字典接口
2. **缓存使用**：频繁使用的字典必须使用缓存
3. **懒加载**：非必要的字典数据延迟加载

### 国际化

字典名称支持国际化，可以通过以下方式实现：

1. **多语言字段**：在字典表中添加 `dict_name_en`、`dict_name_zh` 等字段
2. **国际化表**：使用独立的国际化消息表存储多语言翻译
3. **前端国际化**：在前端使用 vue-i18n 进行翻译

---

## 相关文档

- [数据库设计规范](../../02-规范标准/数据库设计规范.md)
- [代码注释规范](../../02-规范标准/代码注释规范.md)
- [国际化模块说明](../06-附录/Forgex_Common 工具类说明.md)

---

**文档版本**: 2.0  
**创建日期**: 2026-04-03  
**作者**: LiDaoMoM
