package com.forgex.common.mapper.dict;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.dict.SysDictNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("admin")
public interface SysDictNodeMapper extends BaseMapper<SysDictNode> {
}

