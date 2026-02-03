# Notification System Integration Guide

이 문서는 Spring Backend(Manager)와 FastAPI Notification Server(Worker) 간의 **알림 발송 규약(Contract)** 및 **책임 범위**를 정의합니다.

---

## 1. 🏗️ 아키텍처 및 책임 분리

| 구분 | Spring Boot (Manager) | FastAPI (Worker) |
| :--- | :--- | :--- |
| **핵심 역할** | **데이터 관리 & 발송 대상 추출** | **메시지 생성 & FCM 발송** |
| **책임 1** | 이벤트(여석 발생 등) 감지 | Redis Queue(`mju:notification:dispatch`) 모니터링 |
| **책임 2** | 구독자(`Subscription`) 및 기기(`StudentDevice`) 조회 | Payload 파싱 및 플랫폼별(`iOS/Android`) 최적화 |
| **책임 3** | 알림 데이터(JSON) 구성 및 Redis Push | FCM 메시지 객체 생성 (Template 적용) |
| **책임 4** | `mju:device:cleanup` 큐 모니터링 및 DB 삭제 | 유효하지 않은 토큰(Invalid Token)을 정리 Queue로 반환 |
| **책임 5** | `mju:notification:status` 키 확인 (Health Check) | 주기적으로 `mju:notification:status` 갱신 (Heartbeat) |

---

## 2. 📡 Redis Interface Specification

### 2.1 📤 알림 발송 요청 (Spring -> FastAPI)

*   **Key:** `mju:notification:dispatch`
*   **Type:** `List` (Queue, LPUSH / BRPOP)
*   **Payload (JSON):**

```json
{
  "event_type": "string",       // 알림 종류 (SECTION_VACANCY, NOTICE_NEW 등)
  "priority": "string",         // 중요도 (HIGH, NORMAL)
  "common_data": {              // 모든 수신자에게 공통으로 적용되는 변수
    "key1": "value1",
    "key2": "value2"
  },
  "recipients": [               // 수신자 목록 (Batch Size 450)
    {
      "token": "string",        // FCM Token
      "user_name": "string",    // 사용자 이름 (개인화 메시지용)
      "platform": "string"      // 기기 플랫폼 (ANDROID, IOS, PC) - 대문자 필수
    }
  ]
}
```

### 2.2 🧹 토큰 정리 요청 (FastAPI -> Spring)

FastAPI가 발송 중 `Unregistered`, `InvalidRegistration` `NotRegistered` 에러를 FCM으로부터 받으면, 해당 토큰들을 수집하여 이 큐에 넣습니다.

*   **Key:** `mju:device:cleanup`
*   **Type:** `List` (Queue, LPUSH / BRPOP)
*   **Payload (JSON Array):** 단순 문자열 리스트

```json
[
  "fcm_token_invalid_1",
  "fcm_token_invalid_2",
  "..."
]
```

### 2.3 💓 서버 생존 신고 (FastAPI -> Spring)

FastAPI 서버가 살아있음을 알리는 Heartbeat입니다.

*   **Key:** `mju:notification:status`
*   **Type:** `String` (SET)
*   **Value:** "RUNNING" (값 자체는 중요하지 않음)
*   **TTL (Expiration):** 60초 (FastAPI는 30초마다 갱신해야 함)

---

## 3. 🐍 FastAPI 처리 로직 (Logic Specification)

FastAPI는 `recipients` 배열을 순회하며 개별 메시지를 생성해야 합니다.

### 3.1 플랫폼별 처리 규칙

1.  **IOS (PWA)**
    *   **헤더 필수:** `Urgency: "high"` (화면 꺼짐 상태에서 수신 위해 필수)
    *   **Payload:** `notification` 필드 외에 `webpush` 설정에 집중.
2.  **ANDROID / PC**
    *   기본 WebPush 설정 사용.
    *   `icon`, `badge` 등 시각적 요소 포함.

### 3.2 템플릿 처리 (Templating)

*   `event_type`에 매칭되는 텍스트 템플릿을 사용하여 `title`, `body`를 완성합니다.
*   **변수 치환:** `common_data` + `recipients[i]` 데이터를 합쳐서 `{user_name}`, `{subject_name}` 등을 치환합니다.

---

## 4. 🛠️ 구현 가이드 (Spring)

### Repository 추가 필요

`StudentDeviceRepository`에 아래 메서드를 추가하여 N+1 문제 없이 데이터를 조회해야 합니다.

```java
// StudentDeviceRepository.java

/**
 * 특정 학생들의 모든 기기 정보를 한 번에 가져오기 (Fetch Join)
 * - Platform 정보와 Student Name 정보가 필요하므로 Join Fetch 필수
 */
@Query("SELECT d FROM StudentDevice d JOIN FETCH d.student WHERE d.student.studentId IN :studentIds")
List<StudentDevice> findAllByStudentIdIn(@Param("studentIds") List<String> studentIds);
```

> **주의:** `Student` 엔티티의 PK는 `String` 타입이므로 파라미터도 `List<String>`이어야 합니다.