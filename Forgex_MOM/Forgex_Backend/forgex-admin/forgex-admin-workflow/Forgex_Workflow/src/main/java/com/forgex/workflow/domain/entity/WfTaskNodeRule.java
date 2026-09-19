package com.forgex.workflow.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审批节点规则实体。
 * <p>
 * 对应表 {@code wf_task_node_rule}，用于配置审批节点的各种规则，
 * 包括审批模式、超时处理、权限控制等。
 * </p>
 *
 * <p>规则类型说明：</p>
 * <ul>
 *   <li>1=固定审批人：按照配置的固定审批人进行审批</li>
 *   <li>2=发起人自选：由发起人自行选择审批人</li>
 *   <li>3=连续多级审批：按照组织架构向上级审批</li>
 *   <li>4=部门负责人：发起人所在部门的负责人</li>
 *   <li>5=指定角色：指定角色下的所有用户</li>
 *   <li>6=指定职位：指定职位下的所有用户</li>
 * </ul>
 *
 * <p>审批模式说明：</p>
 * <ul>
 *   <li>1=会签：所有审批人都同意才通过</li>
 *   <li>2=或签：任一审批人同意即通过</li>
 *   <li>3=会签投票：超过半数同意即通过</li>
 * </ul>
 *
 * <p>超时动作说明：</p>
 * <ul>
 *   <li>1=自动通过：超时后自动审批通过</li>
 *   <li>2=自动驳回：超时后自动驳回</li>
 *   <li>3=转交管理员：超时后转交给管理员处理</li>
 *   <li>4=不处理：超时后不执行任何操作</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 * @see WfTaskNodeConfig 审批节点配置实体
 * @see WfTaskApprovalInstance 审批实例实体
 */
@Data
@TableName("wf_task_node_rule")
public class WfTaskNodeRule {

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 审批节点配置表 ID。
     * <p>
     * 关联 {@code wf_task_node_config} 表的主键。
     * </p>
     *
     * @see WfTaskNodeConfig#id
     */
    private Long nodeConfigId;

    /**
     * 规则名称。
     * <p>
     * 用于标识该规则的名称，如"部门负责人审批"、"财务审批"等。
     * </p>
     */
    private String ruleName;

    /**
     * 规则类型。
     * <p>
     * 1=固定审批人<br>
     * 2=发起人自选<br>
     * 3=连续多级审批<br>
     * 4=部门负责人<br>
     * 5=指定角色<br>
     * 6=指定职位
     * </p>
     */
    private Integer ruleType;

    /**
     * 审批模式。
     * <p>
     * 1=会签<br>
     * 2=或签<br>
     * 3=会签投票
     * </p>
     */
    private Integer approveMode;

    /**
     * 审批阈值。
     * <p>
     * 会签投票场景下，通过审批所需的最小比例或人数。
     * 例如：0.5 表示 50% 的审批人同意即可通过。
     * </p>
     */
    private BigDecimal approvalThreshold;

    /**
     * 排序号。
     * <p>
     * 用于多个规则时的排序，数值越小优先级越高。
     * </p>
     */
    private Integer sortOrder;

    /**
     * 超时时长（小时）。
     * <p>
     * 审批超时时间，单位为小时。
     * 为 null 或 0 时表示不设置超时。
     * </p>
     */
    private Integer timeoutHours;

    /**
     * 超时动作。
     * <p>
     * 1=自动通过<br>
     * 2=自动驳回<br>
     * 3=转交管理员<br>
     * 4=不处理
     * </p>
     */
    private Integer timeoutAction;

    /**
     * 是否允许发起人自选审批人。
     * <p>
     * true=允许，false=不允许。
     * 仅在规则类型为"固定审批人"时有效。
     * </p>
     */
    private Boolean allowInitiatorSelect;

    /**
     * 上级层级。
     * <p>
     * 连续多级审批场景下，指定需要审批的上级层级数。
     * 例如：1 表示直接上级，2 表示上两级。
     * </p>
     */
    private Integer superiorLevel;

    /**
     * 是否允许加签。
     * <p>
     * true=允许加签，false=不允许加签。
     * </p>
     */
    private Boolean allowAddSign;

    /**
     * 是否允许转交。
     * <p>
     * true=允许转交，false=不允许转交。
     * </p>
     */
    private Boolean allowTransfer;

    /**
     * 是否允许委托。
     * <p>
     * true=允许委托，false=不允许委托。
     * </p>
     */
    private Boolean allowDelegate;

    /**
     * 是否允许撤回。
     * <p>
     * true=允许撤回，false=不允许撤回。
     * </p>
     */
    private Boolean allowRecall;

    /**
     * 兜底审批人 ID 集合（JSON 数组）。
     * <p>
     * 当正常审批人无法审批时（如离职、休假等），
     * 使用兜底审批人进行审批。
     * 格式：[10001, 10002, 10003]
     * </p>
     */
    private String fallbackApproverIds;

    /**
     * 扩展配置（JSON）。
     * <p>
     * 用于存储其他扩展配置信息，具体结构根据规则类型而定。
     * 示例：
     * {
     *   "departmentIds": [1, 2, 3],
     *   "roleIds": [10, 20],
     *   "positionIds": [30, 40]
     * }
     * </p>
     */
    private String extraConfig;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0=未删除，1=已删除。
     */
    @TableField("deleted")
    private Integer deleted;
}
