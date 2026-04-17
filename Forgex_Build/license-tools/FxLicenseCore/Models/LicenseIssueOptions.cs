namespace FxLicenseCore.Models;

/// <summary>
/// 授权签发选项。
/// </summary>
/// <remarks>
/// 用于承载命令行中输入的签发参数，
/// 便于生成器在签发阶段统一组装 LicensePayload。
/// </remarks>
public sealed class LicenseIssueOptions
{
    /// <summary>
    /// 授权编号。
    /// </summary>
    public string? LicenseId { get; set; }

    /// <summary>
    /// 产品名称。
    /// </summary>
    public string? Product { get; set; }

    /// <summary>
    /// 版本类型。
    /// </summary>
    public string? Edition { get; set; }

    /// <summary>
    /// 客户名称。
    /// </summary>
    public string? CustomerName { get; set; }

    /// <summary>
    /// 授权模块。
    /// </summary>
    public List<string>? Modules { get; set; }

    /// <summary>
    /// 最大用户数。
    /// </summary>
    public int? MaxUsers { get; set; }

    /// <summary>
    /// 最大租户数。
    /// </summary>
    public int? MaxTenants { get; set; }

    /// <summary>
    /// 生效时间。
    /// </summary>
    public string? EffectiveAt { get; set; }

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
