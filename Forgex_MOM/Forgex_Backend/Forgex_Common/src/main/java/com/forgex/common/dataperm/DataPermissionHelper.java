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
package com.forgex.common.dataperm;

import com.forgex.common.tenant.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限辅助类
 * <p>提供数据权限相关的工具方法。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class DataPermissionHelper {
    
    /**
     * 数据权限缓存：key=userId, value=DataPermissionInfo
     * <p>缓存用户的数据权限信息，避免频繁查询数据库</p>
     */
    private static final Map<Long, DataPermissionInfo> CACHE = new ConcurrentHashMap<>();
    
    /**
     * 缓存过期时间（毫秒）：5分钟
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000L;
    
    /**
     * 数据权限信息
     */
    public static class DataPermissionInfo {
        private DataScope dataScope;
        private Set<Long> deptIds;
        private Long userId;
        private long cacheTime;
        
        public DataPermissionInfo(DataScope dataScope, Set<Long> deptIds, Long userId) {
            this.dataScope = dataScope;
            this.deptIds = deptIds;
            this.userId = userId;
            this.cacheTime = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - cacheTime > CACHE_EXPIRE_TIME;
        }
        
        public DataScope getDataScope() {
            return dataScope;
        }
        
        public Set<Long> getDeptIds() {
            return deptIds;
        }
        
        public Long getUserId() {
            return userId;
        }
    }
    
    /**
     * 获取当前用户的数据权限信息
     * 
     * @return 数据权限信息，未登录返回null
     */
    public static DataPermissionInfo getCurrentUserPermission() {
        Long userId = UserContext.get();
        if (userId == null) {
            return null;
        }
        
        // 从缓存获取
        DataPermissionInfo info = CACHE.get(userId);
        if (info != null && !info.isExpired()) {
            return info;
        }
        
        // 缓存过期或不存在，需要重新查询
        // 注意：这里返回null，由拦截器负责查询并缓存
        return null;
    }
    
    /**
     * 缓存用户的数据权限信息
     * 
     * @param userId 用户ID
     * @param dataScope 数据权限范围
     * @param deptIds 部门ID列表
     */
    public static void cachePermission(Long userId, DataScope dataScope, Set<Long> deptIds) {
        if (userId == null) {
            return;
        }
        DataPermissionInfo info = new DataPermissionInfo(dataScope, deptIds, userId);
        CACHE.put(userId, info);
        log.debug("缓存数据权限: userId={}, dataScope={}, deptIds={}", userId, dataScope, deptIds);
    }
    
    /**
     * 清除用户的数据权限缓存
     * 
     * @param userId 用户ID
     */
    public static void clearCache(Long userId) {
        if (userId != null) {
            CACHE.remove(userId);
            log.debug("清除数据权限缓存: userId={}", userId);
        }
    }
    
    /**
     * 清除所有数据权限缓存
     */
    public static void clearAllCache() {
        CACHE.clear();
        log.info("清除所有数据权限缓存");
    }
    
    /**
     * 判断当前用户是否有全部数据权限
     * 
     * @return true表示有全部数据权限
     */
    public static boolean hasAllDataPermission() {
        DataPermissionInfo info = getCurrentUserPermission();
        return info != null && info.getDataScope() == DataScope.ALL;
    }
    
    /**
     * 获取当前用户的数据权限部门ID列表
     * 
     * @return 部门ID列表，如果有全部数据权限则返回null
     */
    public static Set<Long> getDataPermissionDeptIds() {
        DataPermissionInfo info = getCurrentUserPermission();
        if (info == null) {
            return Collections.emptySet();
        }
        
        if (info.getDataScope() == DataScope.ALL) {
            return null; // null表示全部数据
        }
        
        return info.getDeptIds();
    }
    
    /**
     * 获取当前用户的数据权限范围
     * 
     * @return 数据权限范围
     */
    public static DataScope getDataScope() {
        DataPermissionInfo info = getCurrentUserPermission();
        return info != null ? info.getDataScope() : DataScope.SELF;
    }
}

