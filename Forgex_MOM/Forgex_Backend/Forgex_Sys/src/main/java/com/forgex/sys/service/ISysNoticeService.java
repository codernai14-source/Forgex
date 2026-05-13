package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysNoticeDTO;
import com.forgex.sys.domain.entity.SysNotice;
import com.forgex.sys.domain.param.SysNoticePageParam;
import com.forgex.sys.domain.param.SysNoticeSaveParam;

import java.util.List;

/**
 * 系统通知服务接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
public interface ISysNoticeService extends IService<SysNotice> {

    /**
     * 分页查询系统通知。
     *
     * @param page 分页对象
     * @param param 查询参数
     * @return 分页结果
     */
    IPage<SysNoticeDTO> pageNotices(Page<SysNotice> page, SysNoticePageParam param);

    /**
     * 查询通知详情。
     *
     * @param id 通知 ID
     * @return 通知详情
     */
    SysNoticeDTO detail(Long id);

    /**
     * 保存通知。
     *
     * @param param 保存参数
     * @return 通知 ID
     */
    Long saveNotice(SysNoticeSaveParam param);

    /**
     * 删除通知。
     *
     * @param id 通知 ID
     */
    void deleteNotice(Long id);

    /**
     * 发布通知。
     *
     * @param id 通知 ID
     */
    void publish(Long id);

    /**
     * 停用通知。
     *
     * @param id 通知 ID
     */
    void disable(Long id);

    /**
     * 查询当前用户待弹通知。
     *
     * @return 待弹通知列表
     */
    List<SysNoticeDTO> listPopupNotices();

    List<SysNoticeDTO> listActiveNotices(Integer maxCount);

    /**
     * 确认当前用户已弹出通知。
     *
     * @param noticeId 通知 ID
     */
    void ackPopup(Long noticeId);
}
