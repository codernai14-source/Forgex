package com.forgex.basic.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员主数据 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Mapper
public interface BasicEmployeeMapper extends BaseMapper<BasicEmployee> {
}
