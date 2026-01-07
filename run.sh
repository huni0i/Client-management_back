#!/bin/bash

# .env 파일에서 환경 변수 로드 후 애플리케이션 실행

# .env 파일이 존재하는지 확인
if [ -f .env ]; then
    echo "환경 변수 로드 중..."
    export $(cat .env | grep -v '^#' | xargs)
    echo "환경 변수 로드 완료!"
    echo ""
    echo "실행 중인 환경 변수:"
    echo "  DB_HOST: ${DB_HOST}"
    echo "  DB_NAME: ${DB_NAME}"
    echo "  DB_USERNAME: ${DB_USERNAME}"
    echo "  JWT_SECRET: ${JWT_SECRET:0:20}..." # 보안을 위해 일부만 표시
    echo ""
else
    echo "경고: .env 파일이 없습니다. 기본 설정을 사용합니다."
fi

# 애플리케이션 실행
./gradlew bootRun

