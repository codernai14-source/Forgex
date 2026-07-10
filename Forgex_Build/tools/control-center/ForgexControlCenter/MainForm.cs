using System.Diagnostics;
using System.Globalization;
using FxLicenseCore.Models;
using FxLicenseCore.Services;

namespace ForgexControlCenter;

internal sealed partial class MainForm : Form
{
    private const string ColumnServiceId = "serviceId";
    private const string ColumnStatus = "status";
    private const string ColumnPort = "port";
    private const string ColumnPath = "path";
    private const string ColumnStop = "stopAction";
    private const string ColumnStart = "startAction";
    private const string ColumnUpdate = "updateAction";
    private const string ColumnLog = "logAction";

    private readonly ForgexControlConfig _config;
    private readonly ForgexServiceManager _serviceManager;
    private readonly FrontendWebServer _webServer;
    private readonly RequestInfoService _requestInfoService = new();
    private readonly LicenseImportService _licenseImportService = new();
    private readonly MachineFingerprintService _machineFingerprintService = new();
    private readonly System.Windows.Forms.Timer _timer = new();
    private readonly Dictionary<Control, string> _localizedControls = [];
    private string _language = ResolveDefaultLanguage();

    public MainForm(string installRoot)
    {
        _config = ForgexControlConfig.Load(installRoot);
        _serviceManager = new ForgexServiceManager(_config);
        _webServer = new FrontendWebServer(_config);

        InitializeComponent();
        InitializeRuntimeUi();
        ApplyLanguage();
        RefreshView();

        _timer.Interval = 5000;
        _timer.Tick += OnTimerTick;
        _timer.Start();
    }

    private void InitializeRuntimeUi()
    {
        _languageComboBox.Items.Add("中文");
        _languageComboBox.Items.Add("English");
        _languageComboBox.SelectedIndex = _language.Equals("zh", StringComparison.OrdinalIgnoreCase) ? 0 : 1;
        _languageComboBox.SelectedIndexChanged += OnLanguageChanged;

        ConfigureGrid(_frontendGrid);
        ConfigureGrid(_backendGrid);
        ConfigureGrid(_licenseHistoryGrid);
        BuildFrontendColumns();
        BuildBackendColumns();
        BuildLicenseHistoryColumns();
        _frontendGrid.CellContentClick += OnFrontendGridCellContentClick;
        _backendGrid.CellContentClick += OnBackendGridCellContentClick;

        WireActionButton(_generateRequestButton, "generateRequest", GenerateRequest);
        WireActionButton(_importLicenseButton, "importLicense", ImportLicense);
        WireActionButton(_stopAllButton, "stopAll", StopAll);
        WireActionButton(_startAllButton, "startAll", StartAll);
        WireActionButton(_startBackendAllButton, "startBackendAll", StartBackendAll);
        WireActionButton(_startWebButton, "startWeb", StartWeb);
        WireActionButton(_openWebButton, "openWeb", OpenWeb);
        WireActionButton(_refreshButton, "refresh", RefreshView);
    }

    private static void ConfigureGrid(DataGridView grid)
    {
        grid.AutoGenerateColumns = false;
        grid.EnableHeadersVisualStyles = false;
        grid.ColumnHeadersHeightSizeMode = DataGridViewColumnHeadersHeightSizeMode.AutoSize;
        grid.RowTemplate.Height = 32;
    }

    private void BuildFrontendColumns()
    {
        _frontendGrid.Columns.Clear();
        AddTextColumn(_frontendGrid, ColumnServiceId, 18F);
        AddTextColumn(_frontendGrid, ColumnStatus, 20F);
        AddTextColumn(_frontendGrid, ColumnPort, 12F);
        AddTextColumn(_frontendGrid, ColumnPath, 32F);
        AddButtonColumn(_frontendGrid, ColumnStop, "stopService", 9F);
        AddButtonColumn(_frontendGrid, ColumnStart, "startService", 9F);
    }

    private void BuildBackendColumns()
    {
        _backendGrid.Columns.Clear();
        AddTextColumn(_backendGrid, ColumnServiceId, 15F);
        AddTextColumn(_backendGrid, ColumnStatus, 15F);
        AddTextColumn(_backendGrid, ColumnPort, 9F);
        AddTextColumn(_backendGrid, ColumnPath, 31F);
        AddButtonColumn(_backendGrid, ColumnStop, "stopService", 8F);
        AddButtonColumn(_backendGrid, ColumnStart, "startService", 8F);
        AddButtonColumn(_backendGrid, ColumnUpdate, "updateService", 7F);
        AddButtonColumn(_backendGrid, ColumnLog, "openLogFolder", 11F);
    }

    private void BuildLicenseHistoryColumns()
    {
        _licenseHistoryGrid.Columns.Clear();
        AddTextColumn(_licenseHistoryGrid, "activatedAt", 18F);
        AddTextColumn(_licenseHistoryGrid, "licenseId", 22F);
        AddTextColumn(_licenseHistoryGrid, "customerCode", 14F);
        AddTextColumn(_licenseHistoryGrid, "edition", 10F);
        AddTextColumn(_licenseHistoryGrid, "effectiveAt", 16F);
        AddTextColumn(_licenseHistoryGrid, "expireAt", 16F);
        AddTextColumn(_licenseHistoryGrid, "modules", 24F);
    }

    private static void AddTextColumn(DataGridView grid, string name, float fillWeight)
    {
        var column = new DataGridViewTextBoxColumn
        {
            Name = name,
            SortMode = DataGridViewColumnSortMode.NotSortable,
            FillWeight = fillWeight
        };
        grid.Columns.Add(column);
    }

    private void AddButtonColumn(DataGridView grid, string name, string textKey, float fillWeight)
    {
        var column = new DataGridViewButtonColumn
        {
            Name = name,
            Tag = textKey,
            UseColumnTextForButtonValue = true,
            SortMode = DataGridViewColumnSortMode.NotSortable,
            FillWeight = fillWeight
        };
        grid.Columns.Add(column);
    }

    private void WireActionButton(Button button, string textKey, Action action)
    {
        _localizedControls[button] = textKey;
        button.Click += (_, _) => SafeUiAction(action);
    }

    private void OnLanguageChanged(object? sender, EventArgs e)
    {
        _language = _languageComboBox.SelectedIndex == 0 ? "zh" : "en";
        ApplyLanguage();
        RefreshServiceGrid();
        RefreshLicenseView();
        AppendLog(T("logLanguageChanged"));
    }

    private void OnTimerTick(object? sender, EventArgs e)
    {
        SafeUiAction(RefreshServiceGrid);
    }

    private void OnFrontendGridCellContentClick(object? sender, DataGridViewCellEventArgs e)
    {
        if (e.RowIndex < 0)
        {
            return;
        }

        var columnName = _frontendGrid.Columns[e.ColumnIndex].Name;
        if (columnName.Equals(ColumnStop, StringComparison.OrdinalIgnoreCase))
        {
            StopWeb();
            return;
        }

        if (columnName.Equals(ColumnStart, StringComparison.OrdinalIgnoreCase))
        {
            StartWeb();
        }
    }

    private void OnBackendGridCellContentClick(object? sender, DataGridViewCellEventArgs e)
    {
        if (e.RowIndex < 0)
        {
            return;
        }

        var service = GetBackendServiceFromRow(e.RowIndex);
        if (service is null)
        {
            return;
        }

        var columnName = _backendGrid.Columns[e.ColumnIndex].Name;
        if (columnName.Equals(ColumnStop, StringComparison.OrdinalIgnoreCase))
        {
            StopBackendService(service);
            return;
        }

        if (columnName.Equals(ColumnStart, StringComparison.OrdinalIgnoreCase))
        {
            StartBackendService(service);
            return;
        }

        if (columnName.Equals(ColumnUpdate, StringComparison.OrdinalIgnoreCase))
        {
            UpdateBackendService(service);
            return;
        }

        if (columnName.Equals(ColumnLog, StringComparison.OrdinalIgnoreCase))
        {
            OpenBackendLogDirectory(service);
        }
    }

    private void RefreshView()
    {
        ApplyLanguage();
        _machineCodeTextBox.Text = _machineFingerprintService.ResolveMachineCode();
        RefreshServiceGrid();
        RefreshLicenseView();
        AppendLog(T("logViewRefreshed"));
    }

    private void RefreshServiceGrid()
    {
        RefreshBackendGrid();
        RefreshFrontendGrid();
    }

    private void RefreshBackendGrid()
    {
        var selectedServiceId = GetCurrentRowValue(_backendGrid, ColumnServiceId);
        _backendGrid.Rows.Clear();

        foreach (var service in _config.Services.OrderBy(item => item.StartOrder))
        {
            _backendGrid.Rows.Add(
                service.ServiceId,
                LocalizeStatus(_serviceManager.GetStatus(service)),
                service.Port,
                service.JarPath);
        }

        RestoreSelectedRow(_backendGrid, selectedServiceId);
    }

    private void RefreshFrontendGrid()
    {
        var selectedServiceId = GetCurrentRowValue(_frontendGrid, ColumnServiceId);
        _frontendGrid.Rows.Clear();
        _frontendGrid.Rows.Add(
            "web",
            LocalizeStatus(_webServer.GetStatus()),
            _config.FrontendPort,
            _config.FrontendDir);
        RestoreSelectedRow(_frontendGrid, selectedServiceId);
    }

    private void RefreshLicenseView()
    {
        try
        {
            var payload = ControlCenterLicenseReader.ReadCurrentLicense(_config.LicenseDir);
            if (payload is null)
            {
                _licenseSummaryLabel.Text = T("licenseMissing");
                _licenseDurationValueLabel.Text = T("licenseUnknown");
                _licenseExpireAtValueLabel.Text = T("licenseUnknown");
            }
            else
            {
                _licenseSummaryLabel.Text = TFormat(
                    "licenseSummary",
                    payload.LicenseId,
                    payload.CustomerCode,
                    payload.Edition,
                    string.Join(", ", payload.Modules));
                _licenseDurationValueLabel.Text = FormatLicenseDuration(payload);
                _licenseExpireAtValueLabel.Text = FormatLicenseDate(payload.ExpireAt, nullAsPermanent: true);
            }
        }
        catch (Exception ex)
        {
            ControlCenterDiagnostics.Write(_config.InstallRoot, "License display refresh failed", ex);
            _licenseSummaryLabel.Text = TFormat("licenseInvalid", ex.Message);
            _licenseDurationValueLabel.Text = T("licenseUnknown");
            _licenseExpireAtValueLabel.Text = T("licenseUnknown");
        }

        RefreshLicenseHistoryGrid();
    }

    private void RefreshLicenseHistoryGrid()
    {
        _licenseHistoryGrid.Rows.Clear();

        IReadOnlyList<LicenseActivationHistoryRecord> records;
        try
        {
            records = ControlCenterLicenseReader.ReadHistory(_config.LicenseDir);
        }
        catch (Exception ex)
        {
            ControlCenterDiagnostics.Write(_config.InstallRoot, "License history refresh failed", ex);
            AppendLog(TFormat("logLicenseHistoryFailed", ex.Message));
            return;
        }

        foreach (var record in records.OrderByDescending(item => ParseDateOrMin(item.ActivatedAt)))
        {
            _licenseHistoryGrid.Rows.Add(
                FormatLicenseDate(record.ActivatedAt),
                record.LicenseId,
                record.CustomerCode,
                record.Edition,
                FormatLicenseDate(record.EffectiveAt),
                FormatLicenseDate(record.ExpireAt, nullAsPermanent: true),
                string.Join(", ", record.Modules));
        }
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
            OpenFolder(_config.LicenseDir);
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
            RefreshLicenseView();
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
        _webServer.Start(AppendRuntimeLog);
        _serviceManager.StartAll(AppendRuntimeLog);
        RefreshServiceGrid();
    }

    private void StopAll()
    {
        _serviceManager.StopAll(AppendRuntimeLog);
        _webServer.Stop(AppendRuntimeLog);
        RefreshServiceGrid();
    }

    private void StartBackendAll()
    {
        _serviceManager.StartAll(AppendRuntimeLog);
        RefreshServiceGrid();
    }

    private void StartWeb()
    {
        _webServer.Start(AppendRuntimeLog);
        RefreshServiceGrid();
    }

    private void StopWeb()
    {
        _webServer.Stop(AppendRuntimeLog);
        RefreshServiceGrid();
    }

    private void OpenWeb()
    {
        _webServer.Start(AppendRuntimeLog);
        Process.Start(new ProcessStartInfo
        {
            FileName = $"http://127.0.0.1:{_config.FrontendPort}/",
            UseShellExecute = true
        });
        RefreshServiceGrid();
    }

    private void StartBackendService(ForgexServiceConfig service)
    {
        SafeUiAction(() =>
        {
            _serviceManager.StartService(service.ServiceId, AppendRuntimeLog);
            RefreshServiceGrid();
        });
    }

    private void StopBackendService(ForgexServiceConfig service)
    {
        SafeUiAction(() =>
        {
            _serviceManager.StopService(service.ServiceId, AppendRuntimeLog);
            RefreshServiceGrid();
        });
    }

    private void UpdateBackendService(ForgexServiceConfig service)
    {
        SafeUiAction(() =>
        {
            using var dialog = new OpenFileDialog
            {
                Title = TFormat("selectBackendJarTitle", service.ServiceId),
                Filter = T("jarFilter"),
                CheckFileExists = true
            };

            if (dialog.ShowDialog(this) != DialogResult.OK)
            {
                return;
            }

            var result = _serviceManager.UpdateServiceJar(service.ServiceId, dialog.FileName, AppendRuntimeLog);
            AppendLog(TFormat("logBackendJarUpdated", service.ServiceId, result.TargetPath));
            if (!string.IsNullOrWhiteSpace(result.BackupPath))
            {
                AppendLog(TFormat("logBackendJarBackup", result.BackupPath));
            }

            RefreshServiceGrid();
            MessageBox.Show(
                this,
                TFormat("backendJarUpdatedMessage", service.ServiceId, result.TargetPath),
                T("windowTitle"),
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        });
    }

    private void OpenBackendLogDirectory(ForgexServiceConfig service)
    {
        SafeUiAction(() => OpenFolder(_serviceManager.GetServiceLogDirectory(service.ServiceId)));
    }

    private ForgexServiceConfig? GetBackendServiceFromRow(int rowIndex)
    {
        if (rowIndex < 0 || rowIndex >= _backendGrid.Rows.Count)
        {
            return null;
        }

        var serviceId = Convert.ToString(_backendGrid.Rows[rowIndex].Cells[ColumnServiceId].Value);
        if (string.IsNullOrWhiteSpace(serviceId))
        {
            return null;
        }

        return _config.Services.FirstOrDefault(item => item.ServiceId.Equals(serviceId, StringComparison.OrdinalIgnoreCase));
    }

    private static string? GetCurrentRowValue(DataGridView grid, string columnName)
    {
        if (grid.CurrentRow is null || !grid.Columns.Contains(columnName))
        {
            return null;
        }

        return Convert.ToString(grid.CurrentRow.Cells[columnName].Value);
    }

    private static void RestoreSelectedRow(DataGridView grid, string? selectedValue)
    {
        if (grid.Rows.Count == 0 || !grid.Columns.Contains(ColumnServiceId))
        {
            return;
        }

        if (!string.IsNullOrWhiteSpace(selectedValue))
        {
            foreach (DataGridViewRow row in grid.Rows)
            {
                if (string.Equals(Convert.ToString(row.Cells[ColumnServiceId].Value), selectedValue, StringComparison.OrdinalIgnoreCase))
                {
                    row.Selected = true;
                    grid.CurrentCell = row.Cells[ColumnServiceId];
                    return;
                }
            }
        }

        grid.Rows[0].Selected = true;
        grid.CurrentCell = grid.Rows[0].Cells[ColumnServiceId];
    }

    private void OpenFolder(string path)
    {
        Directory.CreateDirectory(path);
        Process.Start(new ProcessStartInfo
        {
            FileName = path,
            UseShellExecute = true
        });
    }

    private void SafeUiAction(Action action)
    {
        try
        {
            action();
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
        ControlCenterDiagnostics.Write(_config.InstallRoot, "Control center action failed", ex);
        AppendLog(ex.Message);
        MessageBox.Show(this, ex.Message, T("windowTitle"), MessageBoxButtons.OK, MessageBoxIcon.Error);
    }

    private void ApplyLanguage()
    {
        Text = T("windowTitle");
        _summaryLabel.Text = TFormat("summary", _config.InstanceCode, _config.DeployProfile, _config.InstallRoot);
        _languageLabel.Text = T("language");
        _frontendTabPage.Text = T("frontendTab");
        _backendTabPage.Text = T("backendTab");
        _licenseTabPage.Text = T("licenseTab");
        _machineCodeLabel.Text = T("machineCode");
        _licenseDurationLabel.Text = T("licenseDuration");
        _licenseExpireAtLabel.Text = T("licenseExpireAt");
        _licenseRecordsLabel.Text = T("licenseRecords");

        ApplyServiceGridLanguage(_frontendGrid);
        ApplyServiceGridLanguage(_backendGrid);
        ApplyLicenseHistoryLanguage();

        foreach (var item in _localizedControls)
        {
            item.Key.Text = T(item.Value);
        }
    }

    private void ApplyServiceGridLanguage(DataGridView grid)
    {
        SetHeader(grid, ColumnServiceId, "gridService");
        SetHeader(grid, ColumnStatus, "gridStatus");
        SetHeader(grid, ColumnPort, "gridPort");
        SetHeader(grid, ColumnPath, "gridPath");
        SetButtonText(grid, ColumnStop, "stopService");
        SetButtonText(grid, ColumnStart, "startService");
        SetButtonText(grid, ColumnUpdate, "updateService");
        SetButtonText(grid, ColumnLog, "openLogFolder");
    }

    private void ApplyLicenseHistoryLanguage()
    {
        SetHeader(_licenseHistoryGrid, "activatedAt", "gridActivatedAt");
        SetHeader(_licenseHistoryGrid, "licenseId", "gridLicenseId");
        SetHeader(_licenseHistoryGrid, "customerCode", "gridCustomerCode");
        SetHeader(_licenseHistoryGrid, "edition", "gridEdition");
        SetHeader(_licenseHistoryGrid, "effectiveAt", "gridEffectiveAt");
        SetHeader(_licenseHistoryGrid, "expireAt", "gridExpireAt");
        SetHeader(_licenseHistoryGrid, "modules", "gridModules");
    }

    private void SetHeader(DataGridView grid, string columnName, string textKey)
    {
        if (grid.Columns.Contains(columnName))
        {
            grid.Columns[columnName]!.HeaderText = T(textKey);
        }
    }

    private void SetButtonText(DataGridView grid, string columnName, string textKey)
    {
        if (grid.Columns.Contains(columnName) && grid.Columns[columnName] is DataGridViewButtonColumn column)
        {
            column.HeaderText = T(textKey);
            column.Text = T(textKey);
            grid.InvalidateColumn(column.Index);
        }
    }

    private string T(string key) => ControlCenterText.Resolve(_language, key);

    private string TFormat(string key, params object[] args)
    {
        return string.Format(CultureInfo.CurrentCulture, T(key), args);
    }

    private string FormatLicenseDuration(LicensePayload payload)
    {
        if (payload.DurationDays.HasValue)
        {
            return TFormat("licenseDurationDays", payload.DurationDays.Value);
        }

        if (string.IsNullOrWhiteSpace(payload.ExpireAt))
        {
            return T("licensePermanent");
        }

        if (DateTimeOffset.TryParse(payload.EffectiveAt, out var effectiveAt)
            && DateTimeOffset.TryParse(payload.ExpireAt, out var expireAt))
        {
            var days = Math.Max(0, (int)Math.Ceiling((expireAt - effectiveAt).TotalDays));
            return TFormat("licenseDurationDays", days);
        }

        return T("licenseUnknown");
    }

    private string FormatLicenseDate(string? value, bool nullAsPermanent = false)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return nullAsPermanent ? T("licensePermanent") : T("licenseUnknown");
        }

        return DateTimeOffset.TryParse(value, out var parsed)
            ? parsed.LocalDateTime.ToString("yyyy-MM-dd HH:mm:ss", CultureInfo.CurrentCulture)
            : value;
    }

    private static DateTimeOffset ParseDateOrMin(string? value)
    {
        return DateTimeOffset.TryParse(value, out var parsed) ? parsed : DateTimeOffset.MinValue;
    }

    private string LocalizeStatus(string status)
    {
        if (!_language.Equals("zh", StringComparison.OrdinalIgnoreCase))
        {
            return status;
        }

        if (status.Contains("Running", StringComparison.OrdinalIgnoreCase))
        {
            return status.Replace("Running", "运行中", StringComparison.OrdinalIgnoreCase);
        }

        return status switch
        {
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

        const string failedToStart = "failed to start: ";
        if (detail.StartsWith(failedToStart, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 启动失败：{detail[failedToStart.Length..]}";
        }

        const string failedToStop = "failed to stop: ";
        if (detail.StartsWith(failedToStop, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 停止失败：{detail[failedToStop.Length..]}";
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

        const string backedUp = "old jar backed up: ";
        if (detail.StartsWith(backedUp, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} 旧 Jar 已备份：{detail[backedUp.Length..]}";
        }

        const string updated = "jar updated: ";
        if (detail.StartsWith(updated, StringComparison.OrdinalIgnoreCase))
        {
            return $"{serviceId} Jar 已更新：{detail[updated.Length..]}";
        }

        return message;
    }

    private static string ResolveDefaultLanguage()
    {
        return CultureInfo.CurrentUICulture.TwoLetterISOLanguageName.Equals("zh", StringComparison.OrdinalIgnoreCase)
            ? "zh"
            : "en";
    }
}
