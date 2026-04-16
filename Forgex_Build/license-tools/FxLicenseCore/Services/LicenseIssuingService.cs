using System.Text;
using FxLicenseCore.Models;
using FxLicenseCore.Utilities;

namespace FxLicenseCore.Services;

/// <summary>
/// 授权签发服务。
/// </summary>
/// <remarks>
/// 用于根据 request-info.json 和私钥生成正式的 license.lic。
/// </remarks>
public sealed class LicenseIssuingService
{
    /// <summary>
    /// 默认模块列表。
    /// </summary>
    public static readonly List<string> DefaultModules =
    [
        "gateway",
        "auth",
        "sys",
        "basic",
        "job",
        "integration",
        "workflow",
        "report"
    ];

    /// <summary>
    /// 读取请求文件并签发授权。
    /// </summary>
    /// <param name="requestInfo">请求授权信息。</param>
    /// <param name="options">签发选项。</param>
    /// <param name="privateKeyPath">私钥文件路径。</param>
    /// <param name="outputPath">授权文件输出路径。</param>
    /// <returns>生成后的授权文本。</returns>
    public async Task<string> IssueAsync(
        LicenseRequestInfo requestInfo,
        LicenseIssueOptions options,
        string privateKeyPath,
        string outputPath)
    {
        var privateKey = KeyMaterialHelper.ReadPrivateKey(privateKeyPath);
        var payload = BuildPayload(requestInfo, options);
        var payloadJson = System.Text.Json.JsonSerializer.Serialize(payload, JsonHelper.Options);
        var payloadBytes = Encoding.UTF8.GetBytes(payloadJson);
        var signatureBytes = KeyMaterialHelper.Sign(payloadBytes, privateKey);
        var licenseText = KeyMaterialHelper.EncodeLicense(payloadBytes, signatureBytes);

        var directory = Path.GetDirectoryName(outputPath);
        if (!string.IsNullOrWhiteSpace(directory))
        {
            Directory.CreateDirectory(directory);
        }

        await File.WriteAllTextAsync(outputPath, licenseText, JsonHelper.Utf8NoBom);
        return licenseText;
    }

    /// <summary>
    /// 组装授权载荷。
    /// </summary>
    /// <param name="requestInfo">请求授权信息。</param>
    /// <param name="options">签发选项。</param>
    /// <returns>授权载荷。</returns>
    public LicensePayload BuildPayload(LicenseRequestInfo requestInfo, LicenseIssueOptions options)
    {
        var now = DateTimeOffset.Now;
        var effectiveAt = ResolveDateTime(options.EffectiveAt, now);
        var expireAt = ResolveExpireAt(effectiveAt, options.ExpireAt, options.DurationDays);

        return new LicensePayload
        {
            LicenseId = string.IsNullOrWhiteSpace(options.LicenseId)
                ? $"LIC-{Guid.NewGuid():N}".ToUpperInvariant()
                : options.LicenseId.Trim(),
            Product = ResolveText(options.Product, requestInfo.Product, "Forgex"),
            Edition = ResolveText(options.Edition, requestInfo.Edition, "standard"),
            CustomerCode = requestInfo.CustomerCode,
            CustomerName = ResolveText(options.CustomerName, requestInfo.CustomerCode, requestInfo.CustomerCode),
            MachineCode = requestInfo.MachineCode,
            Modules = options.Modules is { Count: > 0 } ? options.Modules : DefaultModules,
            MaxUsers = options.MaxUsers,
            MaxTenants = options.MaxTenants,
            IssuedAt = now.ToString("O"),
            EffectiveAt = effectiveAt.ToString("O"),
            ExpireAt = expireAt?.ToString("O"),
            DurationDays = ResolveDurationDays(options.DurationDays, effectiveAt, expireAt),
            GraceDays = options.GraceDays ?? 0,
            IssueSerial = options.IssueSerial ?? 1,
            Remark = options.Remark
        };
    }

    private static string ResolveText(string? value, string? fallback, string defaultValue)
    {
        if (!string.IsNullOrWhiteSpace(value))
        {
            return value.Trim();
        }

        if (!string.IsNullOrWhiteSpace(fallback))
        {
            return fallback.Trim();
        }

        return defaultValue;
    }

    private static DateTimeOffset ResolveDateTime(string? value, DateTimeOffset defaultValue)
    {
        return string.IsNullOrWhiteSpace(value) ? defaultValue : DateTimeOffset.Parse(value);
    }

    private static DateTimeOffset? ResolveExpireAt(DateTimeOffset effectiveAt, string? expireAtText, int? durationDays)
    {
        if (!string.IsNullOrWhiteSpace(expireAtText))
        {
            return DateTimeOffset.Parse(expireAtText);
        }

        if (durationDays.HasValue)
        {
            return effectiveAt.AddDays(durationDays.Value);
        }

        return null;
    }

    private static int? ResolveDurationDays(int? durationDays, DateTimeOffset effectiveAt, DateTimeOffset? expireAt)
    {
        if (durationDays.HasValue)
        {
            return durationDays.Value;
        }

        if (!expireAt.HasValue)
        {
            return null;
        }

        return (int)(expireAt.Value - effectiveAt).TotalDays;
    }
}
