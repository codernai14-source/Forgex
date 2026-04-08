package com.forgex.sys.service;

import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;

import java.util.List;

/**
 * 多语言类型配置服务接口
 * <p>
 * 提供多语言类型配置的增删改查功能，用于管理系统支持的语言类型。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
public interface SysI18nLanguageTypeService {

    /**
     * 获取所有启用的语言类型列表
     *
     * @return 启用的语言类型列表
     */
    List<FxI18nLanguageType> listEnabled();

    /**
     * 获取所有语言类型列表
     *
     * @return 所有语言类型列表
     */
    List<FxI18nLanguageType> listAll();

    /**
     * 根据语言代码获取语言类型
     *
     * @param langCode 语言代码
     * @return 语言类型实体
     */
    FxI18nLanguageType getByLangCode(String langCode);

    /**
     * 获取默认语言类型
     *
     * @return 默认语言类型实体
     */
    FxI18nLanguageType getDefault();

    /**
     * 创建语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否创建成功
     */
    Boolean create(FxI18nLanguageType languageType);

    /**
     * 更新语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否更新成功
     */
    Boolean update(FxI18nLanguageType languageType);

    /**
     * 删除语言类型
     *
     * @param id 语言类型ID
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 设置默认语言
     *
     * @param id 语言类型ID
     * @return 是否设置成功
     */
    Boolean setDefault(Long id);
}
