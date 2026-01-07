#!/bin/bash

# 상담방 나가기 및 삭제 API 테스트 스크립트

BASE_URL="http://localhost:8080/api"
echo "=== 상담방 나가기 및 삭제 API 테스트 시작 ==="
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

# 2. 상담사 회원가입 및 로그인
echo "2. 상담사 회원가입 및 로그인..."
COUNSELOR_SIGNUP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "counselor_test@test.com",
    "password": "password123",
    "name": "테스트 상담사",
    "userType": "counselor"
  }')

COUNSELOR_TOKEN=$(echo "$COUNSELOR_SIGNUP" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$COUNSELOR_TOKEN" ]; then
    # 이미 존재하는 경우 로그인 시도
    COUNSELOR_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "counselor_test@test.com",
        "password": "password123"
      }')
    COUNSELOR_TOKEN=$(echo "$COUNSELOR_LOGIN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
fi

if [ -z "$COUNSELOR_TOKEN" ]; then
    echo "❌ 상담사 토큰을 받지 못했습니다."
    exit 1
fi
echo "✅ 상담사 토큰 받기 성공!"
echo ""

# 3. 내담자 회원가입 및 로그인
echo "3. 내담자 회원가입 및 로그인..."
CLIENT_SIGNUP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client_test@test.com",
    "password": "password123",
    "name": "테스트 내담자",
    "userType": "client"
  }')

CLIENT_TOKEN=$(echo "$CLIENT_SIGNUP" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
if [ -z "$CLIENT_TOKEN" ]; then
    # 이미 존재하는 경우 로그인 시도
    CLIENT_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "client_test@test.com",
        "password": "password123"
      }')
    CLIENT_TOKEN=$(echo "$CLIENT_LOGIN" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
fi

if [ -z "$CLIENT_TOKEN" ]; then
    echo "❌ 내담자 토큰을 받지 못했습니다."
    exit 1
fi
echo "✅ 내담자 토큰 받기 성공!"
echo ""

# 4. 상담방 생성
echo "4. 상담방 생성..."
ROOM_RESPONSE=$(curl -s -X POST "$BASE_URL/rooms" \
  -H "Authorization: Bearer $COUNSELOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트 상담방"
  }')

ROOM_ID=$(echo "$ROOM_RESPONSE" | grep -o '"roomId":"[^"]*' | cut -d'"' -f4)
INVITE_CODE=$(echo "$ROOM_RESPONSE" | grep -o '"inviteCode":"[^"]*' | cut -d'"' -f4)

if [ -z "$ROOM_ID" ]; then
    echo "❌ 상담방 생성 실패"
    echo "$ROOM_RESPONSE"
    exit 1
fi
echo "✅ 상담방 생성 성공! Room ID: $ROOM_ID, Invite Code: $INVITE_CODE"
echo ""

# 5. 내담자가 상담방 참가
echo "5. 내담자가 상담방 참가..."
JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/rooms/join" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"inviteCode\": \"$INVITE_CODE\"
  }")

echo "$JOIN_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$JOIN_RESPONSE"
echo ""

# 6. 내담자가 상담방 나가기 테스트
echo "6. 내담자가 상담방 나가기 테스트..."
LEAVE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X DELETE "$BASE_URL/rooms/$ROOM_ID/leave" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$LEAVE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$LEAVE_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 상담방 나가기 성공!"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "❌ 상담방 나가기 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
fi
echo ""

# 7. 다시 상담방 생성 (삭제 테스트용)
echo "7. 삭제 테스트를 위한 상담방 재생성..."
ROOM_RESPONSE2=$(curl -s -X POST "$BASE_URL/rooms" \
  -H "Authorization: Bearer $COUNSELOR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "삭제 테스트 상담방"
  }')

ROOM_ID2=$(echo "$ROOM_RESPONSE2" | grep -o '"roomId":"[^"]*' | cut -d'"' -f4)

if [ -z "$ROOM_ID2" ]; then
    echo "❌ 상담방 생성 실패"
    echo "$ROOM_RESPONSE2"
    exit 1
fi
echo "✅ 상담방 생성 성공! Room ID: $ROOM_ID2"
echo ""

# 8. 상담방 삭제 테스트
echo "8. 상담방 삭제 테스트..."
DELETE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X DELETE "$BASE_URL/rooms/$ROOM_ID2" \
  -H "Authorization: Bearer $COUNSELOR_TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$DELETE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$DELETE_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "200" ]; then
    echo "✅ 상담방 삭제 성공!"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
else
    echo "❌ 상담방 삭제 실패 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
fi
echo ""

# 9. 삭제된 상담방 조회 시도 (404 확인)
echo "9. 삭제된 상담방 조회 시도 (404 확인)..."
GET_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$BASE_URL/rooms/$ROOM_ID2" \
  -H "Authorization: Bearer $COUNSELOR_TOKEN" \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "$GET_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
RESPONSE_BODY=$(echo "$GET_RESPONSE" | sed '/HTTP_STATUS/d')

if [ "$HTTP_STATUS" = "404" ]; then
    echo "✅ 삭제 확인 성공 (404 응답)"
else
    echo "⚠️  예상과 다른 응답 (HTTP Status: $HTTP_STATUS)"
    echo "$RESPONSE_BODY" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE_BODY"
fi
echo ""

echo "=== 테스트 완료 ==="

