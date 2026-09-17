package com.forgex.sys.audit;

import com.forgex.common.crypto.SM3Utils;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.mapper.SysOperationLogMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 审计哈希链校验测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class AuditChainServiceTest {

    /**
     * 篡改后的记录哈希应被检出。
     */
    @Test
    void detectTamperedRecord() {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        AuditChainService service = new AuditChainService(mapper);
        SysOperationLog log = new SysOperationLog();
        log.setId(1L);
        log.setTenantId(1L);
        log.setUserId(9L);
        log.setOperationTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        log.setRequestUrl("/sys/role/create");
        log.setRequestParams("{}");
        log.setResponseResult("ok");
        log.setPrevHash("");
        String content = log.getOperationTime() + "|" + log.getUserId() + "|" + log.getRequestUrl()
                + "|" + log.getRequestParams() + "|" + log.getResponseResult();
        log.setRecordHash(SM3Utils.digestHex("|" + content));
        when(mapper.selectList(any())).thenReturn(List.of(log));
        assertTrue(service.verify(1L));

        log.setRecordHash("deadbeef");
        assertFalse(service.verify(1L));
    }
}
