using System.Text.Encodings.Web;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace FxLicenseCore.Utilities;

/// <summary>
/// JSON 工具类。
/// </summary>
/// <remarks>
/// 统一管理授权工具集使用的序列化配置，
/// 保证请求文件、授权历史和授权载荷的序列化格式一致。
/// </remarks>
public static class JsonHelper
{
    /// <summary>
    /// 统一的 JSON 选项。
    /// </summary>
    public static readonly JsonSerializerOptions Options = new()
    {
        WriteIndented = true,
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };

    /// <summary>
    /// 无 BOM 的 UTF-8 编码。
    /// </summary>
    public static readonly Encoding Utf8NoBom = new UTF8Encoding(false);
}
