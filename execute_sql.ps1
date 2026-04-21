# 执行打印记录表格配置SQL脚本

# 数据库连接信息
$mysqlPath = "mysql"
$host = "127.0.0.1"
$port = "3306"
$user = "root"
$password = "123456"
$database = "forgex_common"
$scriptPath = "label_print_record_table_config.sql"

# 执行SQL脚本
Write-Host "正在执行打印记录表格配置SQL脚本..."
Write-Host "数据库: $database"
Write-Host "脚本路径: $scriptPath"

# 构建命令
$command = "$mysqlPath -h $host -P $port -u $user -p$password $database < $scriptPath"

# 执行命令
try {
    Invoke-Expression $command
    Write-Host "SQL脚本执行成功！"
} catch {
    Write-Host "SQL脚本执行失败: $($_.Exception.Message)"
}
