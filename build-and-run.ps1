param(
    [string]$containerName = "rzd-monitoring",
    [string]$imageName = "rzd-monitoring",
    [string]$port = 8088
)

Write-Host "🛑 Останавливаю контейнер $containerName (если он существует)..."
docker stop $containerName 2>$null
docker rm $containerName 2>$null

Write-Host "🗑 Удаляю образ $imageName (если он существует)..."
docker rmi $imageName 2>$null

Write-Host "🔨 Собираю новый образ $imageName..."
docker build -t $imageName .

Write-Host "🚀 Запускаю контейнер $containerName..."
$portMapping = ("{0}:8088" -f $port)
docker run -d -p $portMapping --name $containerName $imageName

Start-Sleep -Seconds 3

$status = docker inspect -f '{{.State.Status}}' $containerName 2>$null

if ($status -eq "running") {
    Write-Host "✔️ Контейнер $containerName успешно запущен на порту $port"
} else {
    Write-Host "❌ Ошибка: контейнер $containerName не запустился!"
    Write-Host "---- Последние логи контейнера ----"
    docker logs $containerName
    exit 1
}