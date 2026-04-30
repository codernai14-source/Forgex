#define MyAppName "Forgex"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Forgex Team"

[Setup]
AppId={{B8A7C81A-6D66-4F76-9E8C-FA56F0A5A1BC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName=D:\Forgex_{code:GetInstanceCode}
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
en.ProfileDescription=Select the Nacos namespace and configuration profile used by this installation.
zh.ProfileDescription=请选择本次安装使用的 Nacos 命名空间和配置环境。
en.MiddlewareTitle=Middleware Endpoints
zh.MiddlewareTitle=中间件地址
en.MiddlewareSubtitle=Configure external middleware endpoints
zh.MiddlewareSubtitle=配置外部中间件地址
en.MiddlewareDescription=MySQL, Redis, RocketMQ and Nacos are expected to be installed before starting Forgex services.
zh.MiddlewareDescription=启动 Forgex 服务前，请先安装并配置 MySQL、Redis、RocketMQ 和 Nacos。
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

[Icons]
Name: "{autoprograms}\Forgex\{cm:ControlCenterName}"; Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""
Name: "{autoprograms}\Forgex\{cm:StartServices}"; Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\start-all.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\Forgex\{cm:StopServices}"; Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\stop-all.ps1"" -InstallRoot ""{app}"""
Name: "{autoprograms}\Forgex\{cm:OpenWeb}"; Filename: "http://127.0.0.1:{code:GetFrontendPort}/"
Name: "{autodesktop}\{cm:ControlCenterName}"; Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""

[Run]
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\install.ps1"" -InstanceCode ""{code:GetInstanceCode}"" -InstallRoot ""{app}"" -DeployProfile ""{code:GetDeployProfile}"" -NacosAddr ""{code:GetNacosAddr}"" -NacosNamespace ""{code:GetNacosNamespace}"" -RedisAddr ""{code:GetRedisAddr}"" -RocketMqAddr ""{code:GetRocketMqAddr}"" -MysqlUrl ""{code:GetMysqlUrl}"" -FrontendPort ""{code:GetFrontendPort}"""; Flags: runhidden waituntilterminated
Filename: "{app}\tools\control-center\ForgexControlCenter.exe"; Parameters: "--install-root ""{app}"""; Description: "{cm:PostInstallOpenControlCenter}"; Flags: nowait postinstall skipifsilent

[Code]
var
  InstanceCodePage: TInputQueryWizardPage;
  DeployProfilePage: TInputOptionWizardPage;
  MiddlewarePage: TInputQueryWizardPage;

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
    Result := 'prod'
  else if DeployProfilePage.SelectedValueIndex = 0 then
    Result := 'dev'
  else if DeployProfilePage.SelectedValueIndex = 1 then
    Result := 'test'
  else if DeployProfilePage.SelectedValueIndex = 2 then
    Result := 'prod'
  else
    Result := 'yanshi';
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
  DeployProfilePage.Add('dev');
  DeployProfilePage.Add('test');
  DeployProfilePage.Add('prod');
  DeployProfilePage.Add('yanshi');
  DeployProfilePage.SelectedValueIndex := 2;

  MiddlewarePage :=
    CreateInputQueryPage(
      DeployProfilePage.ID,
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
  MiddlewarePage.Values[1] := 'prod';
  MiddlewarePage.Values[2] := '127.0.0.1:6379';
  MiddlewarePage.Values[3] := '127.0.0.1:9876';
  MiddlewarePage.Values[4] := 'jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai';
  MiddlewarePage.Values[5] := '18080';
end;
