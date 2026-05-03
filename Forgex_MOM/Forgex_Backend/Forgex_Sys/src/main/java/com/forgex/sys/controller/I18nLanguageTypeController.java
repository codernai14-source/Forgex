package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.service.SysI18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysI18nLanguageTypeService
 * @see FxI18nLanguageType
 */
@RestController
@RequestMapping("/sys/i18n/languageType")
@RequiredArgsConstructor
public class I18nLanguageTypeController {

    private final SysI18nLanguageTypeService languageTypeService;

    /**
     * 获取所有启用的语言类型列表。
     *
     * @return 启用的语言类型列表
     */
    @PostMapping("/listEnabled")
    public R<List<FxI18nLanguageType>> listEnabled() {
        return R.ok(languageTypeService.listEnabled());
    }

    /**
     * 获取所有语言类型列表。
     *
     * @return 所有语言类型列表
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/listAll")
    public R<List<FxI18nLanguageType>> listAll() {
        return R.ok(languageTypeService.listAll());
    }

    /**
     * 根据语言代码获取语言类型。
     *
     * @param body 请求参数，包含 langCode
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
     * 获取默认语言类型。
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
     * 创建语言类型。
     *
     * @param languageType 语言类型实体
     * @return 是否创建成功
     */
    @RequirePerm("sys:i18nLanguageType:add")
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
     * 更新语言类型。
     *
     * @param languageType 语言类型实体
     * @return 是否更新成功
     */
    @RequirePerm("sys:i18nLanguageType:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody FxI18nLanguageType languageType) {
        if (languageType.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(languageTypeService.update(languageType));
    }

    /**
     * 删除语言类型。
     *
     * @param body 请求参数，包含 id
     * @return 是否删除成功
     */
    @RequirePerm("sys:i18nLanguageType:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> body) {
        Long id = resolveId(body.get("id"));
        if (id == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(languageTypeService.delete(id));
    }

    /**
     * 设置默认语言。
     *
     * @param body 请求参数，包含 id
     * @return 是否设置成功
     */
    @RequirePerm("sys:i18nLanguageType:setDefault")
    @PostMapping("/setDefault")
    public R<Boolean> setDefault(@RequestBody Map<String, Object> body) {
        Long id = resolveId(body.get("id"));
        if (id == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(languageTypeService.setDefault(id));
    }

    /**
     * 分页查询语言类型列表。
     *
     * @param param 查询参数，包含 pageNum、pageSize、langCode、langName 等
     * @return 分页结果
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/page")
    public R<IPage<FxI18nLanguageType>> page(@RequestBody(required = false) Map<String, Object> param) {
        int pageNum = param != null && param.get("pageNum") != null
                ? Integer.parseInt(param.get("pageNum").toString()) : 1;
        int pageSize = param != null && param.get("pageSize") != null
                ? Integer.parseInt(param.get("pageSize").toString()) : 10;
        String langCode = param != null ? (String) param.get("langCode") : null;
        String langName = param != null ? (String) param.get("langName") : null;
        Boolean enabled = param != null ? (Boolean) param.get("enabled") : null;
        return R.ok(languageTypeService.pageQuery(pageNum, pageSize, langCode, langName, enabled));
    }

    /**
     * 根据 ID 获取语言类型详情。
     *
     * @param param 请求参数，包含 id
     * @return 语言类型实体
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/detail")
    public R<FxI18nLanguageType> detail(@RequestBody IdParam param) {
        if (param.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        FxI18nLanguageType languageType = languageTypeService.getById(param.getId());
        if (languageType == null) {
            return R.fail(CommonPrompt.LANG_TYPE_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    private Long resolveId(Object idObj) {
        if (idObj == null) {
            return null;
        }
        if (idObj instanceof Number number) {
            return number.longValue();
        }
        if (idObj instanceof String idText && !idText.isBlank()) {
            return Long.parseLong(idText);
        }
        return null;
    }
}
