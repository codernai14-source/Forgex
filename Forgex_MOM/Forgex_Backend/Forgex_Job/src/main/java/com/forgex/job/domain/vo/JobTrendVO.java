package com.forgex.job.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job 趋势点。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobTrendVO {
    private String time;
    private Long success;
    private Long failed;
    private Long timeout;
}
