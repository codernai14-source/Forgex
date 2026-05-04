package com.forgex.basic.material.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.entity.BasicUnitType;
import com.forgex.basic.material.domain.param.UnitConversionSaveParam;
import com.forgex.basic.material.domain.param.UnitPageParam;
import com.forgex.basic.material.domain.param.UnitTypeParam;
import com.forgex.basic.material.domain.vo.UnitConversionVO;
import com.forgex.basic.material.domain.vo.UnitTypeTreeVO;
import com.forgex.basic.material.domain.vo.UnitVO;
import com.forgex.common.api.dto.UnitConvertRequestDTO;

import java.util.List;

/**
 * 计量单位服务接口。
 * <p>
 * 提供计量单位类型树、单位主数据、换算关系和公共换算能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface IBasicUnitService extends IService<BasicUnit> {

    /**
     * 分页查询计量单位。
     *
     * @param param 查询参数
     * @return 分页数据
     */
    Page<UnitVO> pageUnits(UnitPageParam param);

    /**
     * 查询计量单位列表。
     *
     * @param param 查询参数
     * @return 计量单位列表
     */
    List<UnitVO> listUnits(UnitPageParam param);

    /**
     * 查询计量单位详情。
     *
     * @param id 计量单位 ID
     * @return 计量单位详情
     */
    UnitVO detail(Long id);

    /**
     * 创建计量单位。
     *
     * @param unit 计量单位
     * @return 主键 ID
     */
    Long create(BasicUnit unit);

    /**
     * 更新计量单位。
     *
     * @param unit 计量单位
     * @return 是否成功
     */
    Boolean updateUnit(BasicUnit unit);

    /**
     * 删除计量单位。
     *
     * @param id 计量单位 ID
     * @return 是否成功
     */
    Boolean deleteUnit(Long id);

    /**
     * 批量删除计量单位。
     *
     * @param ids 计量单位 ID 列表
     * @return 是否成功
     */
    Boolean batchDeleteUnits(List<Long> ids);

    /**
     * 查询计量单位类型树。
     *
     * @return 类型树
     */
    List<UnitTypeTreeVO> typeTree();

    /**
     * 查询计量单位类型详情。
     *
     * @param id 类型 ID
     * @return 类型详情
     */
    BasicUnitType typeDetail(Long id);

    /**
     * 创建计量单位类型。
     *
     * @param param 类型参数
     * @return 主键 ID
     */
    Long createType(UnitTypeParam param);

    /**
     * 更新计量单位类型。
     *
     * @param param 类型参数
     * @return 是否成功
     */
    Boolean updateType(UnitTypeParam param);

    /**
     * 删除计量单位类型。
     *
     * @param id 类型 ID
     * @return 是否成功
     */
    Boolean deleteType(Long id);

    /**
     * 查询换算关系。
     *
     * @param unitId 源计量单位 ID
     * @return 换算关系列表
     */
    List<UnitConversionVO> listConversions(Long unitId);

    /**
     * 保存换算关系。
     *
     * @param param 保存参数
     * @return 是否成功
     */
    Boolean saveConversions(UnitConversionSaveParam param);

    /**
     * 删除换算关系。
     *
     * @param id 换算关系 ID
     * @return 是否成功
     */
    Boolean deleteConversion(Long id);

    /**
     * 换算实体中的指定数字字段。
     *
     * @param request 换算请求
     * @return 换算结果
     */
    JSONObject convertFields(UnitConvertRequestDTO request);
}
