package com.forgex.common.service.excel;

import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel 文件导入导出服务接口。
 * <p>
 * 核心能力包括：<br>
 * 1) 根据 {@link FxExcelImportConfigDTO} 导入配置动态生成导入模板（xlsx）；<br>
 * 2) 根据 {@link FxExcelExportConfigDTO} 导出配置与数据行生成导出文件（xlsx/csv）；<br>
 * 3) 根据导入配置将上传的 Excel 文件解析为指定实体类型的列表。<br>
 * </p>
 *
 * <p>
 * 样式信息通过 JSON 持久化到数据库，字段包括字体、颜色、对齐方式、边框、冻结列等，
 * 在实现类 {@link com.forgex.common.service.excel.impl.ExcelFileServiceImpl} 中统一映射到 Apache POI 的样式对象。<br>
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelImportConfigDTO
 * @see FxExcelExportConfigDTO
 * @see com.forgex.common.service.excel.impl.ExcelFileServiceImpl
 */
public interface ExcelFileService {

    /**
     * 生成导入模板文件（xlsx）。
     *
     * @param config 导入配置（主+子），包含主表信息与字段子项顺序
     * @return 生成的 xlsx 文件字节数组；当配置为空或生成失败时返回长度为 0 的数组
     */
    byte[] buildImportTemplateXlsx(FxExcelImportConfigDTO config);

    /**
     * 生成导出文件（xlsx/csv）。
     *
     * @param config 导出配置（主+子），包含导出表头、多语言与样式配置
     * @param rows   数据行（每行以“导出字段名”映射到数据值），可为空
     * @return 生成的导出文件字节数组；当配置为空或生成失败时返回长度为 0 的数组
     */
    byte[] buildExportFile(FxExcelExportConfigDTO config, List<Map<String, Object>> rows);

    /**
     * 解析导入 Excel 文件为实体集合。
     *
     * <p>
     * 按 {@link FxExcelImportConfigDTO} 中的字段顺序（orderNum）依次读取列值，并根据
     * {@link FxExcelImportConfigItemDTO#getImportField()} 映射到目标实体的同名字段。<br>
     * 字段类型优先参考配置中的 {@code fieldType}，再结合实体字段 Java 类型进行转换。<br>
     * 整行数据全部为空时会被自动忽略。<br>
     * </p>
     *
     * @param config      导入配置（主+子），用于确定列顺序与字段类型
     * @param inputStream Excel 文件输入流（只读，方法内部不负责关闭调用方传入的流）
     * @param targetClass 目标实体类型 Class
     * @param <T>         实体泛型参数
     * @return 解析得到的实体列表；当配置、流或实体类型为空或解析异常时返回空列表
     */
    <T> List<T> parseImportFile(FxExcelImportConfigDTO config, InputStream inputStream, Class<T> targetClass);
}
