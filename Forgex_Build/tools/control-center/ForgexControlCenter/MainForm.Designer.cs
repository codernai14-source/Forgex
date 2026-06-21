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
    private TabPage _licenseTabPage = null!;
    private DataGridView _frontendGrid = null!;
    private DataGridView _backendGrid = null!;
    private TextBox _logTextBox = null!;
    private Label _machineCodeLabel = null!;
    private TextBox _machineCodeTextBox = null!;
    private Button _generateRequestButton = null!;
    private Button _importLicenseButton = null!;
    private Label _licenseSummaryLabel = null!;
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
    private FlowLayoutPanel _languagePanel = null!;
    private TableLayoutPanel _licensePanel = null!;
    private TableLayoutPanel _machinePanel = null!;
    private FlowLayoutPanel _licenseRequestPanel = null!;
    private TableLayoutPanel _licenseInfoPanel = null!;
    private FlowLayoutPanel _buttonPanel = null!;

    protected override void Dispose(bool disposing)
    {
        if (disposing && components is not null)
        {
            components.Dispose();
        }

        base.Dispose(disposing);
    }

    private void InitializeComponent()
    {
        components = new System.ComponentModel.Container();
        _rootPanel = new TableLayoutPanel();
        _headerPanel = new TableLayoutPanel();
        _summaryLabel = new Label();
        _languagePanel = new FlowLayoutPanel();
        _languageLabel = new Label();
        _languageComboBox = new ComboBox();
        _mainTabControl = new TabControl();
        _frontendTabPage = new TabPage();
        _frontendGrid = new DataGridView();
        _backendTabPage = new TabPage();
        _backendGrid = new DataGridView();
        _licenseTabPage = new TabPage();
        _licensePanel = new TableLayoutPanel();
        _machinePanel = new TableLayoutPanel();
        _machineCodeLabel = new Label();
        _machineCodeTextBox = new TextBox();
        _licenseRequestPanel = new FlowLayoutPanel();
        _generateRequestButton = new Button();
        _importLicenseButton = new Button();
        _licenseSummaryLabel = new Label();
        _licenseInfoPanel = new TableLayoutPanel();
        _licenseDurationLabel = new Label();
        _licenseDurationValueLabel = new Label();
        _licenseExpireAtLabel = new Label();
        _licenseExpireAtValueLabel = new Label();
        _licenseRecordsLabel = new Label();
        _licenseHistoryGrid = new DataGridView();
        _logTextBox = new TextBox();
        _buttonPanel = new FlowLayoutPanel();
        _stopAllButton = new Button();
        _startAllButton = new Button();
        _startBackendAllButton = new Button();
        _startWebButton = new Button();
        _openWebButton = new Button();
        _refreshButton = new Button();
        var frontendServiceColumn = CreateDesignTextColumn("designFrontendService", "服务", 18F);
        var frontendStatusColumn = CreateDesignTextColumn("designFrontendStatus", "状态", 20F);
        var frontendPortColumn = CreateDesignTextColumn("designFrontendPort", "端口", 12F);
        var frontendPathColumn = CreateDesignTextColumn("designFrontendPath", "路径", 32F);
        var frontendStopColumn = CreateDesignButtonColumn("designFrontendStop", "停止服务", 9F);
        var frontendStartColumn = CreateDesignButtonColumn("designFrontendStart", "启动服务", 9F);
        var backendServiceColumn = CreateDesignTextColumn("designBackendService", "服务", 15F);
        var backendStatusColumn = CreateDesignTextColumn("designBackendStatus", "状态", 15F);
        var backendPortColumn = CreateDesignTextColumn("designBackendPort", "端口", 9F);
        var backendJarColumn = CreateDesignTextColumn("designBackendJar", "Jar", 31F);
        var backendStopColumn = CreateDesignButtonColumn("designBackendStop", "停止服务", 8F);
        var backendStartColumn = CreateDesignButtonColumn("designBackendStart", "启动服务", 8F);
        var backendUpdateColumn = CreateDesignButtonColumn("designBackendUpdate", "更新", 7F);
        var backendLogColumn = CreateDesignButtonColumn("designBackendLog", "打开日志文件夹", 11F);
        var historyActivatedAtColumn = CreateDesignTextColumn("designHistoryActivatedAt", "导入时间", 18F);
        var historyLicenseIdColumn = CreateDesignTextColumn("designHistoryLicenseId", "授权编号", 22F);
        var historyCustomerCodeColumn = CreateDesignTextColumn("designHistoryCustomerCode", "客户编码", 14F);
        var historyEditionColumn = CreateDesignTextColumn("designHistoryEdition", "版本", 10F);
        var historyEffectiveAtColumn = CreateDesignTextColumn("designHistoryEffectiveAt", "生效时间", 16F);
        var historyExpireAtColumn = CreateDesignTextColumn("designHistoryExpireAt", "到期时间", 16F);
        var historyModulesColumn = CreateDesignTextColumn("designHistoryModules", "模块", 24F);

        _rootPanel.SuspendLayout();
        _headerPanel.SuspendLayout();
        _languagePanel.SuspendLayout();
        _mainTabControl.SuspendLayout();
        _frontendTabPage.SuspendLayout();
        _backendTabPage.SuspendLayout();
        _licenseTabPage.SuspendLayout();
        _licensePanel.SuspendLayout();
        _machinePanel.SuspendLayout();
        _licenseRequestPanel.SuspendLayout();
        _licenseInfoPanel.SuspendLayout();
        _buttonPanel.SuspendLayout();
        ((System.ComponentModel.ISupportInitialize)_frontendGrid).BeginInit();
        ((System.ComponentModel.ISupportInitialize)_backendGrid).BeginInit();
        ((System.ComponentModel.ISupportInitialize)_licenseHistoryGrid).BeginInit();
        SuspendLayout();

        _rootPanel.ColumnCount = 1;
        _rootPanel.RowCount = 4;
        _rootPanel.Dock = DockStyle.Fill;
        _rootPanel.Padding = new Padding(14);
        _rootPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        _rootPanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _rootPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 68F));
        _rootPanel.RowStyles.Add(new RowStyle(SizeType.Percent, 32F));
        _rootPanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));

        _headerPanel.ColumnCount = 2;
        _headerPanel.AutoSize = true;
        _headerPanel.Dock = DockStyle.Top;
        _headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        _headerPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));

        _summaryLabel.AutoSize = true;
        _summaryLabel.Anchor = AnchorStyles.Left;
        _summaryLabel.Font = new Font(Font, FontStyle.Bold);
        _summaryLabel.Padding = new Padding(0, 0, 16, 0);
        _summaryLabel.Text = "实例：FXC    环境：standard    安装目录：运行时自动读取";
        _headerPanel.Controls.Add(_summaryLabel, 0, 0);

        _languagePanel.AutoSize = true;
        _languagePanel.FlowDirection = FlowDirection.LeftToRight;
        _languagePanel.WrapContents = false;
        _languagePanel.Anchor = AnchorStyles.Right;

        _languageLabel.AutoSize = true;
        _languageLabel.Anchor = AnchorStyles.Left;
        _languageLabel.Padding = new Padding(0, 5, 8, 0);
        _languageLabel.Text = "语言";
        _languagePanel.Controls.Add(_languageLabel);

        _languageComboBox.DropDownStyle = ComboBoxStyle.DropDownList;
        _languageComboBox.Width = 115;
        _languagePanel.Controls.Add(_languageComboBox);
        _headerPanel.Controls.Add(_languagePanel, 1, 0);
        _rootPanel.Controls.Add(_headerPanel, 0, 0);

        _mainTabControl.Dock = DockStyle.Fill;
        _mainTabControl.Controls.Add(_frontendTabPage);
        _mainTabControl.Controls.Add(_backendTabPage);
        _mainTabControl.Controls.Add(_licenseTabPage);

        _frontendTabPage.Padding = new Padding(8);
        _frontendTabPage.Text = "前端";
        _frontendTabPage.Controls.Add(_frontendGrid);

        _frontendGrid.Dock = DockStyle.Fill;
        _frontendGrid.AllowUserToAddRows = false;
        _frontendGrid.AllowUserToDeleteRows = false;
        _frontendGrid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
        _frontendGrid.RowHeadersVisible = false;
        _frontendGrid.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
        _frontendGrid.MultiSelect = false;
        _frontendGrid.ReadOnly = true;
        _frontendGrid.AllowUserToResizeRows = false;
        _frontendGrid.Columns.AddRange(new DataGridViewColumn[]
        {
            frontendServiceColumn,
            frontendStatusColumn,
            frontendPortColumn,
            frontendPathColumn,
            frontendStopColumn,
            frontendStartColumn
        });
        _frontendGrid.Rows.Add("web", "已停止", "8080", "frontend", "停止服务", "启动服务");

        _backendTabPage.Padding = new Padding(8);
        _backendTabPage.Text = "后端";
        _backendTabPage.Controls.Add(_backendGrid);

        _backendGrid.Dock = DockStyle.Fill;
        _backendGrid.AllowUserToAddRows = false;
        _backendGrid.AllowUserToDeleteRows = false;
        _backendGrid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
        _backendGrid.RowHeadersVisible = false;
        _backendGrid.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
        _backendGrid.MultiSelect = false;
        _backendGrid.ReadOnly = true;
        _backendGrid.AllowUserToResizeRows = false;
        _backendGrid.Columns.AddRange(new DataGridViewColumn[]
        {
            backendServiceColumn,
            backendStatusColumn,
            backendPortColumn,
            backendJarColumn,
            backendStopColumn,
            backendStartColumn,
            backendUpdateColumn,
            backendLogColumn
        });
        _backendGrid.Rows.Add("forgex-service", "已停止", "8080", "service.jar", "停止服务", "启动服务", "更新", "打开日志文件夹");

        _licenseTabPage.Padding = new Padding(8);
        _licenseTabPage.Text = "授权";
        _licenseTabPage.Controls.Add(_licensePanel);

        _licensePanel.ColumnCount = 1;
        _licensePanel.RowCount = 6;
        _licensePanel.Dock = DockStyle.Fill;
        _licensePanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.AutoSize));
        _licensePanel.RowStyles.Add(new RowStyle(SizeType.Percent, 100F));

        _machinePanel.ColumnCount = 2;
        _machinePanel.AutoSize = true;
        _machinePanel.Dock = DockStyle.Top;
        _machinePanel.Padding = new Padding(0, 0, 0, 8);
        _machinePanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        _machinePanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 100F));

        _machineCodeLabel.AutoSize = true;
        _machineCodeLabel.Anchor = AnchorStyles.Left;
        _machineCodeLabel.Padding = new Padding(0, 4, 12, 0);
        _machineCodeLabel.Text = "机器码";
        _machinePanel.Controls.Add(_machineCodeLabel, 0, 0);

        _machineCodeTextBox.Dock = DockStyle.Fill;
        _machineCodeTextBox.ReadOnly = true;
        _machineCodeTextBox.Text = "运行时自动读取";
        _machinePanel.Controls.Add(_machineCodeTextBox, 1, 0);
        _licensePanel.Controls.Add(_machinePanel, 0, 0);

        _licenseRequestPanel.AutoSize = true;
        _licenseRequestPanel.Dock = DockStyle.Top;
        _licenseRequestPanel.FlowDirection = FlowDirection.LeftToRight;
        _licenseRequestPanel.WrapContents = true;
        _licenseRequestPanel.Padding = new Padding(0, 0, 0, 8);

        _generateRequestButton.AutoSize = true;
        _generateRequestButton.Height = 32;
        _generateRequestButton.Margin = new Padding(0, 0, 8, 4);
        _generateRequestButton.Text = "获取授权文件 / 生成申请授权文件";
        _licenseRequestPanel.Controls.Add(_generateRequestButton);

        _importLicenseButton.AutoSize = true;
        _importLicenseButton.Height = 32;
        _importLicenseButton.Margin = new Padding(0, 0, 8, 4);
        _importLicenseButton.Text = "导入授权文件";
        _licenseRequestPanel.Controls.Add(_importLicenseButton);
        _licensePanel.Controls.Add(_licenseRequestPanel, 0, 1);

        _licenseSummaryLabel.AutoSize = true;
        _licenseSummaryLabel.Dock = DockStyle.Top;
        _licenseSummaryLabel.Padding = new Padding(0, 0, 0, 8);
        _licenseSummaryLabel.Text = "未找到当前授权文件。";
        _licensePanel.Controls.Add(_licenseSummaryLabel, 0, 2);

        _licenseInfoPanel.ColumnCount = 4;
        _licenseInfoPanel.RowCount = 1;
        _licenseInfoPanel.AutoSize = true;
        _licenseInfoPanel.Dock = DockStyle.Top;
        _licenseInfoPanel.Padding = new Padding(0, 0, 0, 8);
        _licenseInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        _licenseInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50F));
        _licenseInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.AutoSize));
        _licenseInfoPanel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50F));

        _licenseDurationLabel.AutoSize = true;
        _licenseDurationLabel.Anchor = AnchorStyles.Left;
        _licenseDurationLabel.Font = new Font(Font, FontStyle.Bold);
        _licenseDurationLabel.Padding = new Padding(0, 3, 8, 0);
        _licenseDurationLabel.Text = "授权期限";
        _licenseInfoPanel.Controls.Add(_licenseDurationLabel, 0, 0);

        _licenseDurationValueLabel.AutoSize = true;
        _licenseDurationValueLabel.Anchor = AnchorStyles.Left;
        _licenseDurationValueLabel.Padding = new Padding(0, 3, 24, 0);
        _licenseDurationValueLabel.Text = "未知";
        _licenseInfoPanel.Controls.Add(_licenseDurationValueLabel, 1, 0);

        _licenseExpireAtLabel.AutoSize = true;
        _licenseExpireAtLabel.Anchor = AnchorStyles.Left;
        _licenseExpireAtLabel.Font = new Font(Font, FontStyle.Bold);
        _licenseExpireAtLabel.Padding = new Padding(0, 3, 8, 0);
        _licenseExpireAtLabel.Text = "到期时间";
        _licenseInfoPanel.Controls.Add(_licenseExpireAtLabel, 2, 0);

        _licenseExpireAtValueLabel.AutoSize = true;
        _licenseExpireAtValueLabel.Anchor = AnchorStyles.Left;
        _licenseExpireAtValueLabel.Padding = new Padding(0, 3, 0, 0);
        _licenseExpireAtValueLabel.Text = "未知";
        _licenseInfoPanel.Controls.Add(_licenseExpireAtValueLabel, 3, 0);
        _licensePanel.Controls.Add(_licenseInfoPanel, 0, 3);

        _licenseRecordsLabel.AutoSize = true;
        _licenseRecordsLabel.Dock = DockStyle.Top;
        _licenseRecordsLabel.Font = new Font(Font, FontStyle.Bold);
        _licenseRecordsLabel.Padding = new Padding(0, 0, 0, 6);
        _licenseRecordsLabel.Text = "授权记录";
        _licensePanel.Controls.Add(_licenseRecordsLabel, 0, 4);

        _licenseHistoryGrid.Dock = DockStyle.Fill;
        _licenseHistoryGrid.AllowUserToAddRows = false;
        _licenseHistoryGrid.AllowUserToDeleteRows = false;
        _licenseHistoryGrid.AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill;
        _licenseHistoryGrid.RowHeadersVisible = false;
        _licenseHistoryGrid.SelectionMode = DataGridViewSelectionMode.FullRowSelect;
        _licenseHistoryGrid.MultiSelect = false;
        _licenseHistoryGrid.ReadOnly = true;
        _licenseHistoryGrid.AllowUserToResizeRows = false;
        _licenseHistoryGrid.Columns.AddRange(new DataGridViewColumn[]
        {
            historyActivatedAtColumn,
            historyLicenseIdColumn,
            historyCustomerCodeColumn,
            historyEditionColumn,
            historyEffectiveAtColumn,
            historyExpireAtColumn,
            historyModulesColumn
        });
        _licenseHistoryGrid.Rows.Add("2026-01-01 00:00:00", "LIC-DEMO", "FXC", "standard", "2026-01-01", "2027-01-01", "core");
        _licensePanel.Controls.Add(_licenseHistoryGrid, 0, 5);

        _rootPanel.Controls.Add(_mainTabControl, 0, 1);

        _logTextBox.Dock = DockStyle.Fill;
        _logTextBox.Multiline = true;
        _logTextBox.ReadOnly = true;
        _logTextBox.ScrollBars = ScrollBars.Vertical;
        _rootPanel.Controls.Add(_logTextBox, 0, 2);

        _buttonPanel.AutoSize = true;
        _buttonPanel.Dock = DockStyle.Fill;
        _buttonPanel.FlowDirection = FlowDirection.LeftToRight;
        _buttonPanel.WrapContents = true;
        _buttonPanel.Padding = new Padding(0, 10, 0, 0);

        _stopAllButton.AutoSize = true;
        _stopAllButton.Height = 32;
        _stopAllButton.Margin = new Padding(0, 0, 8, 4);
        _stopAllButton.Text = "停止全部服务";
        _buttonPanel.Controls.Add(_stopAllButton);

        _startAllButton.AutoSize = true;
        _startAllButton.Height = 32;
        _startAllButton.Margin = new Padding(0, 0, 8, 4);
        _startAllButton.Text = "启动全部服务";
        _buttonPanel.Controls.Add(_startAllButton);

        _startBackendAllButton.AutoSize = true;
        _startBackendAllButton.Height = 32;
        _startBackendAllButton.Margin = new Padding(0, 0, 8, 4);
        _startBackendAllButton.Text = "启动后端全部服务";
        _buttonPanel.Controls.Add(_startBackendAllButton);

        _startWebButton.AutoSize = true;
        _startWebButton.Height = 32;
        _startWebButton.Margin = new Padding(0, 0, 8, 4);
        _startWebButton.Text = "启动前端";
        _buttonPanel.Controls.Add(_startWebButton);

        _openWebButton.AutoSize = true;
        _openWebButton.Height = 32;
        _openWebButton.Margin = new Padding(0, 0, 8, 4);
        _openWebButton.Text = "打开前端";
        _buttonPanel.Controls.Add(_openWebButton);

        _refreshButton.AutoSize = true;
        _refreshButton.Height = 32;
        _refreshButton.Margin = new Padding(0, 0, 8, 4);
        _refreshButton.Text = "刷新";
        _buttonPanel.Controls.Add(_refreshButton);
        _rootPanel.Controls.Add(_buttonPanel, 0, 3);

        AutoScaleMode = AutoScaleMode.Font;
        AutoScaleDimensions = new SizeF(7F, 17F);
        StartPosition = FormStartPosition.CenterScreen;
        Width = 1180;
        Height = 760;
        MinimumSize = new Size(980, 620);
        Text = "Forgex 控制中心";
        Controls.Add(_rootPanel);

        _buttonPanel.ResumeLayout(false);
        _buttonPanel.PerformLayout();
        _licenseInfoPanel.ResumeLayout(false);
        _licenseInfoPanel.PerformLayout();
        _licenseRequestPanel.ResumeLayout(false);
        _licenseRequestPanel.PerformLayout();
        _machinePanel.ResumeLayout(false);
        _machinePanel.PerformLayout();
        _licensePanel.ResumeLayout(false);
        _licensePanel.PerformLayout();
        _licenseTabPage.ResumeLayout(false);
        _backendTabPage.ResumeLayout(false);
        _frontendTabPage.ResumeLayout(false);
        _mainTabControl.ResumeLayout(false);
        _languagePanel.ResumeLayout(false);
        _languagePanel.PerformLayout();
        _headerPanel.ResumeLayout(false);
        _headerPanel.PerformLayout();
        _rootPanel.ResumeLayout(false);
        _rootPanel.PerformLayout();
        ((System.ComponentModel.ISupportInitialize)_frontendGrid).EndInit();
        ((System.ComponentModel.ISupportInitialize)_backendGrid).EndInit();
        ((System.ComponentModel.ISupportInitialize)_licenseHistoryGrid).EndInit();
        ResumeLayout(false);
        PerformLayout();
    }

    private static DataGridViewTextBoxColumn CreateDesignTextColumn(string name, string headerText, float fillWeight)
    {
        return new DataGridViewTextBoxColumn
        {
            Name = name,
            HeaderText = headerText,
            FillWeight = fillWeight,
            SortMode = DataGridViewColumnSortMode.NotSortable
        };
    }

    private static DataGridViewButtonColumn CreateDesignButtonColumn(string name, string headerText, float fillWeight)
    {
        return new DataGridViewButtonColumn
        {
            Name = name,
            HeaderText = headerText,
            Text = headerText,
            FillWeight = fillWeight,
            SortMode = DataGridViewColumnSortMode.NotSortable,
            UseColumnTextForButtonValue = true
        };
    }
}
