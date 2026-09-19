package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysKmsKey;
import com.forgex.sys.mapper.SysKmsKeyMapper;
import com.forgex.sys.service.KmsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * KMS 管理接口。
 * <p>
 * 密钥明文永不返回前端，仅回传别名、算法、状态与轮换时间。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see KmsService
 */
@RestController
@RequestMapping("/kms")
@RequiredArgsConstructor
public class KmsController {

    private final KmsService kmsService;
    private final SysKmsKeyMapper keyMapper;

    /**
     * 分页查询密钥元数据。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @RequirePerm("sys:kms:view")
    @PostMapping("/page")
    public R<Page<Map<String, Object>>> page(@RequestBody KmsQuery query) {
        Page<SysKmsKey> page = new Page<>(query == null || query.getCurrent() == null ? 1 : query.getCurrent(),
                query == null || query.getSize() == null ? 20 : query.getSize());
        LambdaQueryWrapper<SysKmsKey> wrapper = new LambdaQueryWrapper<SysKmsKey>()
                .like(query != null && query.getAlias() != null, SysKmsKey::getKeyAlias, query == null ? null : query.getAlias())
                .orderByDesc(SysKmsKey::getId);
        Page<SysKmsKey> raw = keyMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> vo = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        vo.setRecords(raw.getRecords().stream().map(this::toSafeView).toList());
        return R.ok(vo);
    }

    /**
     * 创建密钥。
     *
     * @param body 创建参数
     * @return 密钥 ID
     */
    @OperationLog(module = "sys", menuPath = "/system/kms", operationType = OperationType.ADD, detailTemplateCode = "KMS_CREATE")
    @RequirePerm("sys:kms:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody KmsCreateBody body) {
        return R.ok(kmsService.generateKey(body.getAlias(), body.getKeyType(), body.getKeySize(), body.getDescription()));
    }

    /**
     * 轮换密钥。
     *
     * @param body 含 alias
     * @return 新密钥 ID
     */
    @OperationLog(module = "sys", menuPath = "/system/kms", operationType = OperationType.UPDATE, detailTemplateCode = "KMS_ROTATE")
    @RequirePerm("sys:kms:rotate")
    @PostMapping("/rotate")
    public R<Long> rotate(@RequestBody KmsCreateBody body) {
        return R.ok(kmsService.rotateKey(body.getAlias()));
    }

    /**
     * 禁用密钥。
     *
     * @param body 含 alias
     * @return 是否成功
     */
    @OperationLog(module = "sys", menuPath = "/system/kms", operationType = OperationType.UPDATE, detailTemplateCode = "KMS_DISABLE")
    @RequirePerm("sys:kms:disable")
    @PostMapping("/disable")
    public R<Boolean> disable(@RequestBody KmsCreateBody body) {
        return R.ok(kmsService.disableKey(body.getAlias()));
    }

    /**
     * 查询密钥详情（不含明文）。
     *
     * @param body 含 alias
     * @return 安全视图
     */
    @RequirePerm("sys:kms:view")
    @PostMapping("/detail")
    public R<Map<String, Object>> detail(@RequestBody KmsCreateBody body) {
        SysKmsKey key = keyMapper.selectOne(new LambdaQueryWrapper<SysKmsKey>()
                .eq(SysKmsKey::getKeyAlias, body.getAlias())
                .eq(SysKmsKey::getStatus, "ACTIVE")
                .last("limit 1"));
        return R.ok(key == null ? null : toSafeView(key));
    }

    private Map<String, Object> toSafeView(SysKmsKey key) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", key.getId());
        view.put("alias", key.getKeyAlias());
        view.put("keyType", key.getKeyType());
        view.put("keySize", key.getKeySize());
        view.put("keyVersion", key.getKeyVersion());
        view.put("status", key.getStatus());
        view.put("description", key.getDescription());
        view.put("updateTime", key.getUpdateTime());
        return view;
    }

    /**
     * 分页查询参数。
     */
    @Data
    public static class KmsQuery {
        private Long current;
        private Long size;
        private String alias;
    }

    /**
     * 创建/轮换参数。
     */
    @Data
    public static class KmsCreateBody {
        private String alias;
        private String keyType;
        private Integer keySize;
        private String description;
    }
}
