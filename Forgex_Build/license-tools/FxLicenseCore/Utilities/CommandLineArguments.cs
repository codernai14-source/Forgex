namespace FxLicenseCore.Utilities;

/// <summary>
/// 简易命令行参数解析器。
/// </summary>
/// <remarks>
/// 当前工具集以轻量 CLI 为主，
/// 使用统一解析器减少两个命令行程序的重复代码。
/// </remarks>
public sealed class CommandLineArguments
{
    private readonly Dictionary<string, string> _options;

    private CommandLineArguments(string command, Dictionary<string, string> options)
    {
        Command = command;
        _options = options;
    }

    /// <summary>
    /// 当前命令。
    /// </summary>
    public string Command { get; }

    /// <summary>
    /// 解析命令行参数。
    /// </summary>
    /// <param name="args">原始参数。</param>
    /// <returns>解析结果。</returns>
    public static CommandLineArguments Parse(string[] args)
    {
        if (args.Length == 0)
        {
            return new CommandLineArguments("help", new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase));
        }

        var command = args[0];
        var options = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);

        for (var i = 1; i < args.Length; i++)
        {
            var current = args[i];
            if (!current.StartsWith("--", StringComparison.Ordinal))
            {
                throw new ArgumentException($"无法识别的参数: {current}");
            }

            var key = current[2..];
            var value = "true";
            if (i + 1 < args.Length && !args[i + 1].StartsWith("--", StringComparison.Ordinal))
            {
                value = args[i + 1];
                i++;
            }

            options[key] = value;
        }

        return new CommandLineArguments(command, options);
    }

    /// <summary>
    /// 获取必填参数。
    /// </summary>
    /// <param name="key">参数名。</param>
    /// <returns>参数值。</returns>
    public string Require(string key)
    {
        var value = Get(key);
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new ArgumentException($"缺少必填参数 --{key}");
        }

        return value;
    }

    /// <summary>
    /// 获取可选参数。
    /// </summary>
    /// <param name="key">参数名。</param>
    /// <param name="defaultValue">默认值。</param>
    /// <returns>参数值。</returns>
    public string? Get(string key, string? defaultValue = null)
    {
        return _options.TryGetValue(key, out var value) && !string.IsNullOrWhiteSpace(value) ? value : defaultValue;
    }

    /// <summary>
    /// 获取整数参数。
    /// </summary>
    /// <param name="key">参数名。</param>
    /// <param name="defaultValue">默认值。</param>
    /// <returns>整数值。</returns>
    public int? GetInt32(string key, int? defaultValue = null)
    {
        var value = Get(key);
        return string.IsNullOrWhiteSpace(value) ? defaultValue : int.Parse(value);
    }

    /// <summary>
    /// 获取 CSV 列表参数。
    /// </summary>
    /// <param name="key">参数名。</param>
    /// <param name="defaultValue">默认列表。</param>
    /// <returns>列表值。</returns>
    public List<string> GetCsvList(string key, List<string>? defaultValue = null)
    {
        var value = Get(key);
        if (string.IsNullOrWhiteSpace(value))
        {
            return defaultValue ?? [];
        }

        return value
            .Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
            .Where(item => !string.IsNullOrWhiteSpace(item))
            .ToList();
    }

    /// <summary>
    /// 判断是否存在参数。
    /// </summary>
    /// <param name="key">参数名。</param>
    /// <returns>是否存在。</returns>
    public bool Has(string key)
    {
        return _options.ContainsKey(key);
    }
}
