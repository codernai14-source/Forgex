package com.forgex.sys.service.impl;

import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.service.CodeGenService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现类
 * <p>
 * 提供基于数据库表结构的代码生成功能，支持生成实体、Mapper、Service和Controller等基础代码。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>根据表名生成实体类代码</li>
 *   <li>生成MyBatis-Plus的Mapper接口代码</li>
 *   <li>生成Service接口代码</li>
 *   <li>生成Spring Boot的Controller代码</li>
 *   <li>支持树形结构的DTO生成</li>
 *   <li>支持主从结构的Item实体生成</li>
 *   <li>生成README文档</li>
 *   <li>支持ZIP打包下载</li>
 * </ul>
 * <p><strong>生成模式：</strong></p>
 * <ul>
 *   <li>{@code SINGLE} - 单表模式，只生成基础实体和相关代码</li>
 *   <li>{@code TREE} - 树形模式，生成树形结构的DTO</li>
 *   <li>{@code MASTER_DETAIL} - 主从模式，生成主表和从表Item实体</li>
 *   <li>{@code DETAIL} - 从表模式，只生成Item实体</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see CodeGenService
 * @see CodeGenRequestDTO
 */
@Service
public class CodeGenServiceImpl implements CodeGenService {

    /**
     * 预览生成的代码
     * <p>
     * 根据请求参数生成代码文件，但不打包成ZIP。
     * 返回文件名到代码内容的映射，用于前端预览。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：表名不能为空</li>
     *   <li>确定实体类名：如果未指定，将表名转换为驼峰命名</li>
     *   <li>确定基础包名：如果未指定，使用默认包名</li>
     *   <li>确定生成模式：如果未指定，使用SINGLE模式</li>
     *   <li>生成README文档</li>
     *   <li>生成实体类代码</li>
     *   <li>生成Mapper接口代码</li>
     *   <li>生成Service接口代码</li>
     *   <li>生成Controller代码</li>
     *   <li>如果是TREE模式，生成树形DTO</li>
     *   <li>如果是MASTER_DETAIL或DETAIL模式，生成Item实体</li>
     *   <li>返回文件映射</li>
     * </ol>
     * 
     * @param req 代码生成请求参数
     * @return 文件名到代码内容的映射，参数无效时返回空Map
     */
    @Override
    public Map<String, String> preview(CodeGenRequestDTO req) {
        // 初始化文件映射
        Map<String, String> files = new LinkedHashMap<>();
        
        // 参数校验：表名不能为空
        if (req == null || !StringUtils.hasText(req.getTableName())) {
            return files;
        }
        
        // 确定实体类名：如果未指定，将表名转换为驼峰命名（首字母大写）
        String entity = StringUtils.hasText(req.getEntityName()) ? req.getEntityName() : toCamel(req.getTableName(), true);
        
        // 确定基础包名：如果未指定，使用默认包名
        String basePackage = StringUtils.hasText(req.getBasePackage()) ? req.getBasePackage() : "com.forgex.codegen";
        
        // 确定生成模式：如果未指定，使用SINGLE模式
        String mode = StringUtils.hasText(req.getMode()) ? req.getMode().trim().toUpperCase(Locale.ROOT) : "SINGLE";

        // 生成README文档
        files.put("README.md", buildReadme(req, entity));
        
        // 生成实体类代码
        files.put("backend/" + basePackage.replace('.', '/') + "/domain/entity/" + entity + ".java",
                buildEntity(basePackage, entity));
        
        // 生成Mapper接口代码
        files.put("backend/" + basePackage.replace('.', '/') + "/mapper/" + entity + "Mapper.java",
                buildMapper(basePackage, entity));
        
        // 生成Service接口代码
        files.put("backend/" + basePackage.replace('.', '/') + "/service/" + entity + "Service.java",
                buildService(basePackage, entity));
        
        // 生成Controller代码
        files.put("backend/" + basePackage.replace('.', '/') + "/controller/" + entity + "Controller.java",
                buildController(basePackage, entity));

        // 如果是TREE模式，生成树形DTO
        if ("TREE".equals(mode)) {
            files.put("backend/" + basePackage.replace('.', '/') + "/domain/dto/" + entity + "TreeDTO.java",
                    buildTreeDto(basePackage, entity, req.getParentIdField(), req.getTreeNameField()));
        }
        
        // 如果是MASTER_DETAIL或DETAIL模式，生成Item实体
        if ("MASTER_DETAIL".equals(mode) || "MASTER-DETAIL".equals(mode) || "DETAIL".equals(mode)) {
            files.put("backend/" + basePackage.replace('.', '/') + "/domain/entity/" + entity + "Item.java",
                    buildEntity(basePackage, entity + "Item"));
        }

        // 返回文件映射
        return files;
    }

    /**
     * 生成代码ZIP包
     * <p>
     * 根据请求参数生成代码文件，并打包成ZIP格式。
     * 返回ZIP字节数组，用于前端下载。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>调用preview方法生成所有代码文件</li>
     *   <li>创建ByteArrayOutputStream作为ZIP输出流</li>
     *   <li>创建ZipOutputStream包装输出流</li>
     *   <li>遍历所有文件，创建ZipEntry</li>
     *   <li>将文件内容转换为UTF-8字节数组</li>
     *   <li>写入字节数组到ZIP</li>
     *   <li>关闭当前ZipEntry</li>
     *   <li>完成ZIP写入</li>
     *   <li>返回ZIP字节数组</li>
     * </ol>
     * 
     * @param req 代码生成请求参数
     * @return ZIP字节数组，生成失败时返回空数组
     */
    @Override
    public byte[] generateZip(CodeGenRequestDTO req) {
        // 调用preview方法生成所有代码文件
        Map<String, String> files = preview(req);
        
        try (
            // 创建ByteArrayOutputStream作为ZIP输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            // 创建ZipOutputStream包装输出流
            ZipOutputStream zos = new ZipOutputStream(baos)
        ) {
            // 遍历所有文件
            for (Map.Entry<String, String> e : files.entrySet()) {
                // 创建ZipEntry
                ZipEntry entry = new ZipEntry(e.getKey());
                zos.putNextEntry(entry);
                
                // 将文件内容转换为UTF-8字节数组
                byte[] bytes = (e.getValue() == null ? "" : e.getValue()).getBytes(StandardCharsets.UTF_8);
                
                // 写入字节数组到ZIP
                zos.write(bytes);
                
                // 关闭当前ZipEntry
                zos.closeEntry();
            }
            
            // 完成ZIP写入
            zos.finish();
            
            // 返回ZIP字节数组
            return baos.toByteArray();
        } catch (Exception ex) {
            // 生成失败时返回空数组
            return new byte[0];
        }
    }

    /**
     * 构建README文件内容
     * <p>
     * 生成包含基本信息的README文档，用于说明生成的代码。
     * </p>
     * 
     * @param req 代码生成请求参数
     * @param entity 实体类名称
     * @return README文件内容
     */
    private String buildReadme(CodeGenRequestDTO req, String entity) {
        // 获取生成模式
        String mode = req == null ? null : req.getMode();
        
        // 构建README内容
        return "# Forgex CodeGen\n\n"
                + "- tableName: " + (req == null ? "" : req.getTableName()) + "\n"
                + "- mode: " + (mode == null ? "SINGLE" : mode) + "\n"
                + "- entity: " + entity + "\n";
    }

    /**
     * 构建实体类代码
     * <p>
     * 生成包含Lombok @Data注解的实体类代码。
     * </p>
     * 
     * @param basePackage 基础包名
     * @param entity 实体类名称
     * @return 实体类代码字符串
     */
    private String buildEntity(String basePackage, String entity) {
        return "package " + basePackage + ".domain.entity;\n\n"
                + "import lombok.Data;\n\n"
                + "@Data\n"
                + "public class " + entity + " {\n"
                + "}\n";
    }

    /**
     * 构建Mapper接口代码
     * <p>
     * 生成继承MyBatis-Plus BaseMapper的Mapper接口代码。
     * </p>
     * 
     * @param basePackage 基础包名
     * @param entity 实体类名称
     * @return Mapper接口代码字符串
     */
    private String buildMapper(String basePackage, String entity) {
        return "package " + basePackage + ".mapper;\n\n"
                + "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n"
                + "import " + basePackage + ".domain.entity." + entity + ";\n"
                + "import org.apache.ibatis.annotations.Mapper;\n\n"
                + "@Mapper\n"
                + "public interface " + entity + "Mapper extends BaseMapper<" + entity + "> {\n"
                + "}\n";
    }

    /**
     * 构建Service接口代码
     * <p>
     * 生成基础的Service接口代码，不包含方法定义。
     * </p>
     * 
     * @param basePackage 基础包名
     * @param entity 实体类名称
     * @return Service接口代码字符串
     */
    private String buildService(String basePackage, String entity) {
        return "package " + basePackage + ".service;\n\n"
                + "public interface " + entity + "Service {\n"
                + "}\n";
    }

    /**
     * 构建Controller代码
     * <p>
     * 生成Spring Boot的Controller代码，包含基本的ping方法。
     * </p>
     * 
     * @param basePackage 基础包名
     * @param entity 实体类名称
     * @return Controller代码字符串
     */
    private String buildController(String basePackage, String entity) {
        // 构建请求路径（将实体名转换为小写驼峰）
        String path = "/" + toCamel(entity, false).toLowerCase(Locale.ROOT);
        
        return "package " + basePackage + ".controller;\n\n"
                + "import com.forgex.common.web.R;\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RestController;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"" + path + "\")\n"
                + "public class " + entity + "Controller {\n"
                + "    @RequestMapping(\"/ping\")\n"
                + "    public R<String> ping() {\n"
                + "        return R.ok(\"ok\");\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 构建树形DTO代码
     * <p>
     * 生成用于树形结构显示的DTO代码，包含id、parentId和name字段。
     * </p>
     * 
     * @param basePackage 基础包名
     * @param entity 实体类名称
     * @param parentIdField 父ID字段名
     * @param nameField 名称字段名
     * @return 树形DTO代码字符串
     */
    private String buildTreeDto(String basePackage, String entity, String parentIdField, String nameField) {
        // 确定父ID字段名：如果未指定，使用parentId
        String pid = StringUtils.hasText(parentIdField) ? parentIdField : "parentId";
        
        // 确定名称字段名：如果未指定，使用name
        String name = StringUtils.hasText(nameField) ? nameField : "name";
        
        return "package " + basePackage + ".domain.dto;\n\n"
                + "import lombok.Data;\n\n"
                + "@Data\n"
                + "public class " + entity + "TreeDTO {\n"
                + "    private Long id;\n"
                + "    private Long " + pid + ";\n"
                + "    private String " + name + ";\n"
                + "}\n";
    }

    /**
     * 将字符串转换为驼峰命名
     * <p>
     * 将下划线、连字符或空格分隔的字符串转换为驼峰命名格式。
     * </p>
     * <p><strong>转换规则：</strong></p>
     * <ul>
     *   <li>按下划线、连字符或空格分割字符串</li>
     *   <li>每个单词首字母大写，其余小写</li>
     *   <li>如果upperFirst为false，第一个单词首字母小写</li>
     * </ul>
     * 
     * @param s 原始字符串
     * @param upperFirst 是否首字母大写
     * @return 驼峰命名字符串
     */
    private String toCamel(String s, boolean upperFirst) {
        // 字符串为空，直接返回
        if (!StringUtils.hasText(s)) {
            return s;
        }
        
        // 按下划线、连字符或空格分割字符串
        String[] parts = s.split("[_\\-\\s]+");
        
        // 构建结果字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            
            // 跳过空字符串
            if (p.isEmpty()) continue;
            
            // 转换为小写
            String lower = p.toLowerCase(Locale.ROOT);
            
            // 处理首字母大小写
            if (i == 0 && !upperFirst) {
                // 第一个单词且不需要首字母大写，保持小写
                sb.append(lower);
            } else {
                // 其他情况，首字母大写，其余小写
                sb.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
            }
        }
        return sb.toString();
    }
}

