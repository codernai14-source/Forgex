package com.forgex.basic.supplier.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.mapper.BasicSupplierMapper;
import com.forgex.basic.supplier.service.ISupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupplierServiceImpl extends ServiceImpl<BasicSupplierMapper, BasicSupplier> implements ISupplierService {
}
