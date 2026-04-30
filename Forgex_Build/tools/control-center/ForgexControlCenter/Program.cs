using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.ServiceProcess;
using System.Text.Json;
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

        if (options.ServeWeb)
        {
            return StaticWebServer.RunForeground(installRoot);
        }

        ApplicationConfiguration.Initialize();
        Application.Run(new MainForm(installRoot));
        return 0;
    }
}

internal enum ServiceCommand
{
    StartAll,
    StopAll,
    Status,
    StartWeb,
    StopWeb
}

internal sealed class ControlCenterOptions
{
    public string? InstallRoot { get; private init; }

    public bool StartAll { get; private init; }

    public bool StopAll { get; private init; }

    public bool Status { get; private init; }

    public bool StartWeb { get; private init; }

    public bool StopWeb { get; private init; }

    public bool ServeWeb { get; private init; }

    public static ControlCenterOptions Parse(string[] args)
    {
        string? installRoot = null;
        var startAll = false;
        var stopAll = false;
        var status = false;
        var startWeb = false;
        var stopWeb = false;
        var serveWeb = false;

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

            if (arg.Equals("--serve-web", StringComparison.OrdinalIgnoreCase))
            {
                serveWeb = true;
            }
        }

        return new ControlCenterOptions
        {
            InstallRoot = installRoot,
            StartAll = startAll,
            StopAll = stopAll,
            Status = status,
            StartWeb = startWeb,
            StopWeb = stopWeb,
            ServeWeb = serveWeb
        };
    }
}

internal static class CommandRunner
{
    public static int RunHeadless(string installRoot, ServiceCommand command)
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

internal sealed class MainForm : Form
{
    private readonly ForgexControlConfig _config;
    private readonly ForgexServiceManager _serviceManager;
    private readonly FrontendWebServer _webServer;
    private readonly RequestInfoService _requestInfoService = new();
    private readonly LicenseImportService _licenseImportService = new();
    private readonly MachineFingerprintService _machineFingerprintService = new();
    private readonly Label _summaryLabel = new();
    private readonly Label _machineCodeLabel = new();
    private readonly ComboBox _languageComboBox = new();
    private readonly TextBox _machineCodeTextBox = new();
    private readonly TextBox _logTextBox = new();
    private readonly DataGridView _serviceGrid = new();
    private readonly System.Windows.Forms.Timer _timer = new();
    private readonly Dictionary<Control, string> _localizedControls = [];
    private string _language = ResolveDefaultLanguage();

    public MainForm(string installRoot)
    {
        _config = ForgexControlConfig.Load(installRoot);
        _serviceManager = new ForgexServiceManager(_config);
        _webServer = new FrontendWebServer(_config);

        StartPosition = FormStartPosition.CenterScreen;
        Width = 1000;
        Height = 720;
        MinimumSize = new Size(860, 560);

        BuildUi();
        ApplyLanguage();
        RefreshView();

        _timer.Interval = 5000;
        _timer.Tick += (_, _) => RefreshServiceGrid();
        _timer.Start();
    }

    private void BuildUi()
    {
        var root = new TableLayoutPanel
        {
            Dock = DockStyle.Fill,
            ColumnCount = 1,
            RowCount = 5,
            Padding = new Padding(14)
        };
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 48));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 52));
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));

        var headerPanel = new TableLayoutPanel
        {
            Dock = DockStyle.Top,
            ColumnCount = 2,
            AutoSize = true
        };
        headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));

        _summaryLabel.AutoSize = true;
        _summaryLabel.Font = new Font(Font, FontStyle.Bold);
        _summaryLabel.Anchor = AnchorStyles.Left;
        headerPanel.Controls.Add(_summaryLabel, 0, 0);

        var languagePanel = new FlowLayoutPanel
        {
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            Anchor = AnchorStyles.Right,
            WrapContents = false
        };
        languagePanel.Controls.Add(new Label
        {
            Text = "Language / 语言",
            AutoSize = true,
            Anchor = AnchorStyles.Left,
            Padding = new Padding(0, 5, 8, 0)
        });
        _languageComboBox.DropDownStyle = ComboBoxStyle.DropDownList;
        _languageComboBox.Width = 115;
        _languageComboBox.Items.AddRange(["中文", "English"]);
        _languageComboBox.SelectedIndex = _language.Equals("zh", StringComparison.OrdinalIgnoreCase) ? 0 : 1;
        _languageComboBox.SelectedIndexChanged += (_, _) =>
        {
            _language = _languageComboBox.SelectedIndex == 0 ? "zh" : "en";
            ApplyLanguage();
            RefreshServiceGrid();
            AppendLog(T("logLanguageChanged"));
        };
        languagePanel.Controls.Add(_languageComboBox);
        headerPanel.Controls.Add(languagePanel, 1, 0);
        root.Controls.Add(headerPanel, 0, 0);

        var machinePanel = new TableLayoutPanel
        {
            Dock = DockStyle.Top,
            ColumnCount = 2,
            AutoSize = true,
            Padding = new Padding(0, 12, 0, 8)
        };
        machinePanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        machinePanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        _machineCodeLabel.AutoSize = true;
        _machineCodeLabel.Anchor = AnchorStyles.Left;
        _machineCodeLabel.Padding = new Padding(0, 4, 12, 0);
        machinePanel.Controls.Add(_machineCodeLabel, 0, 0);
        _machineCodeTextBox.Dock = DockStyle.Fill;
        _machineCodeTextBox.ReadOnly = true;
        machinePanel.Controls.Add(_machineCodeTextBox, 1, 0);
        root.Controls.Add(machinePanel, 0, 1);

        _serviceGrid.Dock = DockStyle.Fill;
        _serviceGrid.ReadOnly = true;
        _serviceGrid.AllowUserToAddRows = false;
        _serviceGrid.AllowUserToDeleteRows = false;
        _serviceGrid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
        _serviceGrid.RowHeadersVisible = false;
        _serviceGrid.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
        _serviceGrid.Columns.Add("serviceId", "");
        _serviceGrid.Columns.Add("status", "");
        _serviceGrid.Columns.Add("port", "");
        _serviceGrid.Columns.Add("jarPath", "");
        root.Controls.Add(_serviceGrid, 0, 2);

        _logTextBox.Dock = DockStyle.Fill;
        _logTextBox.Multiline = true;
        _logTextBox.ReadOnly = true;
        _logTextBox.ScrollBars = ScrollBars.Vertical;
        root.Controls.Add(_logTextBox, 0, 3);

        var buttonPanel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            Padding = new Padding(0, 10, 0, 0)
        };
        buttonPanel.Controls.Add(CreateButton("generateRequest", GenerateRequest));
        buttonPanel.Controls.Add(CreateButton("importLicense", ImportLicense));
        buttonPanel.Controls.Add(CreateButton("startWeb", StartWeb));
        buttonPanel.Controls.Add(CreateButton("stopWeb", StopWeb));
        buttonPanel.Controls.Add(CreateButton("openWeb", OpenWeb));
        buttonPanel.Controls.Add(CreateButton("startAll", StartAll));
        buttonPanel.Controls.Add(CreateButton("stopAll", StopAll));
        buttonPanel.Controls.Add(CreateButton("refresh", RefreshView));
        buttonPanel.Controls.Add(CreateButton("openInstallFolder", OpenInstallFolder));
        root.Controls.Add(buttonPanel, 0, 4);

        Controls.Add(root);
    }

    private Button CreateButton(string textKey, Action action)
    {
        var button = new Button
        {
            AutoSize = true,
            Height = 32,
            Margin = new Padding(0, 0, 8, 0)
        };
        _localizedControls[button] = textKey;
        button.Click += (_, _) => action();
        return button;
    }

    private void RefreshView()
    {
        ApplyLanguage();
        _machineCodeTextBox.Text = _machineFingerprintService.ResolveMachineCode();
        RefreshServiceGrid();
        AppendLog(T("logViewRefreshed"));
    }

    private void RefreshServiceGrid()
    {
        _serviceGrid.Rows.Clear();
        foreach (var service in _config.Services.OrderBy(item => item.StartOrder))
        {
            _serviceGrid.Rows.Add(
                service.ServiceId,
                LocalizeStatus(_serviceManager.GetStatus(service)),
                service.Port,
                service.JarPath);
        }

        _serviceGrid.Rows.Add(
            "web",
            LocalizeStatus(_webServer.GetStatus()),
            _config.FrontendPort,
            _config.FrontendDir);
    }

    private async void GenerateRequest()
    {
        try
        {
            var outputPath = Path.Combine(_config.LicenseDir, "request-info.json");
            var info = await _requestInfoService.GenerateAsync(
                _config.Product,
                "standard",
                _config.InstanceCode,
                "FXC",
                null,
                outputPath);

            _machineCodeTextBox.Text = info.MachineCode;
            AppendLog(TFormat("logRequestGenerated", outputPath));
            AppendLog(TFormat("logCustomerCode", info.CustomerCode));
            MessageBox.Show(
                this,
                TFormat("requestGeneratedMessage", outputPath),
                T("licenseTitle"),
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private async void ImportLicense()
    {
        try
        {
            using var dialog = new OpenFileDialog
            {
                Title = T("selectLicenseTitle"),
                Filter = T("licenseFilter"),
                CheckFileExists = true
            };

            if (dialog.ShowDialog(this) != DialogResult.OK)
            {
                return;
            }

            await _licenseImportService.ImportLicenseFileAsync(dialog.FileName, _config.LicenseDir);
            AppendLog(TFormat("logLicenseImported", dialog.FileName));
            MessageBox.Show(
                this,
                TFormat("licenseImportedMessage", _config.LicenseDir),
                T("licenseTitle"),
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void StartAll()
    {
        try
        {
            _webServer.Start(AppendRuntimeLog);
            _serviceManager.StartAll(AppendRuntimeLog);
            RefreshServiceGrid();
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void StopAll()
    {
        try
        {
            _serviceManager.StopAll(AppendRuntimeLog);
            _webServer.Stop(AppendRuntimeLog);
            RefreshServiceGrid();
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void OpenInstallFolder()
    {
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = _config.InstallRoot,
                UseShellExecute = true
            });
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void StartWeb()
    {
        try
        {
            _webServer.Start(AppendRuntimeLog);
            RefreshServiceGrid();
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void StopWeb()
    {
        try
        {
            _webServer.Stop(AppendRuntimeLog);
            RefreshServiceGrid();
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void OpenWeb()
    {
        try
        {
            _webServer.Start(AppendRuntimeLog);
            Process.Start(new ProcessStartInfo
            {
                FileName = $"http://127.0.0.1:{_config.FrontendPort}/",
                UseShellExecute = true
            });
            RefreshServiceGrid();
        }
        catch (Exception ex)
        {
            ShowError(ex);
        }
    }

    private void AppendLog(string message)
    {
        _logTextBox.AppendText($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}{Environment.NewLine}");
    }

    private void AppendRuntimeLog(string message)
    {
        AppendLog(LocalizeRuntimeLog(message));
    }

    private void ShowError(Exception ex)
    {
        AppendLog(ex.Message);
        MessageBox.Show(this, ex.Message, T("windowTitle"), MessageBoxButtons.OK, MessageBoxIcon.Error);
    }

    private void ApplyLanguage()
    {
        Text = T("windowTitle");
        _summaryLabel.Text = TFormat("summary", _config.InstanceCode, _config.DeployProfile, _config.InstallRoot);
        _machineCodeLabel.Text = T("machineCode");

        if (_serviceGrid.Columns.Count >= 4)
        {
            _serviceGrid.Columns["serviceId"]!.HeaderText = T("gridService");
            _serviceGrid.Columns["status"]!.HeaderText = T("gridStatus");
            _serviceGrid.Columns["port"]!.HeaderText = T("gridPort");
            _serviceGrid.Columns["jarPath"]!.HeaderText = T("gridJar");
        }

        foreach (var item in _localizedControls)
        {
            item.Key.Text = T(item.Value);
        }
    }

    private string T(string key)
    {
        return ControlCenterText.Resolve(_language, key);
    }

    private string TFormat(string key, params object[] args)
    {
        return string.Format(System.Globalization.CultureInfo.CurrentCulture, T(key), args);
    }

    private string LocalizeStatus(string status)
    {
        if (!_language.Equals("zh", StringComparison.OrdinalIgnoreCase))
        {
            return status;
        }

        if (status.StartsWith("Running(pid:", StringComparison.OrdinalIgnoreCase))
        {
            return status.Replace("Running", "运行中", StringComparison.OrdinalIgnoreCase);
        }

        return status switch
        {
            "Running" => "运行中",
            "Stopped" => "已停止",
            "NotInstalled" => "未安装",
            "StartPending" => "启动中",
            "StopPending" => "停止中",
            "PausePending" => "暂停中",
            "Paused" => "已暂停",
            "ContinuePending" => "恢复中",
            _ => status
        };
    }

    private string LocalizeRuntimeLog(string message)
    {
        if (!_language.Equals("zh", StringComparison.OrdinalIgnoreCase))
        {
            return message;
        }

        if (message.Equals("Nginx executable or config not found. Falling back to built-in web server.", StringComparison.OrdinalIgnoreCase))
        {
            return "未找到 Nginx 程序或配置，已切换为内置前端服务。";
        }

        if (message.Equals("Nginx executable or config not found.", StringComparison.OrdinalIgnoreCase))
        {
            return "未找到 Nginx 程序或配置。";
        }

        const string nginxAlreadyRunning = "Nginx already running at ";
        if (message.StartsWith(nginxAlreadyRunning, StringComparison.OrdinalIgnoreCase))
        {
            return $"Nginx 已在 {message[nginxAlreadyRunning.Length..]} 运行。";
        }

        const string nginxStarted = "Nginx started: ";
        if (message.StartsWith(nginxStarted, StringComparison.OrdinalIgnoreCase))
        {
            return $"Nginx 已启动：{message[nginxStarted.Length..]}";
        }

        if (message.Equals("Nginx has no pid file.", StringComparison.OrdinalIgnoreCase))
        {
            return "Nginx 没有 pid 文件。";
        }

        const string nginxStopped = "Nginx stopped, pid ";
        if (message.StartsWith(nginxStopped, StringComparison.OrdinalIgnoreCase))
        {
            return $"Nginx 已停止，pid {message[nginxStopped.Length..]}";
        }

        const string nginxPidNotRunningPrefix = "Nginx pid ";
        const string nginxPidNotRunningSuffix = " was not running.";
        if (message.StartsWith(nginxPidNotRunningPrefix, StringComparison.OrdinalIgnoreCase)
            && message.EndsWith(nginxPidNotRunningSuffix, StringComparison.OrdinalIgnoreCase))
        {
            var pid = message[nginxPidNotRunningPrefix.Length..^nginxPidNotRunningSuffix.Length];
            return $"Nginx pid {pid} 未运行。";
        }

        const string webAlreadyRunning = "Web server already running at ";
        if (message.StartsWith(webAlreadyRunning, StringComparison.OrdinalIgnoreCase))
        {
            return $"前端服务已在 {message[webAlreadyRunning.Length..]} 运行。";
        }

        const string webStarted = "Web server started: ";
        if (message.StartsWith(webStarted, StringComparison.OrdinalIgnoreCase))
        {
            return $"前端服务已启动：{message[webStarted.Length..]}";
        }

        if (message.Equals("Web server has no pid file.", StringComparison.OrdinalIgnoreCase))
        {
            return "前端服务没有 pid 文件。";
        }

        const string webStopped = "Web server stopped, pid ";
        if (message.StartsWith(webStopped, StringComparison.OrdinalIgnoreCase))
        {
            return $"前端服务已停止，pid {message[webStopped.Length..]}";
        }

        const string webPidNotRunningPrefix = "Web server pid ";
        const string webPidNotRunningSuffix = " was not running.";
        if (message.StartsWith(webPidNotRunningPrefix, StringComparison.OrdinalIgnoreCase)
            && message.EndsWith(webPidNotRunningSuffix, StringComparison.OrdinalIgnoreCase))
        {
            var pid = message[webPidNotRunningPrefix.Length..^webPidNotRunningSuffix.Length];
            return $"前端服务 pid {pid} 未运行。";
        }

        const string webPortPrefix = "Web port ";
        const string webPortSuffix = " is already in use.";
        if (message.StartsWith(webPortPrefix, StringComparison.OrdinalIgnoreCase)
            && message.EndsWith(webPortSuffix, StringComparison.OrdinalIgnoreCase))
        {
            var port = message[webPortPrefix.Length..^webPortSuffix.Length];
            return $"前端端口 {port} 已被占用。";
        }

        var firstSpace = message.IndexOf(' ');
        if (firstSpace <= 0)
        {
            return message;
        }

        var serviceId = message[..firstSpace];
        var detail = message[(firstSpace + 1)..];
        if (detail.Equals("already running.", StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已在运行。";
        }

        if (detail.Equals("started by Windows service.", StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已通过 Windows 服务启动。";
        }

        if (detail.Equals("already stopped.", StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已停止。";
        }

        if (detail.Equals("stopped by Windows service.", StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已通过 Windows 服务停止。";
        }

        const string jarNotFound = "skipped, jar not found: ";
        if (detail.StartsWith(jarNotFound, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已跳过，未找到 Jar：{detail[jarNotFound.Length..]}";
        }

        const string runningWithPid = "already running with pid ";
        if (detail.StartsWith(runningWithPid, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已在运行，pid {detail[runningWithPid.Length..]}";
        }

        const string startedProcess = "started in process mode, pid ";
        if (detail.StartsWith(startedProcess, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已按进程模式启动，pid {detail[startedProcess.Length..]}";
        }

        if (detail.Equals("has no pid file.", StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 没有 pid 文件。";
        }

        const string stoppedPid = "stopped, pid ";
        if (detail.StartsWith(stoppedPid, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 已停止，pid {detail[stoppedPid.Length..]}";
        }

        const string pidNotRunningSuffix = " was not running.";
        var pidMarker = detail.IndexOf(" pid ", StringComparison.OrdinalIgnoreCase);
        if (pidMarker >= 0 && detail.EndsWith(pidNotRunningSuffix, StringComparison.OrdinalIgnoreCase))
        {
            var pid = detail[(pidMarker + 5)..^pidNotRunningSuffix.Length];
            return $"{serviceId} pid {pid} 未运行。";
        }

        return message;
    }

    private static string ResolveDefaultLanguage()
    {
        return System.Globalization.CultureInfo.CurrentUICulture.TwoLetterISOLanguageName.Equals("zh", StringComparison.OrdinalIgnoreCase)
            ? "zh"
            : "en";
    }
}

internal static class ControlCenterText
{
    private static readonly Dictionary<string, string> Zh = new(StringComparer.OrdinalIgnoreCase)
    {
        ["windowTitle"] = "Forgex 控制中心",
        ["summary"] = "实例：{0}    环境：{1}    安装目录：{2}",
        ["machineCode"] = "机器码",
        ["gridService"] = "服务",
        ["gridStatus"] = "状态",
        ["gridPort"] = "端口",
        ["gridJar"] = "Jar",
        ["generateRequest"] = "生成授权申请",
        ["importLicense"] = "导入授权",
        ["startWeb"] = "启动前端",
        ["stopWeb"] = "停止前端",
        ["openWeb"] = "打开前端",
        ["startAll"] = "启动全部",
        ["stopAll"] = "停止全部",
        ["refresh"] = "刷新",
        ["openInstallFolder"] = "打开安装目录",
        ["licenseTitle"] = "Forgex 授权",
        ["selectLicenseTitle"] = "选择 license.lic",
        ["licenseFilter"] = "Forgex 授权文件 (*.lic)|*.lic|所有文件 (*.*)|*.*",
        ["requestGeneratedMessage"] = "授权申请文件已生成：\r\n{0}",
        ["licenseImportedMessage"] = "授权文件已导入到：\r\n{0}",
        ["logViewRefreshed"] = "视图已刷新。",
        ["logLanguageChanged"] = "界面语言已切换。",
        ["logRequestGenerated"] = "授权申请文件已生成：{0}",
        ["logCustomerCode"] = "客户编码：{0}",
        ["logLicenseImported"] = "授权文件已从 {0} 导入"
    };

    private static readonly Dictionary<string, string> En = new(StringComparer.OrdinalIgnoreCase)
    {
        ["windowTitle"] = "Forgex Control Center",
        ["summary"] = "Instance: {0}    Profile: {1}    Install Root: {2}",
        ["machineCode"] = "Machine Code",
        ["gridService"] = "Service",
        ["gridStatus"] = "Status",
        ["gridPort"] = "Port",
        ["gridJar"] = "Jar",
        ["generateRequest"] = "Generate Request",
        ["importLicense"] = "Import License",
        ["startWeb"] = "Start Web",
        ["stopWeb"] = "Stop Web",
        ["openWeb"] = "Open Web",
        ["startAll"] = "Start All",
        ["stopAll"] = "Stop All",
        ["refresh"] = "Refresh",
        ["openInstallFolder"] = "Open Install Folder",
        ["licenseTitle"] = "Forgex License",
        ["selectLicenseTitle"] = "Select license.lic",
        ["licenseFilter"] = "Forgex license (*.lic)|*.lic|All files (*.*)|*.*",
        ["requestGeneratedMessage"] = "Request file generated:\r\n{0}",
        ["licenseImportedMessage"] = "License imported to:\r\n{0}",
        ["logViewRefreshed"] = "View refreshed.",
        ["logLanguageChanged"] = "Language switched.",
        ["logRequestGenerated"] = "Request file generated: {0}",
        ["logCustomerCode"] = "Customer code: {0}",
        ["logLicenseImported"] = "License imported from {0}"
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
            Start(service, log);
        }
    }

    public void StopAll(Action<string> log)
    {
        foreach (var service in _config.Services.OrderByDescending(item => item.StartOrder))
        {
            Stop(service, log);
        }
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

    private void Start(ForgexServiceConfig service, Action<string> log)
    {
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

        Directory.CreateDirectory(service.LogDir);
        Directory.CreateDirectory(Path.GetDirectoryName(service.PidFile)!);

        var stdout = Path.Combine(service.LogDir, "stdout.log");
        var stderr = Path.Combine(service.LogDir, "stderr.log");
        var startInfo = new ProcessStartInfo
        {
            FileName = _config.JavaExe,
            Arguments = $"-Dfile.encoding=UTF-8 -jar \"{service.JarPath}\" --spring.profiles.active={_config.DeployProfile} --server.port={service.Port}",
            WorkingDirectory = _config.InstallRoot,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true
        };

        ApplyEnvironment(startInfo);
        startInfo.Environment["SERVER_PORT"] = service.Port.ToString();

        var process = new Process { StartInfo = startInfo, EnableRaisingEvents = true };
        var stdoutWriter = new StreamWriter(new FileStream(stdout, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));
        var stderrWriter = new StreamWriter(new FileStream(stderr, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));

        process.OutputDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stdoutWriter.WriteLine(eventArgs.Data);
                stdoutWriter.Flush();
            }
        };
        process.ErrorDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stderrWriter.WriteLine(eventArgs.Data);
                stderrWriter.Flush();
            }
        };
        process.Exited += (_, _) =>
        {
            stdoutWriter.Dispose();
            stderrWriter.Dispose();
        };

        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
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
        startInfo.Environment["FORGEX_BACKUP_DIR"] = _config.BackupDir;
        startInfo.Environment["FORGEX_NACOS_ADDR"] = _config.NacosAddr;
        startInfo.Environment["FORGEX_NACOS_NAMESPACE"] = _config.NacosNamespace;
        startInfo.Environment["FORGEX_NACOS_GROUP"] = _config.NacosGroup;
        startInfo.Environment["FORGEX_REDIS_ADDR"] = _config.RedisAddr;
        startInfo.Environment["FORGEX_ROCKETMQ_NAME_SERVER"] = _config.RocketMqAddr;
        startInfo.Environment["FORGEX_MYSQL_URL"] = _config.MysqlUrl;
        startInfo.Environment["FORGEX_LICENSE_PUBLIC_KEY_FILE_NAME"] = "public-key.base64";
        startInfo.Environment["FORGEX_LICENSE_FILE_NAME"] = "license.lic";
        startInfo.Environment["FORGEX_REQUEST_INFO_FILE_NAME"] = "request-info.json";
        startInfo.Environment["FORGEX_LICENSE_HISTORY_FILE_NAME"] = "activation-history.json";
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
        var stdoutWriter = new StreamWriter(new FileStream(stdout, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));
        var stderrWriter = new StreamWriter(new FileStream(stderr, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));

        process.OutputDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stdoutWriter.WriteLine(eventArgs.Data);
                stdoutWriter.Flush();
            }
        };
        process.ErrorDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stderrWriter.WriteLine(eventArgs.Data);
                stderrWriter.Flush();
            }
        };
        process.Exited += (_, _) =>
        {
            stdoutWriter.Dispose();
            stderrWriter.Dispose();
        };

        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
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
        var stdoutWriter = new StreamWriter(new FileStream(stdout, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));
        var stderrWriter = new StreamWriter(new FileStream(stderr, FileMode.Append, FileAccess.Write, FileShare.ReadWrite));

        process.OutputDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stdoutWriter.WriteLine(eventArgs.Data);
                stdoutWriter.Flush();
            }
        };
        process.ErrorDataReceived += (_, eventArgs) =>
        {
            if (eventArgs.Data is not null)
            {
                stderrWriter.WriteLine(eventArgs.Data);
                stderrWriter.Flush();
            }
        };
        process.Exited += (_, _) =>
        {
            stdoutWriter.Dispose();
            stderrWriter.Dispose();
        };

        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
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

    public string DeployProfile { get; set; } = "prod";

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

    public string NacosNamespace { get; set; } = "prod";

    public string NacosGroup { get; set; } = "DEFAULT_GROUP";

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
