package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysAndroidVersionDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionQueryDTO;
import com.forgex.sys.domain.entity.SysAndroidVersion;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysAndroidVersionMapper;
import com.forgex.sys.service.FileService;
import com.forgex.sys.service.ISysAndroidVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 安卓版本管理服务实现类
 * <p>
 * 实现安卓安装包版本的增删改查和文件上传功能，
 * 文件存储复用系统现有的 FileService（支持 Local / OSS / MinIO）
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see ISysAndroidVersionService
 * @see FileService
 */
@Service
@RequiredArgsConstructor
public class SysAndroidVersionServiceImpl extends ServiceImpl<SysAndroidVersionMapper, SysAndroidVersion>
        implements ISysAndroidVersionService {

    private static final String MODULE_CODE = "sys_android_version";
    private static final String MODULE_NAME = "安卓版本";

    private final FileService fileService;

    /**
     * 分页查询安卓版本列表
     *
     * @param page  分页对象
     * @param query 查询参数（支持版本名称模糊搜索、状态筛选）
     * @return 分页 VO 结果
     */
    @Override
    public IPage<SysAndroidVersionVO> pageVersions(Page<SysAndroidVersion> page, SysAndroidVersionQueryDTO query) {
        LambdaQueryWrapper<SysAndroidVersion> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            String versionName = normalizeQueryValue(query.getVersionName());
            wrapper.like(StringUtils.hasText(versionName), SysAndroidVersion::getVersionName, versionName);
            wrapper.eq(query.getStatus() != null, SysAndroidVersion::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysAndroidVersion::getCreateTime).orderByDesc(SysAndroidVersion::getId);
        return page(page, wrapper).convert(this::toVO);
    }

    /**
     * 上传 APK 文件并创建版本记录
     * <p>
     * 通过 FileService 上传文件获取访问 URL，然后保存版本信息到数据库
     * </p>
     *
     * @param file APK 文件
     * @param dto  版本信息参数（版本号、版本名称、更新日志等）
     * @return 创建后的 VO
     * @throws IOException 文件上传失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAndroidVersionVO uploadApk(MultipartFile file, SysAndroidVersionDTO dto) throws IOException {
        validateUploadParam(file, dto);

        // 通过 FileService 上传文件
        String fileUrl = fileService.upload(file, MODULE_CODE, MODULE_NAME);

        // 构建版本记录
        SysAndroidVersion entity = new SysAndroidVersion();
        entity.setVersionCode(dto.getVersionCode());
        entity.setVersionName(dto.getVersionName());
        entity.setChangelog(dto.getChangelog());
        entity.setFileName(file.getOriginalFilename());
        entity.setFileUrl(fileUrl);
        entity.setFileSize(file.getSize());
        entity.setStorageType("LOCAL");
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        save(entity);
        return toVO(entity);
    }

    /**
     * 编辑版本信息（不涉及文件替换）
     *
     * @param dto 版本信息参数（必须包含 id）
     * @return 更新后的 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAndroidVersionVO updateVersion(SysAndroidVersionDTO dto) {
        validateUpdateParam(dto);
        SysAndroidVersion entity = requireEntity(dto.getId());
        entity.setVersionCode(dto.getVersionCode());
        entity.setVersionName(dto.getVersionName());
        entity.setChangelog(dto.getChangelog());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        updateById(entity);
        return toVO(entity);
    }

    /**
     * 删除版本记录。
     *
     * @param id 主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(Long id) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        requireEntity(id);
        removeById(id);
    }

    /**
     * 获取最新启用的版本信息
     * <p>
     * 按版本号降序排列，取第一条状态为启用的记录
     * </p>
     *
     * @return 最新版本 VO，无数据时返回 null
     */
    @Override
    public SysAndroidVersionVO getLatestVersion() {
        LambdaQueryWrapper<SysAndroidVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAndroidVersion::getStatus, 1);
        wrapper.orderByDesc(SysAndroidVersion::getVersionCode);
        wrapper.last("LIMIT 1");
        SysAndroidVersion entity = getOne(wrapper);
        return entity != null ? toVO(entity) : null;
    }

    /**
     * 实体转 VO
     *
     * @param entity 实体对象
     * @return VO 对象
     */
    private SysAndroidVersionVO toVO(SysAndroidVersion entity) {
        SysAndroidVersionVO vo = new SysAndroidVersionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private void validateUploadParam(MultipartFile file, SysAndroidVersionDTO dto) {
        if (file == null || file.isEmpty() || dto == null
                || dto.getVersionCode() == null
                || !StringUtils.hasText(dto.getVersionName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private void validateUpdateParam(SysAndroidVersionDTO dto) {
        if (dto == null || dto.getId() == null || dto.getVersionCode() == null || !StringUtils.hasText(dto.getVersionName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private SysAndroidVersion requireEntity(Long id) {
        SysAndroidVersion entity = getById(id);
        if (entity == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CONFIG_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 规范化查询值（去除空白）
     *
     * @param value 原始值
     * @return 规范化后的值，空字符串返回 null
     */
    private String normalizeQueryValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
