package com.forgex.basic.label.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打印状态枚举
 * <p>
 * 定义标签打印记录的状态，用于跟踪打印任务的执行过程
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Getter
@AllArgsConstructor
public enum PrintStatusEnum {

    /**
     * 待打印
     */
    PENDING(0, "PENDING", "待打印"),

    /**
     * 打印中
     */
    PRINTING(1, "PRINTING", "打印中"),

    /**
     * 打印成功
     */
    SUCCESS(2, "SUCCESS", "打印成功"),

    /**
     * 打印失败
     */
    FAILED(3, "FAILED", "打印失败"),

    /**
     * 已取消
     */
    CANCELLED(4, "CANCELLED", "已取消"),

    /**
     * 部分成功（批量打印时部分失败）
     */
    PARTIAL_SUCCESS(5, "PARTIAL_SUCCESS", "部分成功");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态标识
     */
    private final String status;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举值，未找到返回 null
     */
    public static PrintStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PrintStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据状态标识获取枚举
     *
     * @param status 状态标识
     * @return 枚举值，未找到返回 null
     */
    public static PrintStatusEnum getByStatus(String status) {
        if (status == null) {
            return null;
        }
        for (PrintStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态（不能再变更的状态）
     *
     * @return true-终态，false-非终态
     */
    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == PARTIAL_SUCCESS;
    }

    /**
     * 判断是否可以取消
     *
     * @return true-可以取消，false-不可取消
     */
    public boolean canCancel() {
        return this == PENDING || this == PRINTING;
    }

    /**
     * 判断是否可以重试
     *
     * @return true-可以重试，false-不可重试
     */
    public boolean canRetry() {
        return this == FAILED || this == PARTIAL_SUCCESS;
    }
}

