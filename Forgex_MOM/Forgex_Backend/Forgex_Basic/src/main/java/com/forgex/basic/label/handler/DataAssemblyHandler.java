package com.forgex.basic.label.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.basic.label.domain.entity.ProductionEngineeringCard;
import com.forgex.basic.label.mapper.ProductionEngineeringCardMapper;
import com.forgex.basic.material.domain.entity.BasicMaterial;
import com.forgex.basic.material.service.IMaterialService;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.service.ISupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据组装处理器
 * <p>
 * 负责在标签打印前组装完整的业务数据，包括：
 * 1. 根据 ID 查询关联信息（物料、供应商、客户、工程卡）
 * 2. 数据格式化处理
 * 3. 补充系统字段，包括打印时间、操作员、防伪编号等
 * 4. 条码生成
 * 5. 数据完整性校验
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataAssemblyHandler {

    private final IMaterialService materialService;
    private final ISupplierService supplierService;
    private final ICustomerService customerService;
    private final ProductionEngineeringCardMapper engineeringCardMapper;

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 组装打印数据
     * <p>
     * 将基础参数扩展为完整的打印数据包
     * </p>
     *
     * @param baseData 基础数据（包含 materialId, supplierId, customerId, engineeringCardNo 等）
     * @return 完整的打印数据 Map
     */
    public Map<String, Object> assemblePrintData(Map<String, Object> baseData) {
        if (baseData == null) {
            baseData = new HashMap<>();
        }

        Map<String, Object> assembledData = new HashMap<>(baseData);

        // 1. 补充工程卡信息（包含工单、LOT号、批次号、销售订单等）
        enrichEngineeringCardInfo(assembledData);

        // 2. 补充物料信息
        enrichMaterialInfo(assembledData);

        // 3. 补充供应商信息
        enrichSupplierInfo(assembledData);

        // 4. 补充客户信息
        enrichCustomerInfo(assembledData);

        // 5. 生成条码号（如果未提供）
        generateBarcodeIfAbsent(assembledData);

        // 6. 补充系统字段
        enrichSystemFields(assembledData);

        // 7. 数据格式化
        formatData(assembledData);

        log.debug("数据组装完成，共 {} 个字段", assembledData.size());
        return assembledData;
    }

    /**
     * 补充工程卡信息
     * <p>
     * 从工程卡实体中提取工单号、LOT号、批次号、销售订单号、总包等关键信息
     * </p>
     *
     * @param data 数据 Map
     */
    private void enrichEngineeringCardInfo(Map<String, Object> data) {
        String engineeringCardNo = getStringValue(data, "engineeringCardNo");
        if (engineeringCardNo == null) {
            return;
        }

        try {
            ProductionEngineeringCard card = getEngineeringCardByNo(engineeringCardNo);
            if (card != null) {
                // 基础字段
                data.putIfAbsent("cardNo", card.getCardNo());
                data.putIfAbsent("workOrderNo", card.getWorkOrderNo());
                data.putIfAbsent("salesOrderNo", card.getSalesOrderNo());
                data.putIfAbsent("batchNo", card.getBatchNo());
                data.putIfAbsent("lotNo", card.getLotNo());
                data.putIfAbsent("weekCode", card.getWeekCode());

                // 数量相关
                data.putIfAbsent("planQty", card.getPlanQty());
                data.putIfAbsent("actualQty", card.getActualQty());
                data.putIfAbsent("spq", card.getSpq());
                data.putIfAbsent("pq", card.getPq());

                // 生产信息
                data.putIfAbsent("productionLine", card.getProductionLine());
                data.putIfAbsent("workCenter", card.getWorkCenter());
                data.putIfAbsent("startDate", card.getStartDate());
                data.putIfAbsent("endDate", card.getEndDate());

                // 如果工程卡中有物料信息，也补充
                if (data.get("materialId") == null && card.getMaterialId() != null) {
                    data.put("materialId", card.getMaterialId());
                }
                data.putIfAbsent("materialCode", card.getMaterialCode());
                data.putIfAbsent("materialName", card.getMaterialName());

                // 如果工程卡中有客户信息，也补充
                if (data.get("customerId") == null && card.getCustomerId() != null) {
                    data.put("customerId", card.getCustomerId());
                }
                data.putIfAbsent("customerCode", card.getCustomerCode());

                log.debug("补充工程卡信息成功: {}", card.getCardNo());
            }
        } catch (Exception e) {
            log.warn("查询工程卡信息失败，engineeringCardNo: {}", engineeringCardNo, e);
        }
    }

    /**
     * 补充物料信息
     *
     * @param data 数据 Map
     */
    private void enrichMaterialInfo(Map<String, Object> data) {
        Long materialId = getLongValue(data, "materialId");
        if (materialId == null) {
            return;
        }

        try {
            BasicMaterial material = materialService.getById(materialId);
            if (material != null) {
                data.putIfAbsent("materialCode", material.getMaterialCode());
                data.putIfAbsent("materialName", material.getMaterialName());
                data.putIfAbsent("materialType", material.getMaterialType());
                data.putIfAbsent("specification", material.getSpecification());
                data.putIfAbsent("unit", material.getUnit());
                data.putIfAbsent("brand", material.getBrand());
                log.debug("补充物料信息成功: {}", material.getMaterialName());
            }
        } catch (Exception e) {
            log.warn("查询物料信息失败，materialId: {}", materialId, e);
        }
    }

    /**
     * 补充供应商信息
     *
     * @param data 数据 Map
     */
    private void enrichSupplierInfo(Map<String, Object> data) {
        Long supplierId = getLongValue(data, "supplierId");
        if (supplierId == null) {
            return;
        }

        try {
            BasicSupplier supplier = supplierService.getById(supplierId);
            if (supplier != null) {
                data.putIfAbsent("supplierCode", supplier.getSupplierCode());
                data.putIfAbsent("supplierName", supplier.getSupplierFullName());
                data.putIfAbsent("supplierContact", supplier.getPrimaryContact());
                data.putIfAbsent("supplierPhone", supplier.getContactPhone());
                log.debug("补充供应商信息成功: {}", supplier.getSupplierFullName());
            }
        } catch (Exception e) {
            log.warn("查询供应商信息失败，supplierId: {}", supplierId, e);
        }
    }

    /**
     * 补充客户信息
     *
     * @param data 数据 Map
     */
    private void enrichCustomerInfo(Map<String, Object> data) {
        Long customerId = getLongValue(data, "customerId");
        if (customerId == null) {
            return;
        }

        try {
            BasicCustomer customer = customerService.getById(customerId);
            if (customer != null) {
                data.putIfAbsent("customerCode", customer.getCustomerCode());
                data.putIfAbsent("customerName", customer.getCustomerName());
                data.putIfAbsent("customerContact", customer.getContactPerson());
                data.putIfAbsent("customerPhone", customer.getContactPhone());
                log.debug("补充客户信息成功: {}", customer.getCustomerName());
            }
        } catch (Exception e) {
            log.warn("查询客户信息失败，customerId: {}", customerId, e);
        }
    }

    /**
     * 生成条码号（如果未提供）
     * <p>
     * 根据业务场景自动生成条码号，支持多种编码规则
     * </p>
     *
     * @param data 数据 Map
     */
    private void generateBarcodeIfAbsent(Map<String, Object> data) {
        if (data.containsKey("barcodeNo") && data.get("barcodeNo") != null) {
            return;
        }

        try {
            String templateType = getStringValue(data, "templateType");
            String barcodeNo = generateBarcodeNo(templateType, data);
            data.put("barcodeNo", barcodeNo);
            log.debug("自动生成条码号: {}", barcodeNo);
        } catch (Exception e) {
            log.warn("生成条码号失败", e);
        }
    }

    /**
     * 生成条码号
     *
     * @param templateType 模板类型
     * @param data 业务数据
     * @return 条码号
     */
    private String generateBarcodeNo(String templateType, Map<String, Object> data) {
        // 根据模板类型生成不同规则的条码
        String prefix = switch (templateType != null ? templateType : "GENERAL") {
            case "INCOMING" -> "IC";
            case "PRODUCT" -> "PD";
            case "LOT" -> "LT";
            case "SPQ_INNER" -> "SI";
            case "PQ_OUTER" -> "PO";
            case "OVERSEAS_OUTER" -> "OO";
            case "ENG_CARD_PACKAGE" -> "EC";
            default -> "LB";
        };

        // 条码规则：前缀 + 时间戳 + 随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));

        return prefix + timestamp + random;
    }

    /**
     * 补充系统字段
     *
     * @param data 数据 Map
     */
    private void enrichSystemFields(Map<String, Object> data) {
        LocalDateTime now = LocalDateTime.now();

        // 打印时间
        data.putIfAbsent("printTime", now.format(DATETIME_FORMATTER));
        data.putIfAbsent("printDate", now.format(DATE_FORMATTER));

        // 自动生成周别（如果未提供）
        data.putIfAbsent("weekCode", calculateWeekCode(now));

        // 如果传入了 operatorId，可以查询操作员姓名
        Long operatorId = getLongValue(data, "operatorId");
        if (operatorId != null) {
            // TODO: 查询用户姓名
            data.putIfAbsent("operatorName", "操作员" + operatorId);
        }

        // 自动计算出库箱号（如果提供了总箱数）
        if (data.containsKey("boxNo") && data.get("boxNo") == null) {
            data.putIfAbsent("boxNo", "001");
        }
    }

    /**
     * 计算周别代码
     *
     * @param date 日期
     * @return 周别代码（格式：2026W15）
     */
    private String calculateWeekCode(LocalDateTime date) {
        int year = date.getYear();
        int week = date.plusDays(3).getDayOfYear() / 7 + 1;
        if (week > 52) {
            week = 52;
        }
        return String.format("%dW%02d", year, week);
    }

    /**
     * 数据格式化
     *
     * @param data 数据 Map
     */
    private void formatData(Map<String, Object> data) {
        // 数量格式化（保留2位小数）
        if (data.containsKey("quantity")) {
            Object quantity = data.get("quantity");
            if (quantity instanceof Number) {
                data.put("quantityFormatted", String.format("%.2f", ((Number) quantity).doubleValue()));
            }
        }

        // LOT 号大写
        if (data.containsKey("lotNo") && data.get("lotNo") instanceof String lotNo) {
            data.put("lotNoUpper", lotNo.toUpperCase());
            data.put("lotNoFormatted", lotNo.replace("-", ""));
        }

        // 批次号格式化
        if (data.containsKey("batchNo") && data.get("batchNo") instanceof String batchNo) {
            data.put("batchNoFormatted", batchNo.replace("-", ""));
        }

        // 销售订单号格式化，去掉分隔符。
        if (data.containsKey("salesOrderNo") && data.get("salesOrderNo") instanceof String soNo) {
            data.put("salesOrderNoFormatted", soNo.replace("-", ""));
        }

        // 工程卡号格式化
        if (data.containsKey("cardNo") && data.get("cardNo") instanceof String cardNo) {
            data.put("cardNoFormatted", cardNo.replace("-", ""));
        }

        // 条码内容拼接（支持多种格式）
        buildBarcodeContent(data);
    }

    /**
     * 构建条码内容
     *
     * @param data 数据 Map
     */
    private void buildBarcodeContent(Map<String, Object> data) {
        String templateType = getStringValue(data, "templateType");
        StringBuilder barcodeContent = new StringBuilder();

        switch (templateType != null ? templateType : "GENERAL") {
            case "INCOMING" -> {
                // 来料标签：供应商+物料+批次
                barcodeContent.append(getStringValue(data, "supplierCode", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "materialCode", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "batchNo", ""));
            }
            case "LOT" -> {
                // LOT标签：LOT号+批次
                barcodeContent.append(getStringValue(data, "lotNo", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "batchNo", ""));
            }
            case "ENG_CARD_PACKAGE" -> {
                // 工程卡包装：工程卡号+LOT+箱号
                barcodeContent.append(getStringValue(data, "cardNo", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "lotNo", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "boxNo", "001"));
            }
            default -> {
                // 默认：物料+批次
                barcodeContent.append(getStringValue(data, "materialCode", ""));
                barcodeContent.append("|");
                barcodeContent.append(getStringValue(data, "batchNo", ""));
            }
        }

        data.putIfAbsent("barcodeContent", barcodeContent.toString());
    }

    /**
     * 验证数据完整性
     *
     * @param data 数据 Map
     * @param requiredFields 必填字段列表
     * @return 缺失的字段列表
     */
    public List<String> validateDataCompleteness(Map<String, Object> data, List<String> requiredFields) {
        List<String> missingFields = new ArrayList<>();

        for (String field : requiredFields) {
            if (!data.containsKey(field) || data.get(field) == null) {
                missingFields.add(field);
            }
        }

        if (!missingFields.isEmpty()) {
            log.warn("数据不完整，缺少字段: {}", missingFields);
        }

        return missingFields;
    }

    /**
     * 从 Map 中获取 Long 值
     *
     * @param data 数据 Map
     * @param key 键
     * @return Long 值
     */
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Map 中获取 String 值
     *
     * @param data 数据 Map
     * @param key 键
     * @return String 值
     */
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从 Map 中获取 String 值（带默认值）
     *
     * @param data 数据 Map
     * @param key 键
     * @param defaultValue 默认值
     * @return String 值
     */
    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * 根据工程卡号查询工程卡
     *
     * @param cardNo 工程卡号
     * @return 工程卡实体
     */
    private ProductionEngineeringCard getEngineeringCardByNo(String cardNo) {
        if (cardNo == null) {
            return null;
        }

        LambdaQueryWrapper<ProductionEngineeringCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionEngineeringCard::getCardNo, cardNo);

        return engineeringCardMapper.selectOne(wrapper);
    }
}
