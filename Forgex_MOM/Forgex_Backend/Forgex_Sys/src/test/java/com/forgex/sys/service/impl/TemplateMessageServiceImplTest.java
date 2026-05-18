package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.mq.message.TemplateMessageRequest;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.entity.SysMessageTemplate;
import com.forgex.sys.domain.entity.SysMessageTemplateContent;
import com.forgex.sys.domain.entity.SysMessageTemplateReceiver;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.mapper.SysMessageTemplateContentMapper;
import com.forgex.sys.mapper.SysMessageTemplateMapper;
import com.forgex.sys.mapper.SysMessageTemplateReceiverMapper;
import com.forgex.sys.service.ISysUserService;
import com.forgex.sys.service.SseEmitterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 模板消息发送服务测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class TemplateMessageServiceImplTest {

    private static final Long TENANT_ID = 100L;
    private static final Long TEMPLATE_ID = 10L;
    private static final String TEMPLATE_CODE = "SYSTEM_NOTICE";

    private SysMessageTemplateMapper templateMapper;
    private SysMessageTemplateContentMapper contentMapper;
    private SysMessageTemplateReceiverMapper receiverMapper;
    private SysMessageMapper messageMapper;
    private SseEmitterService sseEmitterService;
    private ISysUserService userService;
    private TemplateMessageServiceImpl service;

    @BeforeEach
    void setUp() {
        templateMapper = mock(SysMessageTemplateMapper.class);
        contentMapper = mock(SysMessageTemplateContentMapper.class);
        receiverMapper = mock(SysMessageTemplateReceiverMapper.class);
        messageMapper = mock(SysMessageMapper.class);
        sseEmitterService = mock(SseEmitterService.class);
        userService = mock(ISysUserService.class);
        service = new TemplateMessageServiceImpl(
                templateMapper,
                contentMapper,
                receiverMapper,
                messageMapper,
                sseEmitterService,
                userService,
                new ObjectMapper());

        TenantContext.set(TENANT_ID);
        when(templateMapper.selectOne(any(Wrapper.class))).thenReturn(template());
        when(contentMapper.selectList(any(Wrapper.class))).thenReturn(List.of(internalContent()));
        doAnswer(invocation -> {
            SysMessage message = invocation.getArgument(0);
            if (message.getId() == null) {
                message.setId(9000L + message.getReceiverUserId());
            }
            return 1;
        }).when(messageMapper).insert(any(SysMessage.class));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void sendByTemplateShouldResolveUserReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("USER", "[1,2]")));
        when(userService.listValidUserIds(TENANT_ID, List.of(1L, 2L))).thenReturn(List.of(1L, 2L));

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of("userName", "张三"));

        assertEquals(2, count);
        verify(userService).listValidUserIds(TENANT_ID, List.of(1L, 2L));
        assertInsertedReceivers(1L, 2L);
    }

    @Test
    void sendByTemplateShouldResolveRoleReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("ROLE", "[3]")));
        when(userService.listUserIdsByRoleIds(TENANT_ID, List.of(3L))).thenReturn(List.of(11L, 12L));

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of());

        assertEquals(2, count);
        verify(userService).listUserIdsByRoleIds(TENANT_ID, List.of(3L));
        assertInsertedReceivers(11L, 12L);
    }

    @Test
    void sendByTemplateShouldResolveDeptReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("DEPT", "[5,6]")));
        when(userService.listUserIdsByDeptIds(TENANT_ID, List.of(5L, 6L))).thenReturn(List.of(21L, 22L));

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of());

        assertEquals(2, count);
        verify(userService).listUserIdsByDeptIds(TENANT_ID, List.of(5L, 6L));
        assertInsertedReceivers(21L, 22L);
    }

    @Test
    void sendByTemplateShouldResolvePositionReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("POSITION", "[7]")));
        when(userService.listUserIdsByPositionIds(TENANT_ID, List.of(7L))).thenReturn(List.of(31L));

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of());

        assertEquals(1, count);
        verify(userService).listUserIdsByPositionIds(TENANT_ID, List.of(7L));
        assertInsertedReceivers(31L);
    }

    @Test
    void sendByTemplateShouldDeduplicateResolvedReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                receiver("USER", "[1,2]"),
                receiver("ROLE", "[3]"),
                receiver("DEPT", "[4]")));
        when(userService.listValidUserIds(TENANT_ID, List.of(1L, 2L))).thenReturn(List.of(1L, 2L));
        when(userService.listUserIdsByRoleIds(TENANT_ID, List.of(3L))).thenReturn(List.of(2L, 3L));
        when(userService.listUserIdsByDeptIds(TENANT_ID, List.of(4L))).thenReturn(List.of(3L, 4L));

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of());

        assertEquals(4, count);
        assertInsertedReceivers(1L, 2L, 3L, 4L);
    }

    @Test
    void sendByTemplateShouldNotSendWhenReceiverResolveEmpty() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("ROLE", "[3]")));
        when(userService.listUserIdsByRoleIds(TENANT_ID, List.of(3L))).thenReturn(List.of());

        int count = service.sendByTemplate(TEMPLATE_CODE, Map.of());

        assertEquals(0, count);
        verify(messageMapper, never()).insert(any(SysMessage.class));
        verify(sseEmitterService, never()).sendToUser(any(), any(), any(), any());
    }

    @Test
    void processTemplateMessageFromMqShouldPreferCustomRequestReceivers() {
        when(receiverMapper.selectList(any(Wrapper.class))).thenReturn(List.of(receiver("CUSTOM", "")));
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTenantId(TENANT_ID);
        request.setSenderUserId(501L);
        request.setSenderName("系统");
        request.setTemplateCode(TEMPLATE_CODE);
        request.setReceiverUserIds(List.of(41L, 42L, 41L));
        request.setDataMap(Map.of("userName", "李四"));
        request.setBizType("WORKFLOW");

        service.processTemplateMessageFromMq(request);

        verify(userService, never()).listValidUserIds(any(), any());
        assertInsertedReceivers(41L, 42L);
    }

    private SysMessageTemplate template() {
        SysMessageTemplate template = new SysMessageTemplate();
        template.setId(TEMPLATE_ID);
        template.setTenantId(TENANT_ID);
        template.setTemplateCode(TEMPLATE_CODE);
        template.setMessageType("NOTICE");
        template.setBizType("SYSTEM");
        template.setStatus(true);
        template.setDeleted(false);
        return template;
    }

    private SysMessageTemplateContent internalContent() {
        SysMessageTemplateContent content = new SysMessageTemplateContent();
        content.setId(20L);
        content.setTemplateId(TEMPLATE_ID);
        content.setPlatform("INTERNAL");
        content.setContentTitle("通知");
        content.setContentBody("您好，${userName}");
        content.setLinkUrl("/message");
        content.setDeleted(false);
        return content;
    }

    private SysMessageTemplateReceiver receiver(String type, String ids) {
        SysMessageTemplateReceiver receiver = new SysMessageTemplateReceiver();
        receiver.setId(System.nanoTime());
        receiver.setTemplateId(TEMPLATE_ID);
        receiver.setReceiverType(type);
        receiver.setReceiverIds(ids);
        receiver.setDeleted(false);
        return receiver;
    }

    private void assertInsertedReceivers(Long... receiverUserIds) {
        ArgumentCaptor<SysMessage> captor = ArgumentCaptor.forClass(SysMessage.class);
        verify(messageMapper, org.mockito.Mockito.times(receiverUserIds.length)).insert(captor.capture());
        List<Long> actual = new ArrayList<>();
        for (SysMessage message : captor.getAllValues()) {
            actual.add(message.getReceiverUserId());
            assertEquals(TENANT_ID, message.getTenantId());
            assertEquals(TEMPLATE_CODE, message.getTemplateCode());
        }
        assertEquals(List.of(receiverUserIds), actual);
        assertTrue(actual.stream().distinct().count() == actual.size());
    }
}
