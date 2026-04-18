@echo off

REM 执行打印记录表格配置SQL脚本

REM 数据库连接信息
set mysqlPath=mysql
set host=127.0.0.1
set port=3306
set user=root
set password=123456
set database=forgex_common
set scriptPath=label_print_record_table_config.sql

REM 执行SQL脚本
echo 正在执行打印记录表格配置SQL脚本...
echo 数据库: %database%
echo 脚本路径: %scriptPath%

REM 执行命令
%mysqlPath% -h %host% -P %port% -u %user% -p%password% %database% < %scriptPath%

if %errorlevel% equ 0 (
    echo SQL脚本执行成功！
) else (
    echo SQL脚本执行失败！
)

pause
