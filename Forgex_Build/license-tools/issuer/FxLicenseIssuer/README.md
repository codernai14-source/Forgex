# FxLicenseIssuer

`FxLicenseIssuer` 是 Forgex 授权签发端的 Windows 图形化工具，用于根据客户现场提交的 `request-info.json` 和签发端私钥生成 `license.lic`。

## 使用方式

1. 打开 `FxLicenseIssuer.exe`。
2. 选择客户现场生成的 `request-info.json`。
3. 选择签发端私钥 `private-key.pkcs8.base64`。
4. 填写客户名称、版本类型、模块、用户数、租户数和授权期限。
5. 点击 `生成授权`，得到 `license.lic`。

生成后的 `license.lic` 发回客户现场，由客户在 `Forgex 控制中心` 中点击 `导入授权` 后重启服务生效。

## 发布 EXE

在 `Forgex_Build/license-tools` 目录执行：

```powershell
powershell -ExecutionPolicy Bypass -File .\publish-license-issuer.ps1
```

默认输出：

```text
Forgex_Build\license-tools\issuer\publish\win-x64\FxLicenseIssuer.exe
```

`private-key.pkcs8.base64` 只允许保存在授权签发端，不要放入客户交付包。
