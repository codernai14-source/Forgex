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
package com.forgex.common.util;

/**
 * IP 地址解析服务接口
 * <p>
 * 提供 IP 地址到地理位置的解析功能
 * </p>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
public interface IpLocationService {
    
    /**
     * 根据 IP 地址获取归属地信息
     * <p>
     * 调用第三方接口解析 IP 地址，返回省份+城市格式的归属地信息
     * </p>
     * 
     * @param ip IP 地址
     * @return 归属地（格式：省份 城市，如"广东省 深圳市"），解析失败返回"未知"
     */
    String getLocationByIp(String ip);
}
