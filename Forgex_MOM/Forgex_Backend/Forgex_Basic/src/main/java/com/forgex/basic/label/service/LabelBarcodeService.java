package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.dto.LabelBarcodeDTO;
import com.forgex.basic.label.domain.entity.LabelBarcode;
import com.forgex.basic.label.domain.param.BarcodeGenerateParam;
import com.forgex.basic.label.domain.param.BarcodeQueryParam;
import com.forgex.basic.label.domain.vo.BarcodeVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签条码 Service 接口
 * <p>
 * 提供标签条码管理相关的业务操作，包括：
 * 1. 条码记录 CRUD 操作
 * 2. 条码查询与追溯
 * 3. 条码状态管理
 * 4. 条码生成
 * 5. 按业务场景统计
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface LabelBarcodeService extends IService<LabelBarcode> {

    /**
     * 分页查询条码记录
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 条码记录分页数据
     */
    IPage<BarcodeVO> pageBarcodes(BarcodeQueryParam param, Long tenantId);

    /**
     * 根据条码号查询条码记录
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     * @return 条码 VO，不存在则返回 null
     */
    BarcodeVO getByBarcodeNo(String barcodeNo, Long tenantId);

    /**
     * 生成条码
     *
     * @param param 条码生成参数
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 生成的条码 DTO
     */
    LabelBarcodeDTO generateBarcode(BarcodeGenerateParam param, Long userId, Long tenantId);

    /**
     * 创建条码记录
     *
     * @param barcode 条码实体
     * @return 条码 ID
     */
    Long createBarcode(LabelBarcode barcode);

    /**
     * 批量创建条码记录
     *
     * @param barcodes 条码列表
     * @param tenantId 租户 ID
     * @return 成功创建的数量
     */
    int batchCreateBarcodes(List<LabelBarcode> barcodes, Long tenantId);

    /**
     * 更新条码状态
     *
     * @param id 条码 ID
     * @param status 状态：0-失效，1-有效
     * @param tenantId 租户 ID
     */
    void updateStatus(Long id, Integer status, Long tenantId);

    /**
     * 使条码失效
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     */
    void invalidateBarcode(String barcodeNo, Long tenantId);

    /**
     * 查询指定物料的条码列表
     *
     * @param materialCode 物料编码
     * @param limit 限制条数
     * @param tenantId 租户 ID
     * @return 条码记录列表
     */
    List<LabelBarcode> listByMaterialCode(String materialCode, Integer limit, Long tenantId);

    /**
     * 查询指定业务场景的条码列表
     *
     * @param businessScene 业务场景
     * @param limit 限制条数
     * @param tenantId 租户 ID
     * @return 条码记录列表
     */
    List<LabelBarcode> listByBusinessScene(String businessScene, Integer limit, Long tenantId);

    List<BarcodeVO> queryByBusinessData(Long materialId, String lotNo, String businessScene,
                                        Long factoryId, Long tenantId);

    @Transactional(rollbackFor = Exception.class)
    void invalidateBarcode(Long barcodeId, Long tenantId);

    /**
     * 校验条码号是否存在
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    boolean existsByBarcodeNo(String barcodeNo, Long tenantId);

    /**
     * 统计指定业务场景的条码数量
     *
     * @param businessScene 业务场景
     * @param tenantId 租户 ID
     * @return 条码数量
     */
    long countByBusinessScene(String businessScene, Long tenantId);

    /**
     * 删除条码记录（逻辑删除）
     *
     * @param id 条码 ID
     * @param tenantId 租户 ID
     */
    void deleteBarcode(Long id, Long tenantId);
}
