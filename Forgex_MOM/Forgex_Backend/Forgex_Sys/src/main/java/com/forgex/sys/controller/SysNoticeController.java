package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysNoticeDTO;
import com.forgex.sys.domain.entity.SysNotice;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.param.SysNoticeAckParam;
import com.forgex.sys.domain.param.SysNoticePageParam;
import com.forgex.sys.domain.param.SysNoticeSaveParam;
import com.forgex.sys.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统通知控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final ISysNoticeService noticeService;

    /**
     * 分页查询系统通知。
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @RequirePerm("sys:notice:view")
    @PostMapping("/page")
    public R<IPage<SysNoticeDTO>> page(@RequestBody(required = false) SysNoticePageParam param) {
        SysNoticePageParam condition = param == null ? new SysNoticePageParam() : param;
        Page<SysNotice> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        return R.ok(noticeService.pageNotices(page, condition));
    }

    /**
     * 查询系统通知详情。
     *
     * @param param 主键参数
     * @return 通知详情
     */
    @RequirePerm("sys:notice:view")
    @PostMapping("/detail")
    public R<SysNoticeDTO> detail(@RequestBody IdParam param) {
        return R.ok(noticeService.detail(param.getId()));
    }

    /**
     * 保存系统通知。
     *
     * @param param 保存参数
     * @return 通知 ID
     */
    @RequirePerm({"sys:notice:add", "sys:notice:edit"})
    @PostMapping("/save")
    public R<Long> save(@RequestBody SysNoticeSaveParam param) {
        return R.ok(noticeService.saveNotice(param));
    }

    /**
     * 删除系统通知。
     *
     * @param param 主键参数
     * @return 处理结果
     */
    @RequirePerm("sys:notice:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        noticeService.deleteNotice(param.getId());
        return R.ok();
    }

    /**
     * 发布系统通知。
     *
     * @param param 主键参数
     * @return 处理结果
     */
    @RequirePerm("sys:notice:publish")
    @PostMapping("/publish")
    public R<Void> publish(@RequestBody IdParam param) {
        noticeService.publish(param.getId());
        return R.ok();
    }

    /**
     * 停用系统通知。
     *
     * @param param 主键参数
     * @return 处理结果
     */
    @RequirePerm("sys:notice:publish")
    @PostMapping("/disable")
    public R<Void> disable(@RequestBody IdParam param) {
        noticeService.disable(param.getId());
        return R.ok();
    }

    /**
     * 查询当前用户待弹通知。
     *
     * @return 待弹通知列表
     */
    @PostMapping("/popup/list")
    public R<List<SysNoticeDTO>> popupList() {
        return R.ok(noticeService.listPopupNotices());
    }

    /**
     * 确认当前用户已弹通知。
     *
     * @param param 确认参数
     * @return 处理结果
     */
    @PostMapping("/popup/ack")
    public R<Void> popupAck(@RequestBody SysNoticeAckParam param) {
        noticeService.ackPopup(param.getNoticeId());
        return R.ok();
    }
}
