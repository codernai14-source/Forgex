package com.forgex.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.domain.entity.BasicMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 物料管理主表 Mapper 接口
 * <p>
 * 提供基本的 CRUD 操作，继承自 MyBatis-Plus 的 BaseMapper
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 */
@Mapper
public interface BasicMaterialMapper extends BaseMapper<BasicMaterial> {

    /**
     * 查询指定年份的最大物料序号
     * <p>
     * 逻辑：提取物料编码中最后一个 '-' 后的数字部分，转为整数后取最大值
     * </p>
     *
     * @param year 年份（如 "2026"）
     * @param tenantId 租户 ID
     * @return 最大序号
     */
    @Select("SELECT MAX(CAST(SUBSTRING_INDEX(material_code, '-', -1) AS UNSIGNED)) " +
            "FROM basic_material " +
            "WHERE material_code LIKE CONCAT('MAT-', #{year}, '-%') " +
            "AND tenant_id = #{tenantId} AND deleted = 0")
    Integer selectMaxSeqByYear(@Param("year") String year, @Param("tenantId") Long tenantId);

    /**
     * 校验物料编码唯一性
     *
     * @param materialCode 物料编码
     * @param tenantId 租户 ID
     * @return 记录数
     */
    @Select("SELECT COUNT(1) FROM basic_material " +
            "WHERE material_code = #{materialCode} AND tenant_id = #{tenantId} AND deleted = 0")
    Integer countByMaterialCode(@Param("materialCode") String materialCode, @Param("tenantId") Long tenantId);
    /**
     * 校验物料编码唯一性（排除指定ID）
     * <p>
     * 用于更新物料时校验，排除当前物料本身
     * </p>
     *
     * @param materialCode 物料编码
     * @param id 要排除的物料 ID
     * @param tenantId 租户 ID
     * @return 记录数
     */
    @Select("SELECT COUNT(1) FROM basic_material " +
            "WHERE material_code = #{materialCode} AND id != #{id} AND tenant_id = #{tenantId} AND deleted = 0")
    Integer countByMaterialCodeExclude(@Param("materialCode") String materialCode, @Param("id") Long id, @Param("tenantId") Long tenantId);
}


