#!/bin/bash

# Параметры
CONTAINER_NAME="rzd-monitoring"
IMAGE_NAME="rzd-monitoring"

echo "🛑 Останавливаю контейнер $CONTAINER_NAME (если он существует)..."
docker stop $CONTAINER_NAME 2>/dev/null
docker rm $CONTAINER_NAME 2>/dev/null

echo "🗑 Удаляю образ $IMAGE_NAME (если он существует)..."
docker rmi $IMAGE_NAME 2>/dev/null

echo "🔄 Пересборка и запуск..."
docker compose -f docker/docker-compose.yaml up -d --build --force-recreate

sleep 3

STATUS=$(docker inspect -f '{{.State.Status}}' $CONTAINER_NAME 2>/dev/null)

if [ "$STATUS" = "running" ]; then
    echo "✔️ Контейнер $CONTAINER_NAME успешно запущен"
else
    echo "❌ Ошибка: контейнер $CONTAINER_NAME не запустился!"
    echo "---- Последние логи контейнера ----"
    docker logs $CONTAINER_NAME
    exit 1
fi
