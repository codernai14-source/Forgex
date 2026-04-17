package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.dto.LabelBindingDTO;
import com.forgex.basic.label.domain.entity.LabelBinding;
import com.forgex.basic.label.domain.param.LabelBindingSaveParam;
import com.forgex.basic.label.domain.vo.BindingVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签绑定 Service 接口
 * <p>
 * 提供标签绑定管理相关的业务操作，包括：
 * 1. 按物料绑定模板
 * 2. 按供应商绑定模板
 * 3. 按客户绑定模板
 * 4. 模板匹配逻辑（多级降级策略）
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface LabelBindingService extends IService<LabelBinding> {

    /**
     * 分页查询绑定关系列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param templateId 模板 ID（可选）
     * @param bindingType 绑定类型（可选）
     * @param bindingValue 绑定值（可选）
     * @param factoryId 工厂 ID（可选）
     * @param tenantId 租户 ID
     * @return 绑定关系分页数据
     */
    IPage<BindingVO> pageBindings(Integer pageNum, Integer pageSize, Long templateId,String templateCode,
                                  String bindingType, String bindingValue, Long factoryId, Long tenantId);

    /**
     * 新增绑定关系
     *
     * @param param 绑定保存参数
     * @param tenantId 租户 ID
     * @return 绑定 ID
     */
    Long addBinding(LabelBindingSaveParam param, Long tenantId);

    /**
     * 更新绑定关系
     *
     * @param id 绑定 ID
     * @param priority 优先级
     * @param factoryId 工厂 ID
     * @param tenantId 租户 ID
     */
    void updateBinding(Long id, Integer priority, Long factoryId, Long tenantId);

    /**
     * 删除绑定关系
     *
     * @param id 绑定 ID
     * @param tenantId 租户 ID
     */
    void deleteBinding(Long id, Long tenantId);

    /**
     * 根据绑定条件匹配模板
     * <p>
     * 匹配优先级：
     * 1. 按物料精确匹配
     * 2. 按供应商匹配
     * 3. 按客户匹配
     * 4. 返回默认模板
     * </p>
     *
     * @param factoryId 工厂 ID
     * @param templateType 模板类型
     * @param materialId 物料 ID（可选）
     * @param supplierId 供应商 ID（可选）
     * @param customerId 客户 ID（可选）
     * @param tenantId 租户 ID
     * @return 匹配的模板 ID，未找到则抛出异常
     */
    Long matchTemplate(Long factoryId, String templateType, Long materialId,
                       Long supplierId, Long customerId, Long tenantId);

    @Transactional(rollbackFor = Exception.class)
    void saveOrUpdateBinding(LabelBinding binding);
}
