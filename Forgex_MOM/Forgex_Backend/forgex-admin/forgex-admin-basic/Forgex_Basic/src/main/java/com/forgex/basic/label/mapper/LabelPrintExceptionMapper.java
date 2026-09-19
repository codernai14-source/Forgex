package com.forgex.basic.label.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.label.domain.entity.LabelPrintException;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签打印异常记录 Mapper
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface LabelPrintExceptionMapper extends BaseMapper<LabelPrintException> {
}

