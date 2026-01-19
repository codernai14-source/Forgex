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
package com.forgex.common.util.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.util.IpLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * IP 地址解析服务实现类
 * <p>
 * 使用太平洋电脑网的 IP 查询接口解析 IP 地址归属地
 * 接口地址：http://whois.pconline.com.cn/ipJson.jsp
 * </p>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@Slf4j
@Service
public class IpLocationServiceImpl implements IpLocationService {
    
    /**
     * IP 地址查询接口 URL
     */
    private static final String IP_QUERY_URL = "http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";
    
    /**
     * 连接超时时间（毫秒）
     */
    private static final int CONNECT_TIMEOUT = 3000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private static final int READ_TIMEOUT = 3000;
    
    /**
     * 默认返回值（解析失败时）
     */
    private static final String UNKNOWN_LOCATION = "未知";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 根据 IP 地址获取归属地信息
     * <p>
     * 调用太平洋电脑网接口解析 IP 地址，返回省份+城市格式的归属地信息
     * </p>
     * 
     * @param ip IP 地址
     * @return 归属地（格式：省份 城市，如"广东省 深圳市"），解析失败返回"未知"
     */
    @Override
    public String getLocationByIp(String ip) {
        // 参数校验
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            log.warn("IP 地址为空或未知，无法解析归属地");
            return UNKNOWN_LOCATION;
        }
        
        // 本地 IP 特殊处理
        if (isLocalIp(ip)) {
            log.debug("检测到本地 IP: {}", ip);
            return "本地";
        }
        
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            // 构建请求 URL
            String urlString = String.format(IP_QUERY_URL, ip);
            URL url = new URL(urlString);
            
            // 建立连接
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("IP 地址解析接口返回异常状态码: {}, IP: {}", responseCode, ip);
                return UNKNOWN_LOCATION;
            }
            
            reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            // 解析 JSON 响应
            String location = parseLocation(response.toString(), ip);
            log.info("IP 地址解析成功: {} -> {}", ip, location);
            return location;
            
        } catch (Exception e) {
            log.error("IP 地址解析失败: {}, 错误信息: {}", ip, e.getMessage(), e);
            return UNKNOWN_LOCATION;
        } finally {
            // 关闭资源
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                log.error("关闭连接失败", e);
            }
        }
    }
    
    /**
     * 解析 JSON 响应，提取省份和城市信息
     * <p>
     * 响应格式示例：{"ip":"183.14.132.117","pro":"广东省","city":"深圳市"}
     * </p>
     * 
     * @param jsonResponse JSON 响应字符串
     * @param ip IP 地址（用于日志）
     * @return 归属地（省份 城市）
     */
    private String parseLocation(String jsonResponse, String ip) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // 提取省份和城市
            String province = rootNode.has("pro") ? rootNode.get("pro").asText() : "";
            String city = rootNode.has("city") ? rootNode.get("city").asText() : "";
            
            // 拼接归属地
            if (StringUtils.hasText(province) && StringUtils.hasText(city)) {
                return province + " " + city;
            } else if (StringUtils.hasText(province)) {
                return province;
            } else if (StringUtils.hasText(city)) {
                return city;
            } else {
                log.warn("IP 地址解析响应中未找到省份和城市信息: {}, 响应: {}", ip, jsonResponse);
                return UNKNOWN_LOCATION;
            }
            
        } catch (Exception e) {
            log.error("解析 IP 地址响应 JSON 失败: {}, 响应: {}", ip, jsonResponse, e);
            return UNKNOWN_LOCATION;
        }
    }
    
    /**
     * 判断是否为本地 IP
     * 
     * @param ip IP 地址
     * @return 是否为本地 IP
     */
    private boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip) 
            || "localhost".equalsIgnoreCase(ip)
            || "0:0:0:0:0:0:0:1".equals(ip)
            || "::1".equals(ip);
    }
}
