## Script para verificar configurações de monitoramento nos microsserviços
Write-Host "Verificando configurações de monitoramento em todos os microsserviços..." -ForegroundColor Green

$servicos = @(
    "api-gateway",
    "appointment-service",
    "auth-service",
    "professional-service",
    "client-service",
    "finance-service",
    "loyalty-service",
    "report-service"
)

$resultados = @()

foreach ($servico in $servicos) {
    $monitoringPath = Join-Path -Path "." -ChildPath "$servico\src\main\resources\application-monitoring.yml"
    
    if (Test-Path $monitoringPath) {
        $content = Get-Content $monitoringPath -Raw
        
        $checkPrometheus = $content -match "management.prometheus.metrics.export.enabled"
        $checkHttpServerRequests = $content -match "\[http.server.requests\]"
        $checkPercentiles = $content -match "percentiles:"
        
        $resultado = [PSCustomObject]@{
            "Serviço" = $servico
            "Arquivo" = $monitoringPath
            "Configuração Prometheus" = if ($checkPrometheus) { "OK" } else { "Não encontrado" }
            "Formato HTTP Correto" = if ($checkHttpServerRequests) { "OK" } else { "Não encontrado" }
            "Percentis Configurados" = if ($checkPercentiles) { "OK" } else { "Não encontrado" }
            "Status" = if ($checkPrometheus -and $checkHttpServerRequests -and $checkPercentiles) { "Completo" } else { "Incompleto" }
        }
        
        $resultados += $resultado
    } else {
        $resultado = [PSCustomObject]@{
            "Serviço" = $servico
            "Arquivo" = $monitoringPath
            "Configuração Prometheus" = "N/A"
            "Formato HTTP Correto" = "N/A"
            "Percentis Configurados" = "N/A"
            "Status" = "Arquivo não encontrado"
        }
        
        $resultados += $resultado
    }
}

# Verificação do spring.factories
$springFactoriesPath = ".\monitoring-commons\src\main\resources\META-INF\spring.factories"
$autoConfigPath = ".\monitoring-commons\src\main\resources\META-INF\spring\org.springframework.boot.autoconfigure.AutoConfiguration.imports"

if (Test-Path $springFactoriesPath) {
    Write-Host "Arquivo spring.factories encontrado para compatibilidade com versões anteriores" -ForegroundColor Green
} else {
    Write-Host "ALERTA: Arquivo spring.factories não encontrado" -ForegroundColor Yellow
}

if (Test-Path $autoConfigPath) {
    Write-Host "Arquivo AutoConfiguration.imports encontrado para Spring Boot 3" -ForegroundColor Green
} else {
    Write-Host "ERRO: Arquivo AutoConfiguration.imports não encontrado" -ForegroundColor Red
}

# Exibir resultados
$resultados | Format-Table -AutoSize

Write-Host "Verificação de monitoramento concluída" -ForegroundColor Green
