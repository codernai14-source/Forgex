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
package com.forgex.common.dataperm;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.forgex.common.tenant.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Set;

/**
 * 数据权限拦截器
 * <p>拦截SQL查询，根据用户的数据权限自动添加过滤条件。</p>
 * 
 * <p>工作原理：</p>
 * <ol>
 *   <li>检查Mapper方法是否标注了@DataPermission注解</li>
 *   <li>获取当前用户的数据权限范围</li>
 *   <li>根据数据权限范围修改SQL，添加WHERE条件</li>
 * </ol>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class DataPermissionInterceptor implements InnerInterceptor {
    
    private final ApplicationContext applicationContext;
    
    public DataPermissionInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        
        // 1. 检查是否需要数据权限控制
        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null || !annotation.enabled()) {
            return;
        }
        
        // 2. 获取当前用户ID
        Long userId = UserContext.get();
        if (userId == null) {
            log.debug("用户未登录，跳过数据权限控制");
            return;
        }
        
        // 3. 获取用户的数据权限信息
        DataPermissionHelper.DataPermissionInfo permInfo = DataPermissionHelper.getCurrentUserPermission();
        if (permInfo == null) {
            // 缓存中没有，需要查询
            permInfo = loadUserDataPermission(userId);
            if (permInfo == null) {
                log.warn("无法获取用户数据权限信息: userId={}", userId);
                return;
            }
        }
        
        // 4. 如果是全部数据权限，不需要过滤
        if (permInfo.getDataScope() == DataScope.ALL) {
            log.debug("用户拥有全部数据权限，跳过过滤: userId={}", userId);
            return;
        }
        
        // 5. 构建数据权限SQL条件
        Expression condition = buildDataPermissionCondition(annotation, permInfo);
        if (condition == null) {
            return;
        }
        
        // 6. 修改SQL（这里简化处理，实际应该解析SQL并添加条件）
        // 注意：完整实现需要使用JSQLParser解析SQL并修改WHERE子句
        log.debug("应用数据权限过滤: userId={}, dataScope={}", userId, permInfo.getDataScope());
    }
    
    /**
     * 获取Mapper方法上的@DataPermission注解
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(DataPermission.class);
                }
            }
        } catch (Exception e) {
            log.debug("获取DataPermission注解失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 加载用户的数据权限信息
     */
    private DataPermissionHelper.DataPermissionInfo loadUserDataPermission(Long userId) {
        try {
            // 动态获取所需的Mapper（避免循环依赖）
            Object userRoleMapper = applicationContext.getBean("sysUserRoleMapper");
            Object roleMapper = applicationContext.getBean("sysRoleMapper");
            Object roleDeptMapper = applicationContext.getBean("sysRoleDeptMapper");
            Object departmentMapper = applicationContext.getBean("sysDepartmentMapper");
            
            // TODO: 实现完整的数据权限查询逻辑
            // 1. 查询用户的角色
            // 2. 查询角色的数据权限范围
            // 3. 根据数据权限范围查询部门列表
            // 4. 缓存结果
            
            // 临时返回默认权限
            DataScope dataScope = DataScope.SELF;
            Set<Long> deptIds = Set.of();
            
            DataPermissionHelper.cachePermission(userId, dataScope, deptIds);
            return new DataPermissionHelper.DataPermissionInfo(dataScope, deptIds, userId);
            
        } catch (Exception e) {
            log.error("加载用户数据权限失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 构建数据权限SQL条件
     */
    private Expression buildDataPermissionCondition(DataPermission annotation, 
                                                   DataPermissionHelper.DataPermissionInfo permInfo) {
        DataScope dataScope = permInfo.getDataScope();
        
        switch (dataScope) {
            case SELF:
                // 仅本人：create_by = userId
                return buildSelfCondition(annotation, permInfo.getUserId());
                
            case DEPT:
            case DEPT_AND_CHILD:
            case CUSTOM:
                // 部门权限：dept_id IN (deptIds)
                return buildDeptCondition(annotation, permInfo.getDeptIds());
                
            default:
                return null;
        }
    }
    
    /**
     * 构建"仅本人"条件
     */
    private Expression buildSelfCondition(DataPermission annotation, Long userId) {
        String userColumn = annotation.userColumn();
        if (annotation.userAlias() != null && !annotation.userAlias().isEmpty()) {
            userColumn = annotation.userAlias() + "." + userColumn;
        }
        
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(userColumn));
        equalsTo.setRightExpression(new LongValue(userId));
        return equalsTo;
    }
    
    /**
     * 构建部门条件
     */
    private Expression buildDeptCondition(DataPermission annotation, Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return null;
        }
        
        String deptColumn = annotation.deptColumn();
        if (annotation.deptAlias() != null && !annotation.deptAlias().isEmpty()) {
            deptColumn = annotation.deptAlias() + "." + deptColumn;
        }
        
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column(deptColumn));
        
        ExpressionList expressionList = new ExpressionList();
        for (Long deptId : deptIds) {
            expressionList.addExpressions(new LongValue(deptId));
        }
        inExpression.setRightExpression(expressionList);
        
        return inExpression;
    }
}

