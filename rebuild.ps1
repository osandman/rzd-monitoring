param(
    [string]$containerName = "rzd-monitoring",
    [string]$imageName = "rzd-monitoring"
)

Write-Host "Stopping container $containerName if it exists..."
docker stop $containerName 2> $null
docker rm $containerName 2> $null

Write-Host "Removing image $imageName if it exists..."
docker rmi $imageName 2> $null

Write-Host "Rebuilding and starting..."
docker compose -f docker/docker-compose.yaml up -d --build --force-recreate

Start-Sleep -Seconds 3

$status = docker inspect -f '{{.State.Status}}' $containerName 2> $null

if ($status -eq "running") {
    Write-Host "Container $containerName started successfully"
}
else {
    Write-Host "ERROR: Container $containerName failed to start!"
    Write-Host "---- Container logs ----"
    docker logs $containerName
    exit 1
}