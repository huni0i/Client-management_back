# JWT 키 발급 가이드

## 1. JWT Secret Key 생성 (서명용 비밀키)

JWT Secret Key는 토큰을 서명하고 검증하는데 사용하는 비밀키입니다. **한 번 생성하여 설정 파일에 저장**합니다.

### 생성 방법

#### 방법 1: OpenSSL 사용 (권장)

```bash
# Base64 형식 (32바이트)
openssl rand -base64 32

# Hex 형식 (32바이트)
openssl rand -hex 32
```

#### 방법 2: 온라인 도구 사용
- https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
- 최소 32자 이상의 랜덤 문자열 생성

#### 방법 3: Java 코드로 생성

```java
import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 32바이트 = 256비트
        random.nextBytes(bytes);
        String secret = Base64.getEncoder().encodeToString(bytes);
        System.out.println("JWT Secret: " + secret);
    }
}
```

### 설정 방법

생성한 Secret Key를 환경 변수나 설정 파일에 설정:

```bash
# 환경 변수로 설정
export JWT_SECRET=생성된_키_여기에_입력

# 또는 .env 파일에 추가
echo "JWT_SECRET=생성된_키_여기에_입력" >> .env
```

## 2. JWT Token 발급 (사용자 인증 토큰)

JWT Token은 **회원가입 또는 로그인 시 자동으로 발급**됩니다.

### 발급 과정

1. **회원가입** (`POST /api/auth/signup`)
   - 사용자 정보 저장
   - JWT Token 자동 생성 및 반환

2. **로그인** (`POST /api/auth/login`)
   - 이메일/비밀번호 확인
   - JWT Token 자동 생성 및 반환

### 코드 흐름

```java
// AuthService.java
public AuthResponse signup(SignupRequest request) {
    // 1. 사용자 생성
    User user = User.builder()
        .userId(UUID.randomUUID().toString())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .userType(request.getUserTypeEnum())
        .build();
    
    userRepository.save(user);
    
    // 2. JWT Token 생성
    String token = jwtUtil.generateToken(user.getUserId(), user.getUserType().name());
    
    // 3. Token과 함께 응답 반환
    return AuthResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .userType(user.getUserType().name())
        .token(token)  // ← 여기서 JWT Token 반환
        .build();
}
```

### JWT Token 생성 로직

```java
// JwtUtil.java
public String generateToken(String userId, String userType) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userType", userType);
    
    return Jwts.builder()
        .claims(claims)  // 사용자 타입 정보
        .subject(userId)  // 사용자 ID
        .issuedAt(new Date(System.currentTimeMillis()))  // 발급 시간
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))  // 만료 시간 (24시간)
        .signWith(getSigningKey())  // Secret Key로 서명
        .compact();
}
```

## 3. 실제 사용 예시

### 회원가입으로 Token 받기

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "홍길동",
    "userType": "counselor"
  }'
```

**응답:**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": "user123",
    "email": "user@example.com",
    "name": "홍길동",
    "userType": "counselor",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // ← JWT Token
  }
}
```

### 로그인으로 Token 받기

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**응답:**
```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "userId": "user123",
    "email": "user@example.com",
    "name": "홍길동",
    "userType": "counselor",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // ← JWT Token
  }
}
```

### Token 사용하기

받은 Token을 헤더에 포함하여 API 호출:

```bash
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 4. Token 구조

JWT Token은 3부분으로 구성됩니다:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

1. **Header** (헤더): 토큰 타입과 암호화 알고리즘
2. **Payload** (페이로드): 사용자 정보 (userId, userType 등)
3. **Signature** (서명): Secret Key로 서명한 값

## 5. Token 만료 시간

기본 설정: **24시간** (86400000 밀리초)

설정 변경:
```yaml
# application.yml
jwt:
  expiration: 86400000  # 밀리초 단위
```

또는 환경 변수:
```bash
export JWT_EXPIRATION=86400000  # 24시간
export JWT_EXPIRATION=3600000   # 1시간
export JWT_EXPIRATION=604800000 # 7일
```

## 요약

1. **JWT Secret Key**: 한 번 생성하여 설정 파일에 저장 (토큰 서명용)
2. **JWT Token**: 회원가입/로그인 시 자동 발급 (사용자 인증용)
3. **Token 사용**: API 호출 시 `Authorization: Bearer {token}` 헤더에 포함

