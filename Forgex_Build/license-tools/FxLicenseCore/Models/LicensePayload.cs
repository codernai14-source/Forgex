namespace FxLicenseCore.Models;

/// <summary>
/// 授权载荷模型。
/// </summary>
/// <remarks>
/// 与后端 LicensePayload 结构保持一致，
/// 用于生成可被后端验签的 license.lic。
/// </remarks>
public sealed class LicensePayload
{
    /// <summary>
    /// 授权编号。
    /// </summary>
    public string LicenseId { get; set; } = string.Empty;

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
    /// 客户名称。
    /// </summary>
    public string CustomerName { get; set; } = string.Empty;

    /// <summary>
    /// 机器码。
    /// </summary>
    public string MachineCode { get; set; } = string.Empty;

    /// <summary>
    /// 授权模块。
    /// </summary>
    public List<string> Modules { get; set; } = [];

    /// <summary>
    /// 最大用户数。
    /// </summary>
    public int? MaxUsers { get; set; }

    /// <summary>
    /// 最大租户数。
    /// </summary>
    public int? MaxTenants { get; set; }

    /// <summary>
    /// 签发时间。
    /// </summary>
    public string IssuedAt { get; set; } = string.Empty;

    /// <summary>
    /// 生效时间。
    /// </summary>
    public string EffectiveAt { get; set; } = string.Empty;

    /// <summary>
    /// 到期时间。
    /// </summary>
    public string? ExpireAt { get; set; }

    /// <summary>
    /// 授权时长（天）。
    /// </summary>
    public int? DurationDays { get; set; }

    /// <summary>
    /// 宽限期（天）。
    /// </summary>
    public int? GraceDays { get; set; }

    /// <summary>
    /// 签发序号。
    /// </summary>
    public int? IssueSerial { get; set; }

    /// <summary>
    /// 备注。
    /// </summary>
    public string? Remark { get; set; }
}
