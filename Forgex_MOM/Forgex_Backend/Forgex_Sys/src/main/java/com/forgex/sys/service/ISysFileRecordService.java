package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysFileRecordQueryDTO;
import com.forgex.sys.domain.entity.SysFileRecord;
import com.forgex.sys.domain.vo.SysFileRecordVO;
import org.springframework.web.multipart.MultipartFile;

public interface ISysFileRecordService extends IService<SysFileRecord> {

    void saveUploadRecord(
            MultipartFile file,
            String moduleCode,
            String moduleName,
            String relativePath,
            String accessUrl,
            String storageType,
            Long storageConfigId
    );

    IPage<SysFileRecordVO> pageRecords(Page<SysFileRecord> page, SysFileRecordQueryDTO query);
}
