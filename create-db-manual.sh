#!/bin/bash

# MySQL에 직접 접속하여 데이터베이스 생성하는 스크립트

echo "=== counseling_db 데이터베이스 생성 ==="
echo ""
echo "MySQL에 접속하여 다음 SQL을 실행하세요:"
echo ""
echo "----------------------------------------"
echo "CREATE DATABASE IF NOT EXISTS counseling_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "----------------------------------------"
echo ""
echo "MySQL 접속 방법:"
echo "  /usr/local/mysql-8.0.44-macos15-arm64/bin/mysql -u root -p"
echo ""
echo "또는 MySQL이 PATH에 있다면:"
echo "  mysql -u root -p"
echo ""
echo "데이터베이스 생성 후 확인:"
echo "  SHOW DATABASES LIKE 'counseling_db';"
echo ""

