package com.forgex.basic.factory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BasicFactoryMapper extends BaseMapper<BasicFactory> {
}
