#!/bin/bash

# API 테스트 스크립트

BASE_URL="http://localhost:8080/api"
echo "=== API 테스트 시작 ==="
echo ""

# 1. 서버 상태 확인
echo "1. 서버 상태 확인..."
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api-docs)
if [ "$STATUS" = "200" ]; then
    echo "✅ 서버가 정상적으로 실행 중입니다."
else
    echo "❌ 서버가 실행되지 않았습니다. (HTTP Status: $STATUS)"
    echo "애플리케이션을 먼저 실행하세요: ./run.sh"
    exit 1
fi
echo ""

# 2. 회원가입 테스트 (상담사)
echo "2. 회원가입 테스트 (상담사)..."
SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "counselor@test.com",
    "password": "password123",
    "name": "테스트 상담사",
    "userType": "counselor"
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

# 3. 프로필 조회 테스트
echo "3. 프로필 조회 테스트..."
PROFILE_RESPONSE=$(curl -s -X GET "$BASE_URL/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "$PROFILE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$PROFILE_RESPONSE"
echo ""

# 4. 상담방 생성 테스트
echo "4. 상담방 생성 테스트..."
ROOM_RESPONSE=$(curl -s -X POST "$BASE_URL/rooms" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 상담방"
  }')

echo "$ROOM_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ROOM_RESPONSE"
echo ""

# Room ID 추출
ROOM_ID=$(echo "$ROOM_RESPONSE" | grep -o '"roomId":"[^"]*' | cut -d'"' -f4)
if [ -n "$ROOM_ID" ]; then
    echo "✅ 상담방 생성 성공! Room ID: $ROOM_ID"
    echo ""
    
    # 5. 상담방 목록 조회
    echo "5. 상담방 목록 조회..."
    ROOMS_RESPONSE=$(curl -s -X GET "$BASE_URL/rooms" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json")
    
    echo "$ROOMS_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ROOMS_RESPONSE"
    echo ""
fi

# 6. 로그인 테스트
echo "6. 로그인 테스트..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "counselor@test.com",
    "password": "password123"
  }')

echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

echo "=== API 테스트 완료 ==="

