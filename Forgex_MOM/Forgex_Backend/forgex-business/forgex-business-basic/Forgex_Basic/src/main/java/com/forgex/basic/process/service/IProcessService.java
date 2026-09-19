package com.forgex.basic.process.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.process.domain.dto.ProcessDTO;
import com.forgex.basic.process.domain.entity.BasicProcess;
import com.forgex.basic.process.domain.param.ProcessPageParam;

import java.util.List;

/**
 * 工序主数据服务接口。
 * <p>
 * 提供工序的分页查询、列表查询、工段级联下拉、详情查询、新增、修改、删除和批量删除能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
public interface IProcessService extends IService<BasicProcess> {

    /**
     * 分页查询工序。
     *
     * @param param 分页查询参数，允许为 null
     * @return 工序分页结果
     */
    Page<ProcessDTO> page(ProcessPageParam param);

    /**
     * 查询工序列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 工序列表
     */
    List<ProcessDTO> list(ProcessPageParam param);

    /**
     * 根据工段 ID 查询工序列表（通常用于下拉）。
     *
     * @param workSectionId 工段 ID
     * @return 工序列表
     */
    List<ProcessDTO> listByWorkSection(Long workSectionId);

    /**
     * 根据主键查询工序详情。
     *
     * @param id 主键 ID
     * @return 工序详情，未找到返回 null
     */
    ProcessDTO getDetailById(Long id);

    /**
     * 新增工序。
     *
     * @param param 工序实体参数
     * @return 新增工序的主键 ID
     */
    Long create(BasicProcess param);

    /**
     * 更新工序。
     *
     * @param param 工序实体参数，必须携带主键
     * @return 是否处理成功
     */
    Boolean update(BasicProcess param);

    /**
     * 删除工序。
     * <p>
     * 工艺路线/BOM 引用校验待下游模块接入后启用，当前保留校验方法签名。
     * </p>
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    Boolean delete(Long id);

    /**
     * 批量删除工序。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    Boolean batchDelete(List<Long> ids);
}
