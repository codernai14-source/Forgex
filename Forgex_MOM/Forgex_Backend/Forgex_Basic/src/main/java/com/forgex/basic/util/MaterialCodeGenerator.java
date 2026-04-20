package com.forgex.basic.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.forgex.basic.mapper.BasicMaterialMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 物料编码生成器
 * <p>
 * 支持自动生成物料编码，格式：MAT-YYYY-NNN
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Component
public class MaterialCodeGenerator {

    @Autowired
    private BasicMaterialMapper materialMapper;

    /**
     * 生成物料编码
     * <p>
     * 编码格式：MAT-YYYY-NNN
     * MAT：固定前缀
     * YYYY：年份
     * NNN：当年序号（从 001 开始）
     * </p>
     *
     * @param tenantId 租户 ID
     * @return 生成的物料编码
     */
    public String generateMaterialCode(Long tenantId) {
        String year = String.valueOf(DateUtil.year(DateUtil.date()));
        String prefix = "MAT-" + year + "-";

        // 查询当年最大序号
        Integer maxSeq = materialMapper.selectMaxSeqByYear(year, tenantId);
        Integer nextSeq = (maxSeq == null || maxSeq == 0) ? 1 : maxSeq + 1;

        // 格式化为 3 位数字（修改点：使用 padPre 在左侧补零）
        String seqStr = StrUtil.padPre(String.valueOf(nextSeq), 3, '0');

        return prefix + seqStr;
    }

    /**
     * 校验物料编码是否唯一
     *
     * @param materialCode 物料编码
     * @param tenantId 租户 ID
     * @return true-唯一，false-重复
     */
    public boolean validateMaterialCodeUnique(String materialCode, Long tenantId) {
        Integer count = materialMapper.countByMaterialCode(materialCode, tenantId);
        return count == null || count == 0;
    }
}
