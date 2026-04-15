package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.i18n.FxI18nMessageMapper;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/sys/i18n/message")
@RequiredArgsConstructor
public class I18nMessageController {

    private final FxI18nMessageMapper i18nMessageMapper;
    private final ObjectMapper objectMapper;

    @PostMapping("/page")
    public R<IPage<FxI18nMessage>> page(@RequestBody(required = false) I18nMessageQuery body) {
        I18nMessageQuery query = body == null ? new I18nMessageQuery() : body;
        Page<FxI18nMessage> page = new Page<>(
                query.getPageNum() == null ? 1 : query.getPageNum(),
                query.getPageSize() == null ? 10 : query.getPageSize()
        );
        LambdaQueryWrapper<FxI18nMessage> wrapper = new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getDeleted, false)
                .like(StringUtils.hasText(query.getModule()), FxI18nMessage::getModule, query.getModule())
                .like(StringUtils.hasText(query.getPromptCode()), FxI18nMessage::getPromptCode, query.getPromptCode())
                .eq(query.getEnabled() != null, FxI18nMessage::getEnabled, query.getEnabled())
                .orderByAsc(FxI18nMessage::getModule)
                .orderByAsc(FxI18nMessage::getPromptCode);
        return R.ok(i18nMessageMapper.selectPage(page, wrapper));
    }

    @PostMapping("/get")
    public R<FxI18nMessage> get(@RequestBody IdBody body) {
        if (body == null || body.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        FxI18nMessage row = i18nMessageMapper.selectById(body.getId());
        if (row == null || Boolean.TRUE.equals(row.getDeleted())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }
        return R.ok(row);
    }

    @RequirePerm("sys:i18nMessage:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody FxI18nMessage body) {
        String validation = validatePayload(body, false);
        if (validation != null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, validation);
        }

        FxI18nMessage exists = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, body.getModule().trim())
                .eq(FxI18nMessage::getPromptCode, body.getPromptCode().trim())
                .eq(FxI18nMessage::getDeleted, false)
                .last("limit 1"));
        if (exists != null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        body.setId(null);
        body.setModule(body.getModule().trim());
        body.setPromptCode(body.getPromptCode().trim());
        body.setTextI18nJson(body.getTextI18nJson().trim());
        body.setEnabled(body.getEnabled() == null || body.getEnabled());
        body.setVersion(body.getVersion() == null ? 1 : body.getVersion());
        body.setTenantId(null);
        body.setCreateBy(currentUser());
        body.setCreateTime(now);
        body.setUpdateBy(currentUser());
        body.setUpdateTime(now);
        body.setDeleted(false);
        i18nMessageMapper.insert(body);
        return R.ok(body.getId());
    }

    @RequirePerm("sys:i18nMessage:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody FxI18nMessage body) {
        String validation = validatePayload(body, true);
        if (validation != null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, validation);
        }

        FxI18nMessage exists = i18nMessageMapper.selectById(body.getId());
        if (exists == null || Boolean.TRUE.equals(exists.getDeleted())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }

        FxI18nMessage duplicate = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, body.getModule().trim())
                .eq(FxI18nMessage::getPromptCode, body.getPromptCode().trim())
                .eq(FxI18nMessage::getDeleted, false)
                .ne(FxI18nMessage::getId, body.getId())
                .last("limit 1"));
        if (duplicate != null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ALREADY_EXISTS);
        }

        exists.setModule(body.getModule().trim());
        exists.setPromptCode(body.getPromptCode().trim());
        exists.setTextI18nJson(body.getTextI18nJson().trim());
        exists.setEnabled(body.getEnabled() == null || body.getEnabled());
        exists.setVersion(body.getVersion() == null ? exists.getVersion() : body.getVersion());
        exists.setUpdateBy(currentUser());
        exists.setUpdateTime(LocalDateTime.now());
        i18nMessageMapper.updateById(exists);
        return R.ok(true);
    }

    @RequirePerm("sys:i18nMessage:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdBody body) {
        if (body == null || body.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.ID_EMPTY);
        }
        int rows = i18nMessageMapper.update(null, new LambdaUpdateWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getId, body.getId())
                .eq(FxI18nMessage::getDeleted, false)
                .set(FxI18nMessage::getDeleted, true)
                .set(FxI18nMessage::getUpdateBy, currentUser())
                .set(FxI18nMessage::getUpdateTime, LocalDateTime.now()));
        return rows > 0 ? R.ok(true) : R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
    }

    @PostMapping("/resolve")
    public R<String> resolve(@RequestBody ResolveRequest body) {
        if (body == null || !StringUtils.hasText(body.getModule()) || !StringUtils.hasText(body.getCode())) {
            return R.ok("");
        }

        String resolved = resolveTemplate(body.getModule(), body.getCode(), body.getDefaultTemplate());
        return R.ok(StringUtils.hasText(resolved) ? resolved : "");
    }

    private String validatePayload(FxI18nMessage body, boolean requireId) {
        if (body == null) {
            return "请求体不能为空";
        }
        if (requireId && body.getId() == null) {
            return "ID不能为空";
        }
        if (!StringUtils.hasText(body.getModule())) {
            return "module不能为空";
        }
        if (!StringUtils.hasText(body.getPromptCode())) {
            return "promptCode不能为空";
        }
        if (!StringUtils.hasText(body.getTextI18nJson())) {
            return "textI18nJson不能为空";
        }
        try {
            JsonNode node = objectMapper.readTree(body.getTextI18nJson());
            if (!node.isObject() || node.isEmpty()) {
                return "textI18nJson必须是非空JSON对象";
            }
        } catch (Exception ex) {
            return "textI18nJson格式不正确";
        }
        return null;
    }

    private String currentUser() {
        Long userId = UserContext.get();
        return userId == null ? "system" : userId.toString();
    }

    private String resolveTemplate(String module, String promptCode, String defaultTemplate) {
        FxI18nMessage row = i18nMessageMapper.selectOne(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, module)
                .eq(FxI18nMessage::getPromptCode, promptCode)
                .eq(FxI18nMessage::getDeleted, 0)
                .last("limit 1"));
        if (row == null || Boolean.FALSE.equals(row.getEnabled())) {
            return defaultTemplate;
        }

        String json = row.getTextI18nJson();
        if (!StringUtils.hasText(json)) {
            return defaultTemplate;
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            String lang = LangContext.get();

            String val = getText(node, lang);
            if (StringUtils.hasText(val)) {
                return val;
            }

            if (StringUtils.hasText(lang)) {
                int idx = lang.indexOf('-');
                if (idx > 0) {
                    val = getText(node, lang.substring(0, idx));
                    if (StringUtils.hasText(val)) {
                        return val;
                    }
                }
            }

            val = getText(node, "zh-CN");
            if (StringUtils.hasText(val)) {
                return val;
            }

            val = getText(node, "zh");
            if (StringUtils.hasText(val)) {
                return val;
            }

            if (node.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = node.fields();
                while (it.hasNext()) {
                    JsonNode v = it.next().getValue();
                    if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
                        return v.asText();
                    }
                }
            }

            return defaultTemplate;
        } catch (Exception e) {
            return defaultTemplate;
        }
    }

    private String getText(JsonNode node, String key) {
        if (node == null || !node.isObject() || !StringUtils.hasText(key)) {
            return null;
        }
        JsonNode v = node.get(key);
        if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
            return v.asText();
        }
        return null;
    }

    @Data
    public static class ResolveRequest {
        private String module;
        private String code;
        private String defaultTemplate;
        private Object[] args;
    }

    @Data
    public static class IdBody {
        private Long id;
    }

    @Data
    public static class I18nMessageQuery {
        private Long pageNum;
        private Long pageSize;
        private String module;
        private String promptCode;
        private Boolean enabled;
    }
}
