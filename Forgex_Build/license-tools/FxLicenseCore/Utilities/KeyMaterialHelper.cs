using System.Text;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Crypto.Signers;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Pkcs;
using Org.BouncyCastle.X509;

namespace FxLicenseCore.Utilities;

/// <summary>
/// 密钥与签名工具类。
/// </summary>
/// <remarks>
/// 统一处理 Ed25519 密钥对生成、密钥文件读写和授权签名。
/// </remarks>
public static class KeyMaterialHelper
{
    /// <summary>
    /// 公钥文件名。
    /// </summary>
    public const string PublicKeyFileName = "public-key.base64";

    /// <summary>
    /// 私钥文件名。
    /// </summary>
    public const string PrivateKeyFileName = "private-key.pkcs8.base64";

    /// <summary>
    /// 生成 Ed25519 密钥对。
    /// </summary>
    /// <param name="outputDirectory">输出目录。</param>
    public static void GenerateKeyPair(DirectoryInfo outputDirectory)
    {
        outputDirectory.Create();

        var generator = new Ed25519KeyPairGenerator();
        generator.Init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        var keyPair = generator.GenerateKeyPair();
        var privateKeyBytes = PrivateKeyInfoFactory.CreatePrivateKeyInfo(keyPair.Private).GetEncoded();
        var publicKeyBytes = SubjectPublicKeyInfoFactory.CreateSubjectPublicKeyInfo(keyPair.Public).GetEncoded();

        File.WriteAllText(
            Path.Combine(outputDirectory.FullName, PublicKeyFileName),
            Convert.ToBase64String(publicKeyBytes),
            JsonHelper.Utf8NoBom);

        File.WriteAllText(
            Path.Combine(outputDirectory.FullName, PrivateKeyFileName),
            Convert.ToBase64String(privateKeyBytes),
            JsonHelper.Utf8NoBom);
    }

    /// <summary>
    /// 读取私钥。
    /// </summary>
    /// <param name="path">私钥文件路径。</param>
    /// <returns>私钥对象。</returns>
    public static Ed25519PrivateKeyParameters ReadPrivateKey(string path)
    {
        var privateKeyBytes = Convert.FromBase64String(File.ReadAllText(path, Encoding.UTF8).Trim());
        return (Ed25519PrivateKeyParameters)PrivateKeyFactory.CreateKey(privateKeyBytes);
    }

    /// <summary>
    /// 读取公钥。
    /// </summary>
    /// <param name="path">公钥文件路径。</param>
    /// <returns>公钥对象。</returns>
    public static Ed25519PublicKeyParameters ReadPublicKey(string path)
    {
        var publicKeyBytes = Convert.FromBase64String(File.ReadAllText(path, Encoding.UTF8).Trim());
        return (Ed25519PublicKeyParameters)PublicKeyFactory.CreateKey(publicKeyBytes);
    }

    /// <summary>
    /// 对授权载荷进行签名。
    /// </summary>
    /// <param name="payloadBytes">载荷字节。</param>
    /// <param name="privateKey">私钥。</param>
    /// <returns>签名字节。</returns>
    public static byte[] Sign(byte[] payloadBytes, Ed25519PrivateKeyParameters privateKey)
    {
        var signer = new Ed25519Signer();
        signer.Init(true, privateKey);
        signer.BlockUpdate(payloadBytes, 0, payloadBytes.Length);
        return signer.GenerateSignature();
    }

    /// <summary>
    /// 校验签名。
    /// </summary>
    /// <param name="payloadBytes">载荷字节。</param>
    /// <param name="signatureBytes">签名字节。</param>
    /// <param name="publicKey">公钥。</param>
    /// <returns>是否验证通过。</returns>
    public static bool Verify(byte[] payloadBytes, byte[] signatureBytes, Ed25519PublicKeyParameters publicKey)
    {
        var signer = new Ed25519Signer();
        signer.Init(false, publicKey);
        signer.BlockUpdate(payloadBytes, 0, payloadBytes.Length);
        return signer.VerifySignature(signatureBytes);
    }

    /// <summary>
    /// 组装授权文件文本。
    /// </summary>
    /// <param name="payloadBytes">载荷字节。</param>
    /// <param name="signatureBytes">签名字节。</param>
    /// <returns>授权文本。</returns>
    public static string EncodeLicense(byte[] payloadBytes, byte[] signatureBytes)
    {
        var payload = Base64UrlEncode(payloadBytes);
        var signature = Base64UrlEncode(signatureBytes);
        return $"{payload}.{signature}";
    }

    /// <summary>
    /// 解码授权载荷。
    /// </summary>
    /// <param name="licenseText">授权文本。</param>
    /// <returns>载荷字节。</returns>
    public static byte[] DecodePayload(string licenseText)
    {
        var segments = licenseText.Trim().Split('.');
        if (segments.Length != 2)
        {
            throw new ArgumentException("授权码格式错误");
        }

        return Base64UrlDecode(segments[0]);
    }

    /// <summary>
    /// 解码授权签名。
    /// </summary>
    /// <param name="licenseText">授权文本。</param>
    /// <returns>签名字节。</returns>
    public static byte[] DecodeSignature(string licenseText)
    {
        var segments = licenseText.Trim().Split('.');
        if (segments.Length != 2)
        {
            throw new ArgumentException("授权码格式错误");
        }

        return Base64UrlDecode(segments[1]);
    }

    private static string Base64UrlEncode(byte[] bytes)
    {
        return Convert.ToBase64String(bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_');
    }

    private static byte[] Base64UrlDecode(string value)
    {
        var normalized = value.Replace('-', '+').Replace('_', '/');
        var padding = normalized.Length % 4;
        if (padding > 0)
        {
            normalized = normalized.PadRight(normalized.Length + (4 - padding), '=');
        }

        return Convert.FromBase64String(normalized);
    }
}
