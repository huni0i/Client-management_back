# 프론트엔드 API 연동 가이드

백엔드에 구현된 모든 API 엔드포인트와 사용 방법을 정리한 문서입니다.

## 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json`

## 공통 응답 형식

모든 API는 다음 형식의 응답을 반환합니다:

```typescript
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}
```

## 인증 (Authentication)

### 1. 회원가입
- **엔드포인트**: `POST /api/auth/signup`
- **인증 필요**: ❌
- **요청 Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "사용자 이름",
  "userType": "counselor" | "client"
}
```
- **응답**: `ApiResponse<AuthResponse>`
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": "uuid",
    "email": "user@example.com",
    "name": "사용자 이름",
    "userType": "counselor",
    "token": "jwt_token_here"
  }
}
```

### 2. 로그인
- **엔드포인트**: `POST /api/auth/login`
- **인증 필요**: ❌
- **요청 Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **응답**: `ApiResponse<AuthResponse>` (회원가입과 동일)

### 3. 로그아웃 ⭐ (프론트엔드에 추가 필요)
- **엔드포인트**: `POST /api/auth/logout`
- **인증 필요**: ✅
- **요청 Headers**: `Authorization: Bearer {token}`
- **요청 Body**: 없음
- **응답**:
```json
{
  "success": true,
  "message": "로그아웃되었습니다.",
  "data": null
}
```

**프론트엔드 구현 예시:**
```typescript
// 로그아웃 함수
async function logout() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    const result = await response.json();
    
    if (result.success) {
      // 토큰 삭제
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // 로그인 페이지로 이동
      window.location.href = '/login';
    }
  } catch (error) {
    console.error('로그아웃 실패:', error);
  }
}
```

## 프로필 (Profile)

### 1. 프로필 조회
- **엔드포인트**: `GET /api/profile`
- **인증 필요**: ✅

### 2. 프로필 수정
- **엔드포인트**: `PUT /api/profile`
- **인증 필요**: ✅
- **요청 Body**:
```json
{
  "name": "수정할 이름"
}
```

## 상담방 (Rooms)

### 1. 상담방 생성
- **엔드포인트**: `POST /api/rooms`
- **인증 필요**: ✅
- **권한**: 상담사만
- **요청 Body**:
```json
{
  "name": "상담방 이름"
}
```

### 2. 상담방 목록 조회
- **엔드포인트**: `GET /api/rooms`
- **인증 필요**: ✅
- **응답**: `ApiResponse<List<RoomResponse>>`

### 3. 상담방 상세 조회
- **엔드포인트**: `GET /api/rooms/{roomId}`
- **인증 필요**: ✅
- **응답**: `ApiResponse<RoomResponse>`

### 4. 상담방 참가
- **엔드포인트**: `POST /api/rooms/join`
- **인증 필요**: ✅
- **권한**: 내담자만
- **요청 Body**:
```json
{
  "inviteCode": "ABC123"
}
```

### 5. 상담방 삭제 ⭐ (프론트엔드에 추가 필요)
- **엔드포인트**: `DELETE /api/rooms/{roomId}`
- **인증 필요**: ✅
- **권한**: 상담사만 (본인이 생성한 상담방만)
- **요청 Body**: 없음
- **응답**:
```json
{
  "success": true,
  "message": "상담방이 삭제되었습니다.",
  "data": null
}
```

**프론트엔드 구현 예시:**
```typescript
// 상담방 삭제 함수
async function deleteRoom(roomId: string) {
  if (!confirm('정말로 이 상담방을 삭제하시겠습니까?')) {
    return;
  }
  
  try {
    const response = await fetch(`http://localhost:8080/api/rooms/${roomId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    const result = await response.json();
    
    if (result.success) {
      alert('상담방이 삭제되었습니다.');
      // 상담방 목록 새로고침 또는 페이지 이동
      window.location.reload();
    } else {
      alert(result.message || '상담방 삭제에 실패했습니다.');
    }
  } catch (error) {
    console.error('상담방 삭제 실패:', error);
    alert('상담방 삭제 중 오류가 발생했습니다.');
  }
}
```

### 6. 상담방 나가기 ⭐ (프론트엔드에 추가 필요)
- **엔드포인트**: `DELETE /api/rooms/{roomId}/leave`
- **인증 필요**: ✅
- **권한**: 내담자만 (상담사는 나갈 수 없음)
- **요청 Body**: 없음
- **응답**:
```json
{
  "success": true,
  "message": "상담방에서 나갔습니다.",
  "data": null
}
```

**프론트엔드 구현 예시:**
```typescript
// 상담방 나가기 함수
async function leaveRoom(roomId: string) {
  if (!confirm('정말로 이 상담방에서 나가시겠습니까?')) {
    return;
  }
  
  try {
    const response = await fetch(`http://localhost:8080/api/rooms/${roomId}/leave`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      }
    });
    
    const result = await response.json();
    
    if (result.success) {
      alert('상담방에서 나갔습니다.');
      // 상담방 목록 새로고침 또는 페이지 이동
      window.location.reload();
    } else {
      alert(result.message || '상담방 나가기에 실패했습니다.');
    }
  } catch (error) {
    console.error('상담방 나가기 실패:', error);
    alert('상담방 나가기 중 오류가 발생했습니다.');
  }
}
```

## DBT 일기카드 (DBT Cards)

### 1. DBT 일기카드 작성/수정
- **엔드포인트**: `POST /api/rooms/{roomId}/dbt-cards`
- **인증 필요**: ✅
- **권한**: 내담자만

### 2. 내 DBT 일기카드 조회
- **엔드포인트**: `GET /api/rooms/{roomId}/dbt-cards/my`
- **인증 필요**: ✅
- **권한**: 내담자만
- **Query Parameters**: 
  - `date` (optional): `YYYY-MM-DD` 형식

### 3. 내담자별 DBT 일기카드 조회
- **엔드포인트**: `GET /api/rooms/{roomId}/dbt-cards`
- **인증 필요**: ✅
- **권한**: 상담사만
- **Query Parameters**: 
  - `date` (optional): `YYYY-MM-DD` 형식
  - `clientId` (optional): 내담자 ID

## 데이터 모델

### RoomResponse
```typescript
interface RoomResponse {
  roomId: string;
  name: string;
  inviteCode?: string;
  createdAt?: string; // ISO 8601 형식
  createdBy?: string; // userId
  clientCount?: number;
  joinedAt?: string; // 내담자일 경우만
  createdByInfo?: UserInfo; // 상세 조회 시
  clients?: ClientInfo[]; // 상세 조회 시
}
```

### UserInfo
```typescript
interface UserInfo {
  userId: string;
  name: string;
  email: string;
}
```

### ClientInfo
```typescript
interface ClientInfo {
  userId: string;
  name: string;
  email: string;
  joinedAt: string; // ISO 8601 형식
}
```

## 에러 처리

### 공통 에러 응답
```json
{
  "success": false,
  "message": "에러 메시지",
  "error": "ERROR_CODE"
}
```

### 주요 에러 코드
- `UNAUTHORIZED` (401): 인증되지 않은 요청
- `FORBIDDEN` (403): 권한이 없는 요청
- `ROOM_NOT_FOUND` (404): 상담방을 찾을 수 없음
- `INVALID_INVITE_CODE` (404): 유효하지 않은 초대코드
- `ALREADY_JOINED` (400): 이미 참가한 상담방
- `EMAIL_ALREADY_EXISTS` (400): 이미 존재하는 이메일
- `INVALID_CREDENTIALS` (401): 이메일 또는 비밀번호가 올바르지 않음

## 프론트엔드 구현 체크리스트

### 인증 관련
- [x] 회원가입
- [x] 로그인
- [ ] **로그아웃** ⭐ (추가 필요)

### 상담방 관련
- [x] 상담방 생성
- [x] 상담방 목록 조회
- [x] 상담방 상세 조회
- [x] 상담방 참가
- [ ] **상담방 삭제** ⭐ (추가 필요)
- [ ] **상담방 나가기** ⭐ (추가 필요)

### 프로필 관련
- [x] 프로필 조회
- [x] 프로필 수정

### DBT 일기카드 관련
- [x] DBT 일기카드 작성/수정
- [x] 내 DBT 일기카드 조회
- [x] 내담자별 DBT 일기카드 조회

## 인증 토큰 관리

### 토큰 저장
로그인/회원가입 후 받은 토큰을 저장:
```typescript
localStorage.setItem('token', response.data.token);
localStorage.setItem('user', JSON.stringify(response.data));
```

### API 요청 시 토큰 사용
모든 인증이 필요한 API 요청에 헤더 추가:
```typescript
headers: {
  'Authorization': `Bearer ${localStorage.getItem('token')}`,
  'Content-Type': 'application/json'
}
```

### 토큰 만료 처리
401 에러 발생 시 자동 로그아웃:
```typescript
if (response.status === 401) {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/login';
}
```

## Swagger UI

개발 중 API 테스트를 위해 Swagger UI를 사용할 수 있습니다:
- URL: `http://localhost:8080/swagger-ui.html`
- 모든 API 엔드포인트를 브라우저에서 직접 테스트 가능
- 인증 토큰을 입력하면 인증이 필요한 API도 테스트 가능

