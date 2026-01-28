package com.forgex.sys.service;

import com.forgex.sys.domain.dto.CodeGenRequestDTO;

import java.util.Map;

/**
 * 代码生成服务接口
 * <p>提供代码生成功能，支持根据数据库表结构生成实体类、Mapper、Service、Controller等代码。</p>
 * <p><strong>支持的模式：</strong></p>
 * <ul>
 *   <li>SINGLE - 单表模式</li>
 *   <li>TREE - 树形结构模式</li>
 *   <li>MASTER_DETAIL - 主从表模式</li>
 *   <li>DETAIL - 从表模式</li>
 * </ul>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public interface CodeGenService {
    /**
     * 预览生成的代码
     * <p>根据请求参数生成代码文件，返回文件名和内容的映射。</p>
     * 
     * @param req 代码生成请求参数
     * @return 文件名到代码内容的映射
     */
    Map<String, String> preview(CodeGenRequestDTO req);

    /**
     * 生成代码压缩包
     * <p>将生成的代码打包成ZIP文件。</p>
     * 
     * @param req 代码生成请求参数
     * @return ZIP文件的字节数组
     */
    byte[] generateZip(CodeGenRequestDTO req);
}

