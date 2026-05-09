package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysAndroidVersionDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionQueryDTO;
import com.forgex.sys.domain.entity.SysAndroidVersion;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 安卓版本管理服务接口
 * <p>
 * 提供安卓安装包版本的分页查询、上传、编辑、删除、获取最新版本等功能
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see IService
 * @see SysAndroidVersion
 */
public interface ISysAndroidVersionService extends IService<SysAndroidVersion> {

    /**
     * 分页查询安卓版本列表
     *
     * @param page  分页对象
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<SysAndroidVersionVO> pageVersions(Page<SysAndroidVersion> page, SysAndroidVersionQueryDTO query);

    /**
     * 上传 APK 并创建版本记录
     *
     * @param file 文件对象
     * @param dto  版本信息
     * @return 创建的版本记录 VO
     * @throws IOException 文件上传失败时抛出异常
     */
    SysAndroidVersionVO uploadApk(MultipartFile file, SysAndroidVersionDTO dto) throws IOException;

    /**
     * 更新版本信息
     *
     * @param dto 版本信息
     * @return 更新后的版本记录 VO
     */
    SysAndroidVersionVO updateVersion(SysAndroidVersionDTO dto);

    /**
     * 删除版本记录。
     *
     * @param id 主键 ID
     */
    void deleteVersion(Long id);

    /**
     * 获取最新启用的版本
     *
     * @return 最新版本 VO，无数据时返回 null
     */
    SysAndroidVersionVO getLatestVersion();
}
