package com.forgex.basic.employee.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import com.forgex.common.dataperm.DataPermission;
import com.forgex.common.security.SecurityLabeled;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员主数据 Mapper。
 * <p>
 * 重写 {@link #selectList(Wrapper)} 以便挂载数据权限与安全标记。
 * 必须保留 {@code @Param(Constants.WRAPPER)}，与 MyBatis-Plus 注入 SQL 中的 {@code ew} 对齐。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 * @see DataPermission
 * @see SecurityLabeled
 */
@Mapper
public interface BasicEmployeeMapper extends BaseMapper<BasicEmployee> {

    /**
     * 按条件查询人员列表，并叠加数据权限、安全标记过滤。
     *
     * @param queryWrapper 查询条件，对应 MyBatis-Plus 的 {@code ew}
     * @return 人员列表，无匹配时为空列表
     */
    @Override
    @DataPermission(userColumn = "id", deptColumn = "department_id")
    @SecurityLabeled
    List<BasicEmployee> selectList(@Param(Constants.WRAPPER) Wrapper<BasicEmployee> queryWrapper);
}
