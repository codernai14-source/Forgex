using FxLicenseCore.Services;
using FxLicenseCore.Utilities;

try
{
    var parser = CommandLineArguments.Parse(args);
    var requestInfoService = new RequestInfoService();
    var importService = new LicenseImportService();
    var machineFingerprintService = new MachineFingerprintService();

    switch (parser.Command)
    {
        case "generate-request":
        {
            var outputPath = parser.Require("output");
            var requestInfo = await requestInfoService.GenerateAsync(
                parser.Get("product", "Forgex")!,
                parser.Get("edition", "standard")!,
                parser.Get("instance-code", "DEFAULT")!,
                parser.Get("customer-code-prefix", "FXC")!,
                parser.Get("machine-code"),
                outputPath);

            Console.WriteLine("请求授权文件已生成:");
            Console.WriteLine($"  输出文件: {outputPath}");
            Console.WriteLine($"  客户码: {requestInfo.CustomerCode}");
            Console.WriteLine($"  机器码: {requestInfo.MachineCode}");
            break;
        }
        case "print-machine-code":
        {
            Console.WriteLine(machineFingerprintService.ResolveMachineCode(parser.Get("machine-code")));
            break;
        }
        case "import-license":
        {
            var targetDirectory = parser.Require("target-dir");
            var licenseFileName = parser.Get("license-file-name", "license.lic")!;
            var historyFileName = parser.Get("history-file-name", "activation-history.json")!;

            if (parser.Has("license-file"))
            {
                await importService.ImportLicenseFileAsync(
                    parser.Require("license-file"),
                    targetDirectory,
                    licenseFileName,
                    historyFileName);
            }
            else if (parser.Has("activation-code"))
            {
                await importService.ImportLicenseAsync(
                    parser.Require("activation-code"),
                    targetDirectory,
                    licenseFileName,
                    historyFileName);
            }
            else
            {
                throw new ArgumentException("import-license 需要传入 --license-file 或 --activation-code");
            }

            Console.WriteLine("授权已导入:");
            Console.WriteLine($"  目标目录: {targetDirectory}");
            Console.WriteLine($"  授权文件: {Path.Combine(targetDirectory, licenseFileName)}");
            Console.WriteLine($"  历史文件: {Path.Combine(targetDirectory, historyFileName)}");
            break;
        }
        default:
        {
            Console.WriteLine("""
                FxLicenseRequest CLI

                用法:
                  generate-request --output <request-info.json> [可选参数]
                  print-machine-code [--machine-code <覆盖机器码>]
                  import-license --target-dir <授权目录> (--license-file <license.lic> | --activation-code <授权字符串>)

                generate-request 可选参数:
                  --instance-code <实例编码，默认 DEFAULT>
                  --customer-code-prefix <客户码前缀，默认 FXC>
                  --machine-code <覆盖机器码>
                  --product <产品名称，默认 Forgex>
                  --edition <版本类型，默认 standard>

                import-license 可选参数:
                  --license-file-name <授权文件名，默认 license.lic>
                  --history-file-name <历史文件名，默认 activation-history.json>
                """);
            break;
        }
    }
}
catch (Exception ex)
{
    Console.Error.WriteLine($"[FxLicenseRequest] 执行失败: {ex.Message}");
    Environment.Exit(1);
}
