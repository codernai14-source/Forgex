package com.forgex.basic.label.handler;

import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.service.ICustomerService;
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
 * 1. 根据 ID 查询关联信息（物料、供应商、客户）
 * 2. 数据格式化处理
 * 3. 补充系统字段（打印时间、操作员等）
 * 4. 数据完整性校验
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
     * @param baseData 基础数据（包含 materialId, supplierId, customerId 等）
     * @return 完整的打印数据 Map
     */
    public Map<String, Object> assemblePrintData(Map<String, Object> baseData) {
        if (baseData == null) {
            baseData = new HashMap<>();
        }

        Map<String, Object> assembledData = new HashMap<>(baseData);

        // 1. 补充物料信息
        enrichMaterialInfo(assembledData);

        // 2. 补充供应商信息
        enrichSupplierInfo(assembledData);

        // 3. 补充客户信息
        enrichCustomerInfo(assembledData);

        // 4. 补充系统字段
        enrichSystemFields(assembledData);

        // 5. 数据格式化
        formatData(assembledData);

        log.debug("数据组装完成，共 {} 个字段", assembledData.size());
        return assembledData;
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
                data.putIfAbsent("supplierName", supplier.getSupplierName());
                data.putIfAbsent("supplierContact", supplier.getContactPerson());
                data.putIfAbsent("supplierPhone", supplier.getContactPhone());
                log.debug("补充供应商信息成功: {}", supplier.getSupplierName());
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
     * 补充系统字段
     *
     * @param data 数据 Map
     */
    private void enrichSystemFields(Map<String, Object> data) {
        // 打印时间
        data.putIfAbsent("printTime", LocalDateTime.now().format(DATETIME_FORMATTER));
        data.putIfAbsent("printDate", LocalDateTime.now().format(DATE_FORMATTER));

        // 如果传入了 operatorId，可以查询操作员姓名
        Long operatorId = getLongValue(data, "operatorId");
        if (operatorId != null) {
            // TODO: 查询用户姓名
            data.putIfAbsent("operatorName", "操作员" + operatorId);
        }
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
        if (data.containsKey("lotNo") && data.get("lotNo") instanceof String) {
            data.put("lotNoUpper", ((String) data.get("lotNo")).toUpperCase());
        }

        // 批次号格式化
        if (data.containsKey("batchNo") && data.get("batchNo") instanceof String) {
            String batchNo = (String) data.get("batchNo");
            data.put("batchNoFormatted", batchNo.replaceAll("-", ""));
        }
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
}
