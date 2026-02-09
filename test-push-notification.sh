#!/bin/bash
# =======================================
# 푸시 알림 디스패치 스크립트
# - 활성화된 FCM 토큰을 조회하여 Redis 큐에 알림 작업을 추가합니다.
# =======================================

set -e
# =============== 
DB_CONTAINER="mju-sugangsincheong-helper-mysql-dev"
REDIS_CONTAINER="mju-sugangsincheong-helper-redis-dev"
DB_USER="devuser"
DB_PASS="devpassword"
DB_NAME="mju-sugangsincheong-helper-db-dev"
REDIS_KEY="mju:notification:dispatch"

echo "🔍 활성 FCM 토큰 조회 중..."
tokens=$(docker exec "$DB_CONTAINER" \
  mysql -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -N \
  -e "SELECT fcm_token FROM student_devices WHERE is_activated = true;" | awk 'NF')

[ -z "$tokens" ] && { echo "❌ 활성 토큰 없음"; exit 1; }
count=$(echo "$tokens" | wc -l)
echo "✅ $count개 토큰 발견"

# ===== 안전한 JSON 직렬화 (jq로 완전 이스케이프) =====
ts=$(date +%s)
json_payload=$(echo "$tokens" | jq -R . | jq -s --arg ts "$ts" '
  map({
    token: .,
    notification: {
      title: "공지 알림",
      body: "새로운 공지가 등록되었습니다. 앱을 확인하세요!"
    },
    data: {
      type: "NOTICE",
      urgency: "NORMAL",
      timestamp: $ts
    }
  })
')

# ===== Redis에 안전하게 전달 (-x 옵션: stdin → raw value) =====
echo "$json_payload" | docker exec -i "$REDIS_CONTAINER" redis-cli -x LPUSH "$REDIS_KEY" >/dev/null

# ===== 결과 확인 =====
queue_size=$(docker exec "$REDIS_CONTAINER" redis-cli LLEN "$REDIS_KEY")
echo -e "\n✅ $count개 디바이스에 알림 전송 예약 완료"
echo "📊 현재 큐 크기: $queue_size"