package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.sys.domain.dto.SysFileRecordQueryDTO;
import com.forgex.sys.domain.entity.SysFileRecord;
import com.forgex.sys.domain.vo.SysFileRecordVO;
import com.forgex.sys.mapper.SysFileRecordMapper;
import com.forgex.sys.service.ISysFileRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * File record service implementation.
 */
@Service
public class SysFileRecordServiceImpl extends ServiceImpl<SysFileRecordMapper, SysFileRecord>
        implements ISysFileRecordService {

    private static final String DEFAULT_MODULE_CODE = "common";
    private static final String DEFAULT_MODULE_NAME = "公共文件";

    @Override
    public void saveUploadRecord(
            MultipartFile file,
            String moduleCode,
            String moduleName,
            String relativePath,
            String accessUrl,
            String storageType,
            Long storageConfigId
    ) {
        SysFileRecord record = new SysFileRecord();
        record.setModuleCode(StringUtils.hasText(moduleCode) ? moduleCode.trim() : DEFAULT_MODULE_CODE);
        record.setModuleName(StringUtils.hasText(moduleName) ? moduleName.trim() : DEFAULT_MODULE_NAME);
        record.setOriginalName(file.getOriginalFilename());
        record.setStoredName(extractStoredName(relativePath));
        record.setFileType(resolveFileType(file.getOriginalFilename(), file.getContentType()));
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());
        record.setRelativePath(relativePath);
        record.setAccessUrl(accessUrl);
        record.setStorageType(storageType);
        record.setStorageConfigId(storageConfigId);
        save(record);
    }

    @Override
    public IPage<SysFileRecordVO> pageRecords(Page<SysFileRecord> page, SysFileRecordQueryDTO query) {
        LambdaQueryWrapper<SysFileRecord> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            String moduleCode = normalizeQueryValue(query.getModuleCode());
            String moduleName = normalizeQueryValue(query.getModuleName());
            String originalName = normalizeQueryValue(query.getOriginalName());
            String fileType = normalizeQueryValue(query.getFileType());
            wrapper.like(StringUtils.hasText(moduleCode), SysFileRecord::getModuleCode, moduleCode);
            wrapper.like(StringUtils.hasText(moduleName), SysFileRecord::getModuleName, moduleName);
            wrapper.like(StringUtils.hasText(originalName), SysFileRecord::getOriginalName, originalName);
            wrapper.like(StringUtils.hasText(fileType), SysFileRecord::getFileType, fileType);
        }
        wrapper.orderByDesc(SysFileRecord::getCreateTime).orderByDesc(SysFileRecord::getId);
        return page(page, wrapper).convert(this::toVO);
    }

    private SysFileRecordVO toVO(SysFileRecord record) {
        SysFileRecordVO vo = new SysFileRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private String extractStoredName(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return "";
        }
        String normalizedPath = relativePath.replace('\\', '/');
        int lastSlash = normalizedPath.lastIndexOf('/');
        return lastSlash >= 0 ? normalizedPath.substring(lastSlash + 1) : normalizedPath;
    }

    private String resolveFileType(String originalName, String contentType) {
        if (StringUtils.hasText(originalName)) {
            int index = originalName.lastIndexOf('.');
            if (index >= 0 && index < originalName.length() - 1) {
                return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
            }
        }
        return StringUtils.hasText(contentType) ? contentType : "unknown";
    }

    private String normalizeQueryValue(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
