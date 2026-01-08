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
package com.forgex.gateway.web;

import com.forgex.common.web.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GatewayGlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public Mono<R<Object>> handleNotFound(NotFoundException e) {
        String msg = e.getMessage();
        String name = extractServiceName(msg);
        String m = name == null ? "目标服务未启动" : (name + "服务未启动");
        return Mono.just(R.fail(404, m));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<R<Object>> handleStatus(ResponseStatusException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Mono.just(R.fail(404, "接口不存在"));
        }
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return Mono.just(R.fail(401, "用户没有权限"));
        }
        if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            String msg = e.getReason() == null ? (e.getMessage() == null ? "服务器内部错误" : e.getMessage()) : e.getReason();
            return Mono.just(R.fail(500, msg));
        }
        String msg = e.getReason() == null ? (e.getMessage() == null ? "网关错误" : e.getMessage()) : e.getReason();
        return Mono.just(R.fail(502, msg));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<R<Object>> handleAny(Throwable e) {
        String msg = e.getMessage() == null ? "网关错误" : e.getMessage();
        return Mono.just(R.fail(502, msg));
    }

    private String extractServiceName(String msg) {
        if (msg == null) return null;
        String k = "for ";
        int i = msg.lastIndexOf(k);
        if (i >= 0) {
            String s = msg.substring(i + k.length()).trim();
            int j = s.indexOf(' ');
            return j > 0 ? s.substring(0, j) : s;
        }
        return null;
    }
}
