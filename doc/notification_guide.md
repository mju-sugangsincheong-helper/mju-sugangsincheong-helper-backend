# Notification System Integration Guide

이 문서는 명지대 수강신청 도우미의 **알림 시스템(Notification System)** 아키텍처, 데이터 규격 및 운영 가이드를 정의합니다. 현재 시스템은 Spring Boot 기반의 Producer-Consumer 패턴을 따르며, Redis를 메시지 브로커로 활용합니다.

---

## 1. 🏗️ 아키텍처 및 흐름 (Architecture Overview)

시스템은 동일한 어플리케이션 내의 `producer` 패키지와 `consumer` 패키지로 분리되어 있으며, Redis Queue를 통해 비동기적으로 통신합니다.

### 🔄 전체 흐름
1.  **이벤트 발생**: 여석 발생(Vacancy) 또는 테스트 요청 등의 알림 이벤트 발생.
2.  **Producer (`NotificationService`)**:
    *   발송 대상(구독자 및 기기 정보)을 조회합니다.
    *   `FcmMessageDto` 형태의 개인화된 메시지 리스트를 생성합니다.
    *   Redis Queue(`mju:notification:dispatch`)에 JSON 형태로 `LPUSH`합니다.
3.  **Redis Queue**: 메시지 임시 저장 및 버퍼 역할.
4.  **Consumer (`NotificationWorker`)**:
    *   별도 스레드에서 `BRPOP`을 통해 큐를 상시 모니터링합니다.
    *   메시지 리스트를 파싱하여 `FcmSenderService`에 전달합니다.
5.  **FCM 발송 (`FcmSenderService`)**:
    *   Firebase Admin SDK를 사용하여 FCM 서버로 전송합니다 (Batch Size: 450).
    *   발송 결과(Response)를 분석하여 유효하지 않은 토큰(Invalid/Unregistered)을 처리합니다.
6.  **사후 처리**: 유효하지 않은 기기는 DB에서 즉시 비활성화(`is_activated = false`) 처리합니다.

---

## 2. 📡 Redis Interface Specification

Producer와 Consumer 간의 약속된 데이터 통로입니다.

*   **Key:** `mju:notification:dispatch`
*   **Type:** `List` (Queue, LPUSH / BRPOP)
*   **Payload (JSON):** `List<FcmMessageDto>`

```json
[
  {
    "token": "fcm_token_1",
    "notification": {
      "title": "여석 알림",
      "body": "홍길동님 [컴퓨터알고리즘] 강의에 여석이 1개 생겼어요"
    },
    "data": {
      "type": "SECTION_VACANCY",
      "urgency": "HIGH",
      "timestamp": "1707312000"
    }
  },
  ...
]
```

---

## 3. 📦 데이터 규격 (FcmMessageDto)

`kr.mmv.mjusugangsincheonghelper.notification.common.dto.FcmMessageDto` 클래스는 FCM `Message` 객체와 1:1 매칭되도록 설계되었습니다.

| 필드 | 타입 | 설명 |
| :--- | :--- | :--- |
| `token` | String | 수신 기기의 FCM 토큰 (단일 발송 시 필수) |
| `topic` | String | 구독 주제 (전체 공지 등 토픽 발송 시 사용) |
| `notification` | Object | 기본 알림 설정 (title, body, image) |
| `data` | Map | 클라이언트 앱에서 처리할 커스텀 키-값 데이터 |
| `webpush` | Object | PWA/브라우저 전송을 위한 상세 설정 (Headers, Actions 등) |

### 💡 주요 설정 (WebPush)
*   **iOS (PWA) 대응**: `webpush.headers`에 `Urgency: high`를 포함하여 백그라운드 수신 확률을 높입니다.
*   **인터랙션**: `webpush.fcm_options.link`를 통해 알림 클릭 시 이동할 URL을 지정할 수 있습니다.

---

## 4. 🛠️ 구현 및 운영 상세

### 4.1 배치 발송 (Batch Processing)
FCM API는 한 번의 요청에 최대 500개의 메시지를 보낼 수 있습니다. `FcmSenderService`는 안전성을 위해 **450개 단위**로 배치(Batch)를 나누어 `sendEach()` 메서드로 발송합니다.

### 4.2 오류 처리 및 토큰 클린업 (Error Handling)
발송 결과 중 아래 에러 코드가 포함된 경우, 해당 기기는 더 이상 유효하지 않은 것으로 간주합니다.
*   `UNREGISTERED`: 사용자가 앱을 삭제하거나 알림 권한을 취소함.
*   `INVALID_ARGUMENT`: 토큰 형식이 잘못됨.

**처리 로직:**
1.  `BatchResponse`에서 실패한 응답을 필터링합니다.
2.  해당 토큰을 가진 `StudentDevice` 엔티티를 조회합니다.
3.  `deactivate(reason)` 메서드를 호출하여 `is_activated = false`로 변경하고 사유를 기록합니다.

### 4.3 Firebase 설정
`FcmConfig` 클래스는 `application.yml`의 `app.firebase.config-path`에 설정된 경로의 JSON 키 파일을 로드하여 FirebaseApp을 초기화합니다.
*   기본 경로: `src/main/resources/mju-sugangsincheong-helper-firebase-adminsdk.json`

---

## 5. 🧪 테스트 방법

`NotificationController`의 API를 사용하여 현재 로그인한 사용자의 모든 기기로 테스트 알림을 즉시 보낼 수 있습니다.

*   **Endpoint:** `POST /api/v1/notifications/test`
*   **Header:** `Authorization: Bearer {JWT_TOKEN}`
*   **Result**: Redis 큐에 테스트 메시지가 적재되고, 워커가 이를 소비하여 FCM을 발송합니다.
