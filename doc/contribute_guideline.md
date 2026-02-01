# 새로운 서비스 추가 가이드라인

## 📋 목차

1. [사전 준비](#사전-준비)
2. [폴더 구조 생성](#폴더-구조-생성)
3. [핵심 구성 요소 구현](#핵심-구성-요소-구현)
4. [공통 인프라 연동](#공통-인프라-연동)
5. [테스트 전략](#테스트-전략)
6. [체크리스트](#체크리스트)

---

## 사전 준비

### 1. 도메인 명명 규칙

```
도메인명: 소문자, 복수형 금지, 명사 중심
예: ✅ course, ✅ enrollment, ✅ notification
    ❌ courses, ❌ enrollment-service, ❌ notificationHandler
```

### 2. 패키지 경로 결정

```
src/main/java/kr/mmv/mjusugangsincheonghelper/{도메인명}
```

---

## 폴더 구조 생성

### 기본 구조 템플릿

```bash
{도메인명}/
├── controller/
│   └── {도메인명}Controller.java (선택)
├── service/
│   ├── {도메인명}Service.java
│   └── {도메인명}ServiceImpl.java
├── repository/
│   └── {도메인명}Repository.java
├── entity/
│   └── {도메인명}.java
├── dto/
│   ├── request/
│   │   └── {도메인명}Request.java
│   └── response/
│   │   └── {도메인명}Response.java
└── config/
    └── {도메인명}Config.java (선택)
```

> **참고**: Exception 및 ErrorCode는 `global` 패키지에서 통합 관리합니다. 별도의 exception 패키지를 생성하지 마세요.

### 실제 예시: `course` 도메인 추가

```bash
course/
├── controller/
│   ├── CourseController.java
│   └── CourseControllerAdvice.java
├── service/
│   ├── CourseService.java
│   └── CourseServiceImpl.java
├── repository/
│   └── CourseRepository.java
├── entity/
│   └── Course.java
├── dto/
│   ├── request/
│   │   ├── CreateCourseRequest.java
│   │   └── UpdateCourseRequest.java
│   └── response/
│   │   └── CourseResponse.java
└── config/
    └── CourseConfig.java
```

---

## 핵심 구성 요소 구현

### 1. Entity 구현

```java
// course/entity/Course.java
@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer credits;
    
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### 2. Repository 구현

```java
// course/repository/CourseRepository.java
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    @EntityGraph(attributePaths = {"professor", "students"})
    Optional<Course> findByIdWithDetails(Long id);
    
    Page<Course> findByProfessorId(Long professorId, Pageable pageable);
    
    boolean existsByTitle(String title);
}
```

### 3. DTO 구현

```java
// course/dto/request/CreateCourseRequest.java
@Getter
@NoArgsConstructor
public class CreateCourseRequest {
    
    @NotBlank(message = "강의명은 필수입니다")
    @Size(max = 200, message = "강의명은 200자 이내여야 합니다")
    private String title;
    
    @NotBlank(message = "설명은 필수입니다")
    @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
    private String description;
    
    @NotNull(message = "학점은 필수입니다")
    @Min(value = 1, message = "학점은 최소 1학점 이상이어야 합니다")
    @Max(value = 6, message = "학점은 최대 6학점 이하이어야 합니다")
    private Integer credits;
    
    @Builder
    public CreateCourseRequest(String title, String description, Integer credits) {
        this.title = title;
        this.description = description;
        this.credits = credits;
    }
}

// course/dto/response/CourseResponse.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    
    private Long id;
    private String title;
    private String description;
    private Integer credits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .credits(course.getCredits())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }
}
```

### 4. Service 구현

```java
// course/service/CourseService.java
public interface CourseService {
    
    CourseResponse createCourse(CreateCourseRequest request);
    
    CourseResponse getCourseById(Long id);
    
    Page<CourseResponse> getCoursesByProfessor(Long professorId, Pageable pageable);
    
    CourseResponse updateCourse(Long id, UpdateCourseRequest request);
    
    void deleteCourse(Long id);
}

// course/service/CourseServiceImpl.java
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {
    
    private final CourseRepository courseRepository;
    
    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        validateDuplicateTitle(request.getTitle());
        
        Course course = Course.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .credits(request.getCredits())
            .build();
        
        Course savedCourse = courseRepository.save(course);
        log.info("강의 생성 완료: id={}, title={}", savedCourse.getId(), savedCourse.getTitle());
        
        return CourseResponse.from(savedCourse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new BaseException(ErrorCode.COURSE_NOT_FOUND));
        
        return CourseResponse.from(course);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursesByProfessor(Long professorId, Pageable pageable) {
        Page<Course> courses = courseRepository.findByProfessorId(professorId, pageable);
        return courses.map(CourseResponse::from);
    }
    
    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new BaseException(ErrorCode.COURSE_NOT_FOUND));
        
        if (!course.getTitle().equals(request.getTitle())) {
            validateDuplicateTitle(request.getTitle());
        }
        
        course.update(request.getTitle(), request.getDescription(), request.getCredits());
        return CourseResponse.from(course);
    }
    
    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new BaseException(ErrorCode.COURSE_NOT_FOUND));
        
        courseRepository.delete(course);
        log.info("강의 삭제 완료: id={}", id);
    }
    
    private void validateDuplicateTitle(String title) {
        if (courseRepository.existsByTitle(title)) {
            throw new BaseException(ErrorCode.COURSE_TITLE_DUPLICATE);
        }
    }
}
```

### 5. Controller 구현

```java
// course/controller/CourseController.java
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    @Operation(
        summary = "강의 생성",
        description = "새로운 강의를 생성합니다."
    )
    @OperationErrorCodes({
        ErrorCode.COURSE_TITLE_DUPLICATE,
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<CourseResponse>> createCourse(
        @Valid @RequestBody CreateCourseRequest request
    ) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(SingleSuccessResponseEnvelope.of(response));
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "강의 상세 조회",
        description = "강의 ID로 강의 상세 정보를 조회합니다."
    )
    @OperationErrorCodes({
        ErrorCode.COURSE_NOT_FOUND,
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<CourseResponse>> getCourseById(
        @PathVariable Long id
    ) {
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }
    
    @GetMapping("/professor/{professorId}")
    @Operation(
        summary = "교수별 강의 목록 조회",
        description = "특정 교수의 강의 목록을 페이징 조회합니다."
    )
    @OperationErrorCodes({
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<PagedSuccessResponseEnvelope<CourseResponse>> getCoursesByProfessor(
        @PathVariable Long professorId,
        Pageable pageable
    ) {
        Page<CourseResponse> page = courseService.getCoursesByProfessor(professorId, pageable);
        return ResponseEntity.ok(PagedSuccessResponseEnvelope.from(page));
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "강의 수정",
        description = "강의 정보를 수정합니다."
    )
    @OperationErrorCodes({
        ErrorCode.COURSE_NOT_FOUND,
        ErrorCode.COURSE_TITLE_DUPLICATE,
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<CourseResponse>> updateCourse(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCourseRequest request
    ) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "강의 삭제",
        description = "강의를 삭제합니다."
    )
    @OperationErrorCodes({
        ErrorCode.COURSE_NOT_FOUND,
        ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
    })
    public ResponseEntity<SingleSuccessResponseEnvelope<Void>> deleteCourse(
        @PathVariable Long id
    ) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(SingleSuccessResponseEnvelope.empty());
    }
}
```

### 6. Error Code 등록

새로운 도메인에 대한 에러 코드는 `global/api/code/ErrorCode.java` Enum에 직접 추가합니다.

```java
// global/api/code/ErrorCode.java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // ... 기존 코드들 ...

    // ===== Course (강의) =====
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_001", "강의를 찾을 수 없습니다"),
    COURSE_TITLE_DUPLICATE(HttpStatus.CONFLICT, "COURSE_002", "이미 존재하는 강의명입니다"),
    COURSE_PROFESSOR_REQUIRED(HttpStatus.BAD_REQUEST, "COURSE_003", "담당 교수가 필요합니다");
    
    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

---

## 공통 인프라 연동

### 1. 메타데이터 자동화 활용

```java
// ✅ 단일 객체 응답 (메타데이터 자동 포함)
return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));

// ✅ 페이징 응답 (페이지 메타데이터 자동 포함)
return ResponseEntity.ok(PagedSuccessResponseEnvelope.from(page));
```

### 2. 예외 처리 통합

```java
// ✅ BaseException 사용 (자동으로 ErrorResponseEnvelope로 변환)
throw new BaseException(ErrorCode.COURSE_NOT_FOUND);

// ✅ 커스텀 예외도 BaseException 상속 가능하지만, 단순히 ErrorCode만 추가하는 것을 권장
```

### 3. 문서화 자동화 적용

```java
@OperationErrorCodes({
    ErrorCode.COURSE_NOT_FOUND,  // Swagger에 자동으로 에러 응답 예시 생성
    ErrorCode.COURSE_TITLE_DUPLICATE,
    ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
})
```

### 4. 전역 필터 자동 적용

```java
// ✅ GlobalMetaFilter가 자동으로 모든 요청에 적용됨
// - Request ID 자동 생성
// - API Version 추출
// - 응답 시간 측정
// - Client 정보 추출
// 개발자가 따로 설정할 필요 없음
```

---

## 테스트 전략

### 1. Service 테스트

```java
// course/service/CourseServiceTest.java
@SpringBootTest
@Transactional
class CourseServiceTest {
    
    @Autowired
    private CourseService courseService;
    
    @Test
    void 강의_생성_성공() {
        // ... (생략) ...
    }
    
    @Test
    void 중복된_강의명으로_생성_실패() {
        // given
        // ... (생략) ...
        
        // when & then
        assertThatThrownBy(() -> courseService.createCourse(request2))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("이미 존재하는 강의명입니다");
    }
}
```

### 2. Controller 테스트

```java
// course/controller/CourseControllerTest.java
@WebMvcTest(CourseController.class)
class CourseControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CourseService courseService;
    
    @Test
    void 강의_생성_API_성공() throws Exception {
        // given
        // ... (생략) ...
        
        given(courseService.createCourse(any())).willReturn(response);
        
        // when & then
        mockMvc.perform(post("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value("자바 프로그래밍"))
            .andExpect(jsonPath("$.meta.requestId").exists()) // Meta 정보 확인
            .andDo(document("course-create"));
    }
    
    @Test
    void 존재하지_않는_강의_조회_실패() throws Exception {
        // given
        given(courseService.getCourseById(999L))
            .willThrow(new BaseException(ErrorCode.COURSE_NOT_FOUND));
        
        // when & then
        mockMvc.perform(get("/api/v1/courses/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("COURSE_001")) // ErrorCode 확인
            .andExpect(jsonPath("$.error.message").value("강의를 찾을 수 없습니다"))
            .andExpect(jsonPath("$.meta.requestId").exists());
    }
}
```

---

## 체크리스트

### 구조 확인

- [ ] 도메인명이 소문자 명사인지 확인
- [ ] 폴더 구조가 표준 템플릿을 따르는지 확인
- [ ] 각 계층별 패키지가 올바르게 구성되었는지 확인

### 코드 품질

- [ ] Entity에 `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` 적용
- [ ] Repository 메서드 명명 규칙 준수 (findByXxx, existsByXxx)
- [ ] Service 인터페이스와 구현체 분리
- [ ] DTO에 정적 팩토리 메서드 (`from()`) 구현
- [ ] `@Transactional` 어노테이션 적절히 적용

### 공통 인프라 연동

- [ ] `SingleSuccessResponseEnvelope` 또는 `PagedSuccessResponseEnvelope` 사용 (리턴 타입 확인)
- [ ] `BaseException` 사용 및 `global/api/code/ErrorCode.java`에 에러 코드 등록
- [ ] `@OperationErrorCodes` 어노테이션으로 에러 코드 문서화
- [ ] `@Valid` 어노테이션으로 요청 검증
- [ ] 로깅 (`@Slf4j`) 적용

### 예외 처리

- [ ] `ErrorCode` Enum에 도메인별 에러 코드 추가
- [ ] 모든 비즈니스 예외가 `BaseException` 사용
- [ ] 예외 메시지가 사용자 친화적인지 확인

### 테스트

- [ ] Service 레벨 테스트 작성
- [ ] Controller 레벨 테스트 작성
- [ ] 주요 비즈니스 로직 커버리지 확보
- [ ] 예외 상황 테스트 포함

### 문서화

- [ ] `@Operation` 어노테이션으로 API 설명 추가
- [ ] `@OperationErrorCodes`로 발생 가능한 에러 명시
- [ ] 복잡한 비즈니스 로직에 JavaDoc 추가

---

## 모범 사례

### ✅ 좋은 예

```java
// 1. 응답 봉투 자동화 활용
return ResponseEntity.ok(SingleSuccessResponseEnvelope.of(response));

// 2. 예외 처리 표준화
throw new BaseException(ErrorCode.COURSE_NOT_FOUND);

// 3. 문서화 자동화
@OperationErrorCodes({
    ErrorCode.COURSE_NOT_FOUND,
    ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR
})

// 4. 메타데이터 신경 안 씀
// ✅ 자동으로 포함됨 - 개발자 부담 제로
```

### ❌ 피해야 할 예

```java
// 1. 수동 응답 생성 (❌)
Map<String, Object> response = new HashMap<>();
response.put("data", course);
response.put("meta", new ResponseMeta(...)); // ❌ 직접 생성

// 2. 일반 예외 사용 (❌)
throw new RuntimeException("강의 없음"); // ❌ 비표준

// 3. 문서 누락 (❌)
@GetMapping("/{id}")
public ResponseEntity<?> getCourse(Long id) { // ❌ @Operation 없음
    // ...
}

// 4. 메타데이터 직접 관리 (❌)
CustomResponseMetaContextHolder.setRequestId(...); // ❌ 불필요
```

---

## 요약

**핵심 원칙**: "인프라 신경 쓰지 말고 도메인에 집중하라"

1. **표준 구조**를 따르면 자동으로 공통 인프라와 연동됨
2. **응답 봉투**는 `Single` 또는 `Paged` Envelope 사용
3. **예외 처리**는 `ErrorCode` Enum 등록 후 `BaseException` 사용
4. **문서화**는 어노테이션으로 자동화
5. **테스트**는 각 계층별로 분리하여 작성

이 가이드라인을 따르면 새로운 서비스를 기존 아키텍처와 완벽하게 융합할 수 있습니다.