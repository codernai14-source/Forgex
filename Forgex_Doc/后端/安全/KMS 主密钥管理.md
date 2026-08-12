# KMS 主密钥管理

> 分类：后端 / 安全
> 版本：V0.8.5
> 关联模块：`Forgex_Sys`（KmsService）
> 关联问题：问题8「主密钥自举风险」

本文说明 Forgex KMS 主密钥的生成、注入、轮换、备份恢复流程与安全注意事项。主密钥是保护所有业务密钥的根密钥，其安全直接决定全系统数据安全。

---

## 一、背景与设计原则

### 1.1 问题背景

V0.8.0 之前，KMS 主密钥存储在业务数据库（`security.kms.master` 配置项），首次使用时自动生成并写回数据库。这意味着：

- **主密钥与业务密钥同库存储**：数据库被攻破（SQL 注入 / 备份泄露 / 拖库）→ 主密钥暴露 → 所有业务密钥可解密 → 全盘失守。
- **自动生成无审计**：主密钥何时生成、由谁触发不可追溯。

### 1.2 设计原则

| 原则 | 说明 |
|---|---|
| 主密钥不落库 | 主密钥与业务密钥物理隔离，杜绝同库失守 |
| 外部注入 | 由运维或部署平台（容器 Secret / K8s Secret）注入，应用不生成 |
| 缺失即失败 | 主密钥未配置时服务启动直接失败，不降级、不自举 |
| 最小权限 | 密钥文件权限 600，仅服务账号可读 |

---

## 二、主密钥与业务密钥的关系

```
                  外部注入(不落库)
                  ┌──────────────┐
                  │  主密钥(32B)  │  AES-256-GCM
                  └──────┬───────┘
                         │ 加密 / 解密
            ┌────────────┼────────────┐
            ▼            ▼            ▼
      ┌──────────┐ ┌──────────┐ ┌──────────┐
      │ 业务密钥A │ │ 业务密钥B │ │ 业务密钥C │  ← 加密后存储在数据库 sys_kms_key
      └──────────┘ └──────────┘ └──────────┘
```

- **主密钥（Master Key）**：32 字节 AES-256 密钥，仅用于加解密业务密钥，不直接加密业务数据。
- **业务密钥（Business Key）**：由 KMS 管理的 AES / SM4 / RSA / SM2 密钥，加密后存入 `sys_kms_key` 表，使用时由主密钥解密返回明文。
- **隔离关系**：主密钥在外部（环境变量 / 文件），业务密钥在数据库；即便数据库泄露，没有主密钥也无法解密业务密钥。

---

## 三、密钥生成

### 3.1 使用生成工具

Forgex 提供专用生成脚本 `gen-kms-master-key.sh`：

```bash
# 方式一：输出 hex 到 stdout
bash gen-kms-master-key.sh

# 方式二：写入文件（推荐，自动设置 600 权限）
bash gen-kms-master-key.sh -o ${FORGEX_LICENSE_DIR}/kms.key
```

脚本行为：
- 使用 `openssl rand -hex 32` 生成 32 字节随机密钥（无 openssl 时回退到 `/dev/urandom`）。
- 输出 64 位 hex 字符串到 stdout。
- `-o` 参数指定文件路径时，写入文件并设置 600 权限。
- 输出注入方式提示到 stderr。

### 3.2 手动生成（备用）

无脚本环境可手动生成：

```bash
# openssl
openssl rand -hex 32 > kms.key
chmod 600 kms.key

# 或 /dev/urandom
head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n' > kms.key
chmod 600 kms.key
```

### 3.3 校验

主密钥必须满足：
- 长度：32 字节 = 64 位 hex 字符。
- 编码：纯 hex 字符（0-9, a-f），无空格换行。
- 随机性：使用密码学安全随机源生成，禁止使用 `Math.random` 或时间戳。

服务启动时会自动校验长度，不符则启动失败。

---

## 四、注入方式

主密钥按以下优先级读取（`KmsServiceImpl.getMasterKey()`）：

### 4.1 优先级 1：环境变量 `FORGEX_KMS_MASTER_KEY_HEX`

直接将 hex 字符串注入环境变量，适用于容器 / K8s Secret：

```bash
export FORGEX_KMS_MASTER_KEY_HEX=<64位hex字符串>
```

Docker：

```yaml
environment:
  - FORGEX_KMS_MASTER_KEY_HEX=${FORGEX_KMS_MASTER_KEY_HEX}
```

Kubernetes Secret：

```yaml
env:
  - name: FORGEX_KMS_MASTER_KEY_HEX
    valueFrom:
      secretKeyRef:
        name: forgex-kms
        key: master-key-hex
```

### 4.2 优先级 2：密钥文件

通过 `FORGEX_KMS_MASTER_KEY_FILE` 指定密钥文件路径，文件内容为 64 位 hex 字符串：

```bash
export FORGEX_KMS_MASTER_KEY_FILE=/opt/Forgex_ACME_PROD/license/kms.key
```

**默认路径**：若未设置 `FORGEX_KMS_MASTER_KEY_FILE`，则自动尝试 `${FORGEX_LICENSE_DIR}/kms.key`。因此只要密钥文件放在 license 目录下且命名为 `kms.key`，无需额外设置环境变量。

文件要求：
- 权限 600（仅服务账号可读）。
- 内容为纯 hex 字符串（允许首尾空白，读取时自动 trim）。

### 4.3 注入方式对比

| 方式 | 适用场景 | 优点 | 缺点 |
|---|---|---|---|
| 环境变量 | 容器 / K8s | 注入简单，与 Secret 管理集成 | 进程列表 / core dump 可能泄露 |
| 密钥文件 | 物理机 / 虚拟机 | 权限可控，不进进程环境 | 需管理文件权限与备份 |

**推荐**：物理机部署用密钥文件（默认 `${FORGEX_LICENSE_DIR}/kms.key`），容器部署用环境变量。

---

## 五、部署配置（install.sh 集成）

`install.sh` 在初始化实例时会自动生成主密钥：

1. 调用 `gen-kms-master-key.sh` 生成密钥到 `${FORGEX_LICENSE_DIR}/kms.key`（权限 600）。
2. 在 `.env` 中写入 `FORGEX_KMS_MASTER_KEY_FILE=${FORGEX_LICENSE_DIR}/kms.key`。

因此标准部署流程下，主密钥会自动就绪，无需手动操作。如需使用环境变量方式，可手动覆盖。

---

## 六、密钥轮换

### 6.1 轮换场景

主密钥泄露怀疑、定期安全审计、合规要求时需要轮换主密钥。

### 6.2 轮换流程

主密钥轮换需要重新加密所有业务密钥，流程如下：

```
1. 停止 Forgex 服务
2. 生成新主密钥
   bash gen-kms-master-key.sh -o ${FORGEX_LICENSE_DIR}/kms.key.new
3. 使用轮换工具用新主密钥重新加密所有业务密钥
   (sys_kms_key 表中所有 ACTIVE/ROTATED 密钥)
4. 更新主密钥配置
   mv ${FORGEX_LICENSE_DIR}/kms.key.new ${FORGEX_LICENSE_DIR}/kms.key
   chmod 600 ${FORGEX_LICENSE_DIR}/kms.key
5. 启动 Forgex 服务，验证密钥可正常加解密
6. 安全备份旧主密钥后销毁
```

> 注意：轮换期间必须停服，避免新旧主密钥交替导致解密失败。轮换工具将在后续版本提供。

### 6.3 业务密钥轮换

业务密钥轮换（`KmsService.rotateKey`）不涉及主密钥，生成新版本业务密钥并用当前主密钥加密，旧版本标记为 ROTATED。这是轻量操作，可在线进行。

---

## 七、备份与恢复

### 7.1 必须备份

主密钥丢失意味着所有业务密钥永久不可解密，**必须安全备份**：

| 备份方式 | 说明 |
|---|---|
| 离线介质 | USB 加密盘 / 硬件加密柜，与生产网络物理隔离 |
| 密码保险箱 | 企业级密码管理工具（如 Vault），访问审计 |
| 异地容灾 | 至少一份备份存于异地，防站点灾难 |

### 7.2 备份要求

- 备份内容：64 位 hex 字符串或 `kms.key` 文件。
- 备份标识：标注实例编码（INSTANCE_CODE）与生成日期。
- 访问控制：备份访问需双人授权，记录审计。
- 禁止明文：备份介质必须加密，禁止明文存储主密钥。

### 7.3 恢复流程

```
1. 从备份获取主密钥 hex
2. 写入密钥文件或设置环境变量
   echo "<hex>" > ${FORGEX_LICENSE_DIR}/kms.key
   chmod 600 ${FORGEX_LICENSE_DIR}/kms.key
   # 或
   export FORGEX_KMS_MASTER_KEY_HEX=<hex>
3. 启动 Forgex 服务，验证业务密钥可正常解密
```

---

## 八、安全注意事项

| 项 | 要求 |
|---|---|
| 密钥文件权限 | 600，仅服务账号可读，禁止 644 / 666 |
| 密钥不入日志 | 日志仅记录来源（环境变量 / 文件路径），禁止记录密钥内容 |
| 密钥不入版本库 | `kms.key` 必须加入 `.gitignore`，禁止提交到 Git |
| 密钥不进备份包 | 主密钥独立备份，不与数据库备份混存 |
| 进程环境 | 环境变量方式注意 `ps -e` / core dump 不泄露（容器隔离较好） |
| 传输安全 | 密钥传递通过加密通道（SSH / TLS），禁止明文 HTTP / 邮件 |
| 人员管理 | 知晓主密钥的人员最小化，离职及时轮换 |

---

## 九、故障排查

### 9.1 启动失败：主密钥未配置

错误信息示例：
```
KMS: 主密钥未配置, 服务无法启动。
请按以下步骤配置主密钥:
  1. 使用密钥生成工具生成 32 字节主密钥:
     bash gen-kms-master-key.sh -o ${FORGEX_LICENSE_DIR}/kms.key
  ...
```

解决：按提示生成密钥并配置环境变量或文件，重启服务。

### 9.2 启动失败：主密钥长度不符

错误信息示例：
```
KMS: 主密钥未配置, 服务无法启动。 原因: 主密钥长度必须为 32 字节(256 位), 当前为 16 字节。
```

解决：重新生成 32 字节密钥，确认 hex 字符串为 64 位。

### 9.3 启动失败：密钥文件不可读

错误信息示例：
```
KMS: 主密钥未配置, 服务无法启动。 原因: 读取主密钥文件失败: /opt/Forgex_ACME_PROD/license/kms.key, 原因: ...
```

解决：检查文件路径、权限（600）与服务账号读取权限。

---

## 十、关联文档

- [认证授权](../身份与权限/认证授权.md)
- [多租户](../租户与上下文/多租户.md)
- [代码注释规范](../../开发规范/规范文档/代码注释规范.md)
