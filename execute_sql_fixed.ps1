# Execute label print record table configuration SQL script

# Database connection info
$mysqlPath = "mysql"
$host = "127.0.0.1"
$port = "3306"
$user = "root"
$password = "123456"
$database = "forgex_common"
$scriptPath = "label_print_record_table_config.sql"

# Execute SQL script
Write-Host "Executing label print record table configuration SQL script..."
Write-Host "Database: $database"
Write-Host "Script path: $scriptPath"

# Build command
$command = "$mysqlPath -h $host -P $port -u $user -p$password $database < $scriptPath"

# Execute command
try {
    Invoke-Expression $command
    Write-Host "SQL script executed successfully!"
} catch {
    Write-Host "SQL script execution failed: $($_.Exception.Message)"
}
