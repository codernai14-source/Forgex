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
package com.forgex.common.api.example;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.service.UserInfoService;
import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 使用示例：Controller
 * <p>演示如何使用 Feign API 和自动填充功能</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@RestController
@RequestMapping("/example")
@RequiredArgsConstructor
public class ExampleController {
    
    private final UserInfoService userInfoService;
    
    /**
     * 示例1：直接使用 UserInfoService 查询用户信息
     */
    @GetMapping("/user/{userId}")
    public R<UserInfoDTO> getUserInfo(@PathVariable Long userId) {
        UserInfoDTO userInfo = userInfoService.getUserById(userId);
        return R.ok(userInfo);
    }
    
    /**
     * 示例2：查询用户名
     */
    @GetMapping("/username/{userId}")
    public R<String> getUsername(@PathVariable Long userId) {
        String username = userInfoService.getUsernameById(userId);
        return R.ok(username);
    }
    
    /**
     * 示例3：批量查询用户名映射
     */
    @PostMapping("/username-map")
    public R<Map<Long, String>> getUsernameMap(@RequestBody List<Long> userIds) {
        Map<Long, String> usernameMap = userInfoService.getUsernameMap(userIds);
        return R.ok(usernameMap);
    }
    
    /**
     * 示例4：返回单个对象，自动填充用户名
     * <p>返回的 MessageVOExample 对象中的 senderName、receiverName、createByName 会自动填充</p>
     */
    @GetMapping("/message/{id}")
    public R<MessageVOExample> getMessage(@PathVariable Long id) {
        // 模拟从数据库查询消息
        MessageVOExample message = new MessageVOExample();
        message.setId(id);
        message.setTitle("测试消息");
        message.setContent("这是一条测试消息");
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setCreateBy(1L);
        
        // 返回时，切面会自动填充 senderName、receiverName、createByName
        return R.ok(message);
    }
    
    /**
     * 示例5：返回列表，自动填充用户名
     * <p>列表中每个对象的用户名字段都会自动填充</p>
     */
    @GetMapping("/messages")
    public R<List<MessageVOExample>> getMessages() {
        // 模拟从数据库查询消息列表
        MessageVOExample message1 = new MessageVOExample();
        message1.setId(1L);
        message1.setTitle("消息1");
        message1.setSenderId(1L);
        message1.setReceiverId(2L);
        message1.setCreateBy(1L);
        
        MessageVOExample message2 = new MessageVOExample();
        message2.setId(2L);
        message2.setTitle("消息2");
        message2.setSenderId(2L);
        message2.setReceiverId(3L);
        message2.setCreateBy(2L);
        
        List<MessageVOExample> messages = Arrays.asList(message1, message2);
        
        // 返回时，切面会自动批量查询并填充所有用户名
        return R.ok(messages);
    }
    
    /**
     * 示例6：返回分页对象，自动填充用户名
     * <p>分页对象中的记录列表会自动填充用户名</p>
     */
    @GetMapping("/messages/page")
    public R<IPage<MessageVOExample>> getMessagesPage() {
        // 模拟分页查询
        Page<MessageVOExample> page = new Page<>(1, 10);
        
        MessageVOExample message1 = new MessageVOExample();
        message1.setId(1L);
        message1.setTitle("消息1");
        message1.setSenderId(1L);
        message1.setReceiverId(2L);
        message1.setCreateBy(1L);
        
        MessageVOExample message2 = new MessageVOExample();
        message2.setId(2L);
        message2.setTitle("消息2");
        message2.setSenderId(2L);
        message2.setReceiverId(3L);
        message2.setCreateBy(2L);
        
        page.setRecords(Arrays.asList(message1, message2));
        page.setTotal(2);
        
        // 返回时，切面会自动填充分页记录中的用户名
        return R.ok(page);
    }
}

