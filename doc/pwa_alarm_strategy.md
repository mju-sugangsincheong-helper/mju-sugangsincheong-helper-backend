# PWA 전용 알림 및 기기 등록 전략 (PWA Notification Strategy)

이 문서는 네이티브 앱(Android APK, iOS ipa) 없이, 오직 **PWA(Progressive Web App)** 환경에서만 알림 서비스를 제공하기 위한 기술 전략과 시나리오를 정의합니다.

---

## 1. 🛑 핵심 원칙 (Core Principles)

1.  **No PWA, No Notification:** 브라우저 탭(Chrome, Safari 등)으로 접속한 상태에서는 절대 알림 권한을 요청하거나 기기를 등록하지 않습니다. 오직 **홈 화면에 추가된 앱(Standalone)** 상태에서만 동작합니다.
2.  **1 Device = 1 Token Rule:** 기기의 FCM 토큰이 변경되더라도, 백엔드에는 새로운 기기로 등록되지 않고 기존 기기 정보가 **갱신(Update)** 되어야 합니다.
3.  **Frontend as a State Manager:** 프론트엔드는 자신이 과거에 발급받았던 토큰을 localStorage 에 기억하고 있어야 하며, 변경 감지 시 백엔드에 **교체(Rotation)** 를 요청해야 합니다.

---

## 2. 📱 PWA와 토큰 수명주기 (Lifecycle)

### 왜 토큰 갱신(Rotation)이 일어나는가?
FCM(Firebase Cloud Messaging) 토큰은 불변의 고유 ID가 아닙니다. 다음 상황에서 예고 없이 변경될 수 있습니다.
1.  **브라우저 정책:** 보안상의 이유로 주기적으로 토큰을 만료시킬 수 있습니다.
2.  **사용자 행위:** 사용자가 브라우저 캐시/데이터를 삭제했을 때.
3.  **서비스 워커 갱신:** PWA의 Service Worker가 업데이트되거나 재등록될 때.
4.  **복구:** 이전에 발급된 토큰이 손상되었거나 동기화가 깨졌을 때.

### 왜 프론트엔드가 토큰을 저장(LocalStorage)해야 하는가?
백엔드는 요청이 들어왔을 때, 이것이 **"새로운 기기"**인지 **"기존 기기의 토큰만 바뀐 것"**인지 알 방법이 없습니다.
따라서 프론트엔드가 **"나 예전에 토큰 A였는데, 지금 B로 바뀌었어. 바꿔줘"** 라고 `oldToken`을 함께 보내주어야만 백엔드가 `UPDATE`를 수행할 수 있습니다. 이를 위해 `localStorage`에 이전 토큰을 저장해두어야 합니다.

---

## 3. 🎬 상세 시나리오 (Scenarios)

### Scenario A: 일반 브라우저 접속 (Chrome/Safari 탭)
사용자가 URL을 쳐서 웹사이트에 들어온 상황입니다.

1.  **진입:** 사용자가 브라우저로 접속.
2.  **감지:** `isPWA()` 체크 -> **False**.
3.  **동작:**
    *   알림 권한 요청(`Notification.requestPermission`)을 **절대 하지 않음**.
    *   FCM 토큰 발급 시도(`getToken`)를 **하지 않음**.
    *   백엔드 API(`/api/v1/devices`) 호출을 **하지 않음**.
    *   화면 상단/하단에 **"앱으로 설치하고 알림 받기"** 가이드 배너 표시.
4.  **결과:** DB에 기기 정보 없음. 불필요한 토큰 생성 방지.

### Scenario B: PWA 최초 설치 및 실행
사용자가 "홈 화면에 추가"를 완료하고, 아이콘을 눌러 앱을 켠 상황입니다.

1.  **진입:** 홈 화면 아이콘 터치.
2.  **감지:** `isPWA()` 체크 -> **True**.
3.  **스토리지 확인:** `localStorage.getItem('mju_fcm_token_cache')` -> **Null (없음)**.
4.  **권한 요청:** 설정 페이지의 기기관리에서 사용자에게  "현재 기기를 등록 하여 알림을 받으시겠습니까?" UI 노출 -> 승인 시 `Notification.requestPermission()` 실행.
5.  **토큰 발급:** FCM으로부터 새 토큰 **[A]** 발급.
6.  **API 호출:**
    *   `POST /api/v1/devices`
    *   Payload: `{ "fcmToken": "A", "oldToken": null, "platform": "IOS/ANDROID" }`
7.  **백엔드 처리:** `oldToken`이 없으므로 **INSERT** (신규 기기 등록).
8.  **후처리:** `localStorage.setItem('mju_fcm_token_cache', 'A')` 저장.

### Scenario C: 토큰 자동 갱신 (Token Rotation)
앱을 잘 쓰고 있는데, 브라우저 내부 사정으로 토큰이 [A]에서 [B]로 바뀐 상황입니다.

1.  **진입:** 앱 실행 (또는 백그라운드에서 포그라운드 전환).
2.  **감지:** `isPWA()` -> **True**.
3.  **토큰 확인:** FCM `getToken()` 호출 -> **[B]** 반환 (새로운 토큰).
4.  **스토리지 확인:** `localStorage`에는 **[A]** 가 저장되어 있음.
5.  **비교:** `A != B` (변경 감지!).
6.  **API 호출:**
    *   `POST /api/v1/devices`
    *   Payload: `{ "fcmToken": "B", "oldToken": "A", "platform": "..." }`
7.  **백엔드 처리:**
    *   DB에서 `fcm_token = 'A'`인 레코드를 찾음.
    *   해당 레코드의 토큰을 `B`로 **UPDATE**. (기기 ID는 유지됨).
8.  **후처리:** `localStorage` 값을 **[B]** 로 갱신.

### Scenario D: PWA 삭제 후 재설치
사용자가 앱을 지웠다가(홈 화면에서 제거), 나중에 다시 설치한 상황입니다.
*iOS/Android는 PWA가 삭제될 때 LocalStorage도 같이 날려버리는 경우가 많습니다.*

1.  **삭제:** 사용자가 앱 삭제 -> OS가 `LocalStorage` 및 `ServiceWorker` 정리.
2.  **재설치 및 진입:** 사용자가 다시 설치 후 실행.
3.  **감지:** `isPWA()` -> **True**.
4.  **스토리지 확인:** `localStorage` -> **Null (삭제됨)**.
5.  **토큰 발급:** FCM으로부터 새 토큰 **[C]** 발급. (완전히 새로운 토큰일 확률 높음)
6.  **API 호출:**
    *   Payload: `{ "fcmToken": "C", "oldToken": null }`
7.  **백엔드 처리:** `oldToken`이 없으므로 **INSERT** (신규 기기 등록).
    *   *참고:* DB에는 여전히 이전 토큰 [B]가 남아있을 수 있음 (고아 데이터).
8.  **고아 데이터 정리:** 추후 알림 발송 시 [B]로 보내면 FCM이 `NotRegistered` 오류를 뱉음 -> 백엔드 Cleanup Worker가 [B] 삭제.

---

## 4. 🛠️ 구현 스펙 (Implementation Spec)

### 4.1 Backend Request DTO

```java
public class DeviceRegisterRequestDto {
    @NotBlank
    private String fcmToken; // (필수) 현재 발급받은 유효한 토큰

    private String oldToken; // (선택) 이전에 사용하던 토큰 (교체 시 사용)

    @NotBlank
    private String platform; // (필수) ANDROID | IOS | PC
}
```

### 4.2 Frontend Implementation Rules (규칙)

프론트엔드 개발자는 반드시 아래 규칙을 준수하여 구현해야 합니다.

#### ✅ Rule 1. PWA 모드 확인 (Gatekeeper)
*   **어떤 경우에도** 브라우저 탭(Browser Tab) 환경에서는 기기 등록 로직을 실행하지 않는다.
*   `window.matchMedia('(display-mode: standalone)')` 또는 `navigator.standalone`(iOS Legacy)을 통해 앱 실행 모드를 확인 후 로직을 진입시킨다.

#### ✅ Rule 2. LocalStorage 필수 사용 (Token Cache)
*   FCM에서 발급받은 토큰은 반드시 `localStorage` (Key: `mju_fcm_token_cache`)에 저장한다.
*   이는 **State of Truth** 역할을 하며, "내가 서버에 마지막으로 보고한 토큰"을 의미한다.

#### ✅ Rule 3. 토큰 비교 및 전송 (Rotation Logic)
*   앱 실행 시마다 `getToken()` (현재 토큰)과 `localStorage` (저장된 토큰)을 비교한다.
*   **값이 다를 경우에만** 서버 API(`/api/v1/devices`)를 호출한다.
    *   `fcmToken`: 현재 토큰 (`getToken()`)
    *   `oldToken`: 저장된 토큰 (`localStorage`)
*   서버 응답이 200 OK 일 때만, `localStorage` 값을 현재 토큰으로 덮어쓴다.

#### ✅ Rule 4. 테스트 기능 노출 (Testability)
*   QA 및 검증을 위해 개발 환경(`process.env.NODE_ENV === 'development'`)에서는 다음 함수를 `window` 객체에 노출해야 한다.
    1.  **`forceTokenRefresh()`**: FCM SDK의 토큰만 삭제하고 `localStorage`는 유지한다. (→ Rotation 시나리오 재현)
    2.  **`simulateNewInstall()`**: FCM SDK 토큰과 `localStorage`를 모두 삭제한다. (→ 신규 설치 시나리오 재현)

#### ✅ Rule 5. 플랫폼 식별
*   `navigator.userAgent`를 파싱하여 `ANDROID`, `IOS`, `PC` 중 하나를 반드시 식별하여 전송한다.
*   Unknown일 경우 `ETC`가 아닌 최대한 근접한 OS로 매핑한다.

#### ✅ Rule 6. 알림 끄기 = 기기 삭제 (Hard Delete)
*   본 서비스에는 "알림 잠시 끄기(Mute)" 기능이 없다.
*   사용자가 [알림 끄기] 버튼을 누르면, 백엔드에 해당 기기를 **삭제(Hard Delete)** 요청해야 한다 (`DELETE /api/v1/devices`).
*   사용자가 다시 알림을 켜려면 [기기 등록] 절차를 처음부터 다시 수행해야 한다.
