package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysFileRecordQueryDTO;
import com.forgex.sys.domain.entity.SysFileRecord;
import com.forgex.sys.domain.vo.SysFileRecordVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * sys文件record服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
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

    /**
     * 执行sys文件record的分页records操作。
     *
     * @param page 分页对象
     * @param query 查询参数
     * @return 处理结果
     */
    IPage<SysFileRecordVO> pageRecords(Page<SysFileRecord> page, SysFileRecordQueryDTO query);
}
