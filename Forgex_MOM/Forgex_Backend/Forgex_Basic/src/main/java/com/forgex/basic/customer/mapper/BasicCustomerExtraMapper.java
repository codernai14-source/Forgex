package com.forgex.basic.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.customer.domain.entity.BasicCustomerExtra;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户扩展信息 Mapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
public interface BasicCustomerExtraMapper extends BaseMapper<BasicCustomerExtra> {
}
