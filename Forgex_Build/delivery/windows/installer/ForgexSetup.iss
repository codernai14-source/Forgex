#define MyAppName "Forgex"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Forgex Team"

[Setup]
AppId={{B8A7C81A-6D66-4F76-9E8C-FA56F0A5A1BC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName=C:\Forgex_{code:GetInstanceCode}
PrivilegesRequired=admin
DisableDirPage=no
DisableProgramGroupPage=yes
OutputDir=..\..\..\dist\windows
OutputBaseFilename=Forgex-Setup-{#MyAppVersion}
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "zh"; MessagesFile: "languages\ChineseSimplified.isl"
Name: "en"; MessagesFile: "compiler:Default.isl"

[CustomMessages]
en.ControlCenterName=Forgex Control Center
zh.ControlCenterName=Forgex 控制中心
en.StartServices=Start Services
zh.StartServices=启动服务
en.StopServices=Stop Services
zh.StopServices=停止服务
en.OpenWeb=Open Web
zh.OpenWeb=打开前端
en.ImportDatabase=Import Database
zh.ImportDatabase=导入数据库
en.ImportNacos=Import Nacos Config
zh.ImportNacos=导入 Nacos 配置
en.RepairRuntimeConfig=Repair Runtime Config
zh.RepairRuntimeConfig=修复运行配置
en.UpgradeProgram=Upgrade Frontend/Backend
zh.UpgradeProgram=Upgrade Frontend/Backend
en.PostInstallOpenControlCenter=Open Forgex Control Center
zh.PostInstallOpenControlCenter=打开 Forgex 控制中心
en.InstanceTitle=Instance Code
zh.InstanceTitle=实例编码
en.InstanceSubtitle=Enter the deployment instance code
zh.InstanceSubtitle=请输入部署实例编码
en.InstanceDescription=The installer will create a folder named Forgex_<INSTANCE_CODE>.
zh.InstanceDescription=安装程序将创建名为 Forgex_<实例编码> 的目录。
en.InstanceLabel=INSTANCE_CODE:
zh.InstanceLabel=实例编码:
en.ProfileTitle=Deployment Profile
zh.ProfileTitle=部署环境
en.ProfileSubtitle=Choose the target runtime profile
zh.ProfileSubtitle=请选择目标运行环境
en.ProfileDescription=Customer installations can only use production or demo profiles.
zh.ProfileDescription=客户安装程序仅允许选择生产环境或演示环境。
en.MiddlewareTitle=Middleware Endpoints
zh.MiddlewareTitle=中间件地址
en.MiddlewareSubtitle=Configure external middleware endpoints
zh.MiddlewareSubtitle=配置外部中间件地址
en.MiddlewareDescription=MySQL, Redis, RocketMQ and Nacos are expected to be installed before starting Forgex services.
zh.MiddlewareDescription=启动 Forgex 服务前，请先安装并配置 MySQL、Redis、RocketMQ 和 Nacos。
en.RuntimeDirTitle=Runtime Directories
zh.RuntimeDirTitle=运行目录
en.RuntimeDirSubtitle=Choose where runtime files are stored
zh.RuntimeDirSubtitle=请选择运行时文件存放位置
en.RuntimeDirDescription=Logs should be stored on an existing writable disk. The installer will create missing subdirectories.
zh.RuntimeDirDescription=日志目录应放在服务器存在且可写的磁盘上，安装程序会自动创建缺失的子目录。
en.LogDir=Log directory:
zh.LogDir=日志目录:
en.NacosAddr=Nacos address:
zh.NacosAddr=Nacos 地址:
en.NacosNamespace=Nacos namespace:
zh.NacosNamespace=Nacos 命名空间:
en.RedisAddr=Redis address:
zh.RedisAddr=Redis 地址:
en.RocketMqAddr=RocketMQ name server:
zh.RocketMqAddr=RocketMQ NameServer:
en.MysqlUrl=MySQL JDBC URL:
zh.MysqlUrl=MySQL JDBC 地址:
en.FrontendPort=Frontend web port:
zh.FrontendPort=前端访问端口:

[Files]
Source: "..\..\..\staging\windows\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "scripts\uninstall-cleanup.ps1"; Flags: dontcopy

[InstallDelete]
Type: filesandordirs; Name: "{app}\app\jre"

[Icons]
Name: "{autoprograms}\Forgex\{cm:ControlCenterName}"; Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""
Name: "{autoprograms}\Forgex\{cm:StartServices}"; Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\start-all.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\Forgex\{cm:StopServices}"; Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\stop-all.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\Forgex\{cm:OpenWeb}"; Filename: "http://127.0.0.1:{code:GetFrontendPort}/"
Name: "{autoprograms}\Forgex\{cm:ImportDatabase}"; Filename: "{app}\scripts\import-database.bat"; WorkingDir: "{app}"
Name: "{autoprograms}\Forgex\{cm:ImportNacos}"; Filename: "{app}\scripts\import-nacos-config.bat"; WorkingDir: "{app}"
Name: "{autoprograms}\Forgex\{cm:RepairRuntimeConfig}"; Filename: "{app}\scripts\repair-runtime-config.bat"; WorkingDir: "{app}"
Name: "{autoprograms}\Forgex\{cm:UpgradeProgram}"; Filename: "{app}\scripts\upgrade.bat"; WorkingDir: "{app}"
Name: "{autodesktop}\{cm:ControlCenterName}"; Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""

[Run]
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\install.ps1"" -InstanceCode ""{code:GetInstanceCode}"" -InstallRoot ""{app}"" -DeployProfile ""{code:GetDeployProfile}"" -NacosAddr ""{code:GetNacosAddr}"" -NacosNamespace ""{code:GetNacosNamespace}"" -RedisAddr ""{code:GetRedisAddr}"" -RocketMqAddr ""{code:GetRocketMqAddr}"" -MysqlUrl ""{code:GetMysqlUrl}"" -LogDir ""{code:GetLogDir}"" -FrontendPort ""{code:GetFrontendPort}"""; Flags: runhidden waituntilterminated
Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""; Description: "{cm:PostInstallOpenControlCenter}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\scripts\uninstall-cleanup.ps1"" -InstallRoot ""{app}"""; Flags: runhidden waituntilterminated skipifdoesntexist; RunOnceId: "ForgexRuntimeCleanup"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\app"
Type: filesandordirs; Name: "{app}\services\wrappers"
Type: dirifempty; Name: "{app}\services"
Type: dirifempty; Name: "{app}"

[Code]
var
  InstanceCodePage: TInputQueryWizardPage;
  DeployProfilePage: TInputOptionWizardPage;
  RuntimeDirPage: TInputDirWizardPage;
  MiddlewarePage: TInputQueryWizardPage;
  LastAutoNacosNamespace: String;
  LastAutoLogDir: String;

function GetInstanceCode(Param: String): String;
begin
  if InstanceCodePage = nil then
    Result := 'ACME_PROD'
  else
    Result := Trim(InstanceCodePage.Values[0]);
  if Result = '' then
    Result := 'ACME_PROD';
end;

function GetDeployProfile(Param: String): String;
begin
  if DeployProfilePage = nil then
    Result := 'yanshi'
  else if DeployProfilePage.SelectedValueIndex = 0 then
    Result := 'prod'
  else
    Result := 'yanshi';
end;

function GetInstallRoot(Param: String): String;
begin
  Result := WizardDirValue;
  if Result = '' then
    Result := ExpandConstant('{app}');
end;

function GetLogDir(Param: String): String;
begin
  if RuntimeDirPage = nil then
    Result := AddBackslash(GetInstallRoot('')) + 'logs'
  else
    Result := Trim(RuntimeDirPage.Values[0]);
  if Result = '' then
    Result := AddBackslash(GetInstallRoot('')) + 'logs';
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ResultCode: Integer;
  CleanupScript: String;
begin
  Result := '';
  ExtractTemporaryFile('uninstall-cleanup.ps1');
  CleanupScript := ExpandConstant('{tmp}\uninstall-cleanup.ps1');
  Exec(
    'powershell.exe',
    '-NoProfile -ExecutionPolicy Bypass -File "' + CleanupScript + '" -InstallRoot "' + GetInstallRoot('') + '"',
    '',
    SW_HIDE,
    ewWaitUntilTerminated,
    ResultCode);
end;

function GetNacosAddr(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := '127.0.0.1:8848'
  else
    Result := Trim(MiddlewarePage.Values[0]);
  if Result = '' then
    Result := '127.0.0.1:8848';
end;

function GetNacosNamespace(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := GetDeployProfile('')
  else
    Result := Trim(MiddlewarePage.Values[1]);
  if Result = '' then
    Result := GetDeployProfile('');
end;

function GetRedisAddr(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := '127.0.0.1:6379'
  else
    Result := Trim(MiddlewarePage.Values[2]);
  if Result = '' then
    Result := '127.0.0.1:6379';
end;

function GetRocketMqAddr(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := '127.0.0.1:9876'
  else
    Result := Trim(MiddlewarePage.Values[3]);
  if Result = '' then
    Result := '127.0.0.1:9876';
end;

function GetMysqlUrl(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := 'jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
  else
    Result := Trim(MiddlewarePage.Values[4]);
  if Result = '' then
    Result := 'jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai';
end;

function GetFrontendPort(Param: String): String;
begin
  if MiddlewarePage = nil then
    Result := '18080'
  else
    Result := Trim(MiddlewarePage.Values[5]);
  if Result = '' then
    Result := '18080';
end;

procedure InitializeWizard;
begin
  InstanceCodePage :=
    CreateInputQueryPage(
      wpWelcome,
      CustomMessage('InstanceTitle'),
      CustomMessage('InstanceSubtitle'),
      CustomMessage('InstanceDescription'));
  InstanceCodePage.Add(CustomMessage('InstanceLabel'), False);
  InstanceCodePage.Values[0] := 'ACME_PROD';

  DeployProfilePage :=
    CreateInputOptionPage(
      InstanceCodePage.ID,
      CustomMessage('ProfileTitle'),
      CustomMessage('ProfileSubtitle'),
      CustomMessage('ProfileDescription'),
      True,
      False);
  DeployProfilePage.Add('prod');
  DeployProfilePage.Add('yanshi');
  DeployProfilePage.SelectedValueIndex := 1;

  RuntimeDirPage :=
    CreateInputDirPage(
      wpSelectDir,
      CustomMessage('RuntimeDirTitle'),
      CustomMessage('RuntimeDirSubtitle'),
      CustomMessage('RuntimeDirDescription'),
      False,
      '');
  RuntimeDirPage.Add(CustomMessage('LogDir'));
  LastAutoLogDir := 'C:\Forgex_' + InstanceCodePage.Values[0] + '\logs';
  RuntimeDirPage.Values[0] := LastAutoLogDir;

  MiddlewarePage :=
    CreateInputQueryPage(
      RuntimeDirPage.ID,
      CustomMessage('MiddlewareTitle'),
      CustomMessage('MiddlewareSubtitle'),
      CustomMessage('MiddlewareDescription'));
  MiddlewarePage.Add(CustomMessage('NacosAddr'), False);
  MiddlewarePage.Add(CustomMessage('NacosNamespace'), False);
  MiddlewarePage.Add(CustomMessage('RedisAddr'), False);
  MiddlewarePage.Add(CustomMessage('RocketMqAddr'), False);
  MiddlewarePage.Add(CustomMessage('MysqlUrl'), False);
  MiddlewarePage.Add(CustomMessage('FrontendPort'), False);
  MiddlewarePage.Values[0] := '127.0.0.1:8848';
  LastAutoNacosNamespace := GetDeployProfile('');
  MiddlewarePage.Values[1] := LastAutoNacosNamespace;
  MiddlewarePage.Values[2] := '127.0.0.1:6379';
  MiddlewarePage.Values[3] := '127.0.0.1:9876';
  MiddlewarePage.Values[4] := 'jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai';
  MiddlewarePage.Values[5] := '18080';
end;

function NextButtonClick(CurPageID: Integer): Boolean;
var
  nextNamespace: String;
begin
  Result := True;

  if (MiddlewarePage <> nil) and (DeployProfilePage <> nil) and (CurPageID = DeployProfilePage.ID) then
  begin
    nextNamespace := GetDeployProfile('');
    if (Trim(MiddlewarePage.Values[1]) = '') or (Trim(MiddlewarePage.Values[1]) = LastAutoNacosNamespace) then
    begin
      MiddlewarePage.Values[1] := nextNamespace;
      LastAutoNacosNamespace := nextNamespace;
    end;
  end;

  if (RuntimeDirPage <> nil) and (CurPageID = wpSelectDir) then
  begin
    if (Trim(RuntimeDirPage.Values[0]) = '') or (Trim(RuntimeDirPage.Values[0]) = LastAutoLogDir) then
    begin
      RuntimeDirPage.Values[0] := AddBackslash(GetInstallRoot('')) + 'logs';
      LastAutoLogDir := RuntimeDirPage.Values[0];
    end;
  end;
end;
