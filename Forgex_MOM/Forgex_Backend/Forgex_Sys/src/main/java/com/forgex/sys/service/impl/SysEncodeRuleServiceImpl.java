/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.common.util.RedisHelper;
import com.forgex.sys.domain.dto.EncodeRuleDTO;
import com.forgex.sys.domain.dto.EncodeRuleDetailDTO;
import com.forgex.sys.domain.entity.SysEncodeRule;
import com.forgex.sys.domain.entity.SysEncodeRuleDetail;
import com.forgex.sys.domain.param.EncodeRulePageParam;
import com.forgex.sys.domain.param.EncodeRuleSaveParam;
import com.forgex.sys.domain.param.EncodeRuleDetailSaveParam;
import com.forgex.sys.domain.vo.EncodeRuleVO;
import com.forgex.sys.domain.vo.EncodeRuleDetailVO;
import com.forgex.sys.mapper.SysEncodeRuleMapper;
import com.forgex.sys.mapper.SysEncodeRuleDetailMapper;
import com.forgex.sys.service.ISysEncodeRuleService;
import com.forgex.sys.enums.SysPromptEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.ExpirationOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 编码规则服务实现类
 * <p>
 * 提供编码规则的业务逻辑处理，包括编码规则的增删改查、编码生成等核心功能。
 * </p>
 * <p>核心功能：</p>
 * <ul>
 *   <li>编码规则的分页查询、详情查询</li>
 *   <li>编码规则的新增、更新、删除</li>
 *   <li>基于 Redis 的序列号原子递增编码生成</li>
 *   <li>租户隔离的数据访问控制</li>
 * </ul>
 * <p>技术特点：</p>
 * <ul>
 *   <li>使用 Redis INCR 命令保证序列号原子性</li>
 *   <li>支持按重置周期（每日/每月/每年/从不）重置序列号</li>
 *   <li>使用事务保证主表和明细表数据一致性</li>
 *   <li>支持字典翻译，返回前端友好的展示数据</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-10
 * @see ISysEncodeRuleService
 * @see DS 多数据源注解
 */
@Service
@Slf4j
@DS("common")
@RequiredArgsConstructor
public class SysEncodeRuleServiceImpl extends ServiceImpl<SysEncodeRuleMapper, SysEncodeRule> implements ISysEncodeRuleService {

    /**
     * 编码规则明细 Mapper
     */
    private final SysEncodeRuleDetailMapper encodeRuleDetailMapper;
    
    /**
     * Redis 工具类
     */
    private final RedisHelper redisHelper;
    
    /**
     * Redis StringTemplate，用于原子递增操作
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Key 前缀：编码规则序列号
     */
    private static final String REDIS_KEY_SERIAL_PREFIX = "encode:serial:";

    private static final Long PUBLIC_TENANT_ID = 0L;
    
    /**
     * 分页查询编码规则列表
     * <p>根据查询条件分页查询编码规则列表，支持按规则代码、规则名称、模块等条件过滤。</p>
     * 
     * @param page 分页参数
     * @param param 查询条件
     * @return 分页编码规则 DTO 列表
     * @see #buildQueryWrapper(EncodeRulePageParam)
     * @see #convertToDTO(SysEncodeRule)
     */
    @Override
    public IPage<EncodeRuleDTO> pageEncodeRules(Page<SysEncodeRule> page, EncodeRulePageParam param) {
        return runWithTenantIgnore(() -> {
            LambdaQueryWrapper<SysEncodeRule> wrapper = buildQueryWrapper(param);
            Page<SysEncodeRule> resultPage = this.page(page, wrapper);
            return resultPage.convert(this::convertToDTO);
        });
    }
    
    /**
     * 查询编码规则列表
     * <p>根据查询条件查询所有符合条件的编码规则列表。</p>
     * 
     * @param param 查询条件
     * @return 编码规则 DTO 列表
     * @see #buildQueryWrapper(EncodeRulePageParam)
     * @see #convertToDTO(SysEncodeRule)
     */
    @Override
    public List<EncodeRuleDTO> listEncodeRules(EncodeRulePageParam param) {
        return runWithTenantIgnore(() -> {
            LambdaQueryWrapper<SysEncodeRule> wrapper = buildQueryWrapper(param);
            List<SysEncodeRule> rules = this.list(wrapper);
            return rules.stream().map(this::convertToDTO).collect(Collectors.toList());
        });
    }
    
    /**
     * 根据 ID 获取编码规则详情
     * <p>根据编码规则 ID 查询详细信息，包含主表和明细表的完整数据。</p>
     * 
     * @param id 编码规则 ID
     * @return 编码规则详情 DTO
     * @see #convertToDTO(SysEncodeRule)
     * @see #queryDetailsByRuleId(Long)
     */
    @Override
    public EncodeRuleDTO getEncodeRuleById(Long id) {
        return runWithTenantIgnore(() -> {
            SysEncodeRule rule = this.getById(id);
            if (rule == null || !isCurrentOrPublicTenant(rule.getTenantId())) {
                return null;
            }
            EncodeRuleDTO dto = convertToDTO(rule);
            List<EncodeRuleDetailDTO> detailList = queryDetailsByRuleId(id);
            dto.setDetailList(detailList);
            return dto;
        });
    }
    
    /**
     * 根据 ID 获取编码规则详情（VO）
     * <p>根据编码规则 ID 查询详细信息，并将结果转换为 VO 返回给前端。</p>
     * 
     * @param id 编码规则 ID
     * @return 编码规则详情 VO
     * @see #getEncodeRuleById(Long)
     * @see #convertToVO(EncodeRuleDTO)
     */
    @Override
    public EncodeRuleVO getEncodeRuleVOById(Long id) {
        EncodeRuleDTO dto = getEncodeRuleById(id);
        if (dto == null) {
            return null;
        }
        return convertToVO(dto);
    }
    
    /**
     * 保存编码规则（新增或更新）
     * <p>根据参数中是否包含 ID 来判断是新增还是更新操作。</p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验参数合法性</li>
     *   <li>检查规则代码唯一性</li>
     *   <li>保存或更新主表数据</li>
     *   <li>保存或更新明细数据（先删除后新增）</li>
     * </ol>
     * 
     * @param param 编码规则保存参数
     * @return 保存后的编码规则 ID
     * @throws I18nBusinessException 当规则代码已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveEncodeRule(EncodeRuleSaveParam param) {
        return runWithTenantIgnore(() -> {
            SysEncodeRule existRule = null;
            if (param.getId() != null) {
                existRule = getAccessibleRuleById(param.getId());
                if (existRule == null) {
                    throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_NOT_FOUND);
                }
            }

            // 校验规则代码唯一性
            validateRuleCodeUnique(param.getRuleCode(), param.getId());

            // 保存或更新主表
            SysEncodeRule rule = new SysEncodeRule();
            BeanUtils.copyProperties(param, rule);

            if (existRule == null) {
                rule.setTenantId(resolveTenantId());
                if (rule.getIsEnabled() == null) {
                    rule.setIsEnabled(true);
                }
                if (rule.getSortOrder() == null) {
                    rule.setSortOrder(0);
                }
            } else {
                rule.setTenantId(existRule.getTenantId());
                if (rule.getIsEnabled() == null) {
                    rule.setIsEnabled(existRule.getIsEnabled());
                }
                if (rule.getSortOrder() == null) {
                    rule.setSortOrder(existRule.getSortOrder());
                }
            }

            this.saveOrUpdate(rule);
            Long ruleId = rule.getId();

            // 保存或更新明细数据
            if (!CollectionUtils.isEmpty(param.getDetailList())) {
                // 先删除旧的明细数据
                deleteDetailsByRuleId(ruleId);

                // 再新增明细数据
                List<SysEncodeRuleDetail> detailList = new ArrayList<>();
                for (EncodeRuleDetailSaveParam detailParam : param.getDetailList()) {
                    SysEncodeRuleDetail detail = new SysEncodeRuleDetail();
                    BeanUtils.copyProperties(detailParam, detail);
                    detail.setId(null);
                    detail.setRuleId(ruleId);
                    detail.setTenantId(rule.getTenantId());
                    detailList.add(detail);
                }
                insertDetails(detailList);
            }

            return ruleId;
        });
    }
    
    /**
     * 批量插入明细数据
     *
     * @param detailList 明细列表
     */
    private void insertDetails(List<SysEncodeRuleDetail> detailList) {
        for (SysEncodeRuleDetail detail : detailList) {
            encodeRuleDetailMapper.insert(detail);
        }
    }
    
    /**
     * 删除编码规则
     * <p>根据编码规则 ID 删除规则及其关联的明细数据，并清理 Redis 缓存。</p>
     * 
     * @param id 编码规则 ID
     * @throws I18nBusinessException 当规则不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEncodeRule(Long id) {
        SysEncodeRule rule = getAccessibleRuleById(id);
        if (rule == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_NOT_FOUND);
        }
        
        runWithTenantIgnore(() -> {
            // 删除明细数据
            deleteDetailsByRuleId(id);

            // 删除主表数据
            this.removeById(id);

            // 清理 Redis 缓存
            clearRedisCache(rule.getRuleCode());

            return null;
        });

        log.info("删除编码规则成功，规则代码：{}", rule.getRuleCode());
    }
    
    /**
     * 批量删除编码规则
     * <p>批量删除多个编码规则及其关联的明细数据。</p>
     * 
     * @param ids 编码规则 ID 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteEncodeRules(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.DELETE_IDS_REQUIRED);
        }
        
        for (Long id : ids) {
            deleteEncodeRule(id);
        }
        
        log.info("批量删除编码规则成功，删除数量：{}", ids.size());
    }
    
    /**
     * 根据规则代码生成编码
     * <p>根据指定的规则代码生成唯一的业务编码，支持序列号原子递增。</p>
     * <p>编码格式：前缀 + 日期格式 + 序列号</p>
     * <p>示例：SO202604100001</p>
     * 
     * @param ruleCode 规则代码
     * @return 生成的业务编码
     * @throws I18nBusinessException 当规则代码不存在或已禁用时抛出
     */
    @Override
    public String generateCode(String ruleCode) {
        // 忽略租户隔离，查询公共编码规则
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            if (!StringUtils.hasText(ruleCode)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_CODE_EMPTY);
            }
            
            // 查询编码规则
            LambdaQueryWrapper<SysEncodeRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysEncodeRule::getRuleCode, ruleCode);
            wrapper.eq(SysEncodeRule::getIsEnabled, true);
            addCurrentAndPublicTenantCondition(wrapper);
            wrapper.orderByDesc(SysEncodeRule::getTenantId)
                    .orderByAsc(SysEncodeRule::getSortOrder)
                    .last("LIMIT 1");
            SysEncodeRule rule = this.getOne(wrapper);
            
            if (rule == null) {
                log.warn("编码规则不存在或已禁用：{}", ruleCode);
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_NOT_FOUND_OR_DISABLED, ruleCode);
            }
            
            // 构建 Redis Key
            String redisKey = buildRedisKey(rule);
            
            // 使用 Redis INCR 原子递增获取序列号
            Long serial = stringRedisTemplate.opsForValue().increment(redisKey);
            
            // 如果是第一个序列号，设置过期时间
            if (serial == 1) {
                setRedisKeyExpire(rule, redisKey);
            }
            
            // 格式化序列号
            String formattedSerial = formatSerial(serial, rule.getSerialLength());
            
            // 构建完整编码
            StringBuilder code = new StringBuilder();
            
            // 添加前缀
            if (StringUtils.hasText(rule.getPrefix())) {
                code.append(rule.getPrefix());
            }
            
            // 添加日期格式
            if (StringUtils.hasText(rule.getDateFormat())) {
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(rule.getDateFormat()));
                code.append(dateStr);
            }
            
            // 添加序列号
            code.append(formattedSerial);
            
            String generatedCode = code.toString();
            log.info("生成编码成功：规则代码={}, 生成编码={}", ruleCode, generatedCode);
            
            return generatedCode;
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }
    
    /**
     * 启用编码规则
     * <p>将指定编码规则的状态设置为启用。</p>
     * 
     * @param id 编码规则 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableEncodeRule(Long id) {
        SysEncodeRule rule = getAccessibleRuleById(id);
        if (rule == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_NOT_FOUND);
        }
        
        rule.setIsEnabled(true);
        runWithTenantIgnore(() -> {
            this.updateById(rule);
            return null;
        });
        
        log.info("启用编码规则成功：{}", rule.getRuleCode());
    }
    
    /**
     * 禁用编码规则
     * <p>将指定编码规则的状态设置为禁用。</p>
     * 
     * @param id 编码规则 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableEncodeRule(Long id) {
        SysEncodeRule rule = getAccessibleRuleById(id);
        if (rule == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_NOT_FOUND);
        }
        
        rule.setIsEnabled(false);
        runWithTenantIgnore(() -> {
            this.updateById(rule);
            return null;
        });
        
        log.info("禁用编码规则成功：{}", rule.getRuleCode());
    }
    
    /**
     * 构建查询条件
     * <p>根据查询参数构建 LambdaQueryWrapper 查询条件。</p>
     * 
     * @param param 查询参数
     * @return LambdaQueryWrapper 查询条件
     */
    private LambdaQueryWrapper<SysEncodeRule> buildQueryWrapper(EncodeRulePageParam param) {
        LambdaQueryWrapper<SysEncodeRule> wrapper = new LambdaQueryWrapper<>();
        
        // 租户隔离
        addCurrentAndPublicTenantCondition(wrapper);
        
        if (param != null) {
            // 规则代码模糊查询
            wrapper.like(StringUtils.hasText(param.getRuleCode()), 
                SysEncodeRule::getRuleCode, param.getRuleCode());
            
            // 规则名称模糊查询
            wrapper.like(StringUtils.hasText(param.getRuleName()), 
                SysEncodeRule::getRuleName, param.getRuleName());
            
            // 模块精确查询
            wrapper.eq(StringUtils.hasText(param.getModule()), 
                SysEncodeRule::getModule, param.getModule());
            
            // 是否启用精确查询
            wrapper.eq(param.getIsEnabled() != null, 
                SysEncodeRule::getIsEnabled, param.getIsEnabled());
            
            // 重置周期精确查询
            wrapper.eq(StringUtils.hasText(param.getResetCycle()), 
                SysEncodeRule::getResetCycle, param.getResetCycle());
        }
        
        // 按排序号升序，创建时间降序
        wrapper.orderByAsc(SysEncodeRule::getSortOrder)
               .orderByDesc(SysEncodeRule::getCreateTime);
        
        return wrapper;
    }

    private <T> T runWithTenantIgnore(Supplier<T> action) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return action.get();
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }

    private void addCurrentAndPublicTenantCondition(LambdaQueryWrapper<SysEncodeRule> wrapper) {
        Long tenantId = TenantContext.get();
        if (tenantId == null || PUBLIC_TENANT_ID.equals(tenantId)) {
            wrapper.eq(SysEncodeRule::getTenantId, PUBLIC_TENANT_ID);
            return;
        }
        wrapper.in(SysEncodeRule::getTenantId, Arrays.asList(PUBLIC_TENANT_ID, tenantId));
    }

    private boolean isCurrentOrPublicTenant(Long tenantId) {
        if (PUBLIC_TENANT_ID.equals(tenantId)) {
            return true;
        }
        Long currentTenantId = TenantContext.get();
        return currentTenantId != null && currentTenantId.equals(tenantId);
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? PUBLIC_TENANT_ID : tenantId;
    }

    private SysEncodeRule getAccessibleRuleById(Long id) {
        return runWithTenantIgnore(() -> {
            SysEncodeRule rule = this.getById(id);
            if (rule == null || !isCurrentOrPublicTenant(rule.getTenantId())) {
                return null;
            }
            return rule;
        });
    }
    
    /**
     * 校验规则代码唯一性
     * <p>检查规则代码是否已存在，排除当前规则（更新场景）。</p>
     * 
     * @param ruleCode 规则代码
     * @param excludeId 排除的 ID（更新时传入）
     * @throws I18nBusinessException 当规则代码已存在时抛出
     */
    private void validateRuleCodeUnique(String ruleCode, Long excludeId) {
        if (!StringUtils.hasText(ruleCode)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_CODE_EMPTY);
        }
        
        LambdaQueryWrapper<SysEncodeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEncodeRule::getRuleCode, ruleCode);
        addCurrentAndPublicTenantCondition(wrapper);
        
        // 更新时排除当前规则
        if (excludeId != null) {
            wrapper.ne(SysEncodeRule::getId, excludeId);
        }
        
        Long count = runWithTenantIgnore(() -> this.count(wrapper));
        if (count > 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ENCODE_RULE_CODE_EXISTS, ruleCode);
        }
    }
    
    /**
     * 根据规则 ID 查询明细列表
     * 
     * @param ruleId 规则 ID
     * @return 明细 DTO 列表
     */
    private List<EncodeRuleDetailDTO> queryDetailsByRuleId(Long ruleId) {
        LambdaQueryWrapper<SysEncodeRuleDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEncodeRuleDetail::getRuleId, ruleId);
        wrapper.orderByAsc(SysEncodeRuleDetail::getSegmentOrder);
        
        List<SysEncodeRuleDetail> details = encodeRuleDetailMapper.selectList(wrapper);
        return details.stream().map(this::convertToDetailDTO).collect(Collectors.toList());
    }
    
    /**
     * 根据规则 ID 删除明细数据
     * 
     * @param ruleId 规则 ID
     */
    private void deleteDetailsByRuleId(Long ruleId) {
        LambdaQueryWrapper<SysEncodeRuleDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEncodeRuleDetail::getRuleId, ruleId);
        encodeRuleDetailMapper.delete(wrapper);
    }
    
    /**
     * 构建 Redis Key
     * <p>根据重置周期生成不同的 Redis Key。</p>
     * 
     * @param rule 编码规则
     * @return Redis Key
     */
    private String buildRedisKey(SysEncodeRule rule) {
        StringBuilder key = new StringBuilder(REDIS_KEY_SERIAL_PREFIX);
        key.append(rule.getRuleCode());
        
        // 根据重置周期添加时间维度
        String resetCycle = rule.getResetCycle();
        if ("DAILY".equals(resetCycle)) {
            key.append(":").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else if ("MONTHLY".equals(resetCycle)) {
            key.append(":").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        } else if ("YEARLY".equals(resetCycle)) {
            key.append(":").append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")));
        }
        // NEVER 或不设置则不添加时间维度
        
        return key.toString();
    }
    
    /**
     * 设置 Redis Key 过期时间
     * <p>根据重置周期设置不同的过期时间。</p>
     * 
     * @param rule 编码规则
     * @param redisKey Redis Key
     */
    private void setRedisKeyExpire(SysEncodeRule rule, String redisKey) {
        long expireSeconds;
        
        switch (rule.getResetCycle()) {
            case "DAILY":
                // 每日重置：设置到当天 23:59:59
                expireSeconds = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    java.time.LocalDate.now().atTime(23, 59, 59).plusSeconds(1)
                ).getSeconds();
                break;
            case "MONTHLY":
                // 每月重置：设置到当月最后一天 23:59:59
                expireSeconds = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    java.time.LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1).atTime(23, 59, 59).plusSeconds(1)
                ).getSeconds();
                break;
            case "YEARLY":
                // 每年重置：设置到当年最后一天 23:59:59
                expireSeconds = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    java.time.LocalDate.now().withMonth(12).withDayOfMonth(31).atTime(23, 59, 59).plusSeconds(1)
                ).getSeconds();
                break;
            default:
                // 从不重置：永久不过期
                return;
        }
        
        if (expireSeconds > 0) {
            stringRedisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.keyCommands().expire(
                    redisKey.getBytes(StandardCharsets.UTF_8),
                    expireSeconds,
                    ExpirationOptions.Condition.ALWAYS)
            );
        }
    }
    
    /**
     * 格式化序列号
     * <p>将序列号格式化为指定长度的字符串，不足补零。</p>
     * 
     * @param serial 序列号
     * @param length 长度
     * @return 格式化后的序列号
     */
    private String formatSerial(Long serial, Integer length) {
        if (length == null || length <= 0) {
            length = 4; // 默认长度为 4
        }
        
        String serialStr = String.valueOf(serial);
        if (serialStr.length() >= length) {
            return serialStr;
        }
        
        // 不足长度时前面补零
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - serialStr.length(); i++) {
            sb.append("0");
        }
        sb.append(serialStr);
        return sb.toString();
    }
    
    /**
     * 清理 Redis 缓存
     * <p>删除编码规则相关的 Redis 缓存。</p>
     * 
     * @param ruleCode 规则代码
     */
    private void clearRedisCache(String ruleCode) {
        String redisKey = REDIS_KEY_SERIAL_PREFIX + ruleCode;
        // 删除所有可能的 Redis Key（带时间维度的）
        // 实际应用中可能需要使用 SCAN 命令匹配删除
        redisHelper.delete(redisKey);
    }
    
    /**
     * 实体转 DTO
     * <p>将编码规则实体转换为 DTO。</p>
     * 
     * @param rule 编码规则实体
     * @return 编码规则 DTO
     */
    private EncodeRuleDTO convertToDTO(SysEncodeRule rule) {
        EncodeRuleDTO dto = new EncodeRuleDTO();
        BeanUtils.copyProperties(rule, dto);
        return dto;
    }
    
    /**
     * 明细实体转 DTO
     * <p>将编码规则明细实体转换为 DTO。</p>
     * 
     * @param detail 明细实体
     * @return 明细 DTO
     */
    private EncodeRuleDetailDTO convertToDetailDTO(SysEncodeRuleDetail detail) {
        EncodeRuleDetailDTO dto = new EncodeRuleDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        return dto;
    }
    
    /**
     * DTO 转 VO
     * <p>将编码规则 DTO 转换为 VO，添加字典翻译等展示字段。</p>
     * 
     * @param dto 编码规则 DTO
     * @return 编码规则 VO
     */
    private EncodeRuleVO convertToVO(EncodeRuleDTO dto) {
        EncodeRuleVO vo = new EncodeRuleVO();
        BeanUtils.copyProperties(dto, vo);
        
        // TODO: 添加字典翻译逻辑
        // vo.setResetCycleText(translateResetCycle(dto.getResetCycle()));
        // vo.setIsEnabledText(dto.getIsEnabled() ? "启用" : "禁用");
        
        // 转换明细列表
        if (!CollectionUtils.isEmpty(dto.getDetailList())) {
            List<EncodeRuleDetailVO> detailVoList = dto.getDetailList().stream()
                .map(this::convertDetailToVO)
                .collect(Collectors.toList());
            vo.setDetailList(detailVoList);
        }
        
        return vo;
    }
    
    /**
     * 明细 DTO 转 VO
     * <p>将编码规则明细 DTO 转换为 VO。</p>
     * 
     * @param dto 明细 DTO
     * @return 明细 VO
     */
    private EncodeRuleDetailVO convertDetailToVO(EncodeRuleDetailDTO dto) {
        EncodeRuleDetailVO vo = new EncodeRuleDetailVO();
        BeanUtils.copyProperties(dto, vo);
        
        // TODO: 添加字典翻译逻辑
        // vo.setSegmentTypeText(translateSegmentType(dto.getSegmentType()));
        // vo.setIsRequiredText(dto.getIsRequired() ? "是" : "否");
        
        return vo;
    }
}
