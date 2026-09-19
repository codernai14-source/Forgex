package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
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

@Service
@RequiredArgsConstructor
public class SysAndroidVersionServiceImpl extends ServiceImpl<SysAndroidVersionMapper, SysAndroidVersion>
        implements ISysAndroidVersionService {

    private static final String MODULE_CODE = "sys_android_version";
    private static final String MODULE_NAME = "安卓版本";

    private final FileService fileService;

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

    @Override
    public SysAndroidVersionVO getVersion(Long id) {
        return toVO(requireEntity(id));
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public SysAndroidVersionVO uploadApk(MultipartFile file, SysAndroidVersionDTO dto) throws IOException {
        validateUploadParam(file, dto);
        String fileUrl = fileService.upload(file, MODULE_CODE, MODULE_NAME);

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(Long id) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        requireEntity(id);
        removeById(id);
    }

    @Override
    public SysAndroidVersionVO getLatestVersion() {
        LambdaQueryWrapper<SysAndroidVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAndroidVersion::getStatus, 1);
        wrapper.orderByDesc(SysAndroidVersion::getVersionCode);
        wrapper.last("LIMIT 1");
        SysAndroidVersion entity = getOne(wrapper);
        return entity != null ? toVO(entity) : null;
    }

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

    private String normalizeQueryValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
