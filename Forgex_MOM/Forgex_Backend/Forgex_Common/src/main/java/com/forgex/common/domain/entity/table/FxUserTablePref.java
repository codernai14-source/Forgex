package com.forgex.common.domain.entity.table;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 用户表格偏好配置实体
 * <p>
 * 封装用户对表格的个性化配置，包括隐藏字段、字段顺序、分页大小和排序配置。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code userId} - 用户ID，标识配置所属用户</li>
 *   <li>{@code tableCode} - 表编码，标识配置所属的表格</li>
 *   <li>{@code hiddenFieldsJson} - 隐藏字段配置（JSON格式）</li>
 *   <li>{@code fieldOrderJson} - 字段顺序配置（JSON格式）</li>
 *   <li>{@code pageSize} - 分页大小，每页显示的条数</li>
 *   <li>{@code sortJson} - 排序配置（JSON格式）</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>用户自定义表格列的显示顺序</li>
 *   <li>用户隐藏不需要显示的表格列</li>
 *   <li>用户自定义默认分页大小</li>
 *   <li>用户自定义默认排序规则</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfig
 */
@Data
@TableName("fx_user_table_pref")
public class FxUserTablePref extends BaseEntity {
    /**
     * 用户ID
     * <p>标识配置所属的用户。</p>
     */
    private Long userId;

    /**
     * 表编码
     * <p>标识配置所属的表格。</p>
     */
    private String tableCode;

    /**
     * 隐藏字段配置
     * <p>JSON格式的隐藏字段列表，用于指定用户不想显示的列。</p>
     */
    private String hiddenFieldsJson;

    /**
     * 字段顺序配置
     * <p>JSON格式的字段顺序列表，用于自定义列的显示顺序。</p>
     */
    private String fieldOrderJson;

    /**
     * 分页大小
     * <p>指定表格的默认分页大小，每页显示的条数。</p>
     */
    private Integer pageSize;

    /**
     * 排序配置
     * <p>JSON格式的排序配置，用于指定默认的排序字段和排序方向。</p>
     */
    private String sortJson;
}

