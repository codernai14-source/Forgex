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
package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 代码生成数据源保存参数
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Data
public class CodegenDatasourceSaveParam {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 数据源编码
     */
    private String datasourceCode;

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * JDBC 地址
     */
    private String jdbcUrl;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 默认 schema/catalog
     */
    private String schemaName;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;
}
