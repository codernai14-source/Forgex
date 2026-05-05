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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 国际化消息控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/i18n/message")
@RequiredArgsConstructor
public class I18nMessageController {

    private final FxI18nMessageMapper i18nMessageMapper;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询国际化消息。
     *
     * @param body 查询参数
     * @return 国际化消息分页结果
     */
    @RequirePerm("sys:i18nMessage:view")
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

    /**
     * 查询数据详情。
     *
     * @param body 请求体，包含 id
     * @return 统一响应结果
     */
    @RequirePerm("sys:i18nMessage:view")
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

    /**
     * 创建数据。
     *
     * @param body 国际化消息
     * @return 统一响应结果
     */
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

    /**
     * 更新数据。
     *
     * @param body 国际化消息
     * @return 统一响应结果
     */
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

    /**
     * 删除数据。
     *
     * @param body 请求体，包含 id
     * @return 统一响应结果
     */
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

    /**
     * 处理resolve。
     *
     * @param body 解析请求参数
     * @return 统一响应结果
     */
    @PostMapping("/resolve")
    public R<String> resolve(@RequestBody ResolveRequest body) {
        if (body == null || !StringUtils.hasText(body.getModule()) || !StringUtils.hasText(body.getCode())) {
            return R.ok("");
        }

        String resolved = resolveTemplate(body.getModule(), body.getCode(), body.getDefaultTemplate());
        return R.ok(StringUtils.hasText(resolved) ? resolved : "");
    }

    /**
     * 获取移动端翻译包。
     * <p>
     * 复用现有 fx_i18n_message 表数据，按模块筛选后返回 key-value 结构，
     * 供 Android 端在本地资源之外补齐业务文案。
     * </p>
     *
     * @param body 请求参数，包含模块编码与语言编码
     * @return 移动端翻译键值对
     */
    @PostMapping("/mobileBundle")
    public R<Map<String, String>> mobileBundle(@RequestBody MobileBundleRequest body) {
        if (body == null || !StringUtils.hasText(body.getLangCode())) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.LANG_CODE_EMPTY);
        }

        String module = StringUtils.hasText(body.getModule()) ? body.getModule().trim() : "mobile";
        String langCode = body.getLangCode().trim();

        Map<String, String> bundle = new LinkedHashMap<>();
        i18nMessageMapper.selectList(new LambdaQueryWrapper<FxI18nMessage>()
                .eq(FxI18nMessage::getModule, module)
                .eq(FxI18nMessage::getDeleted, false)
                .eq(FxI18nMessage::getEnabled, true)
                .orderByAsc(FxI18nMessage::getPromptCode))
            .forEach(row -> {
                String text = resolveTextByLang(row.getTextI18nJson(), langCode);
                if (StringUtils.hasText(text)) {
                    bundle.put(row.getPromptCode(), text);
                }
            });

        return R.ok(bundle);
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

    /**
     * 按指定语言解析翻译文本。
     *
     * @param json 国际化 JSON
     * @param langCode 语言编码
     * @return 匹配到的翻译文本
     */
    private String resolveTextByLang(String json, String langCode) {
        if (!StringUtils.hasText(json) || !StringUtils.hasText(langCode)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(json);

            String direct = getText(node, langCode);
            if (StringUtils.hasText(direct)) {
                return direct;
            }

            int idx = langCode.indexOf('-');
            if (idx > 0) {
                String primary = getText(node, langCode.substring(0, idx));
                if (StringUtils.hasText(primary)) {
                    return primary;
                }
            }

            String zhCn = getText(node, "zh-CN");
            if (StringUtils.hasText(zhCn)) {
                return zhCn;
            }

            String zh = getText(node, "zh");
            if (StringUtils.hasText(zh)) {
                return zh;
            }

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                JsonNode value = fields.next().getValue();
                if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
                    return value.asText();
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
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

    @Data
    public static class MobileBundleRequest {
        private String module;
        private String langCode;
    }
}
