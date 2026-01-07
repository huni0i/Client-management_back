package com.counseling.service;

import com.counseling.entity.User;
import com.counseling.exception.BusinessException;
import com.counseling.exception.ErrorCode;
import com.counseling.repository.UserRepository;
import com.counseling.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityUtil.clearTestUserId();
        
        // 테스트 사용자 생성
        testUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("test_logout@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트 사용자")
                .userType(User.UserType.client)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("로그아웃 성공")
    void testLogout_Success() {
        // given
        SecurityUtil.setCurrentUserId(testUser.getUserId());

        // when & then
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 로그아웃 시도 시 실패")
    void testLogout_Unauthorized() {
        // given
        SecurityUtil.clearTestUserId();

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.logout();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityUtil.clearTestUserId();
    }
}

