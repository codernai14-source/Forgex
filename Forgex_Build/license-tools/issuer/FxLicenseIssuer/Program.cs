using System.Diagnostics;
using FxLicenseCore.Models;
using FxLicenseCore.Services;
using FxLicenseCore.Utilities;

namespace FxLicenseIssuer;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);
        Application.ThreadException += (_, eventArgs) => StartupDiagnostics.ShowFatal(eventArgs.Exception);
        AppDomain.CurrentDomain.UnhandledException += (_, eventArgs) =>
        {
            if (eventArgs.ExceptionObject is Exception ex)
            {
                StartupDiagnostics.ShowFatal(ex);
            }
        };

        try
        {
            StartupDiagnostics.Write("FxLicenseIssuer starting.");
            ApplicationConfiguration.Initialize();
            Application.Run(new IssuerForm());
            StartupDiagnostics.Write("FxLicenseIssuer closed.");
        }
        catch (Exception ex)
        {
            StartupDiagnostics.ShowFatal(ex);
        }
    }
}

internal sealed class IssuerForm : Form
{
    private readonly RequestInfoService _requestInfoService = new();
    private readonly LicenseIssuingService _issuingService = new();
    private readonly TextBox _requestInfoPathTextBox = new();
    private readonly TextBox _privateKeyPathTextBox = new();
    private readonly TextBox _outputPathTextBox = new();
    private readonly TextBox _productTextBox = new();
    private readonly ComboBox _editionComboBox = new();
    private readonly TextBox _customerNameTextBox = new();
    private readonly TextBox _remarkTextBox = new();
    private readonly TextBox _customerCodeTextBox = new();
    private readonly TextBox _machineCodeTextBox = new();
    private readonly TextBox _hostnameTextBox = new();
    private readonly TextBox _osTypeTextBox = new();
    private readonly TextBox _generatedAtTextBox = new();
    private readonly CheckedListBox _modulesListBox = new();
    private readonly CheckBox _limitUsersCheckBox = new();
    private readonly NumericUpDown _maxUsersNumeric = new();
    private readonly CheckBox _limitTenantsCheckBox = new();
    private readonly NumericUpDown _maxTenantsNumeric = new();
    private readonly DateTimePicker _effectiveAtPicker = new();
    private readonly CheckBox _enableDurationCheckBox = new();
    private readonly NumericUpDown _durationDaysNumeric = new();
    private readonly NumericUpDown _graceDaysNumeric = new();
    private readonly NumericUpDown _issueSerialNumeric = new();
    private readonly TextBox _logTextBox = new();
    private LicenseRequestInfo? _requestInfo;

    public IssuerForm()
    {
        Text = "Forgex 授权签发工具";
        StartPosition = FormStartPosition.CenterScreen;
        Width = 980;
        Height = 760;
        MinimumSize = new Size(880, 660);

        BuildUi();
        ApplyDefaults();

        Shown += async (_, _) =>
        {
            if (File.Exists(_requestInfoPathTextBox.Text))
            {
                await LoadRequestInfoAsync();
            }
        };
    }

    private void BuildUi()
    {
        var root = new TableLayoutPanel
        {
            Dock = DockStyle.Fill,
            ColumnCount = 1,
            RowCount = 6,
            Padding = new Padding(14)
        };
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 45));
        root.RowStyles.Add(new RowStyle(SizeType.Percent, 55));
        root.RowStyles.Add(new RowStyle(SizeType.AutoSize));

        var titleLabel = new Label
        {
            Text = "签发正式授权 license.lic",
            AutoSize = true,
            Font = new Font(Font, FontStyle.Bold),
            Padding = new Padding(0, 0, 0, 10)
        };
        root.Controls.Add(titleLabel, 0, 0);
        root.Controls.Add(BuildFileGroup(), 0, 1);
        root.Controls.Add(BuildRequestGroup(), 0, 2);
        root.Controls.Add(BuildOptionsGroup(), 0, 3);
        root.Controls.Add(BuildLogBox(), 0, 4);
        root.Controls.Add(BuildButtonPanel(), 0, 5);
        Controls.Add(root);
    }

    private Control BuildFileGroup()
    {
        var group = new GroupBox
        {
            Text = "文件",
            Dock = DockStyle.Top,
            AutoSize = true,
            Padding = new Padding(10)
        };
        var table = CreateTwoColumnTable(3);
        table.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        table.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));
        table.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));

        AddFileRow(table, 0, "申请文件", _requestInfoPathTextBox, BrowseRequestInfo);
        AddFileRow(table, 1, "签发私钥", _privateKeyPathTextBox, BrowsePrivateKey);
        AddFileRow(table, 2, "输出授权", _outputPathTextBox, BrowseOutputPath);

        group.Controls.Add(table);
        return group;
    }

    private Control BuildRequestGroup()
    {
        var group = new GroupBox
        {
            Text = "申请信息",
            Dock = DockStyle.Top,
            AutoSize = true,
            Padding = new Padding(10)
        };
        var table = CreateTwoColumnTable(3);
        table.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        table.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));
        table.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        table.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50));

        AddReadonlyRow(table, 0, "客户码", _customerCodeTextBox, "机器码", _machineCodeTextBox);
        AddReadonlyRow(table, 1, "主机名", _hostnameTextBox, "系统", _osTypeTextBox);
        AddReadonlyRow(table, 2, "生成时间", _generatedAtTextBox, "", null);

        group.Controls.Add(table);
        return group;
    }

    private Control BuildOptionsGroup()
    {
        var group = new GroupBox
        {
            Text = "授权参数",
            Dock = DockStyle.Fill,
            Padding = new Padding(10)
        };
        var root = new TableLayoutPanel
        {
            Dock = DockStyle.Fill,
            ColumnCount = 2,
            RowCount = 1
        };
        root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 62));
        root.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 38));

        var optionsTable = CreateTwoColumnTable(8);
        optionsTable.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        optionsTable.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100));

        _editionComboBox.DropDownStyle = ComboBoxStyle.DropDown;
        _editionComboBox.Items.AddRange(["yanshi", "prod", "test", "dev"]);

        ConfigureNumeric(_maxUsersNumeric, 1, 1000000, 100);
        ConfigureNumeric(_maxTenantsNumeric, 1, 100000, 5);
        ConfigureNumeric(_durationDaysNumeric, 1, 36500, 365);
        ConfigureNumeric(_graceDaysNumeric, 0, 3650, 30);
        ConfigureNumeric(_issueSerialNumeric, 1, 1000000, 1);

        _limitUsersCheckBox.Text = "限制用户数";
        _limitUsersCheckBox.Checked = true;
        _limitUsersCheckBox.CheckedChanged += (_, _) => _maxUsersNumeric.Enabled = _limitUsersCheckBox.Checked;
        _limitTenantsCheckBox.Text = "限制租户数";
        _limitTenantsCheckBox.Checked = true;
        _limitTenantsCheckBox.CheckedChanged += (_, _) => _maxTenantsNumeric.Enabled = _limitTenantsCheckBox.Checked;
        _enableDurationCheckBox.Text = "启用到期时间";
        _enableDurationCheckBox.Checked = true;
        _enableDurationCheckBox.CheckedChanged += (_, _) => _durationDaysNumeric.Enabled = _enableDurationCheckBox.Checked;

        _effectiveAtPicker.Format = DateTimePickerFormat.Custom;
        _effectiveAtPicker.CustomFormat = "yyyy-MM-dd HH:mm:ss";

        AddLabeledControl(optionsTable, 0, "产品名称", _productTextBox);
        AddLabeledControl(optionsTable, 1, "版本类型", _editionComboBox);
        AddLabeledControl(optionsTable, 2, "客户名称", _customerNameTextBox);
        AddLabeledControl(optionsTable, 3, "生效时间", _effectiveAtPicker);
        AddLabeledControl(optionsTable, 4, "授权天数", CreateCheckNumericPanel(_enableDurationCheckBox, _durationDaysNumeric));
        AddLabeledControl(optionsTable, 5, "用户/租户", CreateDualLimitPanel());
        AddLabeledControl(optionsTable, 6, "宽限/序号", CreateTwoNumericPanel("宽限期", _graceDaysNumeric, "签发序号", _issueSerialNumeric));
        AddLabeledControl(optionsTable, 7, "备注", _remarkTextBox);

        var modulesPanel = new TableLayoutPanel
        {
            Dock = DockStyle.Fill,
            ColumnCount = 1,
            RowCount = 2
        };
        modulesPanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        modulesPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        modulesPanel.Controls.Add(CreateModuleButtonPanel(), 0, 0);
        _modulesListBox.Dock = DockStyle.Fill;
        _modulesListBox.CheckOnClick = true;
        modulesPanel.Controls.Add(_modulesListBox, 0, 1);

        root.Controls.Add(optionsTable, 0, 0);
        root.Controls.Add(modulesPanel, 1, 0);
        group.Controls.Add(root);
        return group;
    }

    private Control BuildLogBox()
    {
        _logTextBox.Dock = DockStyle.Fill;
        _logTextBox.Multiline = true;
        _logTextBox.ReadOnly = true;
        _logTextBox.ScrollBars = ScrollBars.Vertical;
        return _logTextBox;
    }

    private Control BuildButtonPanel()
    {
        var panel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.RightToLeft,
            Padding = new Padding(0, 10, 0, 0),
            WrapContents = false
        };

        panel.Controls.Add(CreateButton("生成授权", async () => await IssueLicenseAsync(), 110));
        panel.Controls.Add(CreateButton("加载申请", async () => await LoadRequestInfoAsync(), 100));
        panel.Controls.Add(CreateButton("打开输出目录", OpenOutputFolder, 120));
        panel.Controls.Add(CreateButton("生成密钥对", GenerateKeyPair, 110));
        return panel;
    }

    private void ApplyDefaults()
    {
        _requestInfoPathTextBox.Text = ResolveDefaultRequestInfoPath();
        _privateKeyPathTextBox.Text = ResolveDefaultPrivateKeyPath();
        _outputPathTextBox.Text = ResolveDefaultOutputPath();
        _productTextBox.Text = "Forgex";
        _editionComboBox.Text = "yanshi";
        _effectiveAtPicker.Value = DateTime.Now;
        _remarkTextBox.Text = "正式授权";

        foreach (var module in LicenseIssuingService.DefaultModules)
        {
            _modulesListBox.Items.Add(module, true);
        }
    }

    private async Task LoadRequestInfoAsync()
    {
        var requestInfoPath = _requestInfoPathTextBox.Text.Trim();
        if (string.IsNullOrWhiteSpace(requestInfoPath) || !File.Exists(requestInfoPath))
        {
            ShowError("请选择有效的 request-info.json。");
            return;
        }

        try
        {
            _requestInfo = await _requestInfoService.ReadAsync(requestInfoPath);
            _customerCodeTextBox.Text = _requestInfo.CustomerCode;
            _machineCodeTextBox.Text = _requestInfo.MachineCode;
            _hostnameTextBox.Text = _requestInfo.Hostname;
            _osTypeTextBox.Text = _requestInfo.OsType;
            _generatedAtTextBox.Text = _requestInfo.GeneratedAt;

            if (!string.IsNullOrWhiteSpace(_requestInfo.Product))
            {
                _productTextBox.Text = _requestInfo.Product;
            }

            if (!string.IsNullOrWhiteSpace(_requestInfo.Edition))
            {
                _editionComboBox.Text = _requestInfo.Edition;
            }

            if (string.IsNullOrWhiteSpace(_customerNameTextBox.Text))
            {
                _customerNameTextBox.Text = _requestInfo.CustomerCode;
            }

            _outputPathTextBox.Text = Path.Combine(Path.GetDirectoryName(requestInfoPath) ?? AppContext.BaseDirectory, "license.lic");
            AppendLog($"已加载申请文件：{requestInfoPath}");
            AppendLog($"客户码：{_requestInfo.CustomerCode}");
        }
        catch (Exception ex)
        {
            ShowError(ex.Message);
        }
    }

    private async Task IssueLicenseAsync()
    {
        var requestInfoPath = _requestInfoPathTextBox.Text.Trim();
        var privateKeyPath = _privateKeyPathTextBox.Text.Trim();
        var outputPath = _outputPathTextBox.Text.Trim();

        if (string.IsNullOrWhiteSpace(requestInfoPath) || !File.Exists(requestInfoPath))
        {
            ShowError("请选择有效的 request-info.json。");
            return;
        }

        if (string.IsNullOrWhiteSpace(privateKeyPath) || !File.Exists(privateKeyPath))
        {
            ShowError("请选择有效的 private-key.pkcs8.base64。");
            return;
        }

        if (string.IsNullOrWhiteSpace(outputPath))
        {
            ShowError("请选择 license.lic 输出路径。");
            return;
        }

        if (_modulesListBox.CheckedItems.Count == 0)
        {
            ShowError("至少选择一个授权模块。");
            return;
        }

        try
        {
            var requestInfo = _requestInfoPathTextBox.Text.Equals(requestInfoPath, StringComparison.OrdinalIgnoreCase) && _requestInfo is not null
                ? _requestInfo
                : await _requestInfoService.ReadAsync(requestInfoPath);
            var modules = _modulesListBox.CheckedItems.Cast<string>().ToList();
            var options = new LicenseIssueOptions
            {
                Product = NullIfWhiteSpace(_productTextBox.Text),
                Edition = NullIfWhiteSpace(_editionComboBox.Text),
                CustomerName = NullIfWhiteSpace(_customerNameTextBox.Text),
                Modules = modules,
                MaxUsers = _limitUsersCheckBox.Checked ? (int)_maxUsersNumeric.Value : null,
                MaxTenants = _limitTenantsCheckBox.Checked ? (int)_maxTenantsNumeric.Value : null,
                EffectiveAt = new DateTimeOffset(_effectiveAtPicker.Value).ToString("O"),
                DurationDays = _enableDurationCheckBox.Checked ? (int)_durationDaysNumeric.Value : null,
                GraceDays = (int)_graceDaysNumeric.Value,
                IssueSerial = (int)_issueSerialNumeric.Value,
                Remark = NullIfWhiteSpace(_remarkTextBox.Text)
            };

            await _issuingService.IssueAsync(requestInfo, options, privateKeyPath, outputPath);
            AppendLog($"授权已签发：{outputPath}");
            MessageBox.Show(this, $"授权文件已生成：{outputPath}", Text, MessageBoxButtons.OK, MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            ShowError(ex.Message);
        }
    }

    private async void BrowseRequestInfo()
    {
        using var dialog = new OpenFileDialog
        {
            Title = "选择 request-info.json",
            Filter = "授权申请文件|request-info.json|JSON 文件|*.json|所有文件|*.*",
            CheckFileExists = true
        };

        if (File.Exists(_requestInfoPathTextBox.Text))
        {
            dialog.InitialDirectory = Path.GetDirectoryName(_requestInfoPathTextBox.Text);
        }

        if (dialog.ShowDialog(this) != DialogResult.OK)
        {
            return;
        }

        _requestInfoPathTextBox.Text = dialog.FileName;
        await LoadRequestInfoAsync();
    }

    private void BrowsePrivateKey()
    {
        using var dialog = new OpenFileDialog
        {
            Title = "选择签发私钥",
            Filter = "私钥文件|private-key.pkcs8.base64|BASE64 文件|*.base64|所有文件|*.*",
            CheckFileExists = true
        };

        if (File.Exists(_privateKeyPathTextBox.Text))
        {
            dialog.InitialDirectory = Path.GetDirectoryName(_privateKeyPathTextBox.Text);
        }

        if (dialog.ShowDialog(this) == DialogResult.OK)
        {
            _privateKeyPathTextBox.Text = dialog.FileName;
        }
    }

    private void BrowseOutputPath()
    {
        using var dialog = new SaveFileDialog
        {
            Title = "保存 license.lic",
            Filter = "授权文件|license.lic|所有文件|*.*",
            FileName = "license.lic",
            OverwritePrompt = true
        };

        var currentOutput = _outputPathTextBox.Text.Trim();
        if (!string.IsNullOrWhiteSpace(currentOutput))
        {
            dialog.InitialDirectory = Path.GetDirectoryName(currentOutput);
            dialog.FileName = Path.GetFileName(currentOutput);
        }

        if (dialog.ShowDialog(this) == DialogResult.OK)
        {
            _outputPathTextBox.Text = dialog.FileName;
        }
    }

    private void GenerateKeyPair()
    {
        using var dialog = new FolderBrowserDialog
        {
            Description = "选择密钥对输出目录。生成后 public-key.base64 需要放到客户部署实例的 license 目录，private-key.pkcs8.base64 只保存在签发端。"
        };
        var defaultKeyDirectory = ResolveDefaultKeyDirectory();
        if (!string.IsNullOrWhiteSpace(defaultKeyDirectory))
        {
            dialog.SelectedPath = defaultKeyDirectory;
        }

        if (dialog.ShowDialog(this) != DialogResult.OK)
        {
            return;
        }

        var privateKeyPath = Path.Combine(dialog.SelectedPath, KeyMaterialHelper.PrivateKeyFileName);
        var publicKeyPath = Path.Combine(dialog.SelectedPath, KeyMaterialHelper.PublicKeyFileName);
        if ((File.Exists(privateKeyPath) || File.Exists(publicKeyPath)) &&
            MessageBox.Show(this, "目标目录已存在密钥文件，继续会覆盖。确定继续吗？", Text, MessageBoxButtons.YesNo, MessageBoxIcon.Warning) != DialogResult.Yes)
        {
            return;
        }

        try
        {
            KeyMaterialHelper.GenerateKeyPair(new DirectoryInfo(dialog.SelectedPath));
            _privateKeyPathTextBox.Text = privateKeyPath;
            AppendLog($"密钥对已生成：{dialog.SelectedPath}");
            MessageBox.Show(
                this,
                $"密钥对已生成。{Environment.NewLine}公钥：{publicKeyPath}{Environment.NewLine}私钥：{privateKeyPath}",
                Text,
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        }
        catch (Exception ex)
        {
            ShowError(ex.Message);
        }
    }

    private void OpenOutputFolder()
    {
        var outputPath = _outputPathTextBox.Text.Trim();
        var directory = string.IsNullOrWhiteSpace(outputPath) ? AppContext.BaseDirectory : Path.GetDirectoryName(outputPath);
        if (string.IsNullOrWhiteSpace(directory) || !Directory.Exists(directory))
        {
            ShowError("输出目录不存在。");
            return;
        }

        Process.Start(new ProcessStartInfo
        {
            FileName = directory,
            UseShellExecute = true
        });
    }

    private static TableLayoutPanel CreateTwoColumnTable(int rowCount)
    {
        var table = new TableLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            ColumnCount = 4,
            RowCount = rowCount
        };

        for (var i = 0; i < rowCount; i++)
        {
            table.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        }

        return table;
    }

    private static void AddFileRow(TableLayoutPanel table, int row, string labelText, TextBox textBox, Action browseAction)
    {
        textBox.Dock = DockStyle.Fill;
        var button = new Button
        {
            Text = "浏览",
            Width = 72,
            Height = 28,
            Margin = new Padding(8, 2, 0, 2)
        };
        button.Click += (_, _) => browseAction();
        table.Controls.Add(CreateLabel(labelText), 0, row);
        table.Controls.Add(textBox, 1, row);
        table.SetColumnSpan(textBox, 2);
        table.Controls.Add(button, 3, row);
    }

    private static void AddReadonlyRow(
        TableLayoutPanel table,
        int row,
        string leftLabel,
        TextBox leftBox,
        string rightLabel,
        TextBox? rightBox)
    {
        ConfigureReadonlyTextBox(leftBox);
        table.Controls.Add(CreateLabel(leftLabel), 0, row);
        table.Controls.Add(leftBox, 1, row);

        if (rightBox is null)
        {
            table.SetColumnSpan(leftBox, 3);
            return;
        }

        ConfigureReadonlyTextBox(rightBox);
        table.Controls.Add(CreateLabel(rightLabel), 2, row);
        table.Controls.Add(rightBox, 3, row);
    }

    private static void AddLabeledControl(TableLayoutPanel table, int row, string labelText, Control control)
    {
        control.Dock = DockStyle.Fill;
        control.Margin = new Padding(4, 3, 4, 3);
        table.Controls.Add(CreateLabel(labelText), 0, row);
        table.Controls.Add(control, 1, row);
    }

    private static Label CreateLabel(string text)
    {
        return new Label
        {
            Text = text,
            AutoSize = true,
            Anchor = AnchorStyles.Left,
            Padding = new Padding(0, 6, 8, 0)
        };
    }

    private static Button CreateButton(string text, Func<Task> action, int width)
    {
        var button = CreateActionButton(text, () => _ = action(), width);
        return button;
    }

    private static Button CreateButton(string text, Action action, int width)
    {
        return CreateActionButton(text, action, width);
    }

    private static Button CreateActionButton(string text, Action action, int width)
    {
        var button = new Button
        {
            Text = text,
            Width = width,
            Height = 32,
            Margin = new Padding(8, 0, 0, 0)
        };
        button.Click += (_, _) => action();
        return button;
    }

    private Control CreateModuleButtonPanel()
    {
        var panel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = false
        };
        panel.Controls.Add(CreateButton("全选", () => SetAllModules(true), 58));
        panel.Controls.Add(CreateButton("清空", () => SetAllModules(false), 58));
        return panel;
    }

    private Control CreateDualLimitPanel()
    {
        var panel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = false
        };
        panel.Controls.Add(_limitUsersCheckBox);
        panel.Controls.Add(_maxUsersNumeric);
        panel.Controls.Add(_limitTenantsCheckBox);
        panel.Controls.Add(_maxTenantsNumeric);
        return panel;
    }

    private static Control CreateCheckNumericPanel(CheckBox checkBox, NumericUpDown numeric)
    {
        var panel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = false
        };
        panel.Controls.Add(checkBox);
        panel.Controls.Add(numeric);
        return panel;
    }

    private static Control CreateTwoNumericPanel(string leftLabel, NumericUpDown leftNumeric, string rightLabel, NumericUpDown rightNumeric)
    {
        var panel = new FlowLayoutPanel
        {
            Dock = DockStyle.Fill,
            AutoSize = true,
            FlowDirection = FlowDirection.LeftToRight,
            WrapContents = false
        };
        panel.Controls.Add(CreateLabel(leftLabel));
        panel.Controls.Add(leftNumeric);
        panel.Controls.Add(CreateLabel(rightLabel));
        panel.Controls.Add(rightNumeric);
        return panel;
    }

    private static void ConfigureReadonlyTextBox(TextBox textBox)
    {
        textBox.Dock = DockStyle.Fill;
        textBox.ReadOnly = true;
        textBox.Margin = new Padding(4, 3, 12, 3);
    }

    private static void ConfigureNumeric(NumericUpDown numeric, int min, int max, int value)
    {
        numeric.Minimum = min;
        numeric.Maximum = max;
        numeric.Value = value;
        numeric.Width = 92;
        numeric.Margin = new Padding(8, 3, 16, 3);
    }

    private void SetAllModules(bool isChecked)
    {
        for (var i = 0; i < _modulesListBox.Items.Count; i++)
        {
            _modulesListBox.SetItemChecked(i, isChecked);
        }
    }

    private void AppendLog(string message)
    {
        _logTextBox.AppendText($"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}{Environment.NewLine}");
    }

    private void ShowError(string message)
    {
        AppendLog(message);
        MessageBox.Show(this, message, Text, MessageBoxButtons.OK, MessageBoxIcon.Error);
    }

    private static string? NullIfWhiteSpace(string value)
    {
        return string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }

    private static string ResolveDefaultRequestInfoPath()
    {
        var candidates = new[]
        {
            @"C:\Forgex_ACME_PROD\license\request-info.json",
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "request-info.json")
        };

        return candidates.FirstOrDefault(File.Exists) ?? string.Empty;
    }

    private static string ResolveDefaultPrivateKeyPath()
    {
        return FindFileUpwards(Path.Combine("keys", KeyMaterialHelper.PrivateKeyFileName)) ?? string.Empty;
    }

    private string ResolveDefaultOutputPath()
    {
        if (!string.IsNullOrWhiteSpace(_requestInfoPathTextBox.Text))
        {
            var requestDirectory = Path.GetDirectoryName(_requestInfoPathTextBox.Text);
            if (!string.IsNullOrWhiteSpace(requestDirectory))
            {
                return Path.Combine(requestDirectory, "license.lic");
            }
        }

        var desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
        return string.IsNullOrWhiteSpace(desktop)
            ? Path.Combine(AppContext.BaseDirectory, "license.lic")
            : Path.Combine(desktop, "license.lic");
    }

    private static string ResolveDefaultKeyDirectory()
    {
        var privateKeyPath = ResolveDefaultPrivateKeyPath();
        if (!string.IsNullOrWhiteSpace(privateKeyPath))
        {
            return Path.GetDirectoryName(privateKeyPath) ?? string.Empty;
        }

        var candidate = FindDirectoryUpwards("license-tools");
        return string.IsNullOrWhiteSpace(candidate)
            ? AppContext.BaseDirectory
            : Path.Combine(candidate, "keys");
    }

    private static string? FindFileUpwards(string relativePath)
    {
        foreach (var startPath in new[] { AppContext.BaseDirectory, Environment.CurrentDirectory })
        {
            var directory = new DirectoryInfo(startPath);
            while (directory is not null)
            {
                var candidate = Path.Combine(directory.FullName, relativePath);
                if (File.Exists(candidate))
                {
                    return candidate;
                }

                directory = directory.Parent;
            }
        }

        return null;
    }

    private static string? FindDirectoryUpwards(string directoryName)
    {
        foreach (var startPath in new[] { AppContext.BaseDirectory, Environment.CurrentDirectory })
        {
            var directory = new DirectoryInfo(startPath);
            while (directory is not null)
            {
                if (directory.Name.Equals(directoryName, StringComparison.OrdinalIgnoreCase))
                {
                    return directory.FullName;
                }

                directory = directory.Parent;
            }
        }

        return null;
    }
}

internal static class StartupDiagnostics
{
    private static readonly string LogPath = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "Forgex",
        "FxLicenseIssuer",
        "startup.log");

    public static void Write(string message)
    {
        try
        {
            Directory.CreateDirectory(Path.GetDirectoryName(LogPath)!);
            File.AppendAllText(LogPath, $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] {message}{Environment.NewLine}");
        }
        catch
        {
            // Diagnostics must never prevent startup.
        }
    }

    public static void ShowFatal(Exception ex)
    {
        Write(ex.ToString());
        try
        {
            MessageBox.Show(
                $"{ex.Message}{Environment.NewLine}{Environment.NewLine}日志：{LogPath}",
                "Forgex 授权签发工具启动失败",
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }
        catch
        {
            // Last-resort crash path.
        }
    }
}
