using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.ServiceProcess;
using System.Text.Json;
using FxLicenseCore.Models;
using FxLicenseCore.Services;
using FxLicenseCore.Utilities;

namespace ForgexControlCenter;

internal static class Program
{
    [STAThread]
    private static int Main(string[] args)
    {
        var options = ControlCenterOptions.Parse(args);
        var installRoot = options.InstallRoot ?? AppContext.BaseDirectory;

        if (options.StartAll)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StartAll);
        }

        if (options.StopAll)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StopAll);
        }

        if (options.Status)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.Status);
        }

        if (options.StartWeb)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StartWeb);
        }

        if (options.StopWeb)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StopWeb);
        }

        if (options.StartService)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StartService, options.ServiceId);
        }

        if (options.StopService)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.StopService, options.ServiceId);
        }

        if (options.RestartService)
        {
            return CommandRunner.RunHeadless(installRoot, ServiceCommand.RestartService, options.ServiceId);
        }

        if (options.ServeWeb)
        {
            return StaticWebServer.RunForeground(installRoot);
        }

        ApplicationConfiguration.Initialize();
        Application.ThreadException += (_, eventArgs) =>
        {
            ControlCenterDiagnostics.Write(installRoot, "UI thread exception", eventArgs.Exception);
            MessageBox.Show(eventArgs.Exception.Message, "Forgex Control Center", MessageBoxButtons.OK, MessageBoxIcon.Error);
        };
        AppDomain.CurrentDomain.UnhandledException += (_, eventArgs) =>
        {
            if (eventArgs.ExceptionObject is Exception ex)
            {
                ControlCenterDiagnostics.Write(installRoot, "Unhandled exception", ex);
            }
        };
        TaskScheduler.UnobservedTaskException += (_, eventArgs) =>
        {
            ControlCenterDiagnostics.Write(installRoot, "Unobserved task exception", eventArgs.Exception);
            eventArgs.SetObserved();
        };

        try
        {
            Application.Run(new MainForm(installRoot));
        }
        catch (Exception ex)
        {
            ControlCenterDiagnostics.Write(installRoot, "Fatal application exception", ex);
            MessageBox.Show(ex.Message, "Forgex Control Center", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }

        return 0;
    }
}

internal enum ServiceCommand
{
    StartAll,
    StopAll,
    Status,
    StartWeb,
    StopWeb,
    StartService,
    StopService,
    RestartService
}

internal sealed class ControlCenterOptions
{
    public string? InstallRoot { get; private init; }

    public string? ServiceId { get; private init; }

    public bool StartAll { get; private init; }

    public bool StopAll { get; private init; }

    public bool Status { get; private init; }

    public bool StartWeb { get; private init; }

    public bool StopWeb { get; private init; }

    public bool ServeWeb { get; private init; }

    public bool StartService { get; private init; }

    public bool StopService { get; private init; }

    public bool RestartService { get; private init; }

    public static ControlCenterOptions Parse(string[] args)
    {
        string? installRoot = null;
        string? serviceId = null;
        var startAll = false;
        var stopAll = false;
        var status = false;
        var startWeb = false;
        var stopWeb = false;
        var serveWeb = false;
        var startService = false;
        var stopService = false;
        var restartService = false;

        for (var i = 0; i < args.Length; i++)
        {
            var arg = args[i];
            if (arg.Equals("--install-root", StringComparison.OrdinalIgnoreCase) && i + 1 < args.Length)
            {
                installRoot = args[++i];
                continue;
            }

            if (arg.Equals("--start-all", StringComparison.OrdinalIgnoreCase))
            {
                startAll = true;
                continue;
            }

            if (arg.Equals("--stop-all", StringComparison.OrdinalIgnoreCase))
            {
                stopAll = true;
                continue;
            }

            if (arg.Equals("--status", StringComparison.OrdinalIgnoreCase))
            {
                status = true;
                continue;
            }

            if (arg.Equals("--start-web", StringComparison.OrdinalIgnoreCase))
            {
                startWeb = true;
                continue;
            }

            if (arg.Equals("--stop-web", StringComparison.OrdinalIgnoreCase))
            {
                stopWeb = true;
                continue;
            }

            if (arg.Equals("--start-service", StringComparison.OrdinalIgnoreCase) && i + 1 < args.Length)
            {
                startService = true;
                serviceId = args[++i];
                continue;
            }

            if (arg.Equals("--stop-service", StringComparison.OrdinalIgnoreCase) && i + 1 < args.Length)
            {
                stopService = true;
                serviceId = args[++i];
                continue;
            }

            if (arg.Equals("--restart-service", StringComparison.OrdinalIgnoreCase) && i + 1 < args.Length)
            {
                restartService = true;
                serviceId = args[++i];
                continue;
            }

            if (arg.Equals("--serve-web", StringComparison.OrdinalIgnoreCase))
            {
                serveWeb = true;
            }
        }

        return new ControlCenterOptions
        {
            InstallRoot = installRoot,
            ServiceId = serviceId,
            StartAll = startAll,
            StopAll = stopAll,
            Status = status,
            StartWeb = startWeb,
            StopWeb = stopWeb,
            ServeWeb = serveWeb,
            StartService = startService,
            StopService = stopService,
            RestartService = restartService
        };
    }
}

internal static class CommandRunner
{
    public static int RunHeadless(string installRoot, ServiceCommand command, string? serviceId = null)
    {
        try
        {
            var config = ForgexControlConfig.Load(installRoot);
            var manager = new ForgexServiceManager(config);
            var webServer = new FrontendWebServer(config);

            switch (command)
            {
                case ServiceCommand.StartAll:
                    webServer.Start(message => Console.WriteLine(message));
                    manager.StartAll(message => Console.WriteLine(message));
                    break;
                case ServiceCommand.StopAll:
                    manager.StopAll(message => Console.WriteLine(message));
                    webServer.Stop(message => Console.WriteLine(message));
                    break;
                case ServiceCommand.Status:
                    foreach (var service in config.Services.OrderBy(item => item.StartOrder))
                    {
                        Console.WriteLine($"{service.ServiceId}: {manager.GetStatus(service)}");
                    }

                    Console.WriteLine($"web: {webServer.GetStatus()}");
                    break;
                case ServiceCommand.StartWeb:
                    webServer.Start(message => Console.WriteLine(message));
                    break;
                case ServiceCommand.StopWeb:
                    webServer.Stop(message => Console.WriteLine(message));
                    break;
                case ServiceCommand.StartService:
                    if (string.IsNullOrWhiteSpace(serviceId))
                    {
                        throw new InvalidOperationException("Service id is required.");
                    }

                    if (string.Equals(serviceId, "web", StringComparison.OrdinalIgnoreCase))
                    {
                        webServer.Start(message => Console.WriteLine(message));
                    }
                    else
                    {
                        manager.StartService(serviceId, message => Console.WriteLine(message));
                    }

                    break;
                case ServiceCommand.StopService:
                    if (string.IsNullOrWhiteSpace(serviceId))
                    {
                        throw new InvalidOperationException("Service id is required.");
                    }

                    if (string.Equals(serviceId, "web", StringComparison.OrdinalIgnoreCase))
                    {
                        webServer.Stop(message => Console.WriteLine(message));
                    }
                    else
                    {
                        manager.StopService(serviceId, message => Console.WriteLine(message));
                    }

                    break;
                case ServiceCommand.RestartService:
                    if (string.IsNullOrWhiteSpace(serviceId))
                    {
                        throw new InvalidOperationException("Service id is required.");
                    }

                    if (string.Equals(serviceId, "web", StringComparison.OrdinalIgnoreCase))
                    {
                        webServer.Restart(message => Console.WriteLine(message));
                    }
                    else
                    {
                        manager.RestartService(serviceId, message => Console.WriteLine(message));
                    }

                    break;
            }

            return 0;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine(ex.Message);
            return 1;
        }
    }
}

internal static class ControlCenterText
{
    private static readonly Dictionary<string, string> Zh = new(StringComparer.OrdinalIgnoreCase)
    {
        ["windowTitle"] = "Forgex 控制中心",
        ["summary"] = "实例：{0}    环境：{1}    安装目录：{2}",
        ["language"] = "语言",
        ["frontendTab"] = "前端",
        ["backendTab"] = "后端",
        ["licenseTab"] = "授权",
        ["machineCode"] = "机器码",
        ["gridService"] = "服务",
        ["gridStatus"] = "状态",
        ["gridPort"] = "端口",
        ["gridPath"] = "路径",
        ["gridJar"] = "Jar",
        ["stopService"] = "停止服务",
        ["startService"] = "启动服务",
        ["updateService"] = "更新",
        ["openLogFolder"] = "打开日志文件夹",
        ["gridActivatedAt"] = "导入时间",
        ["gridLicenseId"] = "授权编号",
        ["gridCustomerCode"] = "客户编码",
        ["gridEdition"] = "版本",
        ["gridEffectiveAt"] = "生效时间",
        ["gridExpireAt"] = "到期时间",
        ["gridModules"] = "模块",
        ["startSelectedService"] = "启动选中服务",
        ["stopSelectedService"] = "停止选中服务",
        ["restartSelectedService"] = "重启选中服务",
        ["openSelectedLogDir"] = "打开选中日志目录",
        ["selectServicePrompt"] = "请先在服务列表中选择一个服务。",
        ["generateRequest"] = "获取授权文件 / 生成申请授权文件",
        ["importLicense"] = "导入授权文件",
        ["startWeb"] = "启动前端",
        ["stopWeb"] = "停止前端",
        ["openWeb"] = "打开前端",
        ["startAll"] = "启动全部服务",
        ["stopAll"] = "停止全部服务",
        ["startBackendAll"] = "启动后端全部服务",
        ["refresh"] = "刷新",
        ["openInstallFolder"] = "打开安装目录",
        ["licenseTitle"] = "Forgex 授权",
        ["selectLicenseTitle"] = "选择 license.lic",
        ["licenseFilter"] = "Forgex 授权文件 (*.lic)|*.lic|所有文件 (*.*)|*.*",
        ["jarFilter"] = "Java Jar 文件 (*.jar)|*.jar|所有文件 (*.*)|*.*",
        ["selectBackendJarTitle"] = "选择 {0} 的新 Jar 文件",
        ["requestGeneratedMessage"] = "授权申请文件已生成：\r\n{0}",
        ["licenseImportedMessage"] = "授权文件已导入到：\r\n{0}",
        ["backendJarUpdatedMessage"] = "{0} 已更新：\r\n{1}",
        ["licenseDuration"] = "授权期限",
        ["licenseExpireAt"] = "到期时间",
        ["licenseRecords"] = "授权记录",
        ["licenseMissing"] = "未找到当前授权文件。",
        ["licenseInvalid"] = "当前授权文件解析失败：{0}",
        ["licenseUnknown"] = "未知",
        ["licensePermanent"] = "永久",
        ["licenseDurationDays"] = "{0} 天",
        ["licenseSummary"] = "授权编号：{0}    客户编码：{1}    版本：{2}    模块：{3}",
        ["logViewRefreshed"] = "视图已刷新。",
        ["logLanguageChanged"] = "界面语言已切换。",
        ["logRequestGenerated"] = "授权申请文件已生成：{0}",
        ["logCustomerCode"] = "客户编码：{0}",
        ["logLicenseImported"] = "授权文件已从 {0} 导入",
        ["logLicenseHistoryFailed"] = "授权记录读取失败：{0}",
        ["logBackendJarUpdated"] = "{0} Jar 已更新：{1}",
        ["logBackendJarBackup"] = "旧 Jar 已备份：{0}"
    };

    private static readonly Dictionary<string, string> En = new(StringComparer.OrdinalIgnoreCase)
    {
        ["windowTitle"] = "Forgex Control Center",
        ["summary"] = "Instance: {0}    Profile: {1}    Install Root: {2}",
        ["language"] = "Language",
        ["frontendTab"] = "Frontend",
        ["backendTab"] = "Backend",
        ["licenseTab"] = "License",
        ["machineCode"] = "Machine Code",
        ["gridService"] = "Service",
        ["gridStatus"] = "Status",
        ["gridPort"] = "Port",
        ["gridPath"] = "Path",
        ["gridJar"] = "Jar",
        ["stopService"] = "Stop",
        ["startService"] = "Start",
        ["updateService"] = "Update",
        ["openLogFolder"] = "Open Logs",
        ["gridActivatedAt"] = "Imported At",
        ["gridLicenseId"] = "License ID",
        ["gridCustomerCode"] = "Customer Code",
        ["gridEdition"] = "Edition",
        ["gridEffectiveAt"] = "Effective At",
        ["gridExpireAt"] = "Expire At",
        ["gridModules"] = "Modules",
        ["generateRequest"] = "Generate Request File",
        ["importLicense"] = "Import License File",
        ["startWeb"] = "Start Web",
        ["stopWeb"] = "Stop Web",
        ["openWeb"] = "Open Web",
        ["startSelectedService"] = "Start Selected",
        ["stopSelectedService"] = "Stop Selected",
        ["restartSelectedService"] = "Restart Selected",
        ["openSelectedLogDir"] = "Open Selected Logs",
        ["startAll"] = "Start All Services",
        ["stopAll"] = "Stop All Services",
        ["startBackendAll"] = "Start All Backend",
        ["refresh"] = "Refresh",
        ["openInstallFolder"] = "Open Install Folder",
        ["selectServicePrompt"] = "Select a service in the service list first.",
        ["licenseTitle"] = "Forgex License",
        ["selectLicenseTitle"] = "Select license.lic",
        ["licenseFilter"] = "Forgex license (*.lic)|*.lic|All files (*.*)|*.*",
        ["jarFilter"] = "Java Jar (*.jar)|*.jar|All files (*.*)|*.*",
        ["selectBackendJarTitle"] = "Select new Jar for {0}",
        ["requestGeneratedMessage"] = "Request file generated:\r\n{0}",
        ["licenseImportedMessage"] = "License imported to:\r\n{0}",
        ["backendJarUpdatedMessage"] = "{0} updated:\r\n{1}",
        ["licenseDuration"] = "Duration",
        ["licenseExpireAt"] = "Expire At",
        ["licenseRecords"] = "License Records",
        ["licenseMissing"] = "Current license file was not found.",
        ["licenseInvalid"] = "Current license parse failed: {0}",
        ["licenseUnknown"] = "Unknown",
        ["licensePermanent"] = "Permanent",
        ["licenseDurationDays"] = "{0} days",
        ["licenseSummary"] = "License ID: {0}    Customer Code: {1}    Edition: {2}    Modules: {3}",
        ["logViewRefreshed"] = "View refreshed.",
        ["logLanguageChanged"] = "Language switched.",
        ["logRequestGenerated"] = "Request file generated: {0}",
        ["logCustomerCode"] = "Customer code: {0}",
        ["logLicenseImported"] = "License imported from {0}",
        ["logLicenseHistoryFailed"] = "License history read failed: {0}",
        ["logBackendJarUpdated"] = "{0} Jar updated: {1}",
        ["logBackendJarBackup"] = "Old Jar backed up: {0}"
    };

    public static string Resolve(string language, string key)
    {
        var map = language.Equals("zh", StringComparison.OrdinalIgnoreCase) ? Zh : En;
        if (map.TryGetValue(key, out var value))
        {
            return value;
        }

        return En.TryGetValue(key, out var fallback) ? fallback : key;
    }
}

internal static class ControlCenterDiagnostics
{
    public static void Write(string installRoot, string message, Exception ex)
    {
        try
        {
            var logRoot = Path.Combine(installRoot, "logs", "control-center");
            Directory.CreateDirectory(logRoot);
            var logFile = Path.Combine(logRoot, "control-center-error.log");
            var content = $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}{Environment.NewLine}{ex}{Environment.NewLine}";
            File.AppendAllText(logFile, content, JsonHelper.Utf8NoBom);
        }
        catch
        {
            // Diagnostics must never crash the control center.
        }
    }
}

internal static class ControlCenterLicenseReader
{
    public static LicensePayload? ReadCurrentLicense(string licenseDirectory)
    {
        var licensePath = Path.Combine(licenseDirectory, "license.lic");
        if (!File.Exists(licensePath))
        {
            return null;
        }

        var licenseText = File.ReadAllText(licensePath, JsonHelper.Utf8NoBom);
        var payloadJson = System.Text.Encoding.UTF8.GetString(KeyMaterialHelper.DecodePayload(licenseText));
        return JsonSerializer.Deserialize<LicensePayload>(payloadJson, JsonHelper.Options)
               ?? throw new InvalidOperationException("license.lic payload parse failed.");
    }

    public static IReadOnlyList<LicenseActivationHistoryRecord> ReadHistory(string licenseDirectory)
    {
        var historyPath = Path.Combine(licenseDirectory, "activation-history.json");
        if (!File.Exists(historyPath))
        {
            return [];
        }

        var json = File.ReadAllText(historyPath, JsonHelper.Utf8NoBom);
        return JsonSerializer.Deserialize<List<LicenseActivationHistoryRecord>>(json, JsonHelper.Options) ?? [];
    }
}

internal sealed class LicenseActivationHistoryRecord
{
    public string ActivatedAt { get; set; } = "";

    public string LicenseId { get; set; } = "";

    public string CustomerCode { get; set; } = "";

    public string MachineCode { get; set; } = "";

    public string EffectiveAt { get; set; } = "";

    public string? ExpireAt { get; set; }

    public string Edition { get; set; } = "";

    public List<string> Modules { get; set; } = [];

    public string LicensePath { get; set; } = "";
}

internal sealed class ProcessLogRelay : IDisposable
{
    private readonly object _syncRoot = new();
    private readonly StreamWriter _stdoutWriter;
    private readonly StreamWriter _stderrWriter;
    private bool _disposed;

    public ProcessLogRelay(string stdoutPath, string stderrPath)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(stdoutPath)!);
        Directory.CreateDirectory(Path.GetDirectoryName(stderrPath)!);
        _stdoutWriter = new StreamWriter(new FileStream(stdoutPath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));
        _stderrWriter = new StreamWriter(new FileStream(stderrPath, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));
    }

    public void Attach(Process process)
    {
        process.OutputDataReceived += (_, eventArgs) => Write(_stdoutWriter, eventArgs.Data);
        process.ErrorDataReceived += (_, eventArgs) => Write(_stderrWriter, eventArgs.Data);
        process.Exited += (_, _) => Dispose();
    }

    private void Write(StreamWriter writer, string? data)
    {
        if (data is null)
        {
            return;
        }

        lock (_syncRoot)
        {
            if (_disposed)
            {
                return;
            }

            try
            {
                writer.WriteLine(data);
                writer.Flush();
            }
            catch
            {
                // Output redirection is best-effort and must not terminate the control center.
            }
        }
    }

    public void Dispose()
    {
        lock (_syncRoot)
        {
            if (_disposed)
            {
                return;
            }

            _disposed = true;
            try
            {
                _stdoutWriter.Dispose();
            }
            catch
            {
            }

            try
            {
                _stderrWriter.Dispose();
            }
            catch
            {
            }
        }
    }
}

internal sealed record ServiceJarUpdateResult(string TargetPath, string? BackupPath);

internal sealed class ForgexServiceManager
{
    private readonly ForgexControlConfig _config;

    public ForgexServiceManager(ForgexControlConfig config)
    {
        _config = config;
    }

    public void StartAll(Action<string> log)
    {
        foreach (var service in _config.Services.OrderBy(item => item.StartOrder))
        {
            try
            {
                Start(service, log);
            }
            catch (Exception ex)
            {
                ControlCenterDiagnostics.Write(_config.InstallRoot, $"{service.ServiceId} start failed", ex);
                log($"{service.ServiceId} failed to start: {ex.Message}");
            }
        }
    }

    public void StopAll(Action<string> log)
    {
        foreach (var service in _config.Services.OrderByDescending(item => item.StartOrder))
        {
            try
            {
                Stop(service, log);
            }
            catch (Exception ex)
            {
                ControlCenterDiagnostics.Write(_config.InstallRoot, $"{service.ServiceId} stop failed", ex);
                log($"{service.ServiceId} failed to stop: {ex.Message}");
            }
        }
    }

    public bool TryGetService(string serviceId, out ForgexServiceConfig? service)
    {
        service = _config.Services.FirstOrDefault(item => item.ServiceId.Equals(serviceId, StringComparison.OrdinalIgnoreCase));
        return service is not null;
    }

    public void StartService(string serviceId, Action<string> log)
    {
        if (!TryGetService(serviceId, out var service) || service is null)
        {
            throw new InvalidOperationException($"Service not found: {serviceId}");
        }

        Start(service, log);
    }

    public void StopService(string serviceId, Action<string> log)
    {
        if (!TryGetService(serviceId, out var service) || service is null)
        {
            throw new InvalidOperationException($"Service not found: {serviceId}");
        }

        Stop(service, log);
    }

    public void RestartService(string serviceId, Action<string> log)
    {
        if (!TryGetService(serviceId, out var service) || service is null)
        {
            throw new InvalidOperationException($"Service not found: {serviceId}");
        }

        Stop(service, log);
        Start(service, log);
    }

    public ServiceJarUpdateResult UpdateServiceJar(string serviceId, string sourceJarPath, Action<string> log)
    {
        if (!TryGetService(serviceId, out var service) || service is null)
        {
            throw new InvalidOperationException($"Service not found: {serviceId}");
        }

        if (string.IsNullOrWhiteSpace(sourceJarPath) || !File.Exists(sourceJarPath))
        {
            throw new FileNotFoundException("Source jar not found.", sourceJarPath);
        }

        if (string.IsNullOrWhiteSpace(service.JarPath))
        {
            throw new InvalidOperationException($"Jar path is not configured for service: {serviceId}");
        }

        var targetPath = service.JarPath;
        if (string.Equals(
                Path.GetFullPath(sourceJarPath),
                Path.GetFullPath(targetPath),
                StringComparison.OrdinalIgnoreCase))
        {
            throw new InvalidOperationException($"Source jar is already the configured jar for service: {serviceId}");
        }

        var wasRunning = GetStatus(service).Contains("Running", StringComparison.OrdinalIgnoreCase);
        if (wasRunning)
        {
            Stop(service, log);
        }

        var targetDirectory = Path.GetDirectoryName(targetPath);
        if (!string.IsNullOrWhiteSpace(targetDirectory))
        {
            Directory.CreateDirectory(targetDirectory);
        }

        string? backupPath = null;
        if (File.Exists(targetPath))
        {
            var backupRoot = string.IsNullOrWhiteSpace(_config.BackupDir)
                ? Path.Combine(_config.InstallRoot, "backup")
                : _config.BackupDir;
            var backupDirectory = Path.Combine(
                backupRoot,
                $"service-update-{DateTime.Now:yyyyMMddHHmmss}",
                service.ServiceId);
            Directory.CreateDirectory(backupDirectory);
            backupPath = Path.Combine(backupDirectory, Path.GetFileName(targetPath));
            File.Copy(targetPath, backupPath, overwrite: true);
            log($"{service.ServiceId} old jar backed up: {backupPath}");
        }

        File.Copy(sourceJarPath, targetPath, overwrite: true);
        log($"{service.ServiceId} jar updated: {targetPath}");

        if (wasRunning)
        {
            Start(service, log);
        }

        return new ServiceJarUpdateResult(targetPath, backupPath);
    }

    public string GetStatus(ForgexServiceConfig service)
    {
        if (TryGetWindowsService(service, out var controller) && controller is not null)
        {
            using (controller)
            {
                return controller.Status.ToString();
            }
        }

        if (TryReadPid(service.PidFile, out var pid))
        {
            try
            {
                var process = Process.GetProcessById(pid);
                return process.HasExited ? "Stopped" : $"Running(pid:{pid})";
            }
            catch
            {
                return "Stopped";
            }
        }

        return "NotInstalled";
    }

    public string GetServiceLogDirectory(string serviceId)
    {
        if (serviceId.Equals("web", StringComparison.OrdinalIgnoreCase))
        {
            return Path.Combine(_config.LogDir, "web");
        }

        if (!TryGetService(serviceId, out var service) || service is null)
        {
            throw new InvalidOperationException($"Service not found: {serviceId}");
        }

        return service.LogDir;
    }

    private void Start(ForgexServiceConfig service, Action<string> log)
    {
        EnsureServiceDirectories(service);
        EnsureWindowsServiceWrapperConfig(service, log);

        if (TryGetWindowsService(service, out var controller) && controller is not null)
        {
            using (controller)
            {
                if (controller.Status is ServiceControllerStatus.Running or ServiceControllerStatus.StartPending)
                {
                    log($"{service.ServiceId} already running.");
                    return;
                }

                controller.Start();
                controller.WaitForStatus(ServiceControllerStatus.Running, TimeSpan.FromSeconds(45));
                log($"{service.ServiceId} started by Windows service.");
                return;
            }
        }

        StartProcess(service, log);
    }

    private void Stop(ForgexServiceConfig service, Action<string> log)
    {
        if (TryGetWindowsService(service, out var controller) && controller is not null)
        {
            using (controller)
            {
                if (controller.Status is ServiceControllerStatus.Stopped or ServiceControllerStatus.StopPending)
                {
                    log($"{service.ServiceId} already stopped.");
                    return;
                }

                controller.Stop();
                controller.WaitForStatus(ServiceControllerStatus.Stopped, TimeSpan.FromSeconds(45));
                log($"{service.ServiceId} stopped by Windows service.");
                return;
            }
        }

        StopProcess(service, log);
    }

    private void StartProcess(ForgexServiceConfig service, Action<string> log)
    {
        if (string.IsNullOrWhiteSpace(service.JarPath) || !File.Exists(service.JarPath))
        {
            log($"{service.ServiceId} skipped, jar not found: {service.JarPath}");
            return;
        }

        if (TryReadPid(service.PidFile, out var existingPid))
        {
            try
            {
                var existing = Process.GetProcessById(existingPid);
                if (!existing.HasExited)
                {
                    log($"{service.ServiceId} already running with pid {existingPid}.");
                    return;
                }
            }
            catch
            {
                // stale pid file is overwritten below
            }
        }

        EnsureServiceDirectories(service);

        var stdout = Path.Combine(service.LogDir, "stdout.log");
        var stderr = Path.Combine(service.LogDir, "stderr.log");
        var startInfo = new ProcessStartInfo
        {
            FileName = _config.JavaExe,
            Arguments = $"-Dfile.encoding=UTF-8 -Dforgex.deployment.log-dir=\"{_config.LogDir}\" -DLOG_DIR=\"{_config.LogDir}\" -Dlogging.file.path=\"{_config.LogDir}\" -jar \"{service.JarPath}\" --spring.profiles.active={_config.DeployProfile} --server.port={service.Port}",
            WorkingDirectory = _config.InstallRoot,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };

        ApplyEnvironment(startInfo);
        startInfo.Environment["SERVER_PORT"] = service.Port.ToString();

        var process = new Process { StartInfo = startInfo, EnableRaisingEvents = true };
        var relay = new ProcessLogRelay(stdout, stderr);
        relay.Attach(process);

        try
        {
            process.Start();
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();
        }
        catch
        {
            relay.Dispose();
            process.Dispose();
            throw;
        }

        File.WriteAllText(service.PidFile, process.Id.ToString(), JsonHelper.Utf8NoBom);
        log($"{service.ServiceId} started in process mode, pid {process.Id}.");
    }

    private void StopProcess(ForgexServiceConfig service, Action<string> log)
    {
        if (!TryReadPid(service.PidFile, out var pid))
        {
            log($"{service.ServiceId} has no pid file.");
            return;
        }

        try
        {
            var process = Process.GetProcessById(pid);
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
                process.WaitForExit(15000);
            }

            log($"{service.ServiceId} stopped, pid {pid}.");
        }
        catch
        {
            log($"{service.ServiceId} pid {pid} was not running.");
        }
        finally
        {
            if (File.Exists(service.PidFile))
            {
                File.Delete(service.PidFile);
            }
        }
    }

    private void ApplyEnvironment(ProcessStartInfo startInfo)
    {
        startInfo.Environment["FORGEX_HOME"] = _config.InstallRoot;
        startInfo.Environment["FORGEX_INSTANCE_CODE"] = _config.InstanceCode;
        startInfo.Environment["FORGEX_PROFILE"] = _config.DeployProfile;
        startInfo.Environment["FORGEX_DEPLOYMENT_PROFILE"] = _config.DeployProfile;
        startInfo.Environment["FORGEX_LICENSE_DIR"] = _config.LicenseDir;
        startInfo.Environment["FORGEX_UPLOAD_DIR"] = _config.UploadDir;
        startInfo.Environment["FORGEX_LOG_DIR"] = _config.LogDir;
        startInfo.Environment["LOG_DIR"] = _config.LogDir;
        startInfo.Environment["LOG_PATH"] = _config.LogDir;
        startInfo.Environment["LOGGING_FILE_PATH"] = _config.LogDir;
        startInfo.Environment["FORGEX_BACKUP_DIR"] = _config.BackupDir;
        startInfo.Environment["FORGEX_NACOS_ADDR"] = _config.NacosAddr;
        startInfo.Environment["FORGEX_NACOS_NAMESPACE"] = _config.NacosNamespace;
        startInfo.Environment["FORGEX_NACOS_GROUP"] = _config.NacosGroup;
        startInfo.Environment["FORGEX_DATASOURCE_CONFIG"] = _config.DatasourceConfig;
        startInfo.Environment["FORGEX_INTEGRATION_DATASOURCE_CONFIG"] = _config.IntegrationDatasourceConfig;
        startInfo.Environment["FORGEX_REDIS_ADDR"] = _config.RedisAddr;
        startInfo.Environment["FORGEX_ROCKETMQ_NAME_SERVER"] = _config.RocketMqAddr;
        startInfo.Environment["FORGEX_MYSQL_URL"] = _config.MysqlUrl;
        startInfo.Environment["FORGEX_LICENSE_PUBLIC_KEY_FILE_NAME"] = "public-key.base64";
        startInfo.Environment["FORGEX_LICENSE_FILE_NAME"] = "license.lic";
        startInfo.Environment["FORGEX_REQUEST_INFO_FILE_NAME"] = "request-info.json";
        startInfo.Environment["FORGEX_LICENSE_HISTORY_FILE_NAME"] = "activation-history.json";
    }

    private void EnsureServiceDirectories(ForgexServiceConfig service)
    {
        if (!string.IsNullOrWhiteSpace(_config.LogDir))
        {
            Directory.CreateDirectory(_config.LogDir);
            Directory.CreateDirectory(Path.Combine(_config.LogDir, service.ServiceId));
        }

        Directory.CreateDirectory(service.LogDir);
        Directory.CreateDirectory(Path.GetDirectoryName(service.PidFile)!);
    }

    private void EnsureWindowsServiceWrapperConfig(ForgexServiceConfig service, Action<string> log)
    {
        if (string.IsNullOrWhiteSpace(service.WrapperXmlPath) || !File.Exists(service.WrapperXmlPath))
        {
            return;
        }

        try
        {
            var content = File.ReadAllText(service.WrapperXmlPath);
            var original = content;
            var serviceLogDir = Path.Combine(_config.LogDir, service.ServiceId);
            Directory.CreateDirectory(serviceLogDir);

            content = ReplaceSpringProfileArgument(content, _config.DeployProfile);
            content = SetJavaSystemPropertyArgument(content, "forgex.deployment.log-dir", _config.LogDir);
            content = SetJavaSystemPropertyArgument(content, "LOG_DIR", _config.LogDir);
            content = SetJavaSystemPropertyArgument(content, "logging.file.path", _config.LogDir);
            content = SetWrapperLogPath(content, serviceLogDir);
            content = SetWrapperEnvironment(content, "FORGEX_LOG_DIR", _config.LogDir, "FORGEX_UPLOAD_DIR");
            content = SetWrapperEnvironment(content, "LOG_DIR", _config.LogDir, "FORGEX_LOG_DIR");
            content = SetWrapperEnvironment(content, "LOG_PATH", _config.LogDir, "LOG_DIR");
            content = SetWrapperEnvironment(content, "LOGGING_FILE_PATH", _config.LogDir, "LOG_PATH");
            content = SetWrapperEnvironment(content, "FORGEX_PROFILE", _config.DeployProfile, "FORGEX_INSTANCE_CODE");
            content = SetWrapperEnvironment(content, "FORGEX_DEPLOYMENT_PROFILE", _config.DeployProfile, "FORGEX_PROFILE");
            content = SetWrapperEnvironment(content, "FORGEX_NACOS_NAMESPACE", _config.NacosNamespace, "FORGEX_NACOS_ADDR");
            content = SetWrapperEnvironment(content, "FORGEX_NACOS_GROUP", _config.NacosGroup, "FORGEX_NACOS_NAMESPACE");
            content = SetWrapperEnvironment(content, "FORGEX_DATASOURCE_CONFIG", _config.DatasourceConfig, "FORGEX_NACOS_GROUP");
            content = SetWrapperEnvironment(content, "FORGEX_INTEGRATION_DATASOURCE_CONFIG", _config.IntegrationDatasourceConfig, "FORGEX_DATASOURCE_CONFIG");

            if (!string.Equals(content, original, StringComparison.Ordinal))
            {
                File.WriteAllText(service.WrapperXmlPath, content, JsonHelper.Utf8NoBom);
                log($"{service.ServiceId} Windows service wrapper config repaired.");
            }
        }
        catch (Exception ex)
        {
            ControlCenterDiagnostics.Write(_config.InstallRoot, $"{service.ServiceId} wrapper repair failed", ex);
            log($"{service.ServiceId} wrapper repair failed: {ex.Message}");
        }
    }

    private static string ReplaceSpringProfileArgument(string content, string profile)
    {
        return System.Text.RegularExpressions.Regex.Replace(
            content,
            @"(\s--spring\.profiles\.active=)[^\s<""]+",
            match => $"{match.Groups[1].Value}{profile}");
    }

    private static string SetJavaSystemPropertyArgument(string content, string name, string value)
    {
        var escapedName = System.Text.RegularExpressions.Regex.Escape(name);
        var argument = $"-D{name}=\"{value}\"";
        var propertyPattern = $@"-D{escapedName}=(?:""[^""]*""|[^\s<""]+)";
        if (System.Text.RegularExpressions.Regex.IsMatch(content, propertyPattern))
        {
            return System.Text.RegularExpressions.Regex.Replace(content, propertyPattern, argument);
        }

        return System.Text.RegularExpressions.Regex.Replace(
            content,
            @"(\s-jar\s+)",
            match => $" {argument}{match.Groups[1].Value}",
            System.Text.RegularExpressions.RegexOptions.None,
            TimeSpan.FromSeconds(2));
    }

    private static string SetWrapperLogPath(string content, string logPath)
    {
        var escapedValue = System.Security.SecurityElement.Escape(logPath);
        if (System.Text.RegularExpressions.Regex.IsMatch(content, @"<logpath>.*?</logpath>", System.Text.RegularExpressions.RegexOptions.Singleline))
        {
            return System.Text.RegularExpressions.Regex.Replace(
                content,
                @"<logpath>.*?</logpath>",
                $"<logpath>{escapedValue}</logpath>",
                System.Text.RegularExpressions.RegexOptions.Singleline);
        }

        return content.Replace("</service>", $"  <logpath>{escapedValue}</logpath>{Environment.NewLine}</service>", StringComparison.OrdinalIgnoreCase);
    }

    private static string SetWrapperEnvironment(string content, string name, string value, string insertAfter)
    {
        var escapedName = System.Text.RegularExpressions.Regex.Escape(name);
        var escapedValue = System.Security.SecurityElement.Escape(value);
        var line = $"  <env name=\"{name}\" value=\"{escapedValue}\" />";
        var envPattern = $@"<env\s+name=""{escapedName}""\s+value=""[^""]*""\s*/>";

        if (System.Text.RegularExpressions.Regex.IsMatch(content, envPattern))
        {
            return System.Text.RegularExpressions.Regex.Replace(content, envPattern, line);
        }

        var escapedAnchor = System.Text.RegularExpressions.Regex.Escape(insertAfter);
        var anchorPattern = $@"(<env\s+name=""{escapedAnchor}""\s+value=""[^""]*""\s*/>)";
        if (System.Text.RegularExpressions.Regex.IsMatch(content, anchorPattern))
        {
            return System.Text.RegularExpressions.Regex.Replace(content, anchorPattern, match => $"{match.Groups[1].Value}{Environment.NewLine}{line}");
        }

        return content.Replace("</service>", $"{line}{Environment.NewLine}</service>", StringComparison.OrdinalIgnoreCase);
    }

    private static bool TryGetWindowsService(ForgexServiceConfig service, out ServiceController? controller)
    {
        controller = null;
        try
        {
            var item = ServiceController.GetServices()
                .FirstOrDefault(current => current.ServiceName.Equals(service.ServiceName, StringComparison.OrdinalIgnoreCase));
            if (item is null)
            {
                return false;
            }

            controller = item;
            return true;
        }
        catch
        {
            return false;
        }
    }

    private static bool TryReadPid(string path, out int pid)
    {
        pid = 0;
        try
        {
            return File.Exists(path) && int.TryParse(File.ReadAllText(path).Trim(), out pid);
        }
        catch
        {
            return false;
        }
    }
}

internal sealed class StaticWebServer
{
    private readonly ForgexControlConfig _config;
    private readonly string _pidFile;

    public StaticWebServer(ForgexControlConfig config)
    {
        _config = config;
        _pidFile = Path.Combine(config.ServiceStateDir, "web.pid");
    }

    public static int RunForeground(string installRoot)
    {
        try
        {
            var config = ForgexControlConfig.Load(installRoot);
            using var server = new ForegroundStaticWebServer(config);
            server.Run();
            return 0;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine(ex.Message);
            return 1;
        }
    }

    public string GetStatus()
    {
        if (TryReadPid(_pidFile, out var pid))
        {
            try
            {
                var process = Process.GetProcessById(pid);
                return process.HasExited ? "Stopped" : $"Running(pid:{pid})";
            }
            catch
            {
                return "Stopped";
            }
        }

        return "Stopped";
    }

    public void Start(Action<string> log)
    {
        if (TryReadPid(_pidFile, out var existingPid))
        {
            try
            {
                var existing = Process.GetProcessById(existingPid);
                if (!existing.HasExited)
                {
                    log($"Web server already running at http://127.0.0.1:{_config.FrontendPort}/");
                    return;
                }
            }
            catch
            {
                // stale pid file is overwritten below
            }
        }

        if (!Directory.Exists(_config.FrontendDir))
        {
            throw new DirectoryNotFoundException($"Frontend directory not found: {_config.FrontendDir}");
        }

        if (!IsPortAvailable(_config.FrontendPort))
        {
            log($"Web port {_config.FrontendPort} is already in use.");
            return;
        }

        Directory.CreateDirectory(_config.ServiceStateDir);
        Directory.CreateDirectory(Path.Combine(_config.LogDir, "web"));

        var exePath = Environment.ProcessPath ?? Application.ExecutablePath;
        var startInfo = new ProcessStartInfo
        {
            FileName = exePath,
            Arguments = $"--install-root \"{_config.InstallRoot}\" --serve-web",
            WorkingDirectory = _config.InstallRoot,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };

        var stdout = Path.Combine(_config.LogDir, "web", "stdout.log");
        var stderr = Path.Combine(_config.LogDir, "web", "stderr.log");
        var process = new Process { StartInfo = startInfo, EnableRaisingEvents = true };
        var relay = new ProcessLogRelay(stdout, stderr);
        relay.Attach(process);

        try
        {
            process.Start();
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();
        }
        catch
        {
            relay.Dispose();
            process.Dispose();
            throw;
        }

        File.WriteAllText(_pidFile, process.Id.ToString(), JsonHelper.Utf8NoBom);
        log($"Web server started: http://127.0.0.1:{_config.FrontendPort}/");
    }

    public void Stop(Action<string> log)
    {
        if (!TryReadPid(_pidFile, out var pid))
        {
            log("Web server has no pid file.");
            return;
        }

        try
        {
            var process = Process.GetProcessById(pid);
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
                process.WaitForExit(10000);
            }

            log($"Web server stopped, pid {pid}.");
        }
        catch
        {
            log($"Web server pid {pid} was not running.");
        }
        finally
        {
            if (File.Exists(_pidFile))
            {
                File.Delete(_pidFile);
            }
        }
    }

    private static bool TryReadPid(string path, out int pid)
    {
        pid = 0;
        try
        {
            return File.Exists(path) && int.TryParse(File.ReadAllText(path).Trim(), out pid);
        }
        catch
        {
            return false;
        }
    }

    private static bool IsPortAvailable(int port)
    {
        try
        {
            var listener = new TcpListener(IPAddress.Loopback, port);
            listener.Start();
            listener.Stop();
            return true;
        }
        catch
        {
            return false;
        }
    }
}

internal sealed class FrontendWebServer
{
    private readonly ForgexControlConfig _config;
    private readonly NginxFrontendServer _nginxServer;
    private readonly StaticWebServer _fallbackServer;

    public FrontendWebServer(ForgexControlConfig config)
    {
        _config = config;
        _nginxServer = new NginxFrontendServer(config);
        _fallbackServer = new StaticWebServer(config);
    }

    public string GetStatus()
    {
        var nginxStatus = _nginxServer.GetStatus();
        if (nginxStatus.StartsWith("Running", StringComparison.OrdinalIgnoreCase))
        {
            return $"Nginx {nginxStatus}";
        }

        var fallbackStatus = _fallbackServer.GetStatus();
        return fallbackStatus.StartsWith("Running", StringComparison.OrdinalIgnoreCase)
            ? $"Built-in {fallbackStatus}"
            : "Stopped";
    }

    public void Start(Action<string> log)
    {
        if (_nginxServer.CanStart)
        {
            _nginxServer.Start(log);
            return;
        }

        log("Nginx executable or config not found. Falling back to built-in web server.");
        _fallbackServer.Start(log);
    }

    public void Stop(Action<string> log)
    {
        _nginxServer.Stop(log);
        _fallbackServer.Stop(log);
    }

    public void Restart(Action<string> log)
    {
        Stop(log);
        Start(log);
    }
}

internal sealed class NginxFrontendServer
{
    private readonly ForgexControlConfig _config;
    private readonly string _pidFile;

    public NginxFrontendServer(ForgexControlConfig config)
    {
        _config = config;
        _pidFile = Path.Combine(config.ServiceStateDir, "nginx.pid");
    }

    public bool CanStart => ResolveNginxExe() is not null && File.Exists(ResolveConfigPath());

    public string GetStatus()
    {
        if (TryReadPid(_pidFile, out var pid))
        {
            try
            {
                var process = Process.GetProcessById(pid);
                return process.HasExited ? "Stopped" : $"Running(pid:{pid})";
            }
            catch
            {
                return "Stopped";
            }
        }

        var nginxExe = ResolveNginxExe();
        if (nginxExe is null)
        {
            return "Stopped";
        }

        var nginxProcess = Process.GetProcessesByName("nginx").FirstOrDefault(process =>
        {
            try
            {
                return process.MainModule?.FileName.Equals(nginxExe, StringComparison.OrdinalIgnoreCase) == true;
            }
            catch
            {
                return false;
            }
        });

        return nginxProcess is null ? "Stopped" : $"Running(pid:{nginxProcess.Id})";
    }

    public void Start(Action<string> log)
    {
        var nginxExe = ResolveNginxExe();
        var configPath = ResolveConfigPath();
        if (nginxExe is null || !File.Exists(configPath))
        {
            log("Nginx executable or config not found.");
            return;
        }

        var status = GetStatus();
        if (status.StartsWith("Running", StringComparison.OrdinalIgnoreCase))
        {
            log($"Nginx already running at http://127.0.0.1:{_config.FrontendPort}/");
            return;
        }

        Directory.CreateDirectory(_config.ServiceStateDir);
        Directory.CreateDirectory(Path.Combine(_config.LogDir, "nginx"));
        DeleteStalePidFile();

        var startInfo = new ProcessStartInfo
        {
            FileName = nginxExe,
            Arguments = $"-p \"{_config.InstallRoot}\" -c \"{configPath}\"",
            WorkingDirectory = _config.InstallRoot,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };

        var stdout = Path.Combine(_config.LogDir, "nginx", "stdout.log");
        var stderr = Path.Combine(_config.LogDir, "nginx", "stderr.log");
        var process = new Process { StartInfo = startInfo, EnableRaisingEvents = true };
        var relay = new ProcessLogRelay(stdout, stderr);
        relay.Attach(process);

        try
        {
            process.Start();
            process.BeginOutputReadLine();
            process.BeginErrorReadLine();
        }
        catch
        {
            relay.Dispose();
            process.Dispose();
            throw;
        }

        WaitForPidFile(process);
        log($"Nginx started: http://127.0.0.1:{_config.FrontendPort}/");
    }

    public void Stop(Action<string> log)
    {
        var nginxExe = ResolveNginxExe();
        var configPath = ResolveConfigPath();
        if (nginxExe is not null && File.Exists(configPath))
        {
            var startInfo = new ProcessStartInfo
            {
                FileName = nginxExe,
                Arguments = $"-p \"{_config.InstallRoot}\" -c \"{configPath}\" -s stop",
                WorkingDirectory = _config.InstallRoot,
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true
            };

            try
            {
                using var stopProcess = Process.Start(startInfo);
                stopProcess?.WaitForExit(10000);
            }
            catch
            {
                // If nginx cannot process the stop command, fall back to the tracked pid below.
            }
        }

        if (!TryReadPid(_pidFile, out var pid))
        {
            log("Nginx has no pid file.");
            return;
        }

        try
        {
            var process = Process.GetProcessById(pid);
            if (!process.HasExited)
            {
                process.Kill(entireProcessTree: true);
                process.WaitForExit(10000);
            }

            log($"Nginx stopped, pid {pid}.");
        }
        catch
        {
            log($"Nginx pid {pid} was not running.");
        }
        finally
        {
            if (File.Exists(_pidFile))
            {
                File.Delete(_pidFile);
            }
        }
    }

    private string ResolveConfigPath()
    {
        if (!string.IsNullOrWhiteSpace(_config.NginxConfPath))
        {
            return _config.NginxConfPath;
        }

        return Path.Combine(_config.NginxDir, "forgex.conf");
    }

    private string? ResolveNginxExe()
    {
        var candidates = new[]
        {
            Path.Combine(_config.NginxDir, "nginx.exe"),
            Path.Combine(_config.InstallRoot, "tools", "nginx", "nginx.exe"),
            Path.Combine(_config.InstallRoot, "nginx", "sbin", "nginx.exe")
        };

        foreach (var candidate in candidates)
        {
            if (File.Exists(candidate))
            {
                return candidate;
            }
        }

        try
        {
            return Environment.GetEnvironmentVariable("PATH")?
                .Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
                .Select(path => Path.Combine(path, "nginx.exe"))
                .FirstOrDefault(File.Exists);
        }
        catch
        {
            return null;
        }
    }

    private void DeleteStalePidFile()
    {
        if (!File.Exists(_pidFile))
        {
            return;
        }

        try
        {
            File.Delete(_pidFile);
        }
        catch
        {
            // Nginx can overwrite the pid file later; this only cleans common stale files.
        }
    }

    private void WaitForPidFile(Process bootstrapProcess)
    {
        var deadline = DateTime.UtcNow.AddSeconds(8);
        while (DateTime.UtcNow < deadline)
        {
            if (TryReadPid(_pidFile, out _))
            {
                return;
            }

            if (bootstrapProcess.HasExited && bootstrapProcess.ExitCode != 0)
            {
                return;
            }

            Thread.Sleep(200);
        }
    }

    private static bool TryReadPid(string path, out int pid)
    {
        pid = 0;
        try
        {
            return File.Exists(path) && int.TryParse(File.ReadAllText(path).Trim(), out pid);
        }
        catch
        {
            return false;
        }
    }
}

internal sealed class ForegroundStaticWebServer : IDisposable
{
    private readonly ForgexControlConfig _config;
    private readonly HttpListener _listener = new();
    private readonly HttpClient _httpClient = new();
    private readonly Dictionary<string, string> _contentTypes = new(StringComparer.OrdinalIgnoreCase)
    {
        [".html"] = "text/html; charset=utf-8",
        [".js"] = "application/javascript; charset=utf-8",
        [".css"] = "text/css; charset=utf-8",
        [".json"] = "application/json; charset=utf-8",
        [".png"] = "image/png",
        [".jpg"] = "image/jpeg",
        [".jpeg"] = "image/jpeg",
        [".gif"] = "image/gif",
        [".svg"] = "image/svg+xml",
        [".ico"] = "image/x-icon",
        [".mp4"] = "video/mp4",
        [".woff"] = "font/woff",
        [".woff2"] = "font/woff2"
    };

    public ForegroundStaticWebServer(ForgexControlConfig config)
    {
        _config = config;
    }

    public void Run()
    {
        _listener.Prefixes.Add($"http://127.0.0.1:{_config.FrontendPort}/");
        _listener.Start();
        Console.WriteLine($"Forgex frontend server listening on http://127.0.0.1:{_config.FrontendPort}/");

        while (_listener.IsListening)
        {
            try
            {
                var context = _listener.GetContext();
                _ = Task.Run(() => HandleAsync(context));
            }
            catch (HttpListenerException)
            {
                break;
            }
        }
    }

    private async Task HandleAsync(HttpListenerContext context)
    {
        try
        {
            if (context.Request.Url?.AbsolutePath.StartsWith("/api/", StringComparison.OrdinalIgnoreCase) == true)
            {
                await ProxyApiAsync(context);
                return;
            }

            await ServeStaticAsync(context);
        }
        catch (Exception ex)
        {
            await WriteTextAsync(context.Response, 500, ex.Message);
        }
    }

    private async Task ProxyApiAsync(HttpListenerContext context)
    {
        var targetUri = new UriBuilder("http", "127.0.0.1", ResolveGatewayPort(), context.Request.RawUrl ?? "/").Uri;
        using var requestMessage = new HttpRequestMessage(new HttpMethod(context.Request.HttpMethod), targetUri);

        foreach (string headerName in context.Request.Headers)
        {
            if (string.Equals(headerName, "Host", StringComparison.OrdinalIgnoreCase))
            {
                continue;
            }

            requestMessage.Headers.TryAddWithoutValidation(headerName, context.Request.Headers[headerName]);
        }

        if (context.Request.HasEntityBody)
        {
            requestMessage.Content = new StreamContent(context.Request.InputStream);
        }

        using var responseMessage = await _httpClient.SendAsync(requestMessage);
        context.Response.StatusCode = (int)responseMessage.StatusCode;

        foreach (var header in responseMessage.Headers)
        {
            context.Response.Headers[header.Key] = string.Join(",", header.Value);
        }

        foreach (var header in responseMessage.Content.Headers)
        {
            context.Response.Headers[header.Key] = string.Join(",", header.Value);
        }

        await responseMessage.Content.CopyToAsync(context.Response.OutputStream);
        context.Response.Close();
    }

    private async Task ServeStaticAsync(HttpListenerContext context)
    {
        var rawPath = context.Request.Url?.AbsolutePath ?? "/";
        var relativePath = WebUtility.UrlDecode(rawPath.TrimStart('/')).Replace('/', Path.DirectorySeparatorChar);
        if (string.IsNullOrWhiteSpace(relativePath))
        {
            relativePath = "index.html";
        }

        var fullPath = Path.GetFullPath(Path.Combine(_config.FrontendDir, relativePath));
        var frontendRoot = Path.GetFullPath(_config.FrontendDir);
        if (!fullPath.StartsWith(frontendRoot, StringComparison.OrdinalIgnoreCase))
        {
            await WriteTextAsync(context.Response, 403, "Forbidden");
            return;
        }

        if (!File.Exists(fullPath))
        {
            fullPath = Path.Combine(frontendRoot, "index.html");
        }

        if (!File.Exists(fullPath))
        {
            await WriteTextAsync(context.Response, 404, "Frontend index.html not found.");
            return;
        }

        var extension = Path.GetExtension(fullPath);
        context.Response.ContentType = _contentTypes.TryGetValue(extension, out var contentType)
            ? contentType
            : "application/octet-stream";
        using var fileStream = File.OpenRead(fullPath);
        context.Response.ContentLength64 = fileStream.Length;
        await fileStream.CopyToAsync(context.Response.OutputStream);
        context.Response.Close();
    }

    private int ResolveGatewayPort()
    {
        return _config.Services.FirstOrDefault(item => item.ServiceId.Equals("gateway", StringComparison.OrdinalIgnoreCase))?.Port ?? 9000;
    }

    private static async Task WriteTextAsync(HttpListenerResponse response, int statusCode, string text)
    {
        response.StatusCode = statusCode;
        response.ContentType = "text/plain; charset=utf-8";
        await using var writer = new StreamWriter(response.OutputStream);
        await writer.WriteAsync(text);
        response.Close();
    }

    public void Dispose()
    {
        if (_listener.IsListening)
        {
            _listener.Stop();
        }

        _listener.Close();
        _httpClient.Dispose();
    }
}

internal sealed class ForgexControlConfig
{
    public string Product { get; set; } = "Forgex";

    public string InstanceCode { get; set; } = "DEFAULT";

    public string DeployProfile { get; set; } = "yanshi";

    public string InstallRoot { get; set; } = AppContext.BaseDirectory;

    public string JavaExe { get; set; } = "java.exe";

    public string LicenseDir { get; set; } = "";

    public string UploadDir { get; set; } = "";

    public string LogDir { get; set; } = "";

    public string BackupDir { get; set; } = "";

    public string FrontendDir { get; set; } = "";

    public string ServicesDir { get; set; } = "";

    public string NginxDir { get; set; } = "";

    public string NginxConfPath { get; set; } = "";

    public string ServiceStateDir { get; set; } = "";

    public string NacosAddr { get; set; } = "127.0.0.1:8848";

    public string NacosNamespace { get; set; } = "yanshi";

    public string NacosGroup { get; set; } = "DEFAULT_GROUP";

    public string DatasourceConfig { get; set; } = "datasource-forgex-dev.yml";

    public string IntegrationDatasourceConfig { get; set; } = "datasource-forgex-integration-dev.yml";

    public string RedisAddr { get; set; } = "127.0.0.1:6379";

    public string RocketMqAddr { get; set; } = "127.0.0.1:9876";

    public string MysqlUrl { get; set; } = "";

    public int FrontendPort { get; set; } = 18080;

    public List<ForgexServiceConfig> Services { get; set; } = [];

    public static ForgexControlConfig Load(string installRoot)
    {
        var configPath = Path.Combine(installRoot, "config", "forgex-control.json");
        if (!File.Exists(configPath))
        {
            throw new FileNotFoundException("Forgex control config not found.", configPath);
        }

        var json = File.ReadAllText(configPath);
        var config = JsonSerializer.Deserialize<ForgexControlConfig>(json, JsonHelper.Options)
                     ?? throw new InvalidOperationException("Forgex control config parse failed.");
        config.InstallRoot = string.IsNullOrWhiteSpace(config.InstallRoot) ? installRoot : config.InstallRoot;
        return config;
    }
}

internal sealed class ForgexServiceConfig
{
    public string ServiceId { get; set; } = "";

    public string DisplayName { get; set; } = "";

    public string ServiceName { get; set; } = "";

    public string JarPath { get; set; } = "";

    public int Port { get; set; }

    public int StartOrder { get; set; }

    public string WorkingDirectory { get; set; } = "";

    public string LogDir { get; set; } = "";

    public string PidFile { get; set; } = "";

    public string WrapperExePath { get; set; } = "";

    public string WrapperXmlPath { get; set; } = "";

    public string InstanceCode { get; set; } = "";
}

internal static class NetworkProbe
{
    public static bool CanPing(string host)
    {
        try
        {
            using var ping = new Ping();
            var reply = ping.Send(host, 1000);
            return reply?.Status == IPStatus.Success;
        }
        catch
        {
            return false;
        }
    }
}
