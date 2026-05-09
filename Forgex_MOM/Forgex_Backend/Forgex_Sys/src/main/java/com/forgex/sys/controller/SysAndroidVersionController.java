package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysAndroidVersionDTO;
import com.forgex.sys.domain.dto.SysAndroidVersionQueryDTO;
import com.forgex.sys.domain.entity.SysAndroidVersion;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.SysAndroidVersionVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ISysAndroidVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 安卓版本管理控制器
 * <p>
 * 处理安卓安装包版本的增删改查和文件上传请求
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see ISysAndroidVersionService
 */
@RestController
@RequestMapping("/sys/android-version")
@RequiredArgsConstructor
public class SysAndroidVersionController {

    private final ISysAndroidVersionService androidVersionService;

    /**
     * 分页查询安卓版本列表
     *
     * @param query 查询参数（版本名称、状态、分页信息）
     * @return 分页结果
     */
    @RequirePerm("sys:androidVersion:view")
    @PostMapping("/page")
    public R<IPage<SysAndroidVersionVO>> page(@RequestBody SysAndroidVersionQueryDTO query) {
        SysAndroidVersionQueryDTO condition = query == null ? new SysAndroidVersionQueryDTO() : query;
        Page<SysAndroidVersion> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(androidVersionService.pageVersions(page, condition));
    }

    /**
     * 上传 APK 安装包
     * <p>
     * 上传 APK 文件并创建版本记录，需要 sys:androidVersion:add 权限
     * </p>
     *
     * @param file         APK 文件
     * @param versionCode  版本号
     * @param versionName  版本名称
     * @param changelog    更新日志
     * @return 创建后的版本信息
     */
    @RequirePerm("sys:androidVersion:add")
    @PostMapping("/upload")
    public R<SysAndroidVersionVO> upload(
            MultipartFile file,
            Integer versionCode,
            String versionName,
            String changelog
    ) {
        if (file == null || file.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        try {
            SysAndroidVersionDTO dto = new SysAndroidVersionDTO();
            dto.setVersionCode(versionCode);
            dto.setVersionName(versionName);
            dto.setChangelog(changelog);
            SysAndroidVersionVO vo = androidVersionService.uploadApk(file, dto);
            return R.ok(SysPromptEnum.CONFIG_CREATE_SUCCESS, vo);
        } catch (IOException e) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED, e.getMessage());
        }
    }

    /**
     * 编辑版本信息
     * <p>
     * 修改版本名称、更新日志、状态等，不涉及文件替换，需要 sys:androidVersion:edit 权限
     * </p>
     *
     * @param dto 版本信息参数
     * @return 更新后的版本信息
     */
    @RequirePerm("sys:androidVersion:edit")
    @PostMapping("/update")
    public R<SysAndroidVersionVO> update(@RequestBody SysAndroidVersionDTO dto) {
        return R.ok(SysPromptEnum.CONFIG_UPDATE_SUCCESS, androidVersionService.updateVersion(dto));
    }

    /**
     * 删除版本记录
     *
     * @param param 版本记录 ID 参数
     * @return 操作结果
     */
    @RequirePerm("sys:androidVersion:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        androidVersionService.deleteVersion(param.getId());
        return R.ok(SysPromptEnum.CONFIG_DELETE_SUCCESS);
    }

    /**
     * 获取最新启用的版本信息
     * <p>
     * 用于首页安卓图标弹窗展示最新版本和下载二维码
     * </p>
     *
     * @return 最新版本信息
     */
    @PostMapping("/latest")
    public R<SysAndroidVersionVO> latest() {
        return R.ok(androidVersionService.getLatestVersion());
    }
}
