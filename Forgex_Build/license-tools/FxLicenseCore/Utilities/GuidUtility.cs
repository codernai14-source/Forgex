using System.Security.Cryptography;
using System.Text;

namespace FxLicenseCore.Utilities;

/// <summary>
/// Guid v5 工具类。
/// </summary>
/// <remarks>
/// 用于按固定输入生成稳定 GUID，
/// 以便复刻后端通过 UUID.nameUUIDFromBytes 生成客户码后缀的行为。
/// </remarks>
public static class GuidUtility
{
    /// <summary>
    /// DNS 命名空间。
    /// </summary>
    public static readonly Guid DnsNamespace = new("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    /// <summary>
    /// 根据命名空间和名称生成 Guid v5。
    /// </summary>
    /// <param name="namespaceId">命名空间。</param>
    /// <param name="name">名称。</param>
    /// <returns>生成后的 Guid。</returns>
    public static Guid Create(Guid namespaceId, string name)
    {
        var namespaceBytes = namespaceId.ToByteArray();
        SwapByteOrder(namespaceBytes);

        var nameBytes = Encoding.UTF8.GetBytes(name);
        var data = namespaceBytes.Concat(nameBytes).ToArray();

        using var sha1 = SHA1.Create();
        var hash = sha1.ComputeHash(data);

        hash[6] = (byte)((hash[6] & 0x0F) | (5 << 4));
        hash[8] = (byte)((hash[8] & 0x3F) | 0x80);

        var newGuid = hash.Take(16).ToArray();
        SwapByteOrder(newGuid);
        return new Guid(newGuid);
    }

    private static void SwapByteOrder(byte[] guid)
    {
        Swap(guid, 0, 3);
        Swap(guid, 1, 2);
        Swap(guid, 4, 5);
        Swap(guid, 6, 7);
    }

    private static void Swap(byte[] guid, int left, int right)
    {
        (guid[left], guid[right]) = (guid[right], guid[left]);
    }
}
