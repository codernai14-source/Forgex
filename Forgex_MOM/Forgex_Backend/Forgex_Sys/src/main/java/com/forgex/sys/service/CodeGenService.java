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

import com.forgex.sys.domain.dto.CodeGenRequestDTO;
import com.forgex.sys.domain.vo.CodegenPreviewVO;

/**
 * 代码生成服务接口
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
public interface CodeGenService {

    /**
     * 生成预览结果
     *
     * @param req 生成请求
     * @return 预览结果
     */
    CodegenPreviewVO preview(CodeGenRequestDTO req);

    /**
     * 生成 ZIP 压缩包
     *
     * @param req 生成请求
     * @return ZIP 字节数组
     */
    byte[] generateZip(CodeGenRequestDTO req);

    /**
     * 按配置记录生成预览结果
     *
     * @param configId 配置记录 ID
     * @return 预览结果
     */
    CodegenPreviewVO previewByConfigId(Long configId);

    /**
     * 按配置记录生成 ZIP
     *
     * @param configId 配置记录 ID
     * @return ZIP 字节数组
     */
    byte[] generateZipByConfigId(Long configId);
}
