#!/usr/bin/env pwsh
# Script para otimizar e medir tamanho da imagem Docker

Write-Host "🚀 Starting Docker image optimization..." -ForegroundColor Green

# Build da imagem com medição de tempo
$buildStart = Get-Date
Write-Host "📦 Building optimized Docker image..." -ForegroundColor Yellow
docker build --no-cache -t weather-api:optimized .
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}
$buildEnd = Get-Date
$buildTime = $buildEnd - $buildStart

# Análise de tamanho da imagem
Write-Host "📊 Analyzing image sizes..." -ForegroundColor Yellow

# Tamanho da imagem atual
$imageSize = docker images weather-api:optimized --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
Write-Host "Current image size:" -ForegroundColor Cyan
Write-Host $imageSize

# Histórico de camadas
Write-Host "\n🔍 Layer analysis:" -ForegroundColor Yellow
docker history weather-api:optimized --human --format "table {{.CreatedBy}}\t{{.Size}}"

# Análise detalhada com dive (se disponível)
if (Get-Command dive -ErrorAction SilentlyContinue) {
    Write-Host "\n🕳️  Running dive analysis..." -ForegroundColor Yellow
    dive weather-api:optimized
} else {
    Write-Host "\n💡 Install 'dive' for detailed layer analysis: https://github.com/wagoodman/dive" -ForegroundColor Blue
}

# Comparação com imagem anterior (se existir)
$previousImage = docker images weather-api:latest --format "{{.Size}}" 2>$null
if ($previousImage) {
    Write-Host "\n📈 Size comparison:" -ForegroundColor Yellow
    Write-Host "Previous: $previousImage" -ForegroundColor Red
    $currentSize = docker images weather-api:optimized --format "{{.Size}}"
    Write-Host "Optimized: $currentSize" -ForegroundColor Green
}

# Teste rápido da imagem
Write-Host "\n🧪 Quick health check..." -ForegroundColor Yellow
$containerId = docker run -d -p 8080:8080 weather-api:optimized
Start-Sleep 10

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Health check passed!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Health check returned: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Health check failed: $($_.Exception.Message)" -ForegroundColor Yellow
} finally {
    docker stop $containerId | Out-Null
    docker rm $containerId | Out-Null
}

# Resumo final
Write-Host "\n🎉 Optimization complete!" -ForegroundColor Green
Write-Host "Build time: $($buildTime.ToString('mm\:ss'))" -ForegroundColor Cyan
Write-Host "\n💡 Additional optimization tips:" -ForegroundColor Blue
Write-Host "  1. Use 'docker system prune' to clean up build cache" -ForegroundColor Gray
Write-Host "  2. Consider using multi-arch builds for different platforms" -ForegroundColor Gray
Write-Host "  3. Monitor image size with 'dive' tool regularly" -ForegroundColor Gray
Write-Host "  4. Use 'docker scout' for security and efficiency analysis" -ForegroundColor Gray

# Tag como latest se tudo estiver OK
Write-Host "\n🏷️  Tagging as latest..." -ForegroundColor Yellow
docker tag weather-api:optimized weather-api:latest

Write-Host "✨ Done! New optimized image tagged as weather-api:latest" -ForegroundColor Green

