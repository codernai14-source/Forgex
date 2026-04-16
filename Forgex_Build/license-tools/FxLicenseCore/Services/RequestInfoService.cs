using System.Text;
using FxLicenseCore.Models;
using FxLicenseCore.Utilities;

namespace FxLicenseCore.Services;

/// <summary>
/// 请求授权信息服务。
/// </summary>
/// <remarks>
/// 用于生成和读取 request-info.json，
/// 保证客户码算法与后端 LicenseManager 一致。
/// </remarks>
public sealed class RequestInfoService
{
    /// <summary>
    /// 默认工具版本。
    /// </summary>
    public const string ToolVersion = "1.0.0";

    private readonly MachineFingerprintService _machineFingerprintService = new();

    /// <summary>
    /// 生成请求授权信息并写入文件。
    /// </summary>
    /// <param name="product">产品名称。</param>
    /// <param name="edition">版本类型。</param>
    /// <param name="instanceCode">实例编码。</param>
    /// <param name="customerCodePrefix">客户码前缀。</param>
    /// <param name="overrideMachineCode">覆盖机器码。</param>
    /// <param name="outputPath">输出文件路径。</param>
    /// <returns>请求授权信息。</returns>
    public async Task<LicenseRequestInfo> GenerateAsync(
        string product,
        string edition,
        string instanceCode,
        string customerCodePrefix,
        string? overrideMachineCode,
        string outputPath)
    {
        var requestInfo = new LicenseRequestInfo
        {
            Product = product,
            Edition = edition,
            MachineCode = _machineFingerprintService.ResolveMachineCode(overrideMachineCode),
            OsType = Environment.OSVersion.VersionString.ToLowerInvariant(),
            Hostname = _machineFingerprintService.ResolveHostname(),
            GeneratedAt = DateTimeOffset.Now.ToString("O"),
            ToolVersion = ToolVersion
        };
        requestInfo.CustomerCode = GenerateCustomerCode(instanceCode, customerCodePrefix, requestInfo.MachineCode);

        var directory = Path.GetDirectoryName(outputPath);
        if (!string.IsNullOrWhiteSpace(directory))
        {
            Directory.CreateDirectory(directory);
        }

        var json = System.Text.Json.JsonSerializer.Serialize(requestInfo, JsonHelper.Options);
        await File.WriteAllTextAsync(outputPath, json, JsonHelper.Utf8NoBom);
        return requestInfo;
    }

    /// <summary>
    /// 读取请求授权信息。
    /// </summary>
    /// <param name="path">请求文件路径。</param>
    /// <returns>请求授权信息。</returns>
    public async Task<LicenseRequestInfo> ReadAsync(string path)
    {
        var json = await File.ReadAllTextAsync(path, JsonHelper.Utf8NoBom);
        return System.Text.Json.JsonSerializer.Deserialize<LicenseRequestInfo>(json, JsonHelper.Options)
               ?? throw new InvalidOperationException("request-info.json 解析失败");
    }

    /// <summary>
    /// 生成客户码。
    /// </summary>
    /// <param name="instanceCode">实例编码。</param>
    /// <param name="customerCodePrefix">客户码前缀。</param>
    /// <param name="machineCode">机器码。</param>
    /// <returns>客户码。</returns>
    public string GenerateCustomerCode(string instanceCode, string customerCodePrefix, string machineCode)
    {
        var resolvedInstanceCode = string.IsNullOrWhiteSpace(instanceCode) ? "DEFAULT" : instanceCode.Trim().ToUpperInvariant();
        var baseValue = $"{resolvedInstanceCode}|{machineCode}";
        var suffix = GuidUtility.Create(GuidUtility.DnsNamespace, baseValue)
            .ToString("N")[..8]
            .ToUpperInvariant();
        return $"{customerCodePrefix.Trim().ToUpperInvariant()}-{suffix}";
    }
}
