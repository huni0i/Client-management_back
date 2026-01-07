package com.counseling.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    private static String testUserId; // 테스트용
    
    public static String getCurrentUserId() {
        // 테스트 모드
        if (testUserId != null) {
            return testUserId;
        }
        
        // 실제 실행 모드
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
    
    // 테스트용 메서드
    public static void setCurrentUserId(String userId) {
        testUserId = userId;
    }
    
    // 테스트용 초기화 메서드
    public static void clearTestUserId() {
        testUserId = null;
    }
}

