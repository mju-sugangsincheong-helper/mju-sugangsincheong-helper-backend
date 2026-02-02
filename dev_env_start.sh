#!/bin/bash
# start-dev.sh

# 1. 컨테이너 백그라운드 실행
echo "======================================="
echo " Starting Development Environment "
echo "======================================="
echo ""
echo "======================================="
echo -e "\033[33m[docker compose]\033[0m Starting containers..."
echo "======================================="
docker compose -f docker-compose-dev.yml up -d
sleep 3


# 2. Redis monitor
echo "======================================="
echo -e "\033[33m[redis monitor]\033[0m Starting redis monitor..."
echo "======================================="
(
  docker exec -i mju-sugangsincheong-helper-redis-dev redis-cli monitor 2>&1 | \
  awk '{
    if (length($0) > 100) 
        print "\033[34m[redis monitor]\033[0m " substr($0,1,100) "...";
    else 
        print "\033[34m[redis monitor]\033[0m " $0
  }'
) &

REDIS_MONITOR_PID=$!

# 3. Crawler (Logs from container)
echo "======================================="
echo -e "\033[33m[crawler]\033[0m Starting crawler logs..."
echo "======================================="
(
  docker logs -f mju-sugangsincheong-helper-mock-crawler 2>&1 | \
  awk '{print "\033[32m[crawler]\033[0m " $0}'
) &

CRAWLER_PID=$!

# 4. URL 안내 출력
echo "======================================="
echo -e "\033[33m[4][info]\033[0m Development environment started successfully!"
echo "======================================="

echo -e "\n\033[1;36m🔗 redis commands monitor: http://localhost:8081\033[0m"
echo -e "\033[1;36m🔗 swagger-ui: http://localhost:8080/swagger-ui/index.html\033[0m\n"

# 5. Ctrl+C 시 자식 프로세스 정리 + 컨테이너 종료
trap "kill $REDIS_MONITOR_PID $CRAWLER_PID 2>/dev/null; wait 2>/dev/null; echo -e '\n\033[33m[docker compose]\033[0m Stopping containers...'; docker compose -f docker-compose-dev.yml down > /dev/null 2>&1; exit" SIGINT SIGTERM

wait