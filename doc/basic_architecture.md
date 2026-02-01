```bash
src/main/java/kr/mmv/mjusugangsincheonghelper
├── global
│   ├── annotation
│   │   ├── ApiVersion.java
│   │   └── OperationErrorCodes.java
│   ├── api
│   │   ├── code
│   │   │   └── ErrorCode.java                     // ✅ 단일 enum (인터페이스 제거)
│   │   ├── docs
│   │   │   └── ErrorResponsesOperationCustomizer.java
│   │   ├── envelope
│   │   │   ├── ErrorResponseEnvelope.java
│   │   │   ├── PagedSuccessResponseEnvelope.java
│   │   │   ├── ResponseEnvelope.java              // 인터페이스
│   │   │   └── SingleSuccessResponseEnvelope.java
│   │   ├── exception
│   │   │   ├── BaseException.java
│   │   │   ├── ErrorDetail.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── filter
│   │   │   └── GlobalMetaFilter.java
│   │   ├── meta
│   │   │   ├── ClientInfo.java
│   │   │   ├── PageMeta.java
│   │   │   └── ResponseMeta.java
│   │   └── support
│   │       ├── ClientInfoExtractor.java
│   │       ├── CustomResponseMetaContextHolder.java
│   │       └── MetaGenerator.java
│   ├── config
│   │   ├── GlobalAsyncConfig.java
│   │   ├── GlobalOpenApiConfig.java
│   │   ├── GlobalWebMvcConfig.java
│   │   └── RedisConfig.java
│   ├── entity
│   │   └── ... (공통 엔티티)
│   └── repository
│       └── ... (공통 레포지토리)
├── auth
│   ├── controller
│   │    └── ...
│   ├── service
│   │    └── ...
│   ├── dto
│   │    └── ...
│   ├── filter
│   │    └── JwtAuthenticationFilter.java
│   ├── security
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   ├── JwtAccessDeniedHandler.java
│   │    └── JwtTokenProvider.java
│    └── config
│        └── AuthWebSecurityConfig.java
│
└── student
     └── ... (도메인별 구조)
```

### 계층형 아키텍처 (Pragmatic Layered Architecture)

**구조**
- 최상위: `global` + 도메인 서비스(`auth`, `student` 등)
- `global`: 순환 참조 방지를 위한 **공통 데이터 계층** 통합 관리  
  (`entity`, `repository`, `api`, `config`)
- 서비스 계층: 도메인별 비즈니스 로직 집중  
  (`controller`, `service`, `dto`, `config`)
- 각 서비스 계층은 global 에 의존

**요청 흐름**

```
Controller → Service → [Global]JPA Repository → [Global]Entity
```

---

### 응답 구조 표준화 (Envelope Pattern)

**핵심 철학**: 모든 응답은 **봉투(Envelope)** 로 감싸 전달  
→ 메타데이터는 봉투 겉표지, 실제 내용물은 성공/실패에 따라 분기

```java
// 1. 상위 인터페이스: 메타데이터만을 강제하는 최소 계약
public interface ResponseEnvelope {
    ResponseMeta getMeta(); // 모든 응답은 메타데이터 필수 포함
}

// 2. 단일 성공 봉투: 단일 데이터와 메타데이터
@Getter
public class SingleSuccessResponseEnvelope<T> implements ResponseEnvelope {
    private final T data;
    private final ResponseMeta meta; // 상위 인터페이스 구현

    private SingleSuccessResponseEnvelope(T data) {
        this.data = data;
        this.meta = MetaGenerator.auto(); // CustomResponseMetaContextHolder에서 자동 주입
    }

    public static <T> SingleSuccessResponseEnvelope<T> of(T data) { ... }
    public static SingleSuccessResponseEnvelope<Void> empty() { ... }
}

// 3. 페이징 성공 봉투: 리스트 데이터와 페이지 메타데이터
@Getter
public class PagedSuccessResponseEnvelope<T> implements ResponseEnvelope {
    private final List<T> data;
    private final PageMeta pageMeta;
    private final ResponseMeta meta;

    private PagedSuccessResponseEnvelope(List<T> data, PageMeta pageMeta) {
        this.data = data;
        this.pageMeta = pageMeta;
        this.meta = MetaGenerator.auto();
    }

    public static <T> PagedSuccessResponseEnvelope<T> from(Page<T> page) { ... }
}

// 4. 에러 봉투: 에러 상세 정보를 담음
@Getter
public class ErrorResponseEnvelope implements ResponseEnvelope {
    private final ErrorDetail error;
    private final ResponseMeta meta; // 상위 인터페이스 구현

    private ErrorResponseEnvelope(ErrorDetail error) {
        this.error = error;
        this.meta = MetaGenerator.auto(); // CustomResponseMetaContextHolder에서 자동 주입
    }

    public static ErrorResponseEnvelope of(ErrorCode code) { ... }
    public static ErrorResponseEnvelope of(ErrorCode code, Object details) { ... }
}
```

**클라이언트 관점 응답 구조**

```json
// ✅ 단일 성공 (SingleSuccessResponseEnvelope)
{
  "data": { "id": 1, "name": "홍길동" },
  "meta": { "requestId": "req-123", "timestamp": "...", "durationMs": 45 }
}

// ✅ 페이징 성공 (PagedSuccessResponseEnvelope)
{
  "data": [ { "id": 1, "name": "홍길동" }, ... ],
  "pageMeta": { "page": 1, "size": 10, "totalElements": 100, ... },
  "meta": { "requestId": "req-123", "timestamp": "...", "durationMs": 45 }
}

// ❌ 실패 (ErrorResponseEnvelope)
{
  "error": {
    "code": "STUDENT_COURSE_NOT_FOUND",
    "message": "수강 정보를 찾을 수 없습니다"
  },
  "meta": { "requestId": "req-123", "timestamp": "...", "durationMs": 12 }
}
```

**이점**
- `meta` 필드는 **모든 응답에서 동일한 위치**에 존재 → 추적 일관성 보장
- `data` vs `error` 필드 유무로 성공/실패 즉시 판별
- 프론트엔드는 `ResponseEnvelope` 인터페이스만으로 응답 구조를 추론 가능

---

### 보안 예외 인라인 처리 (JWT 사각지대 해결)

**문제**: Spring Security 필터 체인 예외는 `@RestControllerAdvice` 도달 전 발생  
**해결**: 필터 레벨에서 직접 `ErrorResponseEnvelope` 생성 후 직렬화

```java
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
        ErrorCode code = resolveErrorCode(authException); // EXPIRED_TOKEN 등 매핑
        ErrorResponseEnvelope error = ErrorResponseEnvelope.of(code);
        
        response.setStatus(code.getStatus().value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
```

**결과**: 인증 오류도 일반 비즈니스 오류와 **동일한 봉투 구조**로 전달
>`@RestControllerAdvice` 에 걸리지만 valid 같은 부분은 헨들러에서 적절히 처리
---

### 메타데이터 자동화 (Zero-Boilerplate)

**핵심**: 비즈니스 로직에서 인프라 관심사 완전 분리  
→ **`CustomResponseMetaContextHolder`** 가 모든 메타데이터를 **통합 관리**

---

#### 핵심 클래스 3개

| 클래스 | 책임 |
|--------|------|
| **`CustomResponseMetaContextHolder`** | ThreadLocal 기반 메타데이터 저장소 |
| **`GlobalMetaFilter`** | 요청 진입 시 메타데이터 설정 + 종료 시 정리 |
| **`MetaGenerator`** | Envelope 생성 시 자동 주입 |

---

#### `CustomResponseMetaContextHolder.java` (in `support` package)

```java
public class CustomResponseMetaContextHolder {
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> API_VERSION = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private static final ThreadLocal<ClientInfo> CLIENT_INFO = new ThreadLocal<>();

    // Getter / Setter
    public static void setRequestId(String id) { REQUEST_ID.set(id); }
    public static String getRequestId() { return REQUEST_ID.get(); }
    // ... (나머지 getter/setter)
    
    // 메모리 누수 방지
    public static void clear() {
        REQUEST_ID.remove();
        API_VERSION.remove();
        START_TIME.remove();
        CLIENT_INFO.remove();
    }
}
```

---

#### `GlobalMetaFilter.java` (in `filter` package)

```java
@Component
@Order(1)
@RequiredArgsConstructor
public class GlobalMetaFilter implements Filter {
    private final ClientInfoExtractor clientInfoExtractor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // [1] 메타데이터 설정
            CustomResponseMetaContextHolder.setRequestId(UUID.randomUUID().toString());
            CustomResponseMetaContextHolder.setApiVersion(extractApiVersion(httpRequest));
            CustomResponseMetaContextHolder.setStartTime(System.currentTimeMillis());
            CustomResponseMetaContextHolder.setClientInfo(clientInfoExtractor.extract(httpRequest));
            
            // [2] 비즈니스 로직 실행
            chain.doFilter(request, response);
            
        } finally {
            // [3] 메모리 누수 방지
            CustomResponseMetaContextHolder.clear();
        }
    }
    // ... extractApiVersion ...
}
```

---

#### `MetaGenerator.java` (in `support` package)

```java
public class MetaGenerator {
    public static ResponseMeta auto() {
        String requestId = CustomResponseMetaContextHolder.getRequestId();
        // ... (메타데이터 조회)
        
        return new ResponseMeta(
            requestId,
            // ...
        );
    }
}
```

---

#### Envelope에서 자동 주입

```java
public class SingleSuccessResponseEnvelope<T> implements ResponseEnvelope {
    private final T data;
    private final ResponseMeta meta; // ← 자동 주입

    private SingleSuccessResponseEnvelope(T data) {
        this.data = data;
        this.meta = MetaGenerator.auto(); // ThreadLocal에서 자동 조회
    }
}
```

---

## 흐름 요약

```
1. 필터 진입 → ThreadLocal 설정 (ID, Version, Time, Client)
   ↓
2. 비즈니스 로직 실행 (Controller → Service)
   ↓
3. Envelope 생성 → MetaGenerator.auto()로 자동 주입
   ↓
4. finally 블록 → ThreadLocal.clear() (메모리 누수 방지)
```

---

### 핵심 가치

| 항목         | 설명                                             |
| ---------- | ---------------------------------------------- |
| **단일 관리자** | `CustomResponseMetaContextHolder`가 모든 메타데이터 통합 |
| **자동 주입**  | `MetaGenerator.auto()`가 투명하게 처리                |
| **메모리 안전** | `finally` 블록에서 반드시 정리                          |
| **개발자 경험** | 비즈니스 로직에서 메타데이터 신경 불필요                         |

> **개발자는 `meta` 필드를 전혀 신경 쓸 필요 없습니다.**  
> 인프라 계층이 투명하게 모든 것을 처리합니다.
---

### 예외 처리 체계 (3요소 통합)

**1. ErrorCode (단일 전역 Enum)**  
→ 에러 코드 중복 방지 + 관리 효율성 ↑

```java
public enum ErrorCode implements BaseErrorCode {
    // 전역
    GLOBAL_INTERNAL_SERVER_ERROR(500, "GLOBAL_001", "서버 내부 오류");
    
    // 인증/인가 (auth.security 측)
    EXPIRED_TOKEN(401, "AUTH_001", "토큰 만료"),
    INVALID_TOKEN(401, "AUTH_002", "유효하지 않은 토큰"),
    
    // auth 
    
    // student
    STUDENT_COURSE_NOT_FOUND(404, "STUDENT_001", "수강 정보 없음"),
    
    // ...
    
    private final int status;
    private final String code;
    private final String message;
}
```

**2. BaseException (선언적 예외 발생)**  

```java
throw new BaseException(ErrorCode.STUDENT_COURSE_NOT_FOUND);
```

**3. GlobalExceptionHandler (자동 변환)**  

```java
@ExceptionHandler(BaseException.class)
public ResponseEntity<ErrorResponseEnvelope> handle(BaseException ex) {
    return ResponseEntity
        .status(ex.getErrorCode().getStatus())
        .body(ErrorResponseEnvelope.of(ex.getErrorCode()));
}
```

---

### 문서화 철학 (Controller = Specification)

**핵심**: 코드 수정 = 문서 수정 (동기화 100% 보장)

```java
@GetMapping("/courses/{courseId}/students")
@Operation(
    summary = "강의별 수강생 목록 조회",
    description = "특정 강의를 수강하는 학생 목록을 페이징 조회합니다.",
    responses = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    }
)
@OperationErrorCodes({
    ErrorCode.STUDENT_COURSE_NOT_FOUND,
    ErrorCode.EXPIRED_TOKEN,
    ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
})
public ResponseEntity<PagedSuccessResponseEnvelope<StudentResponse>> getStudents(
    @PathVariable Long courseId,
    Pageable pageable
) {
    Page<StudentResponse> page = studentService.getStudentsByCourse(courseId, pageable);
    return ResponseEntity.ok(PagedSuccessResponseEnvelope.from(page));
}
```

**자동화 흐름**
1. `@OperationErrorCodes`로 발생 가능한 에러 선언
2. `ErrorResponsesOperationCustomizer`가 스캔하여 Swagger UI에 **실제 에러 응답 예시 자동 생성**
3. 문서 누락/불일치 제로화

---

### 클래스 계층 구조 요약

```
ResponseEnvelope (Interface)
├── SingleSuccessResponseEnvelope<T> // data + meta
├── PagedSuccessResponseEnvelope<T>  // data + pageMeta + meta
└── ErrorResponseEnvelope            // error + meta
```

- **ResponseEnvelope**: 메타데이터만을 정의하는 최상위 인터페이스
- **SingleSuccessResponseEnvelope**: 단일 데이터(또는 단순 리스트)를 담는 구체 클래스
- **PagedSuccessResponseEnvelope**: 페이징된 데이터와 페이지 정보를 담는 구체 클래스
- **ErrorResponseEnvelope**: 실패 시 에러 상세를 담는 구체 클래스

---

### 아키텍처 핵심 가치

| 축 | 해결한 문제 | 개발자 경험 |
|----|-------------|-------------|
| **봉투 계층화** | 응답 구조 불일치 | "응답 고민 불필요" |
| **메타 자동화** | 보일러플레이트 코드 | "비즈니스만 집중" |
| **예외 체계화** | 에러 정의 분산 | "한 줄로 표준화" |
| **문서 자동화** | 코드-문서 불일치 | "코드 수정 = 문서 수정" |

> 이 아키텍처는 **"개발자가 인프라를 신경 쓰지 않아도 되는 환경"** 을 목표로 합니다.  
> 메타데이터, 에러 처리, 문서화는 인프라 계층이 투명하게 처리하고,  
> 개발자는 오직 **도메인 로직**에만 집중할 수 있습니다.