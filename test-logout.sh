#!/bin/bash

# 로그아웃 API 테스트 스크립트

BASE_URL="http://localhost:8080/api"
echo "=== 로그아웃 API 테스트 시작 ==="
echo ""

# 1. 서버 상태 확인
echo "1. 서버 상태 확인..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api-docs)
if [ "$STATUS" != "200" ]; then
    echo "❌ 서버가 실행되지 않았습니다. (HTTP Status: $STATUS)"
    echo "애플리케이션을 먼저 실행하세요: ./run.sh"
    exit 1
fi
echo "✅ 서버가 정상적으로 실행 중입니다."
echo ""

# 2. 회원가입 테스트
echo "2. 회원가입 테스트..."
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "logout_test@test.com",
    "password": "password123",
    "name": "로그아웃 테스트 사용자",
    "userType": "client"
  }')

echo "$SIGNUP_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$SIGNUP_RESPONSE"
echo ""

# Token 추출
TOKEN=$(echo "$SIGNUP_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$TOKEN" ]; then
    echo "❌ Token을 받지 못했습니다."
    exit 1
fi

echo "✅ Token 받기 성공!"
echo "Token: ${TOKEN:0:50}..."
echo ""

# 3. 인증된 요청 테스트 (프로필 조회)
echo "3. 인증된 요청 테스트 (프로필 조회)..."
PROFILE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$PROFILE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$PROFILE_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 인증된 요청 성공!"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "❌ 인증된 요청 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
fi
echo ""

# 4. 로그아웃 테스트
echo "4. 로그아웃 테스트..."
LOGOUT_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/auth/logout" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$LOGOUT_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$LOGOUT_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 로그아웃 성공!"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "❌ 로그아웃 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
    exit 1
fi
echo ""

# 5. 로그아웃 후 인증된 요청 시도 (실패해야 함)
echo "5. 로그아웃 후 인증된 요청 시도 (실패해야 함)..."
PROFILE_RESPONSE_AFTER_LOGOUT=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$PROFILE_RESPONSE_AFTER_LOGOUT" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$PROFILE_RESPONSE_AFTER_LOGOUT" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "401" ] || [ "$HTTP_STATUS" = "403" ]; then
    echo "✅ 예상대로 인증 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "⚠️  예상과 다른 응답 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
    echo "참고: JWT는 stateless이므로 서버에서 토큰을 무효화하지 않습니다."
    echo "클라이언트에서 토큰을 삭제해야 합니다."
fi
echo ""

# 6. 인증 없이 로그아웃 시도 (실패해야 함)
echo "6. 인증 없이 로그아웃 시도 (실패해야 함)..."
LOGOUT_WITHOUT_AUTH=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$LOGOUT_WITHOUT_AUTH" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$LOGOUT_WITHOUT_AUTH" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "401" ] || [ "$HTTP_STATUS" = "403" ]; then
    echo "✅ 예상대로 인증 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "⚠️  예상과 다른 응답 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
fi
echo ""

echo "=== 로그아웃 API 테스트 완료 ==="

