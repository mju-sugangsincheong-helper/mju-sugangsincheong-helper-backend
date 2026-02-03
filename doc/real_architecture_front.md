# 프론트 부분

기술 스택 `vue.js fcm pwa`

단 매우 복잡함 총 3개의 환경을 모두 지원해야함 push 알림을 위함
1. android/ios 는 수강신청 연습 불가능학게
2. 알림은 일단 fcm 으로만 동작하도록
3. 프로젝트 폴더 구조를 잘 만들어줘
4. 사용하는 알림은 3가지로 구성 이상의 알림시스템은 필요 없음
	1. 구독 알림 => fcm 알림만 지원
	2. 토스트 알림 => 시스템에서 처리하다가 나오는 일시 알림 우측 상단(상단바 아래) 부터
	3. 강제 알림 => 아래의 pwa 설치
5.  iOS와 Android/PC는 설치 방식, 설정 방식등이 세부적으로 달라 확실하게 처리
6. PWA는 한 번 설치되면 캐시된 HTML/JS를 사용 `vite-plugin-pwa`를 사용할 때 `registerType: 'prompt'`로 설정하여, 새 버전이 감지되면 새로고침`window.location.reload()`를 수행, 기본적으로는 api 응답 제외 모든것을 캐시
7. ui 는 기본적으로 모던차분한 디자인, 과도한 라운드 x , 차분한 색깔 검정, 흰색, 회색 + 포인트 색깔 1개 (명지대 파란색) 만 사용 + 가끔 중요한것만 빨랑 파랑 초록
8. 회원 페이지에는 로그아웃, 탈퇴, 기기관리, 각 기기 백그라운드 알림 관리 등이 모두 모여있어 다른곳에서 처리하는것이 아니라 여기서만 처리할 수 있어
	1. 


### 페이지 구성
ui 구성
상단바(fix) : 각 서비스 이동 버튼, 카카오톡 오픈 채팅 문의, 알림, 학번  
구독 페이지, 통계페이지는 수강신청 기간(10:15 ~ 16:50)에만 운영되고 나머지 기간은 우간신청 기간이 아님 안내, 운영관련은 모두 백엔드에 물어보고 처리
하단: 개발자 정보,개인정보 처리방침 / 이용약관 페이지  
1. home : 각 서비스 홍보, 공지
2. 회원 및 설정 페이지 : 회원정보, 로그아웃, 탈퇴
3. 기기 관리 페이지 : 모든 백그라운드 알림 기기 관리
4. login : 처음 로그인이 회원가입(명지대 서버와 동기화) 
5. 구독 페이지 : 검색을 해서 구독 추가, 구독 목록 리스트 (동일 과목 알림 대기명 표시, 구독 취소 버튼), 알림 디바이스 관리
6. 통계 페이지 : 
	1. 경쟁 : 학과별 구독자수, 과목별 구독자수, 학년별 구독자수
	2. ...
7. 시간표 : 검색을 해서 시간표를 짤 수 있는 페이지
8. 수강신청 연습 : 기본적으로 ui 가 완전히 다르고 다른 것들과는 독립적 ui 구성
	1. 신청 버튼 클릭 속도, 알림(alert) 팝업 처리 속도
	2. 순위표( 개인전 순위 ) (단체전 순위 (학과, 학년))

### 수강신청 연습

명지대 실제 수강신청 UI/UX를 MOCK 하여 
1. 연습 : 사용자가 명지대 수강신청을 연습할 수 있도록 하는 것이 최우선
2. 재미요소 : 신청 버튼 클릭 속도, 알림(alert) 팝업 처리 속도 사용자의 반응 속도를 비교 순위표( 개인전 순위 ) (단체전 순위 (학과, 학년))

#### **핵심 동작 흐름**
1. **시작**: 사용자가 특정 과목을 장바구니에 담음(mock) → 연습 시작
2. **큐 시뮬레이션**:
   - 실제 수강신청처럼 "**서버 혼잡, 대기 중...**" 큐 UI 표시
   - **큐 해제 시점**이 실제 신청 가능 타이밍으로 간주
   - 큐는 뜰 수도 있고 안뜰 수도 있고 0.5 확율, 각 큐는 또 오래 기다릴 수도 적게 기다릴 수도 있음(0.5~2)
1. **타이밍 측정**:
   - 큐 해제 → 버튼 클릭까지의 시간만 기록 (`reaction_time_ms`)
   - 큐 대기 시간은 **측정 및 통계에서 제외**
3. **Alert 처리**:
   - 신청 후 알림(alert) 팝업 표시 → 확인 버튼 처리까지 시간도 포함
4. **결과 제출**:
   - 반응 시간, 과목 ID, 성공 여부 등을 백엔드로 전송


### FCM 백그라운드 알림 기능
포그라운드 알림은 sse 로 처리되므로 여기서 처리할 일이 아닙니다

백그라운드 알림 기기 관리 페이지에서 동작하는 일
섹션은 다시 2가지로 나뉩니다 Current Device 섹션, Background Alarm Devices List 섹션

원칙 : 서버 DB(`device_list`)를 **유일한 진실(Single Source of Truth)**
전제 : 회원관리 페이지에 들어오면 벡엔드에서 먼저 모든 기기 리스트를 받아두고 시작합니다

자기 자신의 기기의 상태를 Current Device 측에 표시
유저가 등록한 기기들 즉 Registered 했던 모든 기기들이 리스트로 보입니다


#### A. 내 기기 (Current Device)
*리스트 최상단에 고정 노출*

**1. 🔴 수신 불가 (Impossible)**
*   **조건:** 일반 웹 브라우저 (PC/Mobile Web)
*   **UI:** 
    *   [배지] 🔴 **[수신 불가]**
    *   [메시지] "백그라운드 알림을 받으려면 앱 설치가 필요합니다."
    *   [버튼] **[📲 앱 설치 방법]** (Bottom Sheet / Guide)

**2. 🟡 미등록 (Unregistered)**
*   **조건:** PWA 앱 모드(pc pwa, mobile pwa) && 서버 리스트에 내 토큰이 없음.
*   **UI:** 
    *   [배지] 🟡 **[미등록]**
    *   [메시지] "이 기기에서 알림을 받으시겠습니까?"
    *   [버튼] **[🔔 알림 받기]** (파란색 강조 CTA)
*   **동작:** 권한/토큰 획득 → `POST` 전송(서버에 저장) → 로컬 저장 → **🟢 상태로 전환**, B 를 갱신하여 이 기기도 추가되게 보이게 합니다

**3. 🟢 등록 완료 (Registered)**
*   **조건:**  PWA 앱 모드 (`standalone`) && 서버 리스트에 내 토큰이 있음.
*   **UI:** 
    *   [배지] 🟢 **[수신 중]**
    *   [메시지] "현재 기기(Galaxy S24)로 알림이 발송됩니다."
    *   [버튼] **[🚫 알림 끄기]** (회색/빨간색 보조 버튼)
*   **동작:**
    1.  서버에 `DELETE /api/user/devices/{my_id}` 요청.
    2.  성공 시 `localStorage` 토큰 삭제.
    3.  **🟡 미등록 상태로 전환**

---

#### B. Background Alarm Devices List
*내 기기 아래에 리스트로 나열 (서버에 등록된 기기만 보임)*

*   **조건:** 서버측으로 받은 `device_list` 
*   **UI (List Item):**
    *   [아이콘] 📱 스마트폰 / 💻 PC 아이콘
    *   [텍스트] **iPhone 15 Pro** (마지막 접속: 2024.03.10)
    *   [버튼] **[🗑️ 삭제]**
    *   선택적 ui [자기 기기] : [current device]  local fcm_token == server fcm_token 
*   **동작:**
    1.  서버에 `DELETE /api/user/devices/{other_id}` 요청.
    2.  성공 시 **리스트에서 해당 항목이 즉시 사라짐.**

| 환경                 | display-mode | 역할        | FCM 권한 요청  | 기기 관리 UI                   |
| :----------------- | :----------- | :-------- | :--------- | :------------------------- |
| **PC Browser**     | `browser`    | 관리자       | **절대 안 함** | 앱 설치 안내 + 타 기기 삭제 버튼       |
| **PC PWA**         | `standalone` | 관리자 + 수신자 | 버튼 클릭 시 수행 | **[이 기기 등록/해제]** + 타 기기 삭제 |
| **Mobile Browser** | `browser`    | 관리자       | **절대 안 함** | 앱 설치 안내 + 타 기기 삭제 버튼       |
| **Mobile PWA**     | `standalone` | 관리자 + 수신자 | 버튼 클릭 시 수행 | **[이 기기 등록/해제]** + 타 기기 삭제 |


---
# 프론트 아키텍쳐

### 1. 🛠️ 최종 의존성 설치 (Dependencies)

복사해서 터미널에 입력하세요. 최신 트렌드와 성능을 고려한 조합입니다.


# 🏗️ Mju-SuGangSinCheong-Helper Frontend Architecture (Vite MPA)

## 1. 📦 프로젝트 개요 및 의존성

### 설치 명령어 (Dependencies)
그대로 복사해서 실행하세요.

```bash
# 1. Core Framework & State Management (필수)
npm install vue vue-router pinia axios

# 2. UI & Styling (Boxy & Modern Design)
npm install -D tailwindcss postcss autoprefixer
npm install @heroicons/vue                # Tailwind 팀 공식 아이콘 (가장 안정적)

# 3. PWA & Mobile Utils (필수 기능)
npm install vite-plugin-pwa               # PWA 생성 및 캐싱 전략
npm install firebase                      # FCM 푸시 알림
npm install @vueuse/core                  # 반응형 기기 감지 및 상태 관리

# 4. Features (게임, 통계, 유틸리티)
npm install chart.js vue-chartjs          # 학과별/과목별 경쟁률 시각화
npm install vue-toastification@next       # 시스템 토스트 알림 (우측 상단)
npm install date-fns                      # 날짜/시간 계산 표준
npm install uuid                          # 수강신청 연습 세션 및 기기 식별 ID 생성

# 5. Testing (E2E & Unit)
npm install -D vitest jsdom @vue/test-utils
npm install -D @playwright/test           # 시나리오 기반 통합 테스트
```

---

## 2. 📂 상세 폴더 구조 (Detailed Directory Tree)

**핵심 전략:**
1.  루트의 `index.html`은 **메인 서비스**입니다.
2.  `src/practice/index.html`은 **수강신청 연습**입니다.
3.  `vite.config.js`가 이 두 진입점을 관리합니다.

```graphql
mju-helper-frontend/
├── package.json                  # 의존성 관리
├── vite.config.js                # ⭐️ 핵심: Main과 Practice 진입점 설정 (MPA)
├── tailwind.config.js            # 🎨 스타일 격리 (Practice 폴더 제외 설정)
├── postcss.config.js
├── vitest.config.js              # 유닛 테스트 설정
│
├── public/                       # 정적 파일 (빌드 시 루트로 복사됨)
│   ├── firebase-messaging-sw.js  # FCM 백그라운드 워커 (Service Worker)
│   ├── manifest.json             # PWA 매니페스트
│   ├── pwa-icons/                # 앱 아이콘 (192, 512)
│   └── legacy-assets/            # [Practice용] 구형 버튼, 배경음, 로고
│
└── src/
    ├── api/                      # 🔗 공통 API 모듈 (Axios)
    │   ├── axios.js              # Interceptor (401 발생 시 로그아웃 처리)
    │   └── endpoints.js          # API URL 상수 관리
    │
    ├── utils/                    # 🛠️ 공통 유틸리티
    │   ├── date.js               # 날짜 포맷터
    │   ├── date.spec.js          # [TEST] 날짜 계산 테스트
    │   └── validator.js          # 학번/비밀번호 정규식 검증
    │
    ├── stores/                   # 💾 공통 상태 관리 (Pinia)
    │   └── authStore.js          # 로그인 토큰, 유저 정보 (Main/Practice 공유)
    │
    ├── main/                     # 📱 [메인 서비스] (Modern PWA + Tailwind)
    │   ├── main.js               # Entry Point (Vue Init, FCM Init)
    │   ├── App.vue               # PWA Reload Prompt 포함
    │   ├── style.css             # @tailwind directives
    │   │
    │   ├── router/               # Main 라우터
    │   │   ├── index.js          # 페이지 라우팅 정의
    │   │   └── guards.js         # 로그인 가드 (비로그인 시 /login 리다이렉트)
    │   │
    │   ├── composables/          # 🧩 비즈니스 로직 (Hooks)
    │   │   ├── useFcm.js         # ⭐️ FCM 3단계 상태 관리 (Impossible/Unreg/Reg)
    │   │   ├── useFcm.spec.js    # [TEST] 기기 환경별 상태 판별 테스트
    │   │   └── usePwaInstall.js  # PWA 설치 프롬프트 제어
    │   │
    │   ├── components/
    │   │   ├── common/           # 공통 UI (Boxy Design)
    │   │   │   ├── AppHeader.vue # 상단바 (학번, 알림, 이동 버튼)
    │   │   │   └── PwaReloadPrompt.vue # 업데이트 알림 팝업
    │   │   │
    │   │   ├── member/           # 회원/기기 관리
    │   │   │   ├── DeviceManager.vue # ⭐️ 기기 목록 및 상태별 버튼 UI
    │   │   │   └── DeviceItem.vue    # 개별 기기 항목 (삭제 버튼 포함)
    │   │   │
    │   │   └── charts/           # 통계
    │   │       └── CompetitionChart.vue # Chart.js 래퍼
    │   │
    │   └── pages/                # 화면(View)
    │       ├── HomePage.vue      # 공지사항, 서비스 진입점
    │       ├── LoginPage.vue     # 로그인 (명지대 서버 동기화)
    │       ├── SubscribePage.vue # 구독 검색/관리
    │       ├── StatisticsPage.vue# 경쟁률 통계
    │       └── MemberPage.vue    # 마이페이지 (기기관리 포함)
    │
    └── practice/                 # [수강신청 연습] (Legacy CSS + Mock Logic)
        ├── index.html            # ⭐️ Practice 진입점 (HTML 위치 중요)
        ├── main.js               # Entry (모바일 차단 로직 포함)
        ├── App.vue               # Full Screen Container (No Layout)
        ├── legacy.css            # 🎨 명지대 구형 스타일 (Tailwind 간섭 없음)
        │
        ├── engine/               # ⚙️ 수강신청 연습 엔진
        │   ├── practiceEngine.js # ⭐️ 상태 머신 (Queue -> Active -> Alert -> Finish)
        │   └── practiceEngine.spec.js # [TEST] 반응속도(ms) 측정 로직 테스트
        │
        ├── components/           # Mock UI (Table 태그 위주)
        │   ├── MockHeader.vue    # 구형 헤더 복제
        │   ├── MockBasket.vue    # 장바구니 테이블
        │   └── QueueModal.vue    # "서버 대기중..." 팝업
        │
        └── pages/
            ├── IntroScreen.vue   # 주의사항 및 시작 버튼
            ├── PlayScreen.vue    # 실제 클릭 연습 화면
            └── ResultScreen.vue  # 결과(반응속도, 순위) 표시
```

---

## 3. ⚙️ 핵심 설정 파일 구현

### A. `vite.config.js` (MPA & PWA 설정)
두 개의 앱을 동시에 빌드하고, PWA 설정을 적용합니다.

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'prompt', // 6. 업데이트 감지 시 사용자 확인 후 새로고침
      includeAssets: ['favicon.ico', 'pwa-icons/*.png'],
      manifest: {
        name: 'MJU 수강신청 도우미',
        short_name: 'MJU SuGangSinCheong Helper',
        description: '명지대 수강신청 빈자리 알림 및 연습',
        theme_color: '#00205B', // 명지 블루
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: 'pwa-icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'pwa-icons/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        runtimeCaching: [
          // 1. 🔍 검색 데이터 (강의 목록 리스트): 하루(24시간) 캐시
          {
            // 예: /api/search-data
            urlPattern: ({ url }) => url.pathname.startsWith('/api/search-data')
            // CacheFirst: 캐시가 있으면 네트워크 요청 아예 안 함 (가장 빠름)
            handler: 'CacheFirst', 
            
            options: {
              cacheName: 'api-search-data-cache',
              expiration: {
                maxEntries: 2,             // 최대 2개 요청까지만 저장
                maxAgeSeconds: 60 * 60 * 24 // 24시간 (초 단위: 86400초)
              },
              // 성공적인 응답(200)만 캐시
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          // 2. 📡 그 외 모든 API (수강신청 결과, 경쟁률 등): 절대 캐시 금지
          {
            // 위에서 걸러지지 않은 나머지 /api 요청들
            urlPattern: ({ url }) => url.pathname.startsWith('/api'),
            // NetworkOnly: 무조건 서버에 요청 (실시간 데이터 보장)
            handler: 'NetworkOnly' 
          }
        ]
      }
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src')
    }
  },
  build: {
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),              // 메인 앱
        practice: resolve(__dirname, 'src/practice/index.html') // 연습 게임
      }
    }
  }
})
```

### B. `tailwind.config.js` (스타일 격리)
`practice` 폴더에는 현대적인 스타일이 적용되지 않도록 막습니다.

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/main/**/*.{vue,js,ts}", // ⭐️ Main 앱만 적용
    "./src/components/**/*.{vue,js,ts}",
    // ⚠️ src/practice 폴더는 제외됨 -> legacy.css만 적용
  ],
  theme: {
    extend: {
      colors: {
        mju: {
          blue: '#00205B', // 명지 Deep Blue
          light: '#1A3B7C',
          gray: '#F3F4F6'
        }
      },
      borderRadius: {
        DEFAULT: '0px',   // 7. Boxy Design (라운드 제거)
        'sm': '2px'
      }
    },
  },
  plugins: [],
}
```

---

## 4. 🧩 주요 비즈니스 로직 (Composables)

### A. FCM 기기 상태 머신 (`src/main/composables/useFcm.js`)
**요구사항 4, 5, 8 해결:** 3가지 상태(Impossible, Unregistered, Registered) 관리

```javascript
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { getMessaging, getToken, deleteToken } from "firebase/messaging"
import api from '@/api/auth'

export function useFcm() {
  const authStore = useAuthStore()
  const messaging = getMessaging()
  
  // 1. 환경 감지
  const isStandalone = ref(window.matchMedia('(display-mode: standalone)').matches)
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
  
  // 2. 로컬 토큰 확인
  const localToken = ref(localStorage.getItem('fcm_token'))

  // 3. 상태 머신 (Computed)
  const status = computed(() => {
    // 🔴 수신 불가: 브라우저 모드일 때
    if (!isStandalone.value) return 'IMPOSSIBLE'
    
    // 🟢 등록 완료: 로컬 토큰 존재 && 서버 리스트에 내 토큰 있음
    if (localToken.value && authStore.deviceList.some(d => d.token === localToken.value)) {
      return 'REGISTERED'
    }
    
    // 🟡 미등록: PWA 모드이나 토큰이 없거나 서버에 없음
    return 'UNREGISTERED'
  })

  // 액션: 알림 등록
  const register = async () => {
    try {
      const permission = await Notification.requestPermission()
      if (permission !== 'granted') return false

      const token = await getToken(messaging, { vapidKey: 'YOUR_KEY' })
      
      // 서버에 내 기기 정보 전송 (플랫폼 구분)
      await api.registerDevice({
        token,
        platform: isIOS ? 'iOS' : 'Android/PC',
        userAgent: navigator.userAgent
      })

      localStorage.setItem('fcm_token', token)
      localToken.value = token
      await authStore.fetchDevices() // 리스트 갱신
      return true
    } catch (e) {
      console.error(e)
      return false
    }
  }

  // 액션: 알림 해제 (로그아웃 아님, 이 기기만 끄기)
  const unregister = async () => {
    if (!localToken.value) return
    await api.removeDevice(localToken.value)
    await deleteToken(messaging)
    localStorage.removeItem('fcm_token')
    localToken.value = null
    await authStore.fetchDevices()
  }

  return { status, isIOS, register, unregister }
}
```

### B. 수강신청 연습 엔진 (`src/practice/engine/practiceEngine.js`)
**요구사항:** 수강신청 MOCK 로직, 대기열, 반응속도 측정

```javascript
import { ref } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import api from '@/api/practice'

export function usePracticeEngine() {
  const state = ref('IDLE') 
  const isBusy = ref(false) // ⭐️ 화면 전체를 '먹통'으로 만드는 플래그

  const session = ref({
    startTime: 0,
    queueTime: 0,
    subjectId: null
  })

  // 🐌 렉 유발기 (최소 0.1초 이상 무조건 멈춤)
  const lag = (min = 100, max = 300) => {
    const ms = Math.random() * (max - min) + min
    return new Promise(resolve => setTimeout(resolve, ms))
  }


  // ------------------------------------------------
  // 2. 🖱️ 신청 버튼 (답답함: 중 + 대기열)
  // ------------------------------------------------
  const attemptApply = async (subjectId) => {
    // 이미 뭔가 돌고 있으면 클릭 무시 (렉걸린 느낌)
    if (state.value !== 'IDLE' && state.value !== 'ACTIVE') return
    if (isBusy.value) return

    session.value.subjectId = subjectId
    isBusy.value = true // 버튼 누르자마자 UI 얼림

    // 1️⃣ [UI 렉] 클릭했는데 0.1~0.3초간 아무 반응 없음
    await lag(100, 300) 

    // 2️⃣ [서버 전송] "처리중..." 같은거 안 뜨고 그냥 멈춰있음 (0.5초)
    state.value = 'LOADING' 
    await lag(300, 600)

    // 3️⃣ [대기열] 50% 확률로 폭주
    const isTrafficJam = Math.random() > 0.5
    if (isTrafficJam) {
      state.value = 'QUEUE' // 이제서야 대기열 팝업 뜸
      
      const waitTime = Math.random() * 2000 + 500
      session.value.queueTime = Math.round(waitTime)
      
      await new Promise(r => setTimeout(r, waitTime))
      
      // 대기열 끝나고 바로 안 뜸. 또 잠깐 멈칫 (0.2초)
      await lag(200, 300) 
    }

    // 4️⃣ [알림 팝업] 드디어 뜸
    triggerAlert()
    
    // 알림이 떴지만 isBusy는 안 품 (알림 닫을 때까지 뒤에꺼 클릭 못하게)
  }

  // ------------------------------------------------
  // 3. 🚨 팝업 트리거
  // ------------------------------------------------
  const triggerAlert = () => {
    state.value = 'ALERT'
    // ✨ 반응속도 측정 시작 ✨
    session.value.startTime = performance.now()
  }

  // ------------------------------------------------
  // 4. ✅ 확인 버튼 (답답함: 하)
  // ------------------------------------------------
  const confirmAlert = async () => {
    if (state.value !== 'ALERT') return

    // 클릭하자마자 시간 기록 (사용자 반응)
    const reactionTime = Math.round(performance.now() - session.value.startTime)

    // ⚠️ 확인 버튼 눌렀는데 팝업이 바로 안 꺼짐 (0.2초 렉)
    await lag(150, 300)

    state.value = 'FINISHED'
    isBusy.value = false // 이제서야 모든 UI 얼림 해제

    // 결과 전송 (사용자 눈에는 안 보임)
    api.submitResult({
      sessionId: uuidv4(),
      subjectId: session.value.subjectId,
      reactionTime,
      queueTime: session.value.queueTime
    })

    return reactionTime
  }

  return { 
    state, 
    isBusy, // UI에서 disabled 처리용
    searchCourses, 
    attemptApply, 
    confirmAlert 
  }
}
```

### C. 연습 앱 진입점 (`src/practice/main.js`)
**요구사항 1:** 모바일/태블릿 접근 원천 차단

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './legacy.css' 

// 모바일 감지 (User Agent)
const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent)

if (isMobile) {
  alert("🚫 수강신청 연습은 PC 환경에서만 가능합니다.\n메인 화면으로 이동합니다.")
  window.location.href = "/" // 메인 앱으로 강제 이동
} else {
  // PC일 때만 앱 마운트
  const app = createApp(App)
  app.use(createPinia())
  app.mount('#practice-app')
}
```

---

## 5. 🚢 배포 설정 (Nginx)

MPA 구조이므로 단순히 정적 파일을 서빙하고, `/practice/` 경로에 대한 처리를 추가합니다.

### `docker/nginx.conf`

```nginx
server {
    listen 80;
    server_name mju-helper.mmv.kr;
    root /usr/share/nginx/html; # 빌드된 dist 폴더

    # 1. 메인 앱 (SPA)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 2. 연습 게임 (MPA - 별도 폴더)
    location /practice/ {
        # /practice/ 접속 시 index.html 서빙
        try_files $uri $uri/ /src/practice/index.html; 
        # 주의: Vite 빌드 결과에 따라 경로는 /practice/index.html 일 수 있음
    }

    # 3. 백엔드 API 프록시
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
    }
}
```

이 구조는 **`src/main`**과 **`src/practice`**를 명확히 나누어 복잡한 CSS 충돌 문제를 원천 차단하면서도, **하나의 프로젝트**로 관리하여 개발 효율성을 극대화한 아키텍처입니다.





제시해주신 아키텍처와 요구사항을 바탕으로 **Frontend(Vue.js)** 관점에서 발생할 수 있는 기술적 문제점과 최적화 방안을 분석해 드립니다.

백엔드 설계가 **"데이터의 스냅샷 처리(Redis)"**와 **"무상태성(Stateless)"**을 강조하고 있어 매우 효율적이지만, 이로 인해 **프론트엔드가 감당해야 할 데이터 처리량과 로직 책임이 무거운 구조**입니다.

따라서 프론트엔드에서는 **렌더링 성능 최적화**와 **클라이언트 로직의 신뢰성 확보**가 가장 중요합니다.

---

### 1. 성능 및 렌더링 최적화 (가장 치명적인 부분)

구독 통계 API 등에서 **3,000개 이상의 과목 데이터**가 포함된 거대 JSON(약 500KB~1MB)을 한 번에 받아옵니다. Vue.js의 반응형 시스템(Reactivity System) 특성상 이 데이터를 그대로 `ref`나 `reactive`에 넣으면 브라우저가 멈출 수 있습니다.

#### 🔴 위험 요소
1.  **Deep Reactivity 오버헤드:** Vue는 객체 내부 깊숙한 곳까지 Proxy를 생성하여 변화를 감지합니다. 3,000개의 객체 × 필드 N개를 감지하면 메모리 사용량이 급증하고 초기 렌더링이 수 초간 멈출(Freezing) 수 있습니다.
2.  **DOM 폭발:** 3,000개의 `<tr>`이나 `<div>`를 한 번에 렌더링하면 브라우저가 버티지 못합니다.

#### ✅ 해결 방안
1.  **`shallowRef` 사용 및 `Object.freeze`:**
    데이터 내부의 속성이 개별적으로 변하는 것이 아니라, 통계 데이터 전체가 통째로 교체되는 구조입니다. 따라서 깊은 감시가 필요 없습니다.

    ```javascript
    import { shallowRef } from 'vue';

    // 나쁜 예: const stats = ref({}); -> 내부 속성 수만 개에 Proxy 생성
    // 좋은 예:
    const stats = shallowRef({}); 
    
    const fetchData = async () => {
      const data = await api.getStats();
      // 객체를 동결하여 Vue가 추가적인 관찰을 하지 못하게 막음
      stats.value = Object.freeze(data); 
    };
    ```

2.  **Virtual Scrolling (가상 스크롤) 필수 적용:**
    화면에 보이는 10~20개 항목만 렌더링하고 나머지는 스크롤 위치에 따라 갈아끼우는 방식을 **반드시** 사용해야 합니다.
    *   라이브러리 추천: `vue-virtual-scroller` 또는 `vue-window`

---

### 2. 'Tier 3' 스케줄 로직과 시간 동기화 (Clock Skew)

프론트엔드가 `schedule.json`의 시간과 **사용자 기기(Client)의 시스템 시간**을 비교하여 UI를 결정하는 구조(Tier 3)는 위험할 수 있습니다.

#### 🔴 위험 요소
1.  **사용자 시간 오류:** 사용자의 PC/폰 시간이 실제 시간보다 5분 느리거나 빠르다면, 남들은 수강신청 버튼이 활성화되었는데 본인만 "오픈 전" 화면을 보고 있을 수 있습니다.
2.  **악의적 조작:** 사용자가 로컬 시간을 미래로 돌려서 UI를 강제로 활성화할 수 있습니다 (물론 백엔드가 막겠지만, 사용자 경험상 좋지 않음).

#### ✅ 해결 방안
**"서버 시간 오프셋(Offset)" 계산 로직**을 추가해야 합니다.

1.  앱 최초 진입 시 `/api/system/status` 또는 가벼운 API를 호출합니다.
2.  Response Header의 `Date` 값(서버 시간)과 `clientTime`의 차이(`diff`)를 계산하여 저장합니다.
3.  이후 모든 시간 판단은 `new Date() + diff`를 기준으로 합니다.

    ```javascript
    // useServerTime.js (Composable)
    const timeOffset = ref(0);

    const syncTime = async () => {
      const start = Date.now();
      const response = await fetch('/api/system/status');
      const serverTime = new Date(response.headers.get('date')).getTime();
      const end = Date.now();
      const networkDelay = (end - start) / 2;
      
      // 현재 클라이언트 시간과 서버 시간의 차이 계산
      timeOffset.value = serverTime - (Date.now() - networkDelay);
    };

    const getNow = () => new Date(Date.now() + timeOffset.value);
    ```

---

### 3. 대용량 데이터 캐싱 및 상태 관리

문서에서 **"프론트엔드 캐싱(1분)"**을 언급하셨습니다. 사용자가 페이지를 이동할 때마다 1MB JSON을 다시 받는 것은 네트워크 낭비입니다.

#### 🔴 위험 요소
1.  **메모리 누수:** SPA(Single Page Application)에서 데이터를 계속 쌓아두면 모바일 기기에서 브라우저가 튕길 수 있습니다.
2.  **새로고침 시 데이터 증발:** Vuex/Pinia는 새로고침하면 초기화됩니다.

#### ✅ 해결 방안
1.  **TanStack Query (Vue Query) 도입 추천:**
    *   데이터 페칭, 캐싱, 동기화, 에러 처리를 위한 표준 라이브러리입니다.
    *   `staleTime: 60000` (1분) 옵션만 주면 알아서 1분간 API 호출을 안 하고 캐시를 씁니다.
    *   창 포커스 시(`refetchOnWindowFocus`) 재조회 기능 등을 쉽게 제어할 수 있습니다.

    ```javascript
    // 단순한 구현보다 라이브러리가 훨씬 안정적임
    const { data } = useQuery({
      queryKey: ['subscription-stats'],
      queryFn: fetchStats,
      staleTime: 1000 * 60, // 1분간 캐시 유지
      gcTime: 1000 * 60 * 5, // 5분 뒤 메모리 해제
    });
    ```

---

### 4. 수강신청 연습 (반응 속도 측정) UX

네트워크 지연 시간을 배제하고 **순수 사용자의 클릭 반응 속도**를 측정해야 합니다.

#### 🔴 위험 요소
1.  **네트워크 Latency 포함:** 클릭 -> API 요청 -> 응답 시간을 결과로 사용하면, 인터넷이 느린 사람은 반응속도가 느리게 기록되어 억울합니다.

#### ✅ 해결 방안
1.  **Client-Side 측정:**
    *   버튼이 활성화된 시점의 `performance.now()`와 사용자가 클릭한 시점의 `performance.now()` 차이를 계산합니다.
    *   계산된 `diffTime`을 서버로 전송합니다 (`POST /result { time_ms: 350 }`).
    *   서버는 이 값을 그대로 믿고 저장합니다. (어뷰징 가능성은 있지만, 재미 요소이므로 클라이언트 신뢰 모델로 가는 것이 UX상 좋습니다).

---

### 5. 인증 토큰 (JWT) 관리 및 보안

명지대 인증을 통해 받은 JWT를 어떻게 관리하느냐가 중요합니다.

#### 🔴 위험 요소
1.  **XSS 공격:** `localStorage`에 Access Token을 저장하면 XSS 취약점 발생 시 탈취당할 수 있습니다.
2.  **Refresh Token 처리:** Access Token 만료 시(30분~1시간) 사용자가 모르게(Silent Refresh) 갱신해야 하는데, 이 로직이 꼬이면 갑자기 로그아웃됩니다.

#### ✅ 해결 방안
1.  **저장소 분리:**
    *   **Access Token:** 메모리(Pinia 변수)에만 저장 (새로고침 시 사라짐 -> 재발급 로직 수행).
    *   **Refresh Token:** **HttpOnly Cookie**로 서버가 내려주도록 설정 (프론트엔드 JS가 접근 불가, 보안성 ↑).
2.  **Axios Interceptor:**
    *   401 에러 발생 시 -> 큐에 요청을 담아두고 -> 토큰 재발급 API 호출 -> 성공 시 큐에 있던 요청 재시도 -> 실패 시 로그아웃 처리.

---

### 6. 모바일 대응 UI (시간표 및 리스트)

PC 화면의 시간표와 3,000개 리스트 테이블을 모바일(특히 아이폰/갤럭시 미니)에 구겨 넣는 것은 매우 어렵습니다.

#### ✅ 제안
1.  **시간표 (Timetable):**
    *   PC: 전통적인 격자(Grid) 시간표.
    *   Mobile: **리스트 형태** (월요일: 과목A, 과목B...)로 보여주는 탭 뷰를 제공하거나, CSS Grid를 정교하게 사용하여 가로 스크롤을 허용해야 합니다.
2.  **테이블 (Data Table):**
    *   모바일에서는 `<table>` 태그 대신 **Card Layout**을 사용해야 합니다. (CSS Media Query로 PC에선 Table, Mobile에선 Card로 변환).

---

### 요약: 프론트엔드 개발 시 체크리스트

1.  [ ] **대용량 데이터:** `shallowRef`와 `Object.freeze`를 사용하여 반응성 오버헤드를 제거했는가?
2.  [ ] **리스트 렌더링:** `vue-virtual-scroller` 등을 통해 DOM 개수를 제한했는가?
3.  [ ] **시간 동기화:** 서버 시간(Header Date)과 클라이언트 시간의 오차(Offset)를 보정하고 있는가?
4.  [ ] **상태 관리:** Vue Query 등을 사용하여 데이터 캐싱(1분) 및 중복 호출을 방지했는가?
5.  [ ] **인증:** Access Token 만료 시 자동으로 갱신하는 Interceptor 로직이 견고한가?
6.  [ ] **UX:** 네트워크 상태가 나쁠 때(크롤러 지연 등) 사용자에게 "로딩 중"이 아닌 "이전 데이터(캐시)"라도 보여주는가? (Stale-While-Revalidate 패턴)

설계하신 아키텍처는 백엔드 부하를 최소화하는 훌륭한 전략입니다. 위 프론트엔드 최적화 포인트만 잘 챙기신다면 매우 빠르고 안정적인 서비스가 될 것입니다.
