using FxLicenseCore.Models;
using FxLicenseCore.Services;
using FxLicenseCore.Utilities;

try
{
    var parser = CommandLineArguments.Parse(args);
    var requestInfoService = new RequestInfoService();
    var issuingService = new LicenseIssuingService();

    switch (parser.Command)
    {
        case "gen-keypair":
        {
            var outputDirectory = new DirectoryInfo(parser.Require("out-dir"));
            KeyMaterialHelper.GenerateKeyPair(outputDirectory);
            Console.WriteLine("密钥对已生成:");
            Console.WriteLine($"  公钥: {Path.Combine(outputDirectory.FullName, KeyMaterialHelper.PublicKeyFileName)}");
            Console.WriteLine($"  私钥: {Path.Combine(outputDirectory.FullName, KeyMaterialHelper.PrivateKeyFileName)}");
            break;
        }
        case "issue":
        {
            var requestInfoPath = parser.Require("request-info");
            var privateKeyPath = parser.Require("private-key");
            var outputPath = parser.Get("output") ?? Path.Combine(Path.GetDirectoryName(requestInfoPath) ?? ".", "license.lic");

            var requestInfo = await requestInfoService.ReadAsync(requestInfoPath);
            var options = new LicenseIssueOptions
            {
                LicenseId = parser.Get("license-id"),
                Product = parser.Get("product"),
                Edition = parser.Get("edition"),
                CustomerName = parser.Get("customer-name"),
                Modules = parser.GetCsvList("modules", LicenseIssuingService.DefaultModules),
                MaxUsers = parser.GetInt32("max-users"),
                MaxTenants = parser.GetInt32("max-tenants"),
                EffectiveAt = parser.Get("effective-at"),
                ExpireAt = parser.Get("expire-at"),
                DurationDays = parser.GetInt32("duration-days", 365),
                GraceDays = parser.GetInt32("grace-days", 0),
                IssueSerial = parser.GetInt32("issue-serial", 1),
                Remark = parser.Get("remark")
            };

            var licenseText = await issuingService.IssueAsync(requestInfo, options, privateKeyPath, outputPath);
            Console.WriteLine("授权已签发:");
            Console.WriteLine($"  输出文件: {outputPath}");
            Console.WriteLine($"  客户码: {requestInfo.CustomerCode}");
            Console.WriteLine($"  机器码: {requestInfo.MachineCode}");
            Console.WriteLine($"  授权长度: {licenseText.Length}");
            break;
        }
        default:
        {
            Console.WriteLine("""
                FxLicenseGenerator CLI

                用法:
                  gen-keypair --out-dir <目录>
                  issue --request-info <request-info.json> --private-key <private-key.pkcs8.base64> [可选参数]

                issue 可选参数:
                  --output <license.lic 输出路径>
                  --license-id <授权编号>
                  --product <产品名称>
                  --edition <版本类型>
                  --customer-name <客户名称>
                  --modules <模块列表，逗号分隔>
                  --max-users <最大用户数>
                  --max-tenants <最大租户数>
                  --effective-at <ISO_OFFSET_DATE_TIME>
                  --expire-at <ISO_OFFSET_DATE_TIME>
                  --duration-days <授权天数，默认 365>
                  --grace-days <宽限期天数，默认 0>
                  --issue-serial <签发序号，默认 1>
                  --remark <备注>
                """);
            break;
        }
    }
}
catch (Exception ex)
{
    Console.Error.WriteLine($"[FxLicenseGenerator] 执行失败: {ex.Message}");
    Environment.Exit(1);
}
