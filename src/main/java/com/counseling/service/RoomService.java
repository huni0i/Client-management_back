package com.counseling.service;

import com.counseling.dto.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final DBTCardRepository dbtCardRepository;

    public RoomService(RoomRepository roomRepository, RoomMemberRepository roomMemberRepository, 
                      UserRepository userRepository, DBTCardRepository dbtCardRepository) {
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.userRepository = userRepository;
        this.dbtCardRepository = dbtCardRepository;
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.counselor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        String inviteCode = generateInviteCode();

        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .name(request.getName())
                .inviteCode(inviteCode)
                .createdBy(user)
                .build();

        roomRepository.save(room);

        // 상담사도 멤버로 추가
        RoomMember member = RoomMember.builder()
                .room(room)
                .user(user)
                .build();
        roomMemberRepository.save(member);

        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .inviteCode(room.getInviteCode())
                .createdAt(room.getCreatedAt())
                .createdBy(user.getUserId())
                .clientCount(0)
                .build();
    }

    public List<RoomResponse> getRooms() {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Room> rooms;
        if (user.getUserType() == User.UserType.counselor) {
            rooms = roomRepository.findByCreatedBy(user);
        } else {
            rooms = roomRepository.findRoomsByUserId(userId);
        }

        return rooms.stream().map(room -> {
            List<RoomMember> members = roomMemberRepository.findByRoom(room);
            long clientCount = members.stream()
                    .filter(m -> m.getUser().getUserType() == User.UserType.client)
                    .count();

            RoomMember member = roomMemberRepository.findByRoomAndUser(room, user).orElse(null);
            LocalDateTime joinedAt = member != null ? member.getJoinedAt() : null;

            return RoomResponse.builder()
                    .roomId(room.getRoomId())
                    .name(room.getName())
                    .inviteCode(room.getInviteCode())
                    .createdAt(room.getCreatedAt())
                    .createdBy(room.getCreatedBy().getUserId())
                    .clientCount((int) clientCount)
                    .joinedAt(joinedAt)
                    .build();
        }).collect(Collectors.toList());
    }

    public RoomResponse getRoomDetail(String roomId) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 확인: 상담사는 자신이 생성한 방만, 내담자는 참가한 방만
        if (user.getUserType() == User.UserType.counselor) {
            if (!room.getCreatedBy().getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        } else {
            if (!roomMemberRepository.existsByRoomAndUser(room, user)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        List<RoomMember> members = roomMemberRepository.findByRoom(room);
        List<ClientInfo> clients = members.stream()
                .filter(m -> m.getUser().getUserType() == User.UserType.client)
                .map(m -> ClientInfo.builder()
                        .userId(m.getUser().getUserId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .inviteCode(room.getInviteCode())
                .createdAt(room.getCreatedAt())
                .createdByInfo(UserInfo.builder()
                        .userId(room.getCreatedBy().getUserId())
                        .name(room.getCreatedBy().getName())
                        .email(room.getCreatedBy().getEmail())
                        .build())
                .clients(clients)
                .build();
    }

    @Transactional
    public RoomResponse joinRoom(JoinRoomRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.client) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        if (roomMemberRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(user)
                .build();
        roomMemberRepository.save(member);

        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private String generateInviteCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (roomRepository.findByInviteCode(code).isPresent());
        return code;
    }

    @Transactional
    public void deleteRoom(String roomId) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.counselor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 확인 (getRoomDetail과 동일한 방식)
        User createdBy = room.getCreatedBy();
        if (!createdBy.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 관련 데이터 삭제 (순서 중요: 자식 엔티티를 먼저 삭제)
        // DBTCard 삭제
        dbtCardRepository.deleteByRoom(room);
        
        // RoomMember 삭제
        roomMemberRepository.deleteByRoom(room);
        
        // Room 삭제
        roomRepository.delete(room);
    }

    @Transactional
    public void leaveRoom(String roomId) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.client) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 상담사는 나가기 불가
        if (room.getCreatedBy().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상담사는 상담방을 나갈 수 없습니다.");
        }

        RoomMember member = roomMemberRepository.findByRoomAndUser(room, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "상담방에 참가하지 않은 사용자입니다."));

        roomMemberRepository.delete(member);
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
}

