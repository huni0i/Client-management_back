package com.counseling.service;

import com.counseling.entity.Room;
import com.counseling.entity.RoomMember;
import com.counseling.entity.User;
import com.counseling.exception.BusinessException;
import com.counseling.exception.ErrorCode;
import com.counseling.repository.DBTCardRepository;
import com.counseling.repository.RoomMemberRepository;
import com.counseling.repository.RoomRepository;
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
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DBTCardRepository dbtCardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User counselor;
    private User client;
    private Room room;

    @BeforeEach
    void setUp() {
        SecurityUtil.clearTestUserId();
        // 상담사 생성
        counselor = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("test_counselor@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트 상담사")
                .userType(User.UserType.counselor)
                .build();
        userRepository.save(counselor);

        // 내담자 생성
        client = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("test_client@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("테스트 내담자")
                .userType(User.UserType.client)
                .build();
        userRepository.save(client);

        // 상담방 생성
        room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .name("테스트 상담방")
                .inviteCode("TEST01")
                .createdBy(counselor)
                .build();
        roomRepository.save(room);

        // 상담사도 멤버로 추가
        RoomMember counselorMember = RoomMember.builder()
                .room(room)
                .user(counselor)
                .build();
        roomMemberRepository.save(counselorMember);

        // 내담자도 멤버로 추가
        RoomMember clientMember = RoomMember.builder()
                .room(room)
                .user(client)
                .build();
        roomMemberRepository.save(clientMember);
    }

    @Test
    @DisplayName("내담자가 상담방 나가기 성공")
    void testLeaveRoom_Success() {
        // given
        SecurityUtil.setCurrentUserId(client.getUserId());

        // when
        assertDoesNotThrow(() -> roomService.leaveRoom(room.getRoomId()));

        // then
        assertFalse(roomMemberRepository.existsByRoomAndUser(room, client));
        assertTrue(roomMemberRepository.existsByRoomAndUser(room, counselor));
    }

    @Test
    @DisplayName("상담사가 상담방 나가기 시도 시 실패")
    void testLeaveRoom_CounselorCannotLeave() {
        // given
        SecurityUtil.setCurrentUserId(counselor.getUserId());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.leaveRoom(room.getRoomId());
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        // 예외가 발생했는지 확인 (ErrorCode 검증으로 충분)
    }

    @Test
    @DisplayName("참가하지 않은 내담자가 나가기 시도 시 실패")
    void testLeaveRoom_NotMember() {
        // given
        User anotherClient = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("another_client@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("다른 내담자")
                .userType(User.UserType.client)
                .build();
        userRepository.save(anotherClient);
        SecurityUtil.setCurrentUserId(anotherClient.getUserId());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.leaveRoom(room.getRoomId());
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("상담사가 상담방 삭제 성공")
    void testDeleteRoom_Success() {
        // given
        SecurityUtil.setCurrentUserId(counselor.getUserId());

        // when
        assertDoesNotThrow(() -> roomService.deleteRoom(room.getRoomId()));

        // then
        assertFalse(roomRepository.findById(room.getRoomId()).isPresent());
        assertEquals(0, roomMemberRepository.findByRoom(room).size());
    }

    @Test
    @DisplayName("내담자가 상담방 삭제 시도 시 실패")
    void testDeleteRoom_ClientCannotDelete() {
        // given
        SecurityUtil.setCurrentUserId(client.getUserId());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.deleteRoom(room.getRoomId());
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("다른 상담사가 상담방 삭제 시도 시 실패")
    void testDeleteRoom_OtherCounselorCannotDelete() {
        // given
        User anotherCounselor = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("another_counselor@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("다른 상담사")
                .userType(User.UserType.counselor)
                .build();
        userRepository.save(anotherCounselor);
        SecurityUtil.setCurrentUserId(anotherCounselor.getUserId());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.deleteRoom(room.getRoomId());
        });

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 상담방 삭제 시도 시 실패")
    void testDeleteRoom_RoomNotFound() {
        // given
        SecurityUtil.setCurrentUserId(counselor.getUserId());
        String nonExistentRoomId = UUID.randomUUID().toString();

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roomService.deleteRoom(nonExistentRoomId);
        });

        assertEquals(ErrorCode.ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("상담방 삭제 시 관련 데이터 모두 삭제 확인")
    void testDeleteRoom_AllRelatedDataDeleted() {
        // given
        SecurityUtil.setCurrentUserId(counselor.getUserId());
        
        // 추가 멤버 생성
        User anotherClient = User.builder()
                .userId(UUID.randomUUID().toString())
                .email("another_client2@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("추가 내담자")
                .userType(User.UserType.client)
                .build();
        userRepository.save(anotherClient);
        
        RoomMember anotherMember = RoomMember.builder()
                .room(room)
                .user(anotherClient)
                .build();
        roomMemberRepository.save(anotherMember);

        // when
        roomService.deleteRoom(room.getRoomId());

        // then
        assertFalse(roomRepository.findById(room.getRoomId()).isPresent());
        assertEquals(0, roomMemberRepository.findByRoom(room).size());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityUtil.clearTestUserId();
    }
}

