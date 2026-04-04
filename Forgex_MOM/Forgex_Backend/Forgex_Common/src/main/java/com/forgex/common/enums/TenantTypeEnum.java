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
package com.forgex.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 租户类别枚举
 * <p>
 * 定义系统中租户类型：主租户、客户租户、供应商租户、合作伙伴租户等。
 * <ul>
 *   <li>主租户(MAIN_TENANT)：系统默认租户，具有最高权限，可以管理其他租户</li>
 *   <li>客户租户(CUSTOMER_TENANT)：客户类型的租户</li>
 *   <li>供应商租户(SUPPLIER_TENANT)：供应商类型的租户</li>
 *   <li>合作伙伴租户(PARTNER_TENANT)：合作伙伴类型的租户</li>
 * </ul>
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.auth.domain.entity.SysTenant
 * @see com.forgex.sys.domain.entity.SysTenant
 */
@Getter
public enum TenantTypeEnum {
    
    /**
     * 主租户
     * <p>系统默认租户，具有最高权限，可以管理其他租户</p>
     */
    MAIN_TENANT("MAIN_TENANT", "主租户"),
    
    /**
     * 客户租户
     * <p>客户类型的租户</p>
     */
    CUSTOMER_TENANT("CUSTOMER_TENANT", "客户租户"),
    
    /**
     * 供应商租户
     * <p>供应商类型的租户</p>
     */
    SUPPLIER_TENANT("SUPPLIER_TENANT", "供应商租户"),

    /**
     * 合作伙伴租户
     * <p>合作伙伴类型的租户，与需求文档中 tenant_type=合作伙伴 对应</p>
     */
    PARTNER_TENANT("PARTNER_TENANT", "合作伙伴租户");
    
    /**
     * 枚举值，存储到数据库的值
     */
    @EnumValue
    @JsonValue
    private final String code;
    
    /**
     * 枚举描述
     */
    private final String desc;
    
    /**
     * 构造函数
     * 
     * @param code 枚举编码
     * @param desc 枚举描述
     */
    TenantTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据编码获取枚举
     * 
     * @param code 枚举编码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static TenantTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (TenantTypeEnum typeEnum : TenantTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
    
    /**
     * 判断是否为主租户
     * 
     * @return true表示是主租户，false表示不是
     */
    public boolean isMainTenant() {
        return this == MAIN_TENANT;
    }
    
    /**
     * 判断是否为客户租户
     * 
     * @return true表示是客户租户，false表示不是
     */
    public boolean isCustomerTenant() {
        return this == CUSTOMER_TENANT;
    }
    
    /**
     * 判断是否为供应商租户
     * 
     * @return true表示是供应商租户，false表示不是
     */
    public boolean isSupplierTenant() {
        return this == SUPPLIER_TENANT;
    }

    /**
     * 判断是否为合作伙伴租户
     *
     * @return true 表示是合作伙伴租户，否则为 false
     */
    public boolean isPartnerTenant() {
        return this == PARTNER_TENANT;
    }
}
