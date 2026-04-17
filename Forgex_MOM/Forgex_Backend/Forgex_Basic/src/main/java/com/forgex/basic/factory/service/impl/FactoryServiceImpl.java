package com.forgex.basic.factory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.factory.service.FactoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FactoryServiceImpl extends ServiceImpl<BasicFactoryMapper, BasicFactory> implements FactoryService {
}
