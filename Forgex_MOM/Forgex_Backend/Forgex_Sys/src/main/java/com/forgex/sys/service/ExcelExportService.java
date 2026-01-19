/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service;

import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Excel导出服务接口
 * <p>提供Excel导出相关的业务逻辑，包括登录日志导出、用户导出等功能。</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-15
 */
public interface ExcelExportService {
    
    /**
     * 导出登录日志
     * 
     * @param body     导出参数，包含tableCode和查询条件
     * @param response HTTP响应对象
     */
    void exportLoginLog(ExcelLoginLogExportDTO body, HttpServletResponse response);
    
    /**
     * 导出用户数据
     * 
     * @param body     导出参数，包含tableCode和查询条件
     * @param response HTTP响应对象
     */
    void exportUser(ExcelUserExportDTO body, HttpServletResponse response);
    
    /**
     * 获取配置对应的内容类型
     * 
     * @param cfg Excel导出配置
     * @return 内容类型字符串
     */
    String getContentType(FxExcelExportConfigDTO cfg);
    
    /**
     * 获取配置对应的文件扩展名
     * 
     * @param cfg Excel导出配置
     * @return 文件扩展名
     */
    String getFileExtension(FxExcelExportConfigDTO cfg);
    
    /**
     * 处理文件名，确保URL安全
     * 
     * @param filename 原始文件名
     * @return URL安全的文件名
     */
    String getSafeFilename(String filename);
    
    /**
     * 将字节数据写入HTTP响应
     * 
     * @param response     HTTP响应对象
     * @param bytes        文件字节数据
     * @param contentType  内容类型
     * @param filename     文件名
     */
    void writeFileToResponse(HttpServletResponse response, byte[] bytes, String contentType, String filename);
}