package com.forgex.sys.audit;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.audit.OperationLogRecord;
import com.forgex.common.audit.OperationLogRecorder;
import com.forgex.common.i18n.LangContext;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysOperationTemplate;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysOperationTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysOperationLogRecorder implements OperationLogRecorder {

    private final SysOperationLogMapper logMapper;
    private final SysOperationTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void record(OperationLogRecord record) {
        if (record == null) {
            return;
        }

        SysOperationLog log = new SysOperationLog();
        log.setTenantId(record.getTenantId());
        log.setUserId(record.getUserId());
        log.setUsername(record.getUsername());
        log.setModule(record.getModule());
        log.setMenuPath(record.getMenuPath());
        log.setOperationType(record.getOperationType());
        log.setRequestMethod(record.getRequestMethod());
        log.setRequestUrl(record.getRequestUrl());
        log.setRequestParams(record.getRequestParams());
        log.setResponseStatus(record.getResponseStatus());
        log.setResponseResult(record.getResponseResult());
        log.setErrorStack(record.getErrorStack());
        log.setOperationTime(record.getOperationTime() == null ? LocalDateTime.now() : record.getOperationTime());
        log.setCostTime(record.getCostTime());
        log.setIp(record.getIp());
        log.setUserAgent(record.getUserAgent());
        log.setDetailTemplateCode(record.getDetailTemplateCode());
        log.setDeleted(false);

        Map<String, Object> fields = record.getDetailFields();
        if (fields != null && !fields.isEmpty()) {
            try {
                log.setDetailFieldsJson(objectMapper.writeValueAsString(fields));
            } catch (Exception ignored) {
            }
        }

        if (record.getTenantId() != null && StringUtils.hasText(record.getDetailTemplateCode()) && StringUtils.hasText(record.getModule())) {
            SysOperationTemplate tpl = templateMapper.selectOne(new LambdaQueryWrapper<SysOperationTemplate>()
                    .eq(SysOperationTemplate::getTenantId, record.getTenantId())
                    .eq(SysOperationTemplate::getModule, record.getModule())
                    .eq(SysOperationTemplate::getTemplateCode, record.getDetailTemplateCode())
                    .eq(SysOperationTemplate::getOperationType, record.getOperationType())
                    .last("limit 1"));
            if (tpl != null && StringUtils.hasText(tpl.getTextI18nJson()) && fields != null) {
                String text = resolveI18nText(tpl.getTextI18nJson(), LangContext.get());
                if (StringUtils.hasText(text)) {
                    log.setDetailText(render(text, fields));
                }
            }
        }

        logMapper.insert(log);
    }

    private String resolveI18nText(String textI18nJson, String lang) {
        try {
            JSONObject obj = JSONUtil.parseObj(textI18nJson);
            String t = obj.getStr(lang);
            if (StringUtils.hasText(t)) {
                return t;
            }
            String zh = obj.getStr(LangContext.DEFAULT_LANG);
            if (StringUtils.hasText(zh)) {
                return zh;
            }
            for (String k : obj.keySet()) {
                String v = obj.getStr(k);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String render(String template, Map<String, Object> fields) {
        String out = template;
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            String k = e.getKey();
            if (!StringUtils.hasText(k)) continue;
            String v = e.getValue() == null ? "" : String.valueOf(e.getValue());
            out = out.replace("{" + k + "}", v);
        }
        return out;
    }
}

