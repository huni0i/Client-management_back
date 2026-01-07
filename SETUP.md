# 설정 가이드

## 환경 변수 설정 방법

### 방법 1: 환경 변수로 설정 (권장)

#### macOS/Linux
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=counseling_db
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-jwt-secret-key-minimum-32-characters-long
export JWT_EXPIRATION=86400000
export SPRING_PROFILES_ACTIVE=local

./gradlew bootRun
```

#### Windows (PowerShell)
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="counseling_db"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-jwt-secret-key-minimum-32-characters-long"
$env:JWT_EXPIRATION="86400000"
$env:SPRING_PROFILES_ACTIVE="local"

./gradlew bootRun
```

### 방법 2: .env 파일 사용

1. `.env.example` 파일을 복사하여 `.env` 파일 생성:
```bash
cp .env.example .env
```

2. `.env` 파일을 편집하여 실제 값 입력

3. 애플리케이션 실행 시 환경 변수 로드:
```bash
# macOS/Linux (source 명령어 사용)
export $(cat .env | xargs)
./gradlew bootRun

# 또는 dotenv 플러그인 사용 (Gradle 플러그인 필요)
```

### 방법 3: application-local.yml 사용

1. `application-local.yml.example` 파일을 복사:
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

2. `application-local.yml` 파일을 편집하여 실제 값 입력

3. 애플리케이션 실행:
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## JWT Secret 키 생성

보안을 위해 최소 32자 이상의 랜덤 문자열을 사용하세요:

```bash
# macOS/Linux
openssl rand -base64 32

# 또는
openssl rand -hex 32
```

생성된 키를 환경 변수나 설정 파일에 설정하세요.

## 프로덕션 환경 설정

1. `application-prod.yml.example` 파일을 복사:
```bash
cp src/main/resources/application-prod.yml.example src/main/resources/application-prod.yml
```

2. **절대 실제 비밀번호를 파일에 직접 작성하지 마세요!**
   - 환경 변수로만 설정하거나
   - 시크릿 관리 시스템(AWS Secrets Manager, HashiCorp Vault 등) 사용

3. 프로덕션 실행:
```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## 필수 설정 항목

- `DB_PASSWORD`: 데이터베이스 비밀번호
- `JWT_SECRET`: JWT 서명 키 (최소 32자 이상)
- `DB_USERNAME`: 데이터베이스 사용자명 (기본값: root)

## 보안 주의사항

1. ✅ `.env`, `application-local.yml`, `application-prod.yml` 파일은 `.gitignore`에 포함되어 있습니다
2. ✅ 절대 실제 비밀번호를 Git에 커밋하지 마세요
3. ✅ 프로덕션 환경에서는 반드시 환경 변수나 시크릿 관리 시스템 사용
4. ✅ JWT Secret은 최소 32자 이상의 랜덤 문자열 사용

