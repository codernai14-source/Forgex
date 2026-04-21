# FxLicenseGenerator

`FxLicenseGenerator` 是 Forgex 的独立授权签发程序，当前已提供第一版 CLI。

## 当前能力

- 生成 Ed25519 密钥对
- 读取 `request-info.json`
- 生成可被后端验签的 `license.lic`

## 构建

在 `Forgex_build/license-tools` 目录执行：

```bash
dotnet build FxLicenseTools.sln
```

## 使用示例

生成密钥对：

```bash
dotnet run --project issuer/FxLicenseGenerator -- gen-keypair --out-dir ./keys
```

签发授权：

```bash
dotnet run --project issuer/FxLicenseGenerator -- issue \
  --request-info D:/mine_product/forgex/forgex/license/request-info.json \
  --private-key ./keys/private-key.pkcs8.base64 \
  --output D:/mine_product/forgex/forgex/license/license.lic \
  --customer-name 本机开发环境 \
  --edition dev \
  --modules gateway,auth,sys,basic,job,integration,workflow,report \
  --duration-days 365
```

## 开发环境批量使用说明

如果是开发公司内部统一分发开发授权，不建议每台机器手工执行上面的命令。

请直接使用：

```bat
..\activate-dev-license.bat
```

该脚本会自动完成本地开发密钥生成、请求文件处理、开发授权签发与导入。

注意：

- 该脚本生成的开发授权带有 `DEV_ONLY_LOCAL_LICENSE` 标记
- 后端只会在 `FORGEX_PROFILE=dev` 时接受该授权
- `test`、`yanshi`、`prod` 仍需使用正式签发流程
