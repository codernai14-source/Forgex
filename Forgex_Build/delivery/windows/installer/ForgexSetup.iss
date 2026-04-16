#define MyAppName "Forgex"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Forgex Team"

[Setup]
AppId={{B8A7C81A-6D66-4F76-9E8C-FA56F0A5A1BC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName=D:\Forgex_{code:GetInstanceCode}
DisableDirPage=no
DisableProgramGroupPage=yes
OutputDir=..\dist\windows
OutputBaseFilename=Forgex-Setup-{#MyAppVersion}
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Files]
Source: "..\staging\windows\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\Forgex\Start Services"; Filename: "{app}\scripts\start-all.ps1"
Name: "{autoprograms}\Forgex\Stop Services"; Filename: "{app}\scripts\stop-all.ps1"

[Run]
Filename: "powershell.exe"; Parameters: "-ExecutionPolicy Bypass -File ""{app}\scripts\install.ps1"" -InstanceCode ""{code:GetInstanceCode}"" -InstallRoot ""{app}"" -DeployProfile ""{code:GetDeployProfile}"""; Flags: runhidden waituntilterminated

[Code]
var
  InstanceCodePage: TInputQueryWizardPage;
  DeployProfilePage: TInputOptionWizardPage;

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

procedure InitializeWizard;
begin
  InstanceCodePage :=
    CreateInputQueryPage(
      wpWelcome,
      'Instance Code',
      'Enter the deployment instance code',
      'The installer will create a folder named Forgex_<INSTANCE_CODE>.');
  InstanceCodePage.Add('INSTANCE_CODE:', False);
  InstanceCodePage.Values[0] := 'ACME_PROD';

  DeployProfilePage :=
    CreateInputOptionPage(
      InstanceCodePage.ID,
      'Deployment Profile',
      'Choose the target runtime profile',
      'Select the Nacos namespace and configuration profile used by this installation.',
      True,
      False);
  DeployProfilePage.Add('dev');
  DeployProfilePage.Add('test');
  DeployProfilePage.Add('prod');
  DeployProfilePage.Add('yanshi');
  DeployProfilePage.SelectedValueIndex := 2;
end;
