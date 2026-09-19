package com.forgex.common.dataperm;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.forgex.common.security.SecurityLabeled;
import com.forgex.common.tenant.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Set;

/**
 * 数据权限与强制访问控制拦截器。
 * <p>
 * 对标注 {@link DataPermission} 的查询改写 WHERE；在 L3 下同时叠加 {@link SecurityLabeled} 的下读条件。
 * UNION / 子查询 / 分页 count 与主查询共用同一套改写逻辑。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DataScopeProvider
 */
@Slf4j
@Component
public class DataPermissionInterceptor implements InnerInterceptor {

    private final ObjectProvider<DataScopeProvider> dataScopeProvider;
    private final ObjectProvider<SubjectSecurityLevelProvider> securityLevelProvider;

    /**
     * 构造拦截器。
     *
     * @param dataScopeProvider     数据权限提供者
     * @param securityLevelProvider 主体密级提供者
     */
    public DataPermissionInterceptor(ObjectProvider<DataScopeProvider> dataScopeProvider,
                                     ObjectProvider<SubjectSecurityLevelProvider> securityLevelProvider) {
        this.dataScopeProvider = dataScopeProvider;
        this.securityLevelProvider = securityLevelProvider;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        applyFilters(ms, boundSql);
    }

    /**
     * 改写查询 SQL，同时覆盖分页 count。
     *
     * @param ms       MappedStatement
     * @param boundSql 绑定 SQL
     * @throws SQLException 解析失败时失败关闭
     */
    private void applyFilters(MappedStatement ms, BoundSql boundSql) throws SQLException {
        DataPermission annotation = getAnnotation(ms, DataPermission.class);
        SecurityLabeled labeled = getAnnotation(ms, SecurityLabeled.class);
        if ((annotation == null || !annotation.enabled()) && (labeled == null || !labeled.enabled())) {
            return;
        }

        Long userId = UserContext.get();
        if (userId == null) {
            return;
        }

        Expression condition = null;
        if (annotation != null && annotation.enabled()) {
            DataPermissionHelper.DataPermissionInfo permInfo = DataPermissionHelper.getCurrentUserPermission();
            if (permInfo == null) {
                DataScopeProvider provider = dataScopeProvider.getIfAvailable();
                if (provider == null) {
                    throw new SQLException("DataScopeProvider is not registered");
                }
                permInfo = provider.load(userId);
                if (permInfo == null) {
                    throw new SQLException("Unable to resolve data permission for user " + userId);
                }
            }
            if (permInfo.getDataScope() != DataScope.ALL) {
                condition = buildDataPermissionCondition(annotation, permInfo);
            }
        }

        SubjectSecurityLevelProvider levelProvider = securityLevelProvider.getIfAvailable();
        if (labeled != null && labeled.enabled() && levelProvider != null && levelProvider.mandatoryAccessEnabled()) {
            Expression mac = buildMacReadCondition(labeled, levelProvider.resolve(userId));
            condition = condition == null ? mac : new AndExpression(condition, mac);
        }

        if (condition == null) {
            return;
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
            if (statement instanceof Select select) {
                applyToSelect(select, condition);
                PluginUtils.mpBoundSql(boundSql).sql(select.toString());
            }
        } catch (Exception ex) {
            throw new SQLException("Unable to apply data permission policy", ex);
        }
    }

    /**
     * 将条件应用到 SELECT，包含 UNION 与子查询。
     *
     * @param select    查询
     * @param condition 过滤条件
     */
    private void applyToSelect(Select select, Expression condition) {
        if (select instanceof ParenthesedSelect parenthesed && parenthesed.getSelect() != null) {
            applyToSelect(parenthesed.getSelect(), condition);
            return;
        }
        if (select instanceof SetOperationList setList && setList.getSelects() != null) {
            for (Select nested : setList.getSelects()) {
                applyToSelect(nested, condition);
            }
            return;
        }
        PlainSelect plainSelect = select.getPlainSelect();
        if (plainSelect == null) {
            return;
        }
        if (plainSelect.getFromItem() instanceof ParenthesedSelect nestedFrom && nestedFrom.getSelect() != null) {
            applyToSelect(nestedFrom.getSelect(), condition);
        }
        plainSelect.setWhere(plainSelect.getWhere() == null ? condition : new AndExpression(plainSelect.getWhere(), condition));
    }

    private <T extends java.lang.annotation.Annotation> T getAnnotation(MappedStatement ms, Class<T> type) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf('.'));
            String methodName = id.substring(id.lastIndexOf('.') + 1);
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(type);
                }
            }
        } catch (Exception ex) {
            log.info("读取权限注解失败: {}", ex.getMessage());
        }
        return null;
    }

    private Expression buildDataPermissionCondition(DataPermission annotation,
                                                    DataPermissionHelper.DataPermissionInfo permInfo) {
        return switch (permInfo.getDataScope()) {
            case SELF -> buildSelfCondition(annotation, permInfo.getUserId());
            case DEPT, DEPT_AND_CHILD, CUSTOM -> buildDeptCondition(annotation, permInfo.getDeptIds());
            default -> null;
        };
    }

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

    private Expression buildDeptCondition(DataPermission annotation, Set<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        String deptColumn = annotation.deptColumn();
        if (annotation.deptAlias() != null && !annotation.deptAlias().isEmpty()) {
            deptColumn = annotation.deptAlias() + "." + deptColumn;
        }
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column(deptColumn));
        ExpressionList<Expression> expressionList = new ExpressionList<>();
        for (Long deptId : deptIds) {
            expressionList.add(new LongValue(deptId));
        }
        inExpression.setRightExpression(expressionList);
        return inExpression;
    }

    private Expression buildMacReadCondition(SecurityLabeled labeled, int userLevel) {
        String column = labeled.column();
        if (labeled.alias() != null && !labeled.alias().isEmpty()) {
            column = labeled.alias() + "." + column;
        }
        MinorThanEquals expression = new MinorThanEquals();
        expression.setLeftExpression(new Column(column));
        expression.setRightExpression(new LongValue(userLevel));
        return expression;
    }
}
