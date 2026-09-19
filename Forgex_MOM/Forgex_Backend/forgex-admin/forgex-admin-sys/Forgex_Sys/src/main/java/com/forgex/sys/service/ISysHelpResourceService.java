package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysHelpContactDTO;
import com.forgex.sys.domain.dto.SysHelpPageResolveDTO;
import com.forgex.sys.domain.dto.SysHelpResourceDTO;
import com.forgex.sys.domain.dto.SysHelpUploadResultDTO;
import com.forgex.sys.domain.entity.SysHelpResource;
import com.forgex.sys.domain.param.SysHelpResourceListParam;
import com.forgex.sys.domain.param.SysHelpResourcePageParam;
import com.forgex.sys.domain.param.SysHelpResourceSaveParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 帮助资源服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see SysHelpResource
 */
public interface ISysHelpResourceService extends IService<SysHelpResource> {

    /**
     * 管理端分页查询。
     *
     * @param page 分页对象
     * @param param 查询条件
     * @return 分页结果
     */
    IPage<SysHelpResourceDTO> pageResources(Page<SysHelpResource> page, SysHelpResourcePageParam param);

    /**
     * 新增帮助资源。
     *
     * @param param 保存参数
     * @return 新记录 ID
     */
    Long createResource(SysHelpResourceSaveParam param);

    /**
     * 更新帮助资源。
     *
     * @param param 保存参数
     */
    void updateResource(SysHelpResourceSaveParam param);

    /**
     * 逻辑删除帮助资源。
     *
     * @param id 主键
     */
    void deleteResource(Long id);

    /**
     * 批量逻辑删除帮助资源。
     *
     * @param ids 主键列表
     */
    void deleteResources(List<Long> ids);

    /**
     * 启停帮助资源。
     *
     * @param id 主键
     * @param status 目标状态
     */
    void changeStatus(Long id, Integer status);

    /**
     * 上传帮助文件并返回元数据。
     *
     * @param file 文件
     * @param docType 文档类型或联系模块编码
     * @return 上传结果
     */
    SysHelpUploadResultDTO upload(MultipartFile file, String docType);

    /**
     * 按当前页解析启用中的帮助资源。
     *
     * @param param 解析参数
     * @return 覆盖解析结果
     */
    SysHelpPageResolveDTO listForPage(SysHelpResourceListParam param);

    /**
     * 读取联系方式。
     *
     * @return 联系配置，未配置时返回空对象
     */
    SysHelpContactDTO getContact();

    /**
     * 保存联系方式。
     *
     * @param contact 联系配置
     */
    void saveContact(SysHelpContactDTO contact);
}
