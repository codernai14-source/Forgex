package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.config.ConfigService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.config.FileUploadConfig;
import com.forgex.sys.domain.config.HelpUploadConfig;
import com.forgex.sys.domain.dto.SysHelpContactDTO;
import com.forgex.sys.domain.dto.SysHelpPageResolveDTO;
import com.forgex.sys.domain.dto.SysHelpResourceDTO;
import com.forgex.sys.domain.dto.SysHelpUploadResultDTO;
import com.forgex.sys.domain.entity.SysHelpResource;
import com.forgex.sys.domain.param.SysHelpResourceListParam;
import com.forgex.sys.domain.param.SysHelpResourcePageParam;
import com.forgex.sys.domain.param.SysHelpResourceSaveParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysHelpResourceMapper;
import com.forgex.sys.service.FileService;
import com.forgex.sys.service.ISysHelpResourceService;
import com.forgex.sys.service.help.HelpResourceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 帮助资源服务实现。
 * <p>
 * 管理端负责元数据维护；运行时 {@link #listForPage(SysHelpResourceListParam)}
 * 按菜单精确匹配、最长前缀、全局回退解析文档。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see ISysHelpResourceService
 * @see HelpResourceSupport
 */
@Service
@RequiredArgsConstructor
public class SysHelpResourceServiceImpl extends ServiceImpl<SysHelpResourceMapper, SysHelpResource>
        implements ISysHelpResourceService {

    private static final String KEY_HELP_CONTACT = "system.help.contact";
    private static final String KEY_HELP_UPLOAD = "system.help.upload";
    private static final String KEY_FILE_UPLOAD = "file.upload.settings";
    private static final int BAD_REQUEST = 400;

    private final ConfigService configService;
    private final FileService fileService;

    /**
     * 分页查询当前租户帮助资源。
     *
     * @param page 分页对象
     * @param param 查询条件
     * @return 分页 DTO
     */
    @Override
    public IPage<SysHelpResourceDTO> pageResources(Page<SysHelpResource> page, SysHelpResourcePageParam param) {
        SysHelpResourcePageParam condition = param == null ? new SysHelpResourcePageParam() : param;
        Page<SysHelpResource> result = page(page, buildPageWrapper(condition));
        Page<SysHelpResourceDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toDto).toList());
        return dtoPage;
    }

    /**
     * 新增帮助资源。
     *
     * @param param 保存参数
     * @return 新记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResource(SysHelpResourceSaveParam param) {
        SysHelpResource entity = new SysHelpResource();
        fillEntity(entity, param, true);
        save(entity);
        return entity.getId();
    }

    /**
     * 更新帮助资源。
     *
     * @param param 保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(SysHelpResourceSaveParam param) {
        if (param == null || param.getId() == null) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_RESOURCE_NOT_FOUND);
        }
        SysHelpResource entity = requireResource(param.getId());
        fillEntity(entity, param, false);
        updateById(entity);
    }

    /**
     * 逻辑删除帮助资源。
     *
     * @param id 主键
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long id) {
        requireResource(id);
        removeById(id);
    }

    /**
     * 批量逻辑删除帮助资源。
     *
     * @param ids 主键列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResources(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_RESOURCE_NOT_FOUND);
        }
        for (Long id : ids) {
            if (id != null) {
                deleteResource(id);
            }
        }
    }

    /**
     * 启停帮助资源。
     *
     * @param id 主键
     * @param status 目标状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        SysHelpResource entity = requireResource(id);
        entity.setStatus(status != null && status == 1 ? 1 : 0);
        updateById(entity);
    }

    /**
     * 上传帮助文件。
     *
     * @param file 文件
     * @param docType 文档类型或联系模块编码
     * @return 上传结果
     */
    @Override
    public SysHelpUploadResultDTO upload(MultipartFile file, String docType) {
        if (file == null || file.isEmpty()) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_EMPTY);
        }
        String type = StringUtils.hasText(docType) ? docType.trim() : HelpResourceSupport.DOC_MANUAL;
        String ext = HelpResourceSupport.normalizeExt(file.getOriginalFilename(), null);
        if (!HelpResourceSupport.isAllowedExt(type, ext)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_EXT_INVALID);
        }
        long maxBytes = resolveMaxBytes(type);
        if (file.getSize() > maxBytes) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_SIZE_EXCEEDED);
        }
        try {
            // 视频走独立体积上限，避免被全局 20MB 配置拦截。
            Long overrideMb = HelpResourceSupport.DOC_VIDEO.equalsIgnoreCase(type)
                    ? resolveVideoMaxSizeMb()
                    : null;
            String url = fileService.upload(
                    file,
                    HelpResourceSupport.resolveModuleCode(type),
                    resolveModuleName(type),
                    overrideMb
            );
            SysHelpUploadResultDTO result = new SysHelpUploadResultDTO();
            result.setFileUrl(url);
            result.setFileName(file.getOriginalFilename());
            result.setFileExt(ext);
            result.setFileSize(file.getSize());
            result.setContentType(file.getContentType());
            return result;
        } catch (IOException ex) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 按当前页解析启用文档：菜单精确匹配 → 最长前缀 → 全局。
     *
     * @param param 解析参数
     * @return 覆盖解析结果
     */
    @Override
    public SysHelpPageResolveDTO listForPage(SysHelpResourceListParam param) {
        String docType = param == null ? "" : String.valueOf(param.getDocType()).trim().toUpperCase(Locale.ROOT);
        if (!HelpResourceSupport.DOC_MANUAL.equals(docType) && !HelpResourceSupport.DOC_VIDEO.equals(docType)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_DOC_TYPE_INVALID);
        }
        String menuPath = HelpResourceSupport.normalizeMenuPath(param == null ? null : param.getMenuPath());
        List<SysHelpResource> menuDocs = list(new LambdaQueryWrapper<SysHelpResource>()
                .eq(SysHelpResource::getDocType, docType)
                .eq(SysHelpResource::getScopeType, HelpResourceSupport.SCOPE_MENU)
                .eq(SysHelpResource::getStatus, 1)
                .orderByAsc(SysHelpResource::getSortOrder)
                .orderByAsc(SysHelpResource::getId));
        List<SysHelpResource> matched = HelpResourceSupport.resolveMenuResources(menuPath, menuDocs);
        SysHelpPageResolveDTO result = new SysHelpPageResolveDTO();
        result.setMenuPath(menuPath);
        if (!matched.isEmpty()) {
            result.setResolvedScope(HelpResourceSupport.SCOPE_MENU);
            result.setItems(matched.stream().map(this::toDto).toList());
            return result;
        }
        List<SysHelpResource> globalDocs = list(new LambdaQueryWrapper<SysHelpResource>()
                .eq(SysHelpResource::getDocType, docType)
                .eq(SysHelpResource::getScopeType, HelpResourceSupport.SCOPE_GLOBAL)
                .eq(SysHelpResource::getStatus, 1)
                .orderByAsc(SysHelpResource::getSortOrder)
                .orderByAsc(SysHelpResource::getId));
        result.setResolvedScope(HelpResourceSupport.SCOPE_GLOBAL);
        result.setItems(HelpResourceSupport.sortByOrder(globalDocs).stream().map(this::toDto).toList());
        return result;
    }

    /**
     * 读取联系方式。
     *
     * @return 联系配置
     */
    @Override
    public SysHelpContactDTO getContact() {
        SysHelpContactDTO contact = configService.getGlobalJson(KEY_HELP_CONTACT, SysHelpContactDTO.class, new SysHelpContactDTO());
        return contact == null ? new SysHelpContactDTO() : contact;
    }

    /**
     * 保存联系方式。
     *
     * @param contact 联系配置
     */
    @Override
    public void saveContact(SysHelpContactDTO contact) {
        SysHelpContactDTO safe = contact == null ? new SysHelpContactDTO() : contact;
        if (StringUtils.hasText(safe.getWechatQrUrl())
                && !HelpResourceSupport.isSafeExternalUrl(safe.getWechatQrUrl())
                && !safe.getWechatQrUrl().startsWith("/")) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_EXTERNAL_URL_INVALID);
        }
        configService.setGlobalJson(KEY_HELP_CONTACT, safe);
    }

    /**
     * 组装分页条件。
     *
     * @param param 查询参数
     * @return 查询包装
     */
    private LambdaQueryWrapper<SysHelpResource> buildPageWrapper(SysHelpResourcePageParam param) {
        return new LambdaQueryWrapper<SysHelpResource>()
                .like(StringUtils.hasText(param.getTitle()), SysHelpResource::getTitle, param.getTitle())
                .eq(StringUtils.hasText(param.getDocType()), SysHelpResource::getDocType, normalizeEnum(param.getDocType()))
                .eq(StringUtils.hasText(param.getScopeType()), SysHelpResource::getScopeType, normalizeEnum(param.getScopeType()))
                .eq(StringUtils.hasText(param.getSourceType()), SysHelpResource::getSourceType, normalizeEnum(param.getSourceType()))
                .like(StringUtils.hasText(param.getMenuPath()), SysHelpResource::getMenuPath, param.getMenuPath())
                .eq(param.getStatus() != null, SysHelpResource::getStatus, param.getStatus())
                .orderByAsc(SysHelpResource::getSortOrder)
                .orderByDesc(SysHelpResource::getUpdateTime);
    }

    /**
     * 校验并填充实体。
     *
     * @param entity 目标实体
     * @param param 入参
     * @param creating 是否新增
     */
    private void fillEntity(SysHelpResource entity, SysHelpResourceSaveParam param, boolean creating) {
        if (param == null || !StringUtils.hasText(param.getTitle())) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_TITLE_REQUIRED);
        }
        String docType = normalizeEnum(param.getDocType());
        if (!HelpResourceSupport.DOC_MANUAL.equals(docType) && !HelpResourceSupport.DOC_VIDEO.equals(docType)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_DOC_TYPE_INVALID);
        }
        String scopeType = normalizeEnum(param.getScopeType());
        if (!HelpResourceSupport.SCOPE_GLOBAL.equals(scopeType) && !HelpResourceSupport.SCOPE_MENU.equals(scopeType)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_SCOPE_TYPE_INVALID);
        }
        String sourceType = normalizeEnum(param.getSourceType());
        if (!HelpResourceSupport.SOURCE_FILE.equals(sourceType) && !HelpResourceSupport.SOURCE_EXTERNAL.equals(sourceType)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_SOURCE_TYPE_INVALID);
        }

        // 菜单范围必须绑定工作区路径；全局范围强制清空菜单字段。
        String menuPath = HelpResourceSupport.normalizeMenuPath(param.getMenuPath());
        String scopeBinding = HelpResourceSupport.validateScopeBinding(scopeType, param.getMenuId(), menuPath);
        if ("menuMissing".equals(scopeBinding)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_MENU_REQUIRED);
        }
        if ("globalHasMenu".equals(scopeBinding)) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_GLOBAL_MENU_FORBIDDEN);
        }
        if (HelpResourceSupport.SCOPE_GLOBAL.equals(scopeType)) {
            menuPath = null;
        }

        String fileExt = HelpResourceSupport.normalizeExt(param.getFileName(), param.getFileExt());
        if (HelpResourceSupport.SOURCE_FILE.equals(sourceType)) {
            if (!StringUtils.hasText(param.getFileUrl()) || !StringUtils.hasText(fileExt)) {
                throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_REQUIRED);
            }
            if (!HelpResourceSupport.isAllowedExt(docType, fileExt)) {
                throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_FILE_EXT_INVALID);
            }
        } else {
            if (!StringUtils.hasText(param.getExternalUrl())) {
                throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_EXTERNAL_URL_REQUIRED);
            }
            if (!HelpResourceSupport.isSafeExternalUrl(param.getExternalUrl())) {
                throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_EXTERNAL_URL_INVALID);
            }
        }

        if (creating) {
            entity.setTenantId(TenantContext.get());
        }
        entity.setTitle(param.getTitle().trim());
        entity.setDocType(docType);
        entity.setScopeType(scopeType);
        entity.setSourceType(sourceType);
        entity.setMenuId(HelpResourceSupport.SCOPE_MENU.equals(scopeType) ? param.getMenuId() : null);
        entity.setMenuPath(menuPath);
        entity.setFileName(HelpResourceSupport.SOURCE_FILE.equals(sourceType) ? param.getFileName() : null);
        entity.setFileUrl(HelpResourceSupport.SOURCE_FILE.equals(sourceType) ? param.getFileUrl() : null);
        entity.setFileExt(HelpResourceSupport.SOURCE_FILE.equals(sourceType) ? fileExt : null);
        entity.setFileSize(HelpResourceSupport.SOURCE_FILE.equals(sourceType) ? param.getFileSize() : null);
        entity.setContentType(HelpResourceSupport.SOURCE_FILE.equals(sourceType) ? param.getContentType() : null);
        entity.setExternalUrl(HelpResourceSupport.SOURCE_EXTERNAL.equals(sourceType) ? param.getExternalUrl().trim() : null);
        entity.setStatus(param.getStatus() != null && param.getStatus() == 0 ? 0 : 1);
        entity.setSortOrder(param.getSortOrder() == null ? 0 : param.getSortOrder());
        entity.setRemark(param.getRemark());
    }

    /**
     * 读取并校验资源归属。
     *
     * @param id 主键
     * @return 资源实体
     */
    private SysHelpResource requireResource(Long id) {
        SysHelpResource entity = getById(id);
        if (entity == null) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_RESOURCE_NOT_FOUND);
        }
        Long tenantId = TenantContext.get();
        if (tenantId != null && entity.getTenantId() != null && !Objects.equals(tenantId, entity.getTenantId())) {
            throw new I18nBusinessException(BAD_REQUEST, SysPromptEnum.HELP_RESOURCE_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 转换为 DTO。
     *
     * @param entity 实体
     * @return DTO
     */
    private SysHelpResourceDTO toDto(SysHelpResource entity) {
        SysHelpResourceDTO dto = new SysHelpResourceDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 枚举值转大写。
     *
     * @param value 原始值
     * @return 大写值
     */
    private String normalizeEnum(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 计算上传体积上限。
     *
     * @param docType 文档类型
     * @return 字节上限
     */
    private long resolveMaxBytes(String docType) {
        if (HelpResourceSupport.DOC_VIDEO.equalsIgnoreCase(docType)) {
            return resolveVideoMaxSizeMb() * 1024L * 1024L;
        }
        FileUploadConfig uploadConfig = configService.getGlobalJson(KEY_FILE_UPLOAD, FileUploadConfig.class, FileUploadConfig.defaults());
        long maxMb = uploadConfig == null ? 20L : Math.max(1L, uploadConfig.getMaxSizeMb());
        return maxMb * 1024L * 1024L;
    }

    /**
     * 读取视频体积上限。
     *
     * @return 上限（MB）
     */
    private long resolveVideoMaxSizeMb() {
        HelpUploadConfig helpUpload = configService.getGlobalJson(KEY_HELP_UPLOAD, HelpUploadConfig.class, HelpUploadConfig.defaults());
        return helpUpload == null || helpUpload.getVideoMaxSizeMb() <= 0 ? 200L : helpUpload.getVideoMaxSizeMb();
    }

    /**
     * 解析上传模块名称。
     *
     * @param docType 文档类型
     * @return 模块名称
     */
    private String resolveModuleName(String docType) {
        if (HelpResourceSupport.MODULE_CONTACT.equals(docType)) {
            return "帮助联系二维码";
        }
        if (HelpResourceSupport.DOC_VIDEO.equalsIgnoreCase(docType)) {
            return "帮助操作视频";
        }
        return "帮助操作手册";
    }
}
