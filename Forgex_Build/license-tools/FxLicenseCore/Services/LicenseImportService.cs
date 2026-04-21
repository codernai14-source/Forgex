using System.Text;
using FxLicenseCore.Models;
using FxLicenseCore.Utilities;

namespace FxLicenseCore.Services;

/// <summary>
/// 授权导入服务。
/// </summary>
/// <remarks>
/// 用于在客户现场导入 license.lic，
/// 并维护 activation-history.json 供后端查看导入记录。
/// </remarks>
public sealed class LicenseImportService
{
    /// <summary>
    /// 导入授权文件。
    /// </summary>
    /// <param name="licenseText">授权文本。</param>
    /// <param name="targetDirectory">目标目录。</param>
    /// <param name="licenseFileName">授权文件名。</param>
    /// <param name="historyFileName">历史文件名。</param>
    public async Task ImportLicenseAsync(
        string licenseText,
        string targetDirectory,
        string licenseFileName = "license.lic",
        string historyFileName = "activation-history.json")
    {
        Directory.CreateDirectory(targetDirectory);

        var licensePath = Path.Combine(targetDirectory, licenseFileName);
        await File.WriteAllTextAsync(licensePath, licenseText.Trim(), JsonHelper.Utf8NoBom);

        var historyRecord = BuildHistoryRecord(licenseText, licensePath);
        var historyPath = Path.Combine(targetDirectory, historyFileName);
        var history = await ReadHistoryAsync(historyPath);
        history.Add(historyRecord);

        var historyJson = System.Text.Json.JsonSerializer.Serialize(history, JsonHelper.Options);
        await File.WriteAllTextAsync(historyPath, historyJson, JsonHelper.Utf8NoBom);
    }

    /// <summary>
    /// 从文件导入授权。
    /// </summary>
    /// <param name="sourcePath">源授权文件。</param>
    /// <param name="targetDirectory">目标目录。</param>
    /// <param name="licenseFileName">授权文件名。</param>
    /// <param name="historyFileName">历史文件名。</param>
    public async Task ImportLicenseFileAsync(
        string sourcePath,
        string targetDirectory,
        string licenseFileName = "license.lic",
        string historyFileName = "activation-history.json")
    {
        var licenseText = await File.ReadAllTextAsync(sourcePath, JsonHelper.Utf8NoBom);
        await ImportLicenseAsync(licenseText, targetDirectory, licenseFileName, historyFileName);
    }

    private static Dictionary<string, object?> BuildHistoryRecord(string licenseText, string licensePath)
    {
        var payloadBytes = KeyMaterialHelper.DecodePayload(licenseText);
        var payloadJson = Encoding.UTF8.GetString(payloadBytes);
        var payload = System.Text.Json.JsonSerializer.Deserialize<LicensePayload>(payloadJson, JsonHelper.Options)
                      ?? throw new InvalidOperationException("授权载荷解析失败");

        return new Dictionary<string, object?>
        {
            ["activatedAt"] = DateTimeOffset.Now.ToString("O"),
            ["licenseId"] = payload.LicenseId,
            ["customerCode"] = payload.CustomerCode,
            ["machineCode"] = payload.MachineCode,
            ["effectiveAt"] = payload.EffectiveAt,
            ["expireAt"] = payload.ExpireAt,
            ["edition"] = payload.Edition,
            ["modules"] = payload.Modules,
            ["licensePath"] = licensePath
        };
    }

    private static async Task<List<Dictionary<string, object?>>> ReadHistoryAsync(string historyPath)
    {
        if (!File.Exists(historyPath))
        {
            return [];
        }

        var json = await File.ReadAllTextAsync(historyPath, JsonHelper.Utf8NoBom);
        return System.Text.Json.JsonSerializer.Deserialize<List<Dictionary<string, object?>>>(json, JsonHelper.Options) ?? [];
    }
}
