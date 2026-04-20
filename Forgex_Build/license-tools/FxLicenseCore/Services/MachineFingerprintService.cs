using System.Net;
using System.Net.NetworkInformation;
using System.Security.Cryptography;
using System.Text;

namespace FxLicenseCore.Services;

/// <summary>
/// 机器指纹服务。
/// </summary>
/// <remarks>
/// 与后端 MachineFingerprintService 保持一致，
/// 用于在请求授权客户端生成稳定机器码。
/// </remarks>
public sealed class MachineFingerprintService
{
    /// <summary>
    /// 解析机器码。
    /// </summary>
    /// <param name="overrideMachineCode">覆盖机器码，可为空。</param>
    /// <returns>SHA-256 机器码。</returns>
    public string ResolveMachineCode(string? overrideMachineCode = null)
    {
        if (!string.IsNullOrWhiteSpace(overrideMachineCode))
        {
            return Normalize(overrideMachineCode);
        }

        var parts = new List<string>();
        var osName = Environment.OSVersion.Platform.ToString().ToLowerInvariant();

        if (osName.Contains("win", StringComparison.Ordinal))
        {
            AddIfHasText(parts, Environment.GetEnvironmentVariable("COMPUTERNAME"));
            AddIfHasText(parts, Environment.GetEnvironmentVariable("PROCESSOR_IDENTIFIER"));
            AddIfHasText(parts, Environment.GetEnvironmentVariable("SystemDrive"));
        }
        else
        {
            AddIfHasText(parts, ReadFileQuietly("/etc/machine-id"));
            AddIfHasText(parts, ReadFileQuietly("/sys/class/dmi/id/product_uuid"));
            AddIfHasText(parts, Environment.GetEnvironmentVariable("HOSTNAME"));
        }

        AddIfHasText(parts, ResolveHostname());
        parts.AddRange(ResolveMacAddresses());

        if (parts.Count == 0)
        {
            parts.Add("FORGEX-UNKNOWN-HOST");
        }

        var joined = string.Join("|", parts).ToUpperInvariant();
        using var sha256 = SHA256.Create();
        var hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(joined));
        return Convert.ToHexString(hash);
    }

    /// <summary>
    /// 获取当前主机名。
    /// </summary>
    /// <returns>主机名。</returns>
    public string ResolveHostname()
    {
        try
        {
            return Dns.GetHostName();
        }
        catch
        {
            return "unknown-host";
        }
    }

    private static void AddIfHasText(List<string> parts, string? value)
    {
        if (!string.IsNullOrWhiteSpace(value))
        {
            parts.Add(Normalize(value));
        }
    }

    private static string Normalize(string value)
    {
        return value.Trim().Replace(" ", string.Empty, StringComparison.Ordinal).ToUpperInvariant();
    }

    private static string? ReadFileQuietly(string path)
    {
        try
        {
            return File.Exists(path) ? File.ReadAllText(path).Trim() : null;
        }
        catch
        {
            return null;
        }
    }

    private static IEnumerable<string> ResolveMacAddresses()
    {
        try
        {
            return NetworkInterface.GetAllNetworkInterfaces()
                .Where(item => item.OperationalStatus == OperationalStatus.Up)
                .Where(item => item.NetworkInterfaceType != NetworkInterfaceType.Loopback)
                .Where(item => item.NetworkInterfaceType != NetworkInterfaceType.Tunnel)
                .Select(item => item.GetPhysicalAddress()?.ToString())
                .Where(item => !string.IsNullOrWhiteSpace(item))
                .Cast<string>()
                .ToList();
        }
        catch
        {
            return [];
        }
    }
}
