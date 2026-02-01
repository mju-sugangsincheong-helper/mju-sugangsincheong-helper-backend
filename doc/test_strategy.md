# 테스트 전략 (Test Strategy)

## 1. 테스트 철학
본 프로젝트는 **"실제 운영 환경과의 일치성(Parity)"** 을 최우선으로 합니다.
단위 테스트(Unit Test)보다는 **통합 테스트(Integration Test)** 에 집중하여, 애플리케이션의 모든 구성 요소(DB, Redis, Web Layer)가 유기적으로 잘 동작하는지 검증합니다.

> **"Mocking은 최소화하고, 인프라는 실제와 동일하게 구성한다."**

---

## 2. 테스트 인프라 (Infrastructure)

`Testcontainers`를 사용하여 실제 운영 환경과 동일한 미들웨어를 도커 컨테이너로 띄워서 테스트합니다. H2 데이터베이스나 Embedded Redis는 사용하지 않습니다.

| 컴포넌트 | 테스트 환경 | 설명 |
|---|---|---|
| **Database** | **MySQL 8.0 Container** | 운영 환경과 동일한 MySQL 인스턴스를 사용합니다. JPA/Hibernate의 방언(Dialect) 차이나 특정 SQL 문법 오류를 확실하게 잡아냅니다. |
| **Cache / MQ** | **Redis Container** | 캐싱, Pub/Sub, 데이터 저장소 역할을 검증하기 위해 실제 Redis 인스턴스를 사용합니다. |
| **External API** | **MockBean** | `MjuUnivAuthService` 등 외부 명지대 인증 서버는 네트워크 호출을 할 수 없으므로 Mocking 처리합니다. |

---

## 3. 테스트 환경 설정

### 3.1 `application-test.yml`
테스트 실행 시에만 활성화되는 설정 파일입니다.

```yaml
spring:
  config:
    activate:
      on-profile: test
  jpa:
    hibernate:
      ddl-auto: create-drop # 테스트 시작 시 스키마 생성, 종료 시 삭제
    show-sql: true
  
  # 실제 연결 정보는 Testcontainers가 동적으로 주입하므로
  # 이곳에는 placeholder나 기본값만 둡니다.
```

### 3.2 `AbstractIntegrationTest` (Base Class)
모든 통합 테스트 클래스는 이 추상 클래스를 상속받습니다.

- **역할**:
  1.  `@SpringBootTest` 컨텍스트 로드
  2.  **Testcontainers 실행 및 관리** (Singleton Pattern 적용하여 테스트 간 컨테이너 재사용 → 속도 향상)
  3.  `@DynamicPropertySource`를 사용하여 컨테이너의 동적 IP/Port를 Spring 환경 변수에 주입
  4.  `MockMvc`, `ObjectMapper` 등 공통 유틸리티 제공
  5.  `@Transactional`을 통한 테스트 간 데이터 격리 (Rollback)

---

## 4. 주요 테스트 시나리오

### A. 인증 (Auth)
*   **목표**: 외부 API Mocking과 DB 연동 검증
*   **시나리오**:
    1.  `MjuUnivAuthService`를 Mocking 하여 가상의 학생 정보 반환 설정.
    2.  로그인 API (`POST /api/v1/auth/login`) 호출.
    3.  **검증**:
        -   HTTP 200 OK 및 JWT 토큰 발급 확인.
        -   MySQL `students` 테이블에 해당 학생 정보가 `INSERT` 또는 `UPDATE` 되었는지 확인.
        -   Redis에 Refresh Token이 저장되었는지 확인.

### B. 시스템 상태 (System)
*   **목표**: Redis와의 연동 확인
*   **시나리오**:
    1.  테스트 코드에서 Redis에 `mju:system:status` 키 값을 직접 `SET`.
    2.  시스템 상태 조회 API (`GET /api/v1/system/status`) 호출.
    3.  **검증**:
        -   응답 JSON의 `running` 상태가 Redis 값에 따라 올바르게 변하는지 확인.

### C. 수강신청/구독 (Subscription)
*   **목표**: DB 관계 매핑 및 제약조건 검증
*   **시나리오**:
    1.  사전 데이터 세팅: `Student`와 `Section` 데이터를 DB에 저장.
    2.  유효한 JWT 토큰 생성 (Test Helper 사용).
    3.  구독 API (`POST /api/v1/subscriptions`) 호출.
    4.  **검증**:
        -   `subscriptions` 테이블에 매핑 데이터 생성 확인.
        -   중복 구독 시 예외 처리 동작 확인.

### D. 데이터 동기화 (Section Sync) - *Optional*
*   **목표**: Redis Pub/Sub 메시지 처리 검증
*   **시나리오**:
    1.  `RedisTemplate`을 사용해 `mju:section:change` 채널로 메시지 발행.
    2.  약간의 대기(Awaitility 사용) 후 DB의 `Section` 데이터가 변경되었는지 확인.

---

## 5. 의존성 추가 (build.gradle)

```groovy
dependencies {
    // ... 기존 의존성 ...

    // Testcontainers
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:mysql'
    testImplementation 'org.testcontainers:redis' // 필요 시 추가
}
```

---

## 6. 실행 방법

1.  Docker Desktop (또는 Docker Engine) 실행 필수.
2.  IDE 또는 터미널에서 `./gradlew test` 실행.
