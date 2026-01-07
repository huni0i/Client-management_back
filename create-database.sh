#!/bin/bash

# MySQL 데이터베이스 생성 스크립트

echo "=== counseling_db 데이터베이스 생성 ==="
echo ""

# .env 파일에서 데이터베이스 정보 읽기
if [ -f .env ]; then
    source <(grep -v '^#' .env | sed 's/^/export /')
fi

DB_NAME=${DB_NAME:-counseling_db}
DB_USERNAME=${DB_USERNAME:-root}
DB_PASSWORD=${DB_PASSWORD:-root}

echo "데이터베이스 이름: $DB_NAME"
echo "사용자: $DB_USERNAME"
echo ""

# MySQL 경로 찾기
MYSQL_CMD=""
if command -v mysql &> /dev/null; then
    MYSQL_CMD="mysql"
elif [ -f "/usr/local/mysql/bin/mysql" ]; then
    MYSQL_CMD="/usr/local/mysql/bin/mysql"
elif [ -f "/opt/homebrew/bin/mysql" ]; then
    MYSQL_CMD="/opt/homebrew/bin/mysql"
else
    echo "❌ MySQL을 찾을 수 없습니다."
    echo ""
    echo "MySQL 설치 방법:"
    echo "  brew install mysql"
    echo ""
    echo "또는 수동으로 MySQL에 접속하여 다음 SQL을 실행하세요:"
    echo "  CREATE DATABASE IF NOT EXISTS counseling_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    exit 1
fi

echo "MySQL 경로: $MYSQL_CMD"
echo ""

# 데이터베이스 생성
echo "데이터베이스 생성 중..."
$MYSQL_CMD -u "$DB_USERNAME" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 데이터베이스 생성 성공!"
    echo ""
    echo "데이터베이스 확인:"
    $MYSQL_CMD -u "$DB_USERNAME" -p"$DB_PASSWORD" -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null
else
    echo "❌ 데이터베이스 생성 실패"
    echo ""
    echo "수동으로 생성하는 방법:"
    echo "1. MySQL에 접속:"
    echo "   $MYSQL_CMD -u $DB_USERNAME -p"
    echo ""
    echo "2. 다음 SQL 실행:"
    echo "   CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "   exit;"
    exit 1
fi

