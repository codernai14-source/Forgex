package com.forgex.common.web;

import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置Controller
 * <p>
 * 提供多语言类型配置的API接口，用于管理系统支持的语言类型。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.service.i18n.I18nLanguageTypeService
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
@RestController
@RequestMapping("/common/i18n/languageType")
@RequiredArgsConstructor
public class I18nLanguageTypeController {

    private final I18nLanguageTypeService languageTypeService;

    /**
     * 获取所有启用的语言类型列表
     *
     * @return 启用的语言类型列表
     */
    @PostMapping("/listEnabled")
    public R<List<FxI18nLanguageType>> listEnabled() {
        return R.ok(languageTypeService.listEnabled());
    }

    /**
     * 获取所有语言类型列表
     *
     * @return 所有语言类型列表
     */
    @PostMapping("/listAll")
    public R<List<FxI18nLanguageType>> listAll() {
        return R.ok(languageTypeService.listAll());
    }

    /**
     * 根据语言代码获取语言类型
     *
     * @param body 请求参数，包含langCode
     * @return 语言类型实体
     */
    @PostMapping("/getByLangCode")
    public R<FxI18nLanguageType> getByLangCode(@RequestBody Map<String, Object> body) {
        String langCode = (String) body.get("langCode");
        if (langCode == null || langCode.isEmpty()) {
            return R.fail(CommonPrompt.LANG_CODE_EMPTY);
        }
        FxI18nLanguageType languageType = languageTypeService.getByLangCode(langCode);
        if (languageType == null) {
            return R.fail(CommonPrompt.LANG_TYPE_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    /**
     * 获取默认语言类型
     *
     * @return 默认语言类型实体
     */
    @PostMapping("/getDefault")
    public R<FxI18nLanguageType> getDefault() {
        FxI18nLanguageType languageType = languageTypeService.getDefault();
        if (languageType == null) {
            return R.fail(CommonPrompt.DEFAULT_LANG_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    /**
     * 创建语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否创建成功
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody FxI18nLanguageType languageType) {
        if (languageType.getLangCode() == null || languageType.getLangCode().isEmpty()) {
            return R.fail(CommonPrompt.LANG_CODE_EMPTY);
        }
        if (languageType.getLangName() == null || languageType.getLangName().isEmpty()) {
            return R.fail(CommonPrompt.LANG_NAME_EMPTY);
        }
        return R.ok(languageTypeService.create(languageType));
    }

    /**
     * 更新语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody FxI18nLanguageType languageType) {
        if (languageType.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(languageTypeService.update(languageType));
    }

    /**
     * 删除语言类型
     *
     * @param body 请求参数，包含id
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        Long id = null;
        if (idObj instanceof Integer) {
            id = ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            id = (Long) idObj;
        } else if (idObj instanceof String) {
            id = Long.parseLong((String) idObj);
        }
        return R.ok(languageTypeService.delete(id));
    }

    /**
     * 设置默认语言
     *
     * @param body 请求参数，包含id
     * @return 是否设置成功
     */
    @PostMapping("/setDefault")
    public R<Boolean> setDefault(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        Long id = null;
        if (idObj instanceof Integer) {
            id = ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            id = (Long) idObj;
        } else if (idObj instanceof String) {
            id = Long.parseLong((String) idObj);
        }
        return R.ok(languageTypeService.setDefault(id));
    }
}
