#!/usr/bin/env python3
"""
Mock Crawler for MJU Sugangsincheong Helper
- 모든 과목 만석 상태로 시작 (listennow = takelim)
- 매 9초마다 1~3개 과목만 여석 발생 (극히 희귀한 이벤트)
- 고유 식별자: coursecls (명지대 공식 유일 키)
- 문자열 타입 유지 (원본 API 호환)
"""

import json
import time
import random
import logging
from pathlib import Path
from redis import Redis, ConnectionError

# ==================== CONFIGURATION ====================
REDIS_HOST = "localhost"
REDIS_PORT = 6379
DATA_FILE = Path("data/resource/sample_past_lecture_result_2026_1.json")
INTERVAL_SEC = 5

# ==================== LOGGING ====================
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(message)s",
    datefmt="%H:%M:%S"
)
logger = logging.getLogger("Crawler")

# ==================== UTILS ====================
def safe_int(val, default=0):
    try:
        return int(float(val)) if val not in (None, "") else default
    except:
        return default

def get_redis():
    for _ in range(3):
        try:
            r = Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True, socket_timeout=5)
            r.ping()
            logger.info(f"✓ Connected to Redis {REDIS_HOST}:{REDIS_PORT}")
            return r
        except ConnectionError as e:
            logger.warning(f"Redis connection failed: {e}")
            time.sleep(2)
    raise ConnectionError("Redis connection failed after 3 attempts")

# ==================== MAIN ====================
def main():
    # 1. 데이터 로드
    with open(DATA_FILE, "r", encoding="utf-8") as f:
        lectures = json.load(f)
        if isinstance(lectures, dict) and "data" in lectures:
            lectures = lectures["data"]
    
    total = len(lectures)
    logger.info(f"✓ Loaded {total} courses from {DATA_FILE.name}")
    
    # 2. 모든 과목 만석으로 설정 (문자열 타입 유지)
    full_count = 0
    for lec in lectures:
        takelim = lec.get("takelim", "0")
        if safe_int(takelim) > 0:
            lec["listennow"] = takelim  # 문자열 복사
            full_count += 1
    
    logger.info(f"✓ All {full_count} valid courses set to FULL (listennow = takelim)")
    
    # 3. Redis 연결
    redis = get_redis()
    
    # # 4. 초기 스냅샷 저장 (알림 폭탄 방지)
    # initial_json = json.dumps(lectures, ensure_ascii=False, separators=(',', ':'))
    # redis.set("mju:section:curr", initial_json)
    # redis.set("mju:section:prev", initial_json)
    # redis.publish("mju:section:change", "initialized")
    # logger.info("✓ Initial FULL snapshot saved to Redis (prev = curr)")
    
    # 5. 메인 루프
    cycle = 0
    while True:
        cycle += 1
        start = time.time()
        
        # 만석 과목 인덱스 수집
        full_indices = [
            i for i, lec in enumerate(lectures)
            if safe_int(lec.get("listennow", "0")) >= safe_int(lec.get("takelim", "0")) > 0
        ]
        
        changed, released = 0, 0
        released_details = []
        
        if full_indices and random.random() < 0.6:  # 60% 확률로 여석 발생 (40%는 조용히 대기)
            # 1~3개 과목 랜덤 선택
            num_courses = random.randint(1, min(3, len(full_indices)))
            selected = random.sample(full_indices, num_courses)
            
            for idx in selected:
                lec = lectures[idx]
                current = safe_int(lec["listennow"])
                limit = safe_int(lec["takelim"])
                coursecls = lec.get("coursecls", "UNKNOWN")
                
                # 1~3석 해제 (70%:1석, 25%:2석, 5%:3석)
                r = random.random()
                release = 1 if r < 0.70 else (2 if r < 0.95 else 3)
                
                # 실제 해제 가능 석수 계산 (현재 신청인원을 초과하지 않도록)
                actual_release = min(release, current)
                new_val = current - actual_release
                
                # 문자열 타입 유지 업데이트
                lec["listennow"] = str(new_val)
                
                # ✅ 올바른 석수 누적
                released += actual_release
                changed += 1
                
                released_details.append(f"{coursecls}(-{actual_release})")
        
        # Redis 저장
        snapshot = json.dumps(lectures, ensure_ascii=False, separators=(',', ':'))
        redis.set("mju:section:curr", snapshot)
        redis.publish("mju:section:change", "updated")
        redis.setex("mju:system:status", 60, str(int(time.time())))
        
        # 로깅 (상세하게)
        icon = "🟢" if released > 0 else "⚪"
        elapsed = time.time() - start
        
        if released > 0:
            details_str = " | ".join(released_details[:3])
            if len(released_details) > 3:
                details_str += f" +{len(released_details)-3} more"
            logger.info(
                f"{icon} Cycle #{cycle:4d} | "
                f"Courses: {changed:2d}/2507 | "
                f"Seats: {released:2d} | "
                f"{details_str} | "
                f"({elapsed:.2f}s)"
            )
        else:
            logger.info(
                f"{icon} Cycle #{cycle:4d} | "
                f"No seat releases | "
                f"({elapsed:.2f}s)"
            )
        
        # 9초 주기 유지
        time.sleep(max(0, INTERVAL_SEC - (time.time() - start)))

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("🛑 Stopped by user")
    except Exception as e:
        logger.exception(f"✗ Error: {e}")
        exit(1)