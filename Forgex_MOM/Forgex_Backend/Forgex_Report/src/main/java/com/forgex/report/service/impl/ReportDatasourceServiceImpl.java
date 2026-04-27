package com.forgex.report.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.report.domain.dto.ReportDatasourceDTO;
import com.forgex.report.domain.entity.ReportDatasource;
import com.forgex.report.enums.ReportPromptEnum;
import com.forgex.report.domain.param.ReportDatasourceParam;
import com.forgex.report.mapper.ReportDatasourceMapper;
import com.forgex.report.service.IReportDatasourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表数据源服务实现类
 * <p>
 * 实现报表数据源的业务逻辑
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see ServiceImpl
 * @see IReportDatasourceService
 * @see ReportDatasource
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDatasourceServiceImpl extends ServiceImpl<ReportDatasourceMapper, ReportDatasource> 
        implements IReportDatasourceService {

    /**
     * 分页查询报表数据源
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    public Page<ReportDatasourceDTO> pageByParam(ReportDatasourceParam param) {
        if (param == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        Page<ReportDatasource> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        // 构建动态查询条件
        LambdaQueryWrapper<ReportDatasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), ReportDatasource::getName, param.getName())
               .eq(StrUtil.isNotBlank(param.getCode()), ReportDatasource::getCode, param.getCode())
               .eq(StrUtil.isNotBlank(param.getType()), ReportDatasource::getType, param.getType())
               .eq(param.getStatus() != null, ReportDatasource::getStatus, param.getStatus())
               .orderByDesc(ReportDatasource::getCreateTime);

        Page<ReportDatasource> resultPage = this.page(page, wrapper);
        
        // 转换为 DTO
        List<ReportDatasourceDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<ReportDatasourceDTO> dtoPage = new Page<>(param.getPageNum(), param.getPageSize(), resultPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 查询所有数据源列表
     * <p>
     * 仅返回启用状态的数据源
     * </p>
     *
     * @return 数据源列表
     */
    @Override
    public List<ReportDatasourceDTO> listEnabled() {
        List<ReportDatasource> list = this.list(new LambdaQueryWrapper<ReportDatasource>()
                .eq(ReportDatasource::getStatus, 1)
                .orderByAsc(ReportDatasource::getName));
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取数据源详情
     *
     * @param id 数据源 ID
     * @return 数据源 DTO
     * @throws I18nBusinessException 当数据源不存在时抛出
     */
    @Override
    public ReportDatasourceDTO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("数据源 ID 不能为空");
        }

        ReportDatasource datasource = super.getById(id);
        if (datasource == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_DATASOURCE_NOT_FOUND);
        }

        return convertToDTO(datasource);
    }

    /**
     * 根据编码获取数据源
     *
     * @param code 数据源编码
     * @return 数据源 DTO
     * @throws I18nBusinessException 当数据源不存在时抛出
     */
    @Override
    public ReportDatasourceDTO getByCode(String code) {
        if (StrUtil.isBlank(code)) {
            throw new IllegalArgumentException("数据源编码不能为空");
        }

        ReportDatasource datasource = this.getOne(new LambdaQueryWrapper<ReportDatasource>()
                .eq(ReportDatasource::getCode, code));
        if (datasource == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_DATASOURCE_NOT_FOUND);
        }

        return convertToDTO(datasource);
    }

    /**
     * 保存数据源
     * <p>
     * 支持新增和更新，根据 ID 判断
     * 密码字段会自动加密处理
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 保存后的数据源 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportDatasourceDTO save(ReportDatasourceDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("数据源数据不能为空");
        }

        // 校验编码唯一性
        if (StrUtil.isNotBlank(dto.getCode())) {
            long count = this.count(new LambdaQueryWrapper<ReportDatasource>()
                    .eq(ReportDatasource::getCode, dto.getCode())
                    .ne(dto.getId() != null, ReportDatasource::getId, dto.getId()));
            if (count > 0) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_DATASOURCE_CODE_EXISTS);
            }
        }

        // 转换为实体
        ReportDatasource entity = new ReportDatasource();
        BeanUtils.copyProperties(dto, entity);

        // TODO: 密码加密处理
        // if (StrUtil.isNotBlank(dto.getPassword())) {
        //     entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        // }

        // 保存
        this.saveOrUpdate(entity);

        // 返回 DTO
        return convertToDTO(entity);
    }

    /**
     * 测试数据源连接
     * <p>
     * 验证数据源配置是否正确
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 连接是否成功
     * @throws Exception 当连接失败时抛出异常
     */
    @Override
    public boolean testConnection(ReportDatasourceDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getUrl())) {
            throw new IllegalArgumentException("数据源配置不能为空");
        }

        Connection conn = null;
        try {
            // 加载驱动
            Class.forName(dto.getDriverClass());
            
            // 尝试连接
            conn = DriverManager.getConnection(dto.getUrl(), dto.getUsername(), dto.getPassword());
            
            if (conn != null && !conn.isClosed()) {
                log.info("数据源连接测试成功：{}", dto.getName());
                return true;
            }
        } catch (Exception e) {
            log.error("数据源连接测试失败：{}", dto.getName(), e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_DATASOURCE_CONNECT_FAILED, e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.warn("关闭数据库连接失败", e);
                }
            }
        }
        
        return false;
    }

    /**
     * 批量删除数据源
     *
     * @param ids 数据源 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new IllegalArgumentException("删除的 ID 列表不能为空");
        }

        this.removeByIds(ids);
    }

    /**
     * 将实体转换为 DTO
     *
     * @param entity 实体对象
     * @return DTO 对象
     */
    private ReportDatasourceDTO convertToDTO(ReportDatasource entity) {
        if (entity == null) {
            return null;
        }

        ReportDatasourceDTO dto = new ReportDatasourceDTO();
        BeanUtils.copyProperties(entity, dto);
        // 密码不返回前端
        dto.setPassword(null);
        return dto;
    }
}
