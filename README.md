# 내담자 관리 서비스 API

Spring Boot 기반의 내담자 관리 서비스 백엔드 API입니다.

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- JWT (JSON Web Token)
- MySQL
- Swagger/OpenAPI 3.0
- Gradle

## 실행 방법

### 1. 데이터베이스 설정

MySQL 데이터베이스를 생성하고 `application.yml` 파일에서 연결 정보를 수정하세요.

```sql
CREATE DATABASE counseling_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 설정 파일 수정

#### 방법 1: 환경 변수 사용 (권장)

환경 변수로 설정:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your-jwt-secret-key-minimum-32-characters-long
```

#### 방법 2: application-local.yml 사용

1. `application-local.yml.example` 파일을 복사:
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

2. `application-local.yml` 파일을 편집하여 실제 값 입력

#### 방법 3: .env 파일 사용

1. `.env.example` 파일을 복사:
```bash
cp .env.example .env
```

2. `.env` 파일을 편집하여 실제 값 입력

자세한 설정 방법은 [SETUP.md](SETUP.md) 파일을 참고하세요.

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/counseling-service-1.0.0.jar
```

## API 문서

애플리케이션 실행 후 다음 URL에서 Swagger UI를 확인할 수 있습니다:

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## API 엔드포인트

### 인증
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

### 상담방
- `POST /api/rooms` - 상담방 생성 (상담사만)
- `GET /api/rooms` - 상담방 목록 조회
- `GET /api/rooms/{roomId}` - 상담방 상세 조회
- `POST /api/rooms/join` - 상담방 참가 (내담자만)

### DBT 일기카드
- `POST /api/rooms/{roomId}/dbt-cards` - DBT 일기카드 작성/수정
- `GET /api/rooms/{roomId}/dbt-cards/my` - 내 DBT 일기카드 조회 (내담자)
- `GET /api/rooms/{roomId}/dbt-cards` - 내담자별 DBT 일기카드 조회 (상담사)

### 프로필
- `GET /api/profile` - 프로필 조회
- `PUT /api/profile` - 프로필 수정

## 인증

대부분의 API는 JWT 토큰 인증이 필요합니다. 요청 헤더에 다음을 포함하세요:

```
Authorization: Bearer {token}
```

## 데이터베이스 스키마

애플리케이션 실행 시 JPA가 자동으로 테이블을 생성합니다 (`ddl-auto: update`).

주요 테이블:
- `users` - 사용자 정보
- `rooms` - 상담방 정보
- `room_members` - 상담방 멤버 정보
- `dbt_cards` - DBT 일기카드 정보

## JWT 키 발급

JWT 키 발급 방법에 대한 자세한 내용은 [JWT_GUIDE.md](JWT_GUIDE.md) 파일을 참고하세요.

**간단 요약:**
- **JWT Secret Key**: `openssl rand -base64 32` 명령어로 생성
- **JWT Token**: 회원가입/로그인 API 호출 시 자동 발급

## 주의사항

1. 프로덕션 환경에서는 반드시 JWT secret 키를 변경하세요.
2. 데이터베이스 연결 정보를 안전하게 관리하세요.
3. CORS 설정을 프로덕션 환경에 맞게 조정하세요.

