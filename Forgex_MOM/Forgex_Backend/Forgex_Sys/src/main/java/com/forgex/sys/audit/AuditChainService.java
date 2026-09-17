package com.forgex.sys.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.crypto.SM3Utils;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 SM3 哈希链。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuditChainService {

    private final SysOperationLogMapper logMapper;

    /**
     * 计算并回填当前记录的哈希链字段。
     *
     * @param log 待写入记录
     */
    public void fill(SysOperationLog log) {
        String prev = latestHash(log.getTenantId());
        String content = String.valueOf(log.getOperationTime()) + '|' + log.getUserId() + '|'
                + log.getRequestUrl() + '|' + log.getRequestParams() + '|' + log.getResponseResult();
        log.setPrevHash(prev);
        log.setRecordHash(SM3Utils.digestHex(prev + '|' + content));
    }

    /**
     * 校验指定租户哈希链是否完整。
     *
     * @param tenantId 租户 ID
     * @return true 表示完整
     */
    public boolean verify(Long tenantId) {
        List<SysOperationLog> logs = logMapper.selectList(new LambdaQueryWrapper<SysOperationLog>()
                .eq(tenantId != null, SysOperationLog::getTenantId, tenantId)
                .isNotNull(SysOperationLog::getRecordHash)
                .orderByAsc(SysOperationLog::getId));
        String prev = "";
        for (SysOperationLog log : logs) {
            if (log.getPrevHash() != null && !log.getPrevHash().equals(prev) && !log.getPrevHash().isBlank()) {
                if (!prev.isBlank()) {
                    return false;
                }
            }
            String content = String.valueOf(log.getOperationTime()) + '|' + log.getUserId() + '|'
                    + log.getRequestUrl() + '|' + log.getRequestParams() + '|' + log.getResponseResult();
            String expected = SM3Utils.digestHex((log.getPrevHash() == null ? "" : log.getPrevHash()) + '|' + content);
            if (log.getRecordHash() != null && !log.getRecordHash().equalsIgnoreCase(expected)) {
                return false;
            }
            prev = log.getRecordHash() == null ? prev : log.getRecordHash();
        }
        return true;
    }

    private String latestHash(Long tenantId) {
        SysOperationLog last = logMapper.selectOne(new LambdaQueryWrapper<SysOperationLog>()
                .eq(tenantId != null, SysOperationLog::getTenantId, tenantId)
                .isNotNull(SysOperationLog::getRecordHash)
                .orderByDesc(SysOperationLog::getId)
                .last("limit 1"));
        return last == null || last.getRecordHash() == null ? "" : last.getRecordHash();
    }
}
