# MyBatis-Plus 使用规范

> 分类：开发规范 / 规范文档
> 版本：V0.8.5

本规范约束 Forgex 后端（Java 17 + Spring Boot 3.5.6 + MyBatis-Plus）的数据访问层写法，覆盖分页、条件构造、自动填充、多租户、逻辑删除、乐观锁与批量操作，确保跨模块一致、可维护、可审计。

---

## 一、Entity 标准写法

### 1.1 基类字段约定

所有业务表 Entity 统一继承 `BaseEntity`，公共审计字段由框架自动填充，业务代码不得手动 set：

| 字段 | 类型 | 说明 | 填充时机 |
|---|---|---|---|
| `id` | Long | 主键，雪花算法 | insert |
| `create_time` | LocalDateTime | 创建时间 | insert 自动填充 |
| `update_time` | LocalDateTime | 更新时间 | insert + update 自动填充 |
| `create_by` | String | 创建人 | insert 自动填充 |
| `update_by` | String | 更新人 | insert + update 自动填充 |
| `deleted` | Boolean | 逻辑删除标记（0=未删，1=已删） | 框架自动处理 |
| `tenant_id` | Long | 租户 ID | insert 自动填充 + 查询自动拼接 |

### 1.2 Entity 标准示例

```java
@Data
@TableName("sys_kms_key")
public class SysKmsKey extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 密钥别名（业务唯一标识） */
    private String keyAlias;

    /** 密钥类型：AES / SM4 / RSA / SM2 */
    private String keyType;

    /** 密钥长度（位） */
    private Integer keySize;

    /** 加密后的密钥值（Base64） */
    private String encryptedKeyValue;

    /** 密钥版本号 */
    private Integer keyVersion;

    /** 状态：ACTIVE / ROTATED / DISABLED */
    private String status;

    /** 描述 */
    private String description;
}
```

规范要点：
- `@TableId(type = IdType.ASSIGN_ID)` 使用雪花算法，避免自增 ID 在分库分表场景冲突。
- 字段使用驼峰命名，`@TableName` 指定下划线表名，由 MyBatis-Plus 自动映射。
- `@TableField(exist = false)` 标记非数据库字段。
- `@TableLogic` 注解在 `BaseEntity.deleted` 上，由基类统一声明，子类不重复。

---

## 二、Mapper 标准写法

Mapper 继承 `BaseMapper<T>`，仅声明自定义 SQL，标准 CRUD 由框架提供：

```java
public interface SysKmsKeyMapper extends BaseMapper<SysKmsKey> {

    /**
     * 按租户统计激活密钥数量（自定义 SQL 示例）。
     *
     * @param tenantId 租户 ID
     * @return 激活密钥数量
     */
    @Select("SELECT COUNT(*) FROM sys_kms_key WHERE tenant_id = #{tenantId} AND status = 'ACTIVE' AND deleted = 0")
    int countActiveByTenant(@Param("tenantId") Long tenantId);
}
```

规范要点：
- 简单单表 CRUD 直接用 `BaseMapper` 方法，不写 SQL。
- 自定义查询优先用 `@Select` 注解；动态 SQL 用 XML 或 `@SelectProvider`。
- `@Param` 注解必须有，参数名与 SQL 占位符一致。
- 禁止在 Mapper 写业务逻辑，Mapper 只做数据存取。

---

## 三、Service 标准写法

Service 继承 `ServiceImpl<Mapper, Entity>` 并实现业务接口：

```java
public interface KmsService extends IService<SysKmsKey> {
    Long generateKey(String alias, String keyType, int keySize, String description);
    String getActiveKey(String alias);
}

@Service
public class KmsServiceImpl extends ServiceImpl<SysKmsKeyMapper, SysKmsKey>
        implements KmsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateKey(String alias, String keyType, int keySize, String description) {
        // 业务校验
        if (StrUtil.isBlank(alias)) {
            throw new IllegalArgumentException("密钥别名不能为空");
        }
        SysKmsKey entity = new SysKmsKey();
        entity.setKeyAlias(alias);
        entity.setKeyType(keyType.toUpperCase());
        entity.setKeySize(keySize);
        entity.setKeyVersion(getMaxVersion(alias) + 1);
        entity.setStatus("ACTIVE");
        entity.setDescription(description);
        save(entity);
        return entity.getId();
    }
}
```

规范要点：
- Service 接口与实现分离，接口在前，`Impl` 后缀。
- `ServiceImpl` 提供 `save / updateById / removeById / getById / list / page` 等便捷方法，优先使用。
- 写操作必须加 `@Transactional(rollbackFor = Exception.class)`，只读查询不加事务。
- 业务校验、加密、审计日志等业务逻辑放 Service，不放 Mapper。

---

## 四、分页查询（IPage + PaginationInnerInterceptor）

### 4.1 配置分页插件

分页插件在 MyBatis-Plus 配置类中统一注册，全局生效：

```java
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantLineHandler tenantLineHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件（必须排在分页之前）
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

> 插件顺序很重要：多租户 -> 分页 -> 乐观锁，顺序错误会导致 SQL 拼接异常。

### 4.2 分页用法

```java
public Page<SysKmsKey> pageKeys(int current, int size, String alias) {
    Page<SysKmsKey> page = new Page<>(current, size);
    LambdaQueryWrapper<SysKmsKey> wrapper = new LambdaQueryWrapper<SysKmsKey>()
            .like(StrUtil.isNotBlank(alias), SysKmsKey::getKeyAlias, alias)
            .eq(SysKmsKey::getStatus, "ACTIVE")
            .orderByDesc(SysKmsKey::getCreateTime);
    return page(page, wrapper);
}
```

规范要点：
- 使用 `Page<T>` 而非 `IPage<T>` 作为返回类型（`Page` 是 `IPage` 的实现）。
- 条件构造用 `condition` 三元重载（`like(condition, column, value)`），避免写 `if` 包裹。
- 分页参数由 Controller 校验，`current` 从 1 开始，`size` 不超过 100。
- 禁止用 `selectList` 查全表后内存分页。

---

## 五、LambdaQueryWrapper / LambdaUpdateWrapper

### 5.1 LambdaQueryWrapper 查询

```java
// 正确：Lambda 方法引用，编译期检查字段名
LambdaQueryWrapper<SysKmsKey> wrapper = new LambdaQueryWrapper<SysKmsKey>()
        .eq(SysKmsKey::getKeyAlias, alias)
        .eq(SysKmsKey::getStatus, "ACTIVE")
        .eq(SysKmsKey::getDeleted, false)
        .orderByDesc(SysKmsKey::getKeyVersion)
        .last("LIMIT 1");
SysKmsKey key = kmsKeyMapper.selectOne(wrapper);
```

### 5.2 LambdaUpdateWrapper 更新

```java
// 正确：Lambda 方法引用
kmsKeyMapper.update(null, new LambdaUpdateWrapper<SysKmsKey>()
        .eq(SysKmsKey::getId, id)
        .set(SysKmsKey::getStatus, "ROTATED"));
```

### 5.3 禁忌

| 禁止 | 原因 | 正确做法 |
|---|---|---|
| 用字符串字段名 `QueryWrapper<SysKmsKey>().eq("key_alias", v)` | 字段名拼写错误编译期不报错，重构易漏 | 用 `LambdaQueryWrapper` + 方法引用 |
| 在 Wrapper 中拼接用户输入的原始 SQL | SQL 注入风险 | 用参数化条件，禁止 `apply` 拼接外部输入 |
| 查询全部字段再在内存过滤 | 性能与内存浪费 | 条件下推到 SQL，只查需要的列 |
| `last("LIMIT 1")` 滥用 | 仅限确定单条场景，普通查询用分页 | 单条用 `selectOne` 或 `getOne` |

---

## 六、通用字段自动填充（MetaObjectHandler）

审计字段（create_time / update_time / create_by / update_by / deleted / tenant_id）由 `MetaObjectHandler` 自动填充，业务代码不手动 set。

### 6.1 实现示例

```java
@Component
public class ForgexMetaObjectHandler implements MetaObjectHandler {

    private final SecurityContextHelper securityContextHelper;

    public ForgexMetaObjectHandler(SecurityContextHelper securityContextHelper) {
        this.securityContextHelper = securityContextHelper;
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = securityContextHelper.getCurrentUserId();
        Long currentTenantId = securityContextHelper.getCurrentTenantId();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, String.valueOf(currentUserId));
        this.strictInsertFill(metaObject, "updateBy", String.class, String.valueOf(currentUserId));
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
        this.strictInsertFill(metaObject, "tenantId", Long.class, currentTenantId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, String.valueOf(currentUserId));
    }
}
```

### 6.2 Entity 字段标注

```java
// BaseEntity 中
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;

@TableField(fill = FieldFill.INSERT)
private Long tenantId;

@TableLogic
@TableField(fill = FieldFill.INSERT)
private Boolean deleted;
```

规范要点：
- `createTime` / `createBy` / `tenantId` / `deleted` 仅 INSERT 填充。
- `updateTime` / `updateBy` 在 INSERT 和 UPDATE 时都填充。
- `strictInsertFill` / `strictUpdateFill` 只在字段为 null 时填充，业务代码显式 set 的值不会被覆盖。
- 禁止在业务代码手动 `setCreateTime`，交给 Handler。

---

## 七、多租户（TenantLineInnerInterceptor）

### 7.1 工作原理

`TenantLineInnerInterceptor` 自动在 SQL 的 WHERE 条件中追加 `tenant_id = ?`，业务代码无感知。

### 7.2 配置

```java
@Component
public class ForgexTenantLineHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("当前上下文无租户信息");
        }
        return new LongValue(tenantId);
    }

    /** 忽略租户过滤的表（如 sys_config 全局配置） */
    @Override
    public boolean ignoreTable(String tableName) {
        return TenantIgnoreTables.TABLES.contains(tableName);
    }
}
```

### 7.3 SQL 约束注意事项

| 项 | 要求 |
|---|---|
| 所有业务表必须含 `tenant_id` 列 | 无该列的表会因拦截器追加条件报错，必须加入忽略名单或补列 |
| 全局表（字典、系统配置） | 在 `ignoreTable` 中声明，不追加租户条件 |
| 手写 SQL（XML / @Select） | 也受拦截器影响，确保 SQL 中表名与 @TableName 一致 |
| 跨租户查询（管理员后台） | 调用前用 `TenantContext.setIgnore(true)` 临时关闭，用完恢复，并在审计日志记录 |
| JOIN 查询 | 拦截器会对所有表追加 tenant_id，确保 JOIN 的表都有该列或在忽略名单 |

> 禁止在业务代码中手动拼 `tenant_id` 条件，统一交给拦截器，避免遗漏导致越权。

---

## 八、逻辑删除

### 8.1 配置

`@TableLogic` 标注在 `BaseEntity.deleted` 字段，框架自动将 `deleteById` 转为 `UPDATE ... SET deleted=1`，查询自动追加 `deleted=0`。

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 8.2 规范

- 业务代码调用 `removeById` / `remove` 即逻辑删除，不写物理删除。
- 需要查询已删除数据（如审计）时，用 `@Select` 手写 SQL 并显式不加 `deleted` 条件。
- 禁止用 `UPDATE` 直接改 `deleted` 字段绕过框架，避免审计字段（update_time）未填充。

---

## 九、乐观锁

### 9.1 用法

高并发更新场景使用 `@Version` 乐观锁：

```java
@Version
private Integer version;
```

更新时框架自动追加 `WHERE version = ?` 并 `SET version = version + 1`：

```java
SysKmsKey key = kmsKeyMapper.selectById(id);
key.setStatus("ROTATED");
int rows = kmsKeyMapper.updateById(key);  // rows=0 表示版本冲突，需重试
if (rows == 0) {
    throw new IllegalStateException("数据已被其他操作修改，请刷新重试");
}
```

规范要点：
- 仅在真实并发冲突场景使用，普通更新不需要。
- 更新失败（rows=0）必须抛异常或重试，不可静默忽略。
- `@Version` 字段初始值为 0，由 insert 自动填充。

---

## 十、批量操作

### 10.1 批量插入

```java
// 推荐：saveBatch 内部分批执行，避免单条 SQL 过大
List<SysKmsKey> keys = buildKeys();
saveBatch(keys, 500);  // 每批 500 条
```

### 10.2 批量更新

```java
// 推荐:updateBatchById
updateBatchById(keys, 500);
```

规范要点：
- 批量大小默认 1000，超过 MySQL 参数上限（`max_allowed_packet`）会报错，建议 500。
- 批量操作同样触发 `MetaObjectHandler` 填充。
- 超大批量（万级以上）考虑分批事务，避免长事务锁表。

---

## 十一、关联文档

- [数据库字段统一规范文档](./数据库字段统一规范文档.md)
- [多租户](../../后端/租户与上下文/多租户.md)
- [代码注释规范](./代码注释规范.md)
