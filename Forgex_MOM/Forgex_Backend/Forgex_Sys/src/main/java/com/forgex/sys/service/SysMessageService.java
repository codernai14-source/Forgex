package com.forgex.sys.service;

import com.forgex.sys.domain.dto.SysMessageSendDTO;
import com.forgex.sys.domain.vo.SysMessageVO;

import java.util.List;

/**
 * 系统消息服务接口
 * <p>提供系统消息的发送、查询和标记功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SysMessageService {
    /**
     * 发送系统消息
     * 
     * @param dto 消息发送参数
     * @return 消息ID，发送失败返回null
     */
    Long send(SysMessageSendDTO dto);

    /**
     * 查询未读消息列表
     * 
     * @param limit 最大返回数量
     * @return 未读消息列表
     */
    List<SysMessageVO> listUnread(Integer limit);

    /**
     * 标记消息已读
     * 
     * @param id 消息ID
     * @return true表示标记成功，false表示标记失败
     */
    boolean markRead(Long id);
}

