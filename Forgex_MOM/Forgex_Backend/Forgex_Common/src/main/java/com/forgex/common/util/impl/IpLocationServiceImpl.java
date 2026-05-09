/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */
package com.forgex.common.util.impl;

import com.forgex.common.util.IpLocationService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * IP 地址解析服务实现类。
 * <p>
 * 使用 ip2region 本地离线库解析登录 IP 归属地，避免依赖外部公网接口。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-05-05
 * @see IpLocationService
 */
@Slf4j
@Service
public class IpLocationServiceImpl implements IpLocationService {

    /**
     * ip2region xdb 资源路径。
     */
    private static final String XDB_RESOURCE_PATH = "ip2region/ip2region_v4.xdb";

    /**
     * 未知归属地。
     */
    private static final String UNKNOWN_LOCATION = "未知";

    /**
     * 本地地址。
     */
    private static final String LOCAL_LOCATION = "本地";

    /**
     * 内网地址。
     */
    private static final String PRIVATE_LOCATION = "内网";

    /**
     * ip2region 查询器。
     */
    private volatile Searcher searcher;

    /**
     * 根据 IP 地址获取归属地信息。
     *
     * @param ip IP 地址
     * @return 归属地，解析失败返回“未知”；本地地址返回“本地”；内网地址返回“内网”
     */
    @Override
    public String getLocationByIp(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            log.warn("IP 地址为空或未知，无法解析归属地");
            return UNKNOWN_LOCATION;
        }

        String normalizedIp = ip.trim();
        if (isLocalIp(normalizedIp)) {
            return LOCAL_LOCATION;
        }
        if (isPrivateIpv4(normalizedIp)) {
            return PRIVATE_LOCATION;
        }
        if (normalizedIp.indexOf(':') >= 0) {
            log.debug("当前仅使用 ip2region_v4.xdb 解析 IPv4 地址，跳过 IPv6: {}", normalizedIp);
            return UNKNOWN_LOCATION;
        }

        try {
            Searcher currentSearcher = getSearcher();
            if (currentSearcher == null) {
                return UNKNOWN_LOCATION;
            }
            String region = currentSearcher.search(normalizedIp);
            String location = formatRegion(region);
            log.debug("ip2region 解析成功: {} -> {}", normalizedIp, location);
            return location;
        } catch (Exception e) {
            log.error("ip2region 解析失败: {}, 错误信息: {}", normalizedIp, e.getMessage(), e);
            return UNKNOWN_LOCATION;
        }
    }

    /**
     * 懒加载 ip2region 查询器。
     *
     * @return 查询器，加载失败返回 null
     */
    private Searcher getSearcher() {
        Searcher current = searcher;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = searcher;
            if (current == null) {
                current = loadSearcher();
                searcher = current;
            }
            return current;
        }
    }

    /**
     * 从类路径加载 xdb 并创建查询器。
     *
     * @return 查询器，加载失败返回 null
     */
    private Searcher loadSearcher() {
        ClassPathResource resource = new ClassPathResource(XDB_RESOURCE_PATH);
        if (!resource.exists()) {
            log.error("ip2region xdb 资源不存在: {}", XDB_RESOURCE_PATH);
            return null;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            byte[] buffer = inputStream.readAllBytes();
            Searcher loadedSearcher = Searcher.newWithBuffer(buffer);
            log.info("ip2region xdb 加载完成: resource={}, size={} bytes", XDB_RESOURCE_PATH, buffer.length);
            return loadedSearcher;
        } catch (IOException e) {
            log.error("ip2region xdb 加载失败: {}", XDB_RESOURCE_PATH, e);
            return null;
        }
    }

    /**
     * 格式化 ip2region 的原始返回结果。
     * <p>
     * 官方 xdb 自带数据格式为：国家|省份|城市|ISP。
     * </p>
     *
     * @param region 原始查询结果
     * @return 适合日志展示的归属地字符串
     */
    private String formatRegion(String region) {
        if (!StringUtils.hasText(region)) {
            return UNKNOWN_LOCATION;
        }

        String[] parts = region.split("\\|", -1);
        if (parts.length < 4) {
            return UNKNOWN_LOCATION;
        }

        String country = cleanRegionPart(parts[0]);
        String province = cleanRegionPart(parts[1]);
        String city = cleanRegionPart(parts[2]);

        if (StringUtils.hasText(province) && StringUtils.hasText(city)) {
            return province + " " + city;
        }
        if (StringUtils.hasText(province)) {
            return province;
        }
        if (StringUtils.hasText(city)) {
            return city;
        }
        if (StringUtils.hasText(country) && !"中国".equals(country)) {
            return country;
        }
        return UNKNOWN_LOCATION;
    }

    /**
     * 清理 ip2region 字段值。
     *
     * @param value 原始字段值
     * @return 清理后的字段值
     */
    private String cleanRegionPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String trimmed = value.trim();
        if ("0".equals(trimmed) || "-".equals(trimmed) || "内网IP".equalsIgnoreCase(trimmed)) {
            return "";
        }
        return trimmed;
    }

    /**
     * 判断是否为本地地址。
     *
     * @param ip IP 地址
     * @return 是否为本地地址
     */
    private boolean isLocalIp(String ip) {
        return "127.0.0.1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)
                || "0:0:0:0:0:0:0:1".equals(ip)
                || "::1".equals(ip)
                || "0.0.0.0".equals(ip);
    }

    /**
     * 判断是否为内网 IPv4。
     *
     * @param ip IP 地址
     * @return 是否为内网 IPv4
     */
    private boolean isPrivateIpv4(String ip) {
        String[] segments = ip.split("\\.");
        if (segments.length != 4) {
            return false;
        }

        try {
            int first = Integer.parseInt(segments[0]);
            int second = Integer.parseInt(segments[1]);

            return first == 10
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 168)
                    || (first == 169 && second == 254)
                    || (first == 100 && second >= 64 && second <= 127);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * 关闭查询器。
     */
    @PreDestroy
    public void destroy() {
        Searcher current = searcher;
        if (current == null) {
            return;
        }
        try {
            current.close();
        } catch (IOException e) {
            log.warn("关闭 ip2region 查询器失败", e);
        }
    }
}
