#!/usr/bin/env bash
set -e

# Параметры
CONTAINER_NAME="rzd-monitoring"
IMAGE_NAME="rzd-monitoring"
PORT="${1:-8088}"   # Можно передать порт первым аргументом или по умолчанию 8088

echo "🛑 Останавливаю контейнер $CONTAINER_NAME (если он существует)…"
if docker ps -q --filter "name=^/${CONTAINER_NAME}$" | grep -q .; then
  docker stop "$CONTAINER_NAME"
  docker rm "$CONTAINER_NAME"
else
  echo "Контейнер $CONTAINER_NAME не найден, пропускаем."
fi

echo "🗑 Удаляю образ $IMAGE_NAME (если он существует)…"
if docker images -q "$IMAGE_NAME" | grep -q .; then
  docker rmi "$IMAGE_NAME"
else
  echo "Образ $IMAGE_NAME не найден, пропускаем."
fi

echo "🔨 Собираю новый образ $IMAGE_NAME…"
docker build -t "$IMAGE_NAME" .

echo "🚀 Запускаю контейнер $CONTAINER_NAME на порту $PORT…"
docker run -d \
  --name "$CONTAINER_NAME" \
  -p "${PORT}:8088" \
  "$IMAGE_NAME"

# Небольшая пауза для инициализации
sleep 3

# Проверяем статус
STATUS=$(docker inspect -f '{{.State.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo "not_found")

if [[ "$STATUS" == "running" ]]; then
  echo "✔️ Контейнер $CONTAINER_NAME успешно запущен на порту $PORT"
  exit 0
else
  echo "❌ Ошибка: контейнер $CONTAINER_NAME не запустился! Текущий статус: $STATUS"
  echo "---- Последние 100 строк логов контейнера ----"
  docker logs --tail 100 "$CONTAINER_NAME" || true
  exit 1
fi