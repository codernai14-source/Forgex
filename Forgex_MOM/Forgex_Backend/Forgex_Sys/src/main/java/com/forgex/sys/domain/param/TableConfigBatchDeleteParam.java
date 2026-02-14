package com.forgex.sys.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 表格配置批量删除参数
 * 
 * @author Forgex
 * @version 1.0.0
 */
@Data
public class TableConfigBatchDeleteParam {
    /** ID列表 */
    private List<Long> ids;
}




