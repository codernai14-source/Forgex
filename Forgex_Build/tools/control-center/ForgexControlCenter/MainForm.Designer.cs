#nullable enable

namespace ForgexControlCenter;

internal sealed partial class MainForm
{
    private System.ComponentModel.IContainer? components = null;
    private Label _summaryLabel = null!;
    private Label _languageLabel = null!;
    private ComboBox _languageComboBox = null!;
    private TabControl _mainTabControl = null!;
    private TabPage _frontendTabPage = null!;
    private TabPage _backendTabPage = null!;
    private DataGridView _frontendGrid = null!;
    private DataGridView _backendGrid = null!;
    private TextBox _logTextBox = null!;
    private Label _machineCodeLabel = null!;
    private TextBox _machineCodeTextBox = null!;
    private Label _customerCodeLabel = null!;
    private TextBox _customerCodeTextBox = null!;
    private Label _customerNameLabel = null!;
    private TextBox _customerNameTextBox = null!;
    private Button _generateRequestButton = null!;
    private Button _importLicenseButton = null!;
    private Label _licenseSummaryLabel = null!;
    private Label _licenseRequestAtLabel = null!;
    private Label _licenseRequestAtValueLabel = null!;
    private Label _licenseIssuedAtLabel = null!;
    private Label _licenseIssuedAtValueLabel = null!;
    private Label _licenseIssuerLabel = null!;
    private Label _licenseIssuerValueLabel = null!;
    private Label _licenseDurationLabel = null!;
    private Label _licenseDurationValueLabel = null!;
    private Label _licenseExpireAtLabel = null!;
    private Label _licenseExpireAtValueLabel = null!;
    private Label _licenseRecordsLabel = null!;
    private DataGridView _licenseHistoryGrid = null!;
    private Button _stopAllButton = null!;
    private Button _startAllButton = null!;
    private Button _startBackendAllButton = null!;
    private Button _startWebButton = null!;
    private Button _openWebButton = null!;
    private Button _refreshButton = null!;
    private TableLayoutPanel _rootPanel = null!;
    private TableLayoutPanel _headerPanel = null!;
    private TableLayoutPanel _headerInfoPanel = null!;
    private FlowLayoutPanel _languagePanel = null!;
    private TableLayoutPanel _licensePanel = null!;
    private FlowLayoutPanel _licenseRequestPanel = null!;
    private TableLayoutPanel _licenseInfoPanel = null!;
    private FlowLayoutPanel _buttonPanel = null!;

    protected override void Dispose(bool disposing)
    {
        if (disposing && components is not null) components.Dispose();
        base.Dispose(disposing);
    }

    private void InitializeComponent()
    {
        components = new System.ComponentModel.Container();
        _rootPanel = new TableLayoutPanel();
        _headerPanel = new TableLayoutPanel();
        _headerInfoPanel = new TableLayoutPanel();
        _summaryLabel = new Label(); _languagePanel = new FlowLayoutPanel(); _languageLabel = new Label(); _languageComboBox = new ComboBox();
        _machineCodeLabel = new Label(); _machineCodeTextBox = new TextBox(); _customerCodeLabel = new Label(); _customerCodeTextBox = new TextBox(); _customerNameLabel = new Label(); _customerNameTextBox = new TextBox();
        _mainTabControl = new TabControl(); _frontendTabPage = new TabPage(); _frontendGrid = new DataGridView(); _backendTabPage = new TabPage(); _backendGrid = new DataGridView();
        _licensePanel = new TableLayoutPanel(); _licenseRequestPanel = new FlowLayoutPanel(); _generateRequestButton = new Button(); _importLicenseButton = new Button(); _licenseSummaryLabel = new Label(); _licenseInfoPanel = new TableLayoutPanel();
        _licenseRequestAtLabel = new Label(); _licenseRequestAtValueLabel = new Label(); _licenseIssuedAtLabel = new Label(); _licenseIssuedAtValueLabel = new Label(); _licenseIssuerLabel = new Label(); _licenseIssuerValueLabel = new Label(); _licenseDurationLabel = new Label(); _licenseDurationValueLabel = new Label(); _licenseExpireAtLabel = new Label(); _licenseExpireAtValueLabel = new Label(); _licenseRecordsLabel = new Label(); _licenseHistoryGrid = new DataGridView();
        _logTextBox = new TextBox(); _buttonPanel = new FlowLayoutPanel(); _stopAllButton = new Button(); _startAllButton = new Button(); _startBackendAllButton = new Button(); _startWebButton = new Button(); _openWebButton = new Button(); _refreshButton = new Button();

        var frontendColumns = new DataGridViewColumn[] { CreateDesignTextColumn("designFrontendService", "服务", 16), CreateDesignTextColumn("designFrontendStatus", "状态", 16), CreateDesignTextColumn("designFrontendPort", "端口", 8), CreateDesignTextColumn("designFrontendPath", "路径", 22), CreateDesignButtonColumn("designFrontendUpdate", "更新", 8), CreateDesignButtonColumn("designFrontendRestart", "重启", 8), CreateDesignButtonColumn("designFrontendLog", "日志", 8), CreateDesignButtonColumn("designFrontendStop", "关闭", 7), CreateDesignButtonColumn("designFrontendStart", "开启", 7) };
        var backendColumns = new DataGridViewColumn[] { CreateDesignTextColumn("designBackendService", "服务", 14), CreateDesignTextColumn("designBackendStatus", "状态", 13), CreateDesignTextColumn("designBackendPort", "端口", 8), CreateDesignTextColumn("designBackendJar", "Jar", 22), CreateDesignButtonColumn("designBackendUpdate", "更新", 8), CreateDesignButtonColumn("designBackendRestart", "重启", 8), CreateDesignButtonColumn("designBackendLog", "日志", 8), CreateDesignButtonColumn("designBackendStop", "关闭", 7), CreateDesignButtonColumn("designBackendStart", "开启", 7) };
        var historyColumns = new DataGridViewColumn[] { CreateDesignTextColumn("designHistoryRequestAt", "申请时间", 15), CreateDesignTextColumn("designHistoryActivatedAt", "授权时间", 15), CreateDesignTextColumn("designHistoryIssuer", "授权人", 12), CreateDesignTextColumn("designHistoryDuration", "授权年限", 10), CreateDesignTextColumn("designHistoryExpireAt", "过期时间", 15), CreateDesignTextColumn("designHistoryLicenseId", "授权编号", 14), CreateDesignTextColumn("designHistoryCustomerCode", "客户编码", 12), CreateDesignTextColumn("designHistoryEdition", "版本", 7) };

        _rootPanel.Dock = DockStyle.Fill; _rootPanel.ColumnCount = 1; _rootPanel.RowCount = 3; _rootPanel.Padding = new Padding(14); _rootPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100)); _rootPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 20)); _rootPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 60)); _rootPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 20));
        _headerPanel.Dock = DockStyle.Fill; _headerPanel.ColumnCount = 2; _headerPanel.RowCount = 2; _headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100)); _headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize)); _headerPanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); _headerPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 100));
        _summaryLabel.AutoSize = true; _summaryLabel.Font = new Font(Font, FontStyle.Bold); _summaryLabel.Anchor = AnchorStyles.Left; _headerPanel.Controls.Add(_summaryLabel, 0, 0);
        _languagePanel.AutoSize = true; _languagePanel.Anchor = AnchorStyles.Top | AnchorStyles.Right; _languageLabel.AutoSize = true; _languageLabel.Padding = new Padding(0, 5, 8, 0); _languagePanel.Controls.Add(_languageLabel); _languageComboBox.DropDownStyle = ComboBoxStyle.DropDownList; _languageComboBox.Width = 115; _languagePanel.Controls.Add(_languageComboBox); _headerPanel.Controls.Add(_languagePanel, 1, 0);
        _headerInfoPanel.Dock = DockStyle.Fill; _headerInfoPanel.ColumnCount = 6; _headerInfoPanel.RowCount = 1; _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize)); _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 34)); _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize)); _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 22)); _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize)); _headerInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 44));
        AddHeaderField(_headerInfoPanel, _machineCodeLabel, _machineCodeTextBox, "机器码", 0); AddHeaderField(_headerInfoPanel, _customerCodeLabel, _customerCodeTextBox, "客户编号", 2); AddHeaderField(_headerInfoPanel, _customerNameLabel, _customerNameTextBox, "客户名称", 4); _headerPanel.Controls.Add(_headerInfoPanel, 0, 1); _headerPanel.SetColumnSpan(_headerInfoPanel, 2); _rootPanel.Controls.Add(_headerPanel, 0, 0);

        var middlePanel = new TableLayoutPanel { Dock = DockStyle.Fill, ColumnCount = 1, RowCount = 3 }; middlePanel.RowStyles.Add(new RowStyle(SizeType.Percent, 100)); middlePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); middlePanel.RowStyles.Add(new RowStyle(SizeType.Percent, 18));
        _mainTabControl.Dock = DockStyle.Fill; _frontendTabPage.Text = "前端"; _frontendTabPage.Padding = new Padding(6); ConfigureGridDesigner(_frontendGrid, frontendColumns); _frontendTabPage.Controls.Add(_frontendGrid); _backendTabPage.Text = "后端"; _backendTabPage.Padding = new Padding(6); ConfigureGridDesigner(_backendGrid, backendColumns); _backendTabPage.Controls.Add(_backendGrid); _mainTabControl.Controls.Add(_frontendTabPage); _mainTabControl.Controls.Add(_backendTabPage); middlePanel.Controls.Add(_mainTabControl, 0, 0);
        _buttonPanel.AutoSize = true; _buttonPanel.Dock = DockStyle.Fill; _buttonPanel.Padding = new Padding(0, 6, 0, 2); _buttonPanel.WrapContents = true; AddActionButton(_buttonPanel, _stopAllButton, "停止全部服务"); AddActionButton(_buttonPanel, _startAllButton, "启动全部服务"); AddActionButton(_buttonPanel, _startBackendAllButton, "启动后端全部服务"); AddActionButton(_buttonPanel, _startWebButton, "启动前端"); AddActionButton(_buttonPanel, _openWebButton, "打开前端"); AddActionButton(_buttonPanel, _refreshButton, "刷新"); middlePanel.Controls.Add(_buttonPanel, 0, 1);
        _logTextBox.Dock = DockStyle.Fill; _logTextBox.Multiline = true; _logTextBox.ReadOnly = true; _logTextBox.ScrollBars = ScrollBars.Vertical; middlePanel.Controls.Add(_logTextBox, 0, 2); _rootPanel.Controls.Add(middlePanel, 0, 1);

        _licensePanel.Dock = DockStyle.Fill; _licensePanel.ColumnCount = 1; _licensePanel.RowCount = 5; _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize)); _licensePanel.RowStyles.Add(new RowStyle(SizeType.Percent, 100)); _licenseRequestPanel.AutoSize = true; AddActionButton(_licenseRequestPanel, _generateRequestButton, "获取授权文件 / 生成申请授权文件"); AddActionButton(_licenseRequestPanel, _importLicenseButton, "导入授权文件"); _licensePanel.Controls.Add(_licenseRequestPanel, 0, 0); _licenseSummaryLabel.AutoSize = true; _licenseSummaryLabel.Padding = new Padding(0, 4, 0, 4); _licensePanel.Controls.Add(_licenseSummaryLabel, 0, 1);
        _licenseInfoPanel.Dock = DockStyle.Fill; _licenseInfoPanel.ColumnCount = 10; _licenseInfoPanel.RowCount = 1; for (var i = 0; i < 10; i++) _licenseInfoPanel.ColumnStyles.Add(new ColumnStyle(i % 2 == 0 ? SizeType.AutoSize : SizeType.Percent, i % 2 == 0 ? 0 : 20)); AddInfoField(_licenseInfoPanel, _licenseRequestAtLabel, _licenseRequestAtValueLabel, "申请时间", 0); AddInfoField(_licenseInfoPanel, _licenseIssuedAtLabel, _licenseIssuedAtValueLabel, "授权时间", 2); AddInfoField(_licenseInfoPanel, _licenseIssuerLabel, _licenseIssuerValueLabel, "授权人", 4); AddInfoField(_licenseInfoPanel, _licenseDurationLabel, _licenseDurationValueLabel, "授权年限", 6); AddInfoField(_licenseInfoPanel, _licenseExpireAtLabel, _licenseExpireAtValueLabel, "过期时间", 8); _licensePanel.Controls.Add(_licenseInfoPanel, 0, 2); _licenseRecordsLabel.AutoSize = true; _licenseRecordsLabel.Font = new Font(Font, FontStyle.Bold); _licenseRecordsLabel.Padding = new Padding(0, 4, 0, 2); _licensePanel.Controls.Add(_licenseRecordsLabel, 0, 3); ConfigureGridDesigner(_licenseHistoryGrid, historyColumns); _licensePanel.Controls.Add(_licenseHistoryGrid, 0, 4); _rootPanel.Controls.Add(_licensePanel, 0, 2);

        AutoScaleMode = AutoScaleMode.Font; AutoScaleDimensions = new SizeF(7F, 17F); StartPosition = FormStartPosition.CenterScreen; Width = 1480; Height = 920; MinimumSize = new Size(1180, 760); Text = "Forgex 控制中心"; Controls.Add(_rootPanel);
    }

    private static void AddHeaderField(TableLayoutPanel panel, Label label, TextBox box, string text, int column)
    { label.Text = text; label.AutoSize = true; label.Anchor = AnchorStyles.Left; label.Padding = new Padding(0, 6, 8, 0); box.ReadOnly = true; box.Dock = DockStyle.Fill; panel.Controls.Add(label, column, 0); panel.Controls.Add(box, column + 1, 0); }

    private static void AddInfoField(TableLayoutPanel panel, Label label, Label value, string text, int column)
    { label.Text = text; label.AutoSize = true; label.Anchor = AnchorStyles.Left; label.Font = new Font(SystemFonts.DefaultFont, FontStyle.Bold); label.Padding = new Padding(0, 3, 8, 0); value.Text = "未知"; value.AutoEllipsis = true; value.Dock = DockStyle.Fill; value.Padding = new Padding(0, 3, 12, 0); panel.Controls.Add(label, column, 0); panel.Controls.Add(value, column + 1, 0); }

    private static void ConfigureGridDesigner(DataGridView grid, DataGridViewColumn[] columns)
    { grid.Dock = DockStyle.Fill; grid.AllowUserToAddRows = false; grid.AllowUserToDeleteRows = false; grid.AllowUserToResizeRows = false; grid.AutoGenerateColumns = false; grid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill; grid.EnableHeadersVisualStyles = false; grid.RowHeadersVisible = false; grid.SelectionMode = DataGridViewSelectionMode.FullRowSelect; grid.MultiSelect = false; grid.ReadOnly = true; grid.RowTemplate.Height = 30; grid.Columns.AddRange(columns); }

    private static void AddActionButton(FlowLayoutPanel panel, Button button, string text)
    { button.Text = text; button.AutoSize = true; button.Height = 30; button.Margin = new Padding(0, 0, 8, 3); panel.Controls.Add(button); }

    private static DataGridViewTextBoxColumn CreateDesignTextColumn(string name, string headerText, float fillWeight)
    { return new DataGridViewTextBoxColumn { Name = name, HeaderText = headerText, FillWeight = fillWeight, SortMode = DataGridViewColumnSortMode.NotSortable }; }

    private static DataGridViewButtonColumn CreateDesignButtonColumn(string name, string headerText, float fillWeight)
    { return new DataGridViewButtonColumn { Name = name, HeaderText = headerText, Text = headerText, FillWeight = fillWeight, UseColumnTextForButtonValue = true, SortMode = DataGridViewColumnSortMode.NotSortable }; }
}
