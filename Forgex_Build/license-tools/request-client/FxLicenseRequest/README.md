# FxLicenseRequest

`FxLicenseRequest` 是 Forgex 的独立请求授权程序，当前已提供第一版 CLI。

## 当前能力

- 生成 `request-info.json`
- 输出当前机器码
- 导入 `license.lic` 或授权字符串
- 写入 `activation-history.json`

## 开发环境一键授权

开发机推荐直接使用上级目录中的脚本：

```bat
..\activate-dev-license.bat
```

该脚本仅在 `FORGEX_PROFILE=dev` 时执行，会自动调用本客户端生成或复用 `request-info.json`，再导入开发专用 `license.lic`。

注意：开发授权只能在 `dev` 环境生效，不能用于 `test`、`yanshi`、`prod`。

## 构建

在 `Forgex_build/license-tools` 目录执行：

```bash
dotnet build FxLicenseTools.sln
```

## 使用示例

生成请求文件：

```bash
dotnet run --project request-client/FxLicenseRequest -- generate-request \
  --output D:/mine_product/forgex/forgex/license/request-info.json \
  --instance-code DEV_LOCAL \
  --edition dev
```

导入授权文件：

```bash
dotnet run --project request-client/FxLicenseRequest -- import-license \
  --license-file D:/mine_product/forgex/forgex/license/license.lic \
  --target-dir D:/mine_product/forgex/forgex/license
```
