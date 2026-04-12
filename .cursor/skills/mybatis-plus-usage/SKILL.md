---
name: mybatis-plus-usage
description: MyBatis-Plus 和 MPJ 的使用规范指导，包括 BaseMapper 基础 CRUD、MPJLambdaQueryWrapper 联表查询、XML 复杂 SQL 编写等。当开发数据访问层、编写 Mapper/Service 代码、或进行数据库操作时自动应用此技能。
---

# MyBatis-Plus/MPJ 使用规范技能

## 核心原则

### 严格禁止使用注解 SQL

**禁止**在 Mapper 接口中使用 `@Select`、`@Update`、`@Insert`、`@Delete` 等注解编写 SQL。

❌ **错误示例**：

```java
@Select("SELECT * FROM sys_user WHERE username = #{username}")
SysUser selectByUsername(String username);
```

✅ **正确做法**：使用 BaseMapper 方法、MPJ 条件构造器或 XML 文件

### 使用场景分类

| 操作类型 | 实现方式 | 说明 |
|----------|----------|------|
| 简单单表 CRUD | MyBatis-Plus BaseMapper | 直接使用 BaseMapper 提供的方法 |
| 简单条件查询 | MPJLambdaQueryWrapper | 使用 MPJ 的条件构造器 |
| 简单联表查询 | MPJLambdaQueryWrapper | 使用 MPJ 的 join 方法 |
| 复杂多表 SQL | XML 文件 | 在 XML 中编写 SQL |
| 批量操作 | MyBatis-Plus 批量方法 | 使用 `saveBatch`、`updateBatchById` 等 |

---

## MyBatis-Plus 基础使用

### Mapper 接口定义

所有 Mapper 接口必须继承 `BaseMapper`：

```java
package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * <p>
 * 提供用户表的数据访问操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-03-28
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    // 无需添加任何方法，即可使用 BaseMapper 提供的所有 CRUD 方法
    
}
```

### BaseMapper 提供的核心方法

**插入操作**：
```java
int insert(T entity);
int insertBatch(Collection<T> entities);
```

**删除操作**：
```java
int deleteById(Serializable id);
int delete(Wrapper<T> queryWrapper);
int deleteBatchIds(Collection<? extends Serializable> idList);
```

**更新操作**：
```java
int updateById(T entity);
int update(T entity, Wrapper<T> queryWrapper);
```

**查询操作**：
```java
T selectById(Serializable id);
T selectOne(Wrapper<T> queryWrapper);
List<T> selectList(Wrapper<T> queryWrapper);
IPage<T> selectPage(IPage<T> page, Wrapper<T> queryWrapper);
Integer selectCount(Wrapper<T> queryWrapper);
```

### Service 层实现示例

```java
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    public SysUser getByUsername(String username) {
        // 使用 LambdaQueryWrapper 构建条件查询
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username)
               .eq(SysUser::getDeleted, 0);
        return userMapper.selectOne(wrapper);
    }
    
    @Override
    public Page<SysUser> pageUsers(UserParam param) {
        // 分页查询
        Page<SysUser> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(param.getUsername()), 
                   SysUser::getUsername, param.getUsername())
               .eq(param.getStatus() != null, 
                   SysUser::getStatus, param.getStatus())
               .orderByDesc(SysUser::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(UserParam param) {
        // 参数转 Entity
        SysUser user = BeanUtil.copyProperties(param, SysUser.class);
        // 插入数据库
        return userMapper.insert(user) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserParam param) {
        // 参数转 Entity
        SysUser user = BeanUtil.copyProperties(param, SysUser.class);
        // 根据 ID 更新
        return userMapper.updateById(user) > 0;
    }
}
```

---

## MPJ（MyBatis-Plus-Join）使用规范

### 什么是 MPJ

MPJ（MyBatis-Plus-Join）是 MyBatis-Plus 的增强插件，提供强大的联表查询能力：
- 支持 LEFT JOIN、RIGHT JOIN、INNER JOIN 等
- 支持一对一、一对多关联查询
- 支持字段别名、字段选择
- 完全兼容 MyBatis-Plus 的语法

### Mapper 接口定义

使用 MPJ 需要继承 `MPJBaseMapper`：

```java
@Mapper
public interface SysUserMapper extends MPJBaseMapper<SysUser> {
    
    // 继承 MPJBaseMapper 后，可使用 MPJ 提供的联表查询方法
    
}
```

### 简单联表查询示例

**场景**：查询用户及其所属部门信息

```java
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    public List<UserResponse> listUsersWithDept() {
        // 使用 MPJLambdaQueryWrapper 进行联表查询
        MPJLambdaQueryWrapper<SysUser> wrapper = new MPJLambdaQueryWrapper<>();
        wrapper.select(SysUser::getId,
                      SysUser::getUsername,
                      SysUser::getRealName,
                      SysUser::getDeptId,
                      // 关联查询部门字段
                      SysDepartment::getId,
                      SysDepartment::getDeptName,
                      SysDepartment::getDeptType)
               .leftJoin(SysDepartment.class, 
                        SysDepartment::getId, 
                        SysUser::getDeptId)
               .eq(SysUser::getDeleted, 0)
               .eq(SysDepartment::getDeleted, 0);
        
        // 执行联表查询
        List<SysUser> users = userMapper.selectJoinList(wrapper);
        
        // 转换为 Response 对象
        return users.stream()
                   .map(this::convertToResponse)
                   .collect(Collectors.toList());
    }
}
```

### 一对多关联查询

**场景**：查询角色及其拥有的菜单列表

```java
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    
    @Override
    public List<RoleResponse> listRolesWithMenus() {
        MPJLambdaQueryWrapper<SysRole> wrapper = new MPJLambdaQueryWrapper<>();
        wrapper.select(SysRole::getId,
                      SysRole::getRoleName,
                      SysRole::getRoleCode,
                      // 一对多关联查询菜单
                      SysMenu::getId,
                      SysMenu::getMenuName,
                      SysMenu::getMenuType,
                      SysMenu::getPath)
               .leftJoin(SysRoleMenu.class,
                        SysRoleMenu::getRoleId,
                        SysRole::getId)
               .leftJoin(SysMenu.class,
                        SysMenu::getId,
                        SysRoleMenu::getMenuId)
               .eq(SysRole::getDeleted, 0)
               .eq(SysMenu::getDeleted, 0);
        
        // 执行查询
        List<SysRole> roles = roleMapper.selectJoinList(wrapper);
        
        // 转换为 Response 对象（包含菜单列表）
        return roles.stream()
                   .map(this::convertToResponseWithMenus)
                   .collect(Collectors.toList());
    }
}
```

### 复杂条件联表查询

**场景**：根据多条件分页查询用户，关联部门和角色

```java
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Override
    public Page<UserResponse> pageUsersWithRelations(UserParam param) {
        // 创建分页对象
        Page<SysUser> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        // 构建 MPJ 查询条件
        MPJLambdaQueryWrapper<SysUser> wrapper = new MPJLambdaQueryWrapper<>();
        wrapper.select(SysUser::getId,
                      SysUser::getUsername,
                      SysUser::getRealName,
                      SysUser::getPhone,
                      SysUser::getEmail,
                      SysUser::getStatus,
                      // 关联部门字段
                      SysDepartment::getId,
                      SysDepartment::getDeptName)
               .leftJoin(SysDepartment.class,
                        SysDepartment::getId,
                        SysUser::getDeptId)
               // 动态条件
               .like(StringUtils.isNotBlank(param.getUsername()),
                    SysUser::getUsername, param.getUsername())
               .like(StringUtils.isNotBlank(param.getRealName()),
                    SysUser::getRealName, param.getRealName())
               .eq(param.getStatus() != null,
                  SysUser::getStatus, param.getStatus())
               .eq(param.getDeptId() != null,
                  SysUser::getDeptId, param.getDeptId())
               .eq(SysUser::getDeleted, 0)
               .eq(SysDepartment::getDeleted, 0)
               .orderByDesc(SysUser::getCreateTime);
        
        // 分页查询
        Page<SysUser> userPage = userMapper.selectJoinPage(page, wrapper);
        
        // 转换为 Response 对象
        List<UserResponse> responses = userPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        Page<UserResponse> responsePage = new Page<>(
            userPage.getCurrent(),
            userPage.getSize(),
            userPage.getTotal()
        );
        responsePage.setRecords(responses);
        
        return responsePage;
    }
}
```

---

## XML 方式编写复杂 SQL

### 使用场景

当遇到以下情况时，使用 XML 方式编写 SQL：
- 多表关联超过 3 张表
- 需要使用复杂的 SQL 函数
- 需要动态 SQL（多条件组合）
- 需要子查询、UNION 等复杂语法
- 性能优化需要手写 SQL

### Mapper 接口定义

```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据角色 ID 查询用户列表（复杂联表查询）
     *
     * @param roleId 角色 ID
     * @return 用户列表
     */
    List<UserResponse> selectUsersByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 统计各部门用户数（复杂统计查询）
     *
     * @return 部门用户统计列表
     */
    List<DeptUserStatResponse> selectDeptUserStatistics();
}
```

### XML 文件编写

**文件路径**：`src/main/resources/mapper/SysUserMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.forgex.sys.mapper.SysUserMapper">
    
    <!-- 结果映射 -->
    <resultMap id="UserResponseMap" type="com.forgex.sys.domain.response.UserResponse">
        <id property="id" column="id"/>
        <result property="username" column="username"/>
        <result property="realName" column="real_name"/>
        <result property="phone" column="phone"/>
        <result property="email" column="email"/>
        <result property="status" column="status"/>
        <result property="deptName" column="dept_name"/>
        <result property="roleNames" column="role_names"/>
    </resultMap>
    
    <!-- 根据角色 ID 查询用户列表 -->
    <select id="selectUsersByRoleId" resultMap="UserResponseMap">
        SELECT DISTINCT
            u.id,
            u.username,
            u.real_name,
            u.phone,
            u.email,
            u.status,
            d.dept_name,
            GROUP_CONCAT(r.role_name SEPARATOR ',') AS role_names
        FROM sys_user u
        LEFT JOIN sys_department d ON u.dept_id = d.id AND d.deleted = 0
        INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.deleted = 0
        INNER JOIN sys_role r ON ur.role_id = r.id AND r.deleted = 0
        WHERE u.deleted = 0
        <if test="roleId != null">
            AND r.id = #{roleId}
        </if>
        GROUP BY u.id, u.username, u.real_name, u.phone, u.email, u.status, d.dept_name
        ORDER BY u.create_time DESC
    </select>
    
    <!-- 统计各部门用户数 -->
    <select id="selectDeptUserStatistics" 
            resultType="com.forgex.sys.domain.response.DeptUserStatResponse">
        SELECT
            d.id AS dept_id,
            d.dept_name,
            COUNT(u.id) AS user_count,
            SUM(CASE WHEN u.status = 1 THEN 1 ELSE 0 END) AS active_count,
            SUM(CASE WHEN u.status = 0 THEN 1 ELSE 0 END) AS inactive_count
        FROM sys_department d
        LEFT JOIN sys_user u ON d.id = u.dept_id AND u.deleted = 0
        WHERE d.deleted = 0
        GROUP BY d.id, d.dept_name
        ORDER BY d.ancestors
    </select>
    
</mapper>
```

### Service 层调用

```java
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Override
    public List<UserResponse> getUsersByRoleId(Long roleId) {
        // 调用 XML 中定义的复杂查询
        return userMapper.selectUsersByRoleId(roleId);
    }
    
    @Override
    public List<DeptUserStatResponse> getDeptUserStatistics() {
        // 调用 XML 中定义的统计查询
        return userMapper.selectDeptUserStatistics();
    }
}
```

---

## 最佳实践

### 查询优化

**使用索引字段**：

```java
// ✅ 好的写法 - 使用索引字段
wrapper.eq(SysUser::getUsername, username);

// ❌ 差的写法 - 使用函数导致索引失效
// wrapper.like(SysUser::getUsername, username); // 前缀匹配可以使用索引
```

**避免 N+1 查询**：

```java
// ❌ 差的写法 - N+1 查询问题
List<SysUser> users = userMapper.selectList(null);
for (SysUser user : users) {
    // 每次循环都会查询一次数据库
    SysDept dept = deptMapper.selectById(user.getDeptId());
}

// ✅ 好的写法 - 使用联表查询一次获取
MPJLambdaQueryWrapper<SysUser> wrapper = new MPJLambdaQueryWrapper<>();
wrapper.select(SysUser::getId,
              SysUser::getUsername,
              SysDepartment::getId,
              SysDepartment::getDeptName)
       .leftJoin(SysDepartment.class,
                SysDepartment::getId,
                SysUser::getDeptId);
List<SysUser> users = userMapper.selectJoinList(wrapper);
```

### 分页查询

```java
// ✅ 正确的分页查询
Page<SysUser> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
wrapper.orderByDesc(SysUser::getCreateTime);
IPage<SysUser> result = userMapper.selectPage(page, wrapper);

// 返回分页结果
return PageResult.of(result.getRecords(), result.getTotal());
```

### 批量操作

```java
// ✅ 批量插入（性能更好）
List<SysUser> users = Arrays.asList(user1, user2, user3);
boolean success = userService.saveBatch(users);

// ✅ 批量更新
List<SysUser> users = getUsersToUpdate();
boolean success = userService.updateBatchById(users);
```

### 事务控制

```java
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(UserParam param) {
        // 插入用户
        SysUser user = convertToEntity(param);
        userMapper.insert(user);
        
        // 插入用户角色关联
        if (param.getRoleIds() != null) {
            for (Long roleId : param.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        
        return true;
    }
}
```

---

## 常见错误

### 错误使用注解 SQL

❌ **错误示例**：
```java
@Select("SELECT * FROM sys_user WHERE id = #{id}")
SysUser selectById(Long id);
```

✅ **正确做法**：
```java
// 直接使用 BaseMapper 的方法
SysUser user = userMapper.selectById(id);
```

### 忘记继承 MPJBaseMapper

❌ **错误**：无法使用 MPJ 的联表查询方法

```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 无法使用 selectJoinList 等方法
}
```

✅ **正确**：

```java
@Mapper
public interface SysUserMapper extends MPJBaseMapper<SysUser> {
    // 可以使用 MPJ 的所有方法
}
```

### 联表查询未指定字段

❌ **错误**：查询所有字段，性能差

```java
wrapper.leftJoin(SysDepartment.class,
                SysDepartment::getId,
                SysUser::getDeptId);
```

✅ **正确**：明确指定需要的字段

```java
wrapper.select(SysUser::getId,
              SysUser::getUsername,
              SysDepartment::getId,
              SysDepartment::getDeptName)
       .leftJoin(SysDepartment.class,
                SysDepartment::getId,
                SysUser::getDeptId);
```

---

## 总结决策表

| 场景 | 推荐方案 | 说明 |
|------|----------|------|
| 单表 CRUD | BaseMapper | 无需写 SQL |
| 单表条件查询 | LambdaQueryWrapper | 类型安全 |
| 简单联表（1-2 张表） | MPJLambdaQueryWrapper | 简洁高效 |
| 复杂联表（3 张表以上） | XML | 灵活可控 |
| 批量操作 | MyBatis-Plus 批量方法 | 性能优化 |
| 动态 SQL | XML | 支持 if/foreach 等标签 |

---

**文档版本**: 1.0  
**创建日期**: 2026-03-28  
**作者**: ForGexTeam
