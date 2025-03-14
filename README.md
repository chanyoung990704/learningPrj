# Spring Boot 학습 게시판 프로젝트

이 프로젝트는 Spring Boot 기반의 커뮤니티 플랫폼으로, 게시글, 댓글, 파일 업로드 및 사용자 관리 기능을 제공합니다. 백엔드 개발 역량을 보여주기 위해 다양한 Spring 생태계 기술과 디자인 패턴을 적용했습니다.

#### 테스트 서버 : http://54.163.62.58:8080/ (EC2 FreeTier)

## 주요 기능

- **사용자 관리**: 회원가입, 로그인, 권한 관리
- **게시글 관리**: 글 작성, 수정, 삭제, 검색, 페이징
- **댓글 관리**: 댓글 작성, 수정, 삭제
- **파일 관리**: 이미지 및 첨부파일 업로드, 다운로드
- **카테고리 관리**: 계층형 카테고리 구조

## 기술 스택

### 백엔드
- **Java 17**
- **Spring Boot 3.1.0**
- **Spring Data JPA**: 데이터 접근 계층
- **QueryDSL**: 타입 안전한 쿼리 작성
- **Spring Security**: 인증 및 권한 관리
- **Lombok**: 보일러플레이트 코드 감소
- **H2 Database/MySQL**: 개발 및 프로덕션 환경
- **JUnit 5 & Mockito**: 테스트 프레임워크

### 프론트엔드
- **Thymeleaf**: 서버 사이드 템플릿 엔진
- **Bootstrap**: UI 디자인

## 아키텍처 및 디자인 패턴

### 계층 구조
- **Controller Layer**: 클라이언트 요청 처리
- **Service Layer**: 비즈니스 로직 처리
- **Repository Layer**: 데이터 액세스
- **Domain Layer**: 도메인 모델 정의

### 주요 디자인 패턴
- **MVC 패턴**: Model-View-Controller 구조
- **DTO 패턴**: 계층 간 데이터 전송
- **Repository 패턴**: 데이터 접근 추상화
- **도메인 주도 설계**: 풍부한 도메인 모델과 비즈니스 로직 캡슐화
- **이벤트 기반 아키텍처**: 회원가입 시 이메일 발송 등의 비동기 처리

## 고급 기능 구현

### AOP(관점 지향 프로그래밍)
- **@TimeTrace**: 메소드 실행 시간 측정
- **@VerifyPostOwner**: 게시글 소유자 검증
- **@VerifyCommentOwner**: 댓글 소유자 검증

### 비동기 처리
- 이메일 발송을 위한 별도의 ThreadPool 구성
- Spring Events를 통한 비동기 이벤트 처리

### 캐싱
- 카테고리 정보 캐싱으로 성능 최적화

### RESTful API
- 표준 HTTP 메서드 및 상태 코드 준수
- API 응답 일관성 유지를 위한 래퍼 클래스 구현

### 보안
- Spring Security를 활용한 인증 및 권한 관리
- 비밀번호 암호화 (BCrypt)
- CSRF 보호

### 예외 처리
- 글로벌 예외 핸들러 구현
- API 예외 응답 표준화

## REST API 구현

게시글 관리를 위한 REST API를 구현하여 프론트엔드와의 분리 및 다양한 클라이언트 지원이 가능합니다.

```
GET /api/posts - 게시글 목록 조회
POST /api/posts - 게시글 생성
GET /api/posts/{id} - 특정 게시글 조회
PUT /api/posts/{id} - 게시글 수정
DELETE /api/posts/{id} - 게시글 삭제
```

## 테스트

- **단위 테스트**: 각 컴포넌트의 독립적인 기능 검증
- **통합 테스트**: 컴포넌트 간 상호작용 검증
- **API 테스트**: REST API 엔드포인트 검증


## 프로젝트 구조

```
src
├── main
│   ├── java
│   │   └── org.example.demo
│   │       ├── aop             # AOP 관련 클래스
│   │       ├── api             # REST API 컨트롤러
│   │       ├── config          # 애플리케이션 설정
│   │       ├── controller      # 웹 컨트롤러
│   │       ├── domain          # 도메인 모델
│   │       ├── dto             # 데이터 전송 객체
│   │       ├── events          # 이벤트 클래스
│   │       ├── exception       # 예외 처리
│   │       ├── repository      # 데이터 액세스
│   │       ├── service         # 비즈니스 로직
│   │       └── validator       # 유효성 검증
│   └── resources
│       ├── static              # 정적 리소스
│       └── templates           # 템플릿 파일
└── test                        # 테스트 코드
```

## 향후 개선 사항

- 소셜 로그인 통합 (OAuth2)
- API 문서화 (Swagger/OpenAPI)
- 실시간 알림 기능 (WebSocket)
- SpringAI 게시판 통합
- 마이크로서비스 아키텍처로 전환
- 컨테이너화 및 CI/CD 파이프라인 구축

## 연락처

이 프로젝트에 대한 문의나 제안이 있으시면 아래로 연락해주세요:
- 이메일: chanyoung990704@naver.com

---
