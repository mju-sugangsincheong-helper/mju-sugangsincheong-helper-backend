
### 프로젝트 구조 및 철학
doc/basic_architecture.md

### 기여 가이드
doc/contribute_guideline.md

### 개발환경 시작
```bash
bash dev_env_start.sh
or
zsh dev_env_start.sh

-> 

spring start


->
api 테스트
http://localhost:8080/swagger-ui/index.html
```



### 기본 브랜치 dev
기본 브랜치는 dev 로 하며
main 은 안정 버전
향후 stable 을 배포 버전으로 한다
긱 인원은 닉네임으로 브랜치를 따로 dev 에 머지 한다

### TODO
테스트 코드를 만들어야 하며 단위 테스트 보다는 통합 테스트를 목표로 합니다 (testcontainer 모듈 사용해서 진행해야 합니다)
test 프로파일에서 진행되며 test 모듈 실행시에 test 프로파일로 로드해야 합니다

### 고민할 지점
1. database 쿼리 최적화를 위해 view 접근 vs index 접근 무었을 주 전략으로 해야 하는가
2. database, redis 와는 몇개의 연결을 유지하는것이 좋을까?
3. 
