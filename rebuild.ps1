param(
    [string]$containerName = "rzd-monitoring",
    [string]$imageName = "rzd-monitoring"
)

Write-Host "🛑 Останавливаю контейнер $containerName (если он существует)..."
docker stop $containerName 2> $null
docker rm $containerName 2> $null

Write-Host "🗑 Удаляю образ $imageName (если он существует)..."
docker rmi $imageName 2> $null

Write-Host "🔄 Пересборка и запуск..."
docker compose -f docker/docker-compose.yaml up -d --build --force-recreate

Start-Sleep -Seconds 3

$status = docker inspect -f '{{.State.Status}}' $containerName 2> $null

if ($status -eq "running")
{
    Write-Host "✔️ Контейнер $containerName успешно запущен"
}
else
{
    Write-Host "❌ Ошибка: контейнер $containerName не запустился!"
    Write-Host "---- Последние логи контейнера ----"
    docker logs $containerName
    exit 1
}