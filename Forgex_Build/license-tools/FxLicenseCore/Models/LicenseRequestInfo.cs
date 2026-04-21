namespace FxLicenseCore.Models;

/// <summary>
/// 请求授权信息模型。
/// </summary>
/// <remarks>
/// 与后端 LicenseRequestInfo 结构保持一致，
/// 用于现场生成 request-info.json 并提交给授权签发端。
/// </remarks>
public sealed class LicenseRequestInfo
{
    /// <summary>
    /// 产品名称。
    /// </summary>
    public string Product { get; set; } = string.Empty;

    /// <summary>
    /// 版本类型。
    /// </summary>
    public string Edition { get; set; } = string.Empty;

    /// <summary>
    /// 客户码。
    /// </summary>
    public string CustomerCode { get; set; } = string.Empty;

    /// <summary>
    /// 机器码。
    /// </summary>
    public string MachineCode { get; set; } = string.Empty;

    /// <summary>
    /// 操作系统类型。
    /// </summary>
    public string OsType { get; set; } = string.Empty;

    /// <summary>
    /// 主机名。
    /// </summary>
    public string Hostname { get; set; } = string.Empty;

    /// <summary>
    /// 生成时间。
    /// </summary>
    public string GeneratedAt { get; set; } = string.Empty;

    /// <summary>
    /// 工具版本。
    /// </summary>
    public string ToolVersion { get; set; } = string.Empty;
}
