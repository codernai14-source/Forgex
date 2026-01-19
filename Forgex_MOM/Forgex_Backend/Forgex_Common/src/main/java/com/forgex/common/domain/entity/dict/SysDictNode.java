package com.forgex.common.domain.entity.dict;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 字典节点实体
 * <p>
 * 封装字典节点的完整信息，支持树形结构的字典管理。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code parentId} - 父节点ID，用于构建树形结构</li>
 *   <li>{@code dictName} - 字典名称</li>
 *   <li>{@code dictCode} - 字典编码，用于唯一标识</li>
 *   <li>{@code dictValue} - 字典值，用于数据存储</li>
 *   <li>{@code dictValueI18nJson} - 字典值国际化JSON</li>
 *   <li>{@code nodePath} - 节点路径，用于快速定位节点</li>
 *   <li>{@code level} - 层级，表示节点在树中的深度</li>
 *   <li>{@code childrenCount} - 子节点数量</li>
 *   <li>{@code orderNum} - 排序号，用于同级节点的排序</li>
 *   <li>{@code status} - 状态，0=禁用，1=启用</li>
 *   <li>{@code remark} - 备注</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_dict")
public class SysDictNode extends BaseEntity {
    /**
     * 父节点ID
     * <p>用于构建树形结构，根节点的parentId为null。</p>
     */
    private Long parentId;

    /**
     * 字典名称
     * <p>字典节点的名称，用于显示。</p>
     */
    private String dictName;

    /**
     * 字典编码
     * <p>字典节点的唯一标识，用于数据关联。</p>
     */
    private String dictCode;

    /**
     * 字典值
     * <p>字典节点的实际值，用于数据存储和查询。</p>
     */
    private String dictValue;

    /**
     * 字典值国际化JSON
     * <p>包含多语言的字典值配置，用于国际化显示。</p>
     */
    private String dictValueI18nJson;

    /**
     * 节点路径
     * <p>字典节点的完整路径，用于快速定位和查询。</p>
     */
    private String nodePath;

    /**
     * 层级
     * <p>表示节点在树形结构中的深度，根节点为0。</p>
     */
    private Integer level;

    /**
     * 子节点数量
     * <p>当前节点的直接子节点数量。</p>
     */
    private Integer childrenCount;

    /**
     * 排序号
     * <p>用于同级节点的排序，数值越小越靠前。</p>
     */
    private Integer orderNum;

    /**
     * 状态
     * <p>0=禁用，1=启用。</p>
     */
    private Integer status;

    /**
     * 备注
     * <p>字典节点的补充说明信息。</p>
     */
    private String remark;
}

