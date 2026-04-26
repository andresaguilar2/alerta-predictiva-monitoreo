Set-StrictMode -Version Latest
$ErrorActionPreference = "SilentlyContinue"

$ports = @(8081, 8082)

foreach ($port in $ports) {
    $pid = Get-NetTCPConnection -LocalPort $port -State Listen |
        Select-Object -ExpandProperty OwningProcess -Unique

    if ($pid) {
        foreach ($p in $pid) {
            Stop-Process -Id $p -Force
            Write-Host "Proceso $p detenido en puerto $port" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Sin proceso escuchando en puerto $port" -ForegroundColor DarkGray
    }
}
