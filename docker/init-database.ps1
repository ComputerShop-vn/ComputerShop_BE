#!/usr/bin/env pwsh
# ===============================
# Init SQL Server Database Script
# ===============================

Write-Host "Waiting for SQL Server to be ready..." -ForegroundColor Yellow

# Wait for SQL Server to be ready
$maxRetries = 30
$retryCount = 0
$isReady = $false

while (-not $isReady -and $retryCount -lt $maxRetries) {
    $retryCount++
    Write-Host "Attempt $retryCount/$maxRetries..."
    
    try {
        # Run sqlcmd inside the container (works without host sqlcmd installed)
        $result = docker exec sba301_sqlserver /opt/mssql-tools18/bin/sqlcmd `
            -S localhost -U sa -P "Sa@12345" -Q "SELECT 1" -b -C 2>&1
        if ($LASTEXITCODE -eq 0) {
            $isReady = $true
            Write-Host "SQL Server is ready!" -ForegroundColor Green
        } else {
            Write-Host "SQL Server not ready yet, waiting..." -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "SQL Server not ready yet, waiting..." -ForegroundColor Gray
    }
    
    if (-not $isReady) {
        Start-Sleep -Seconds 2
    }
}

if (-not $isReady) {
    Write-Host "ERROR: SQL Server did not become ready in time!" -ForegroundColor Red
    exit 1
}

# Run init script
Write-Host "`nRunning database initialization script..." -ForegroundColor Yellow

$initScript = Join-Path $PSScriptRoot "init-db\init.sql"
$logFile = Join-Path $PSScriptRoot "..\init-output.log"

# Copy the SQL file into the container and execute it
docker cp $initScript sba301_sqlserver:/tmp/init.sql
docker exec sba301_sqlserver /opt/mssql-tools18/bin/sqlcmd `
    -S localhost -U sa -P "Sa@12345" -i /tmp/init.sql -C 2>&1 | Tee-Object -FilePath $logFile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nDatabase initialized successfully!" -ForegroundColor Green
    Write-Host "`nDefault users:" -ForegroundColor Cyan
    Write-Host "  - admin  / Admin@123" -ForegroundColor White
    Write-Host "  - user1  / Admin@123" -ForegroundColor White
    Write-Host "  - staff1 / Admin@123" -ForegroundColor White
    Write-Host "`nLog file: $logFile" -ForegroundColor Gray
} else {
    Write-Host "`nERROR: Database initialization failed!" -ForegroundColor Red
    Write-Host "Check log file: $logFile" -ForegroundColor Gray
    exit 1
}
