package com.counseling.service;

import com.counseling.dto.ProfileResponse;
import com.counseling.dto.ProfileUpdateRequest;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final DBTCardRepository dbtCardRepository;

    public ProfileService(UserRepository userRepository, RoomRepository roomRepository,
                         RoomMemberRepository roomMemberRepository, DBTCardRepository dbtCardRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.dbtCardRepository = dbtCardRepository;
    }

    public ProfileResponse getProfile() {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProfileResponse.StatsInfo stats;
        List<ProfileResponse.RoomInfo> rooms;

        if (user.getUserType() == User.UserType.counselor) {
            List<Room> createdRooms = roomRepository.findByCreatedBy(user);
            long totalClientCards = createdRooms.stream()
                    .mapToLong(dbtCardRepository::countByRoom)
                    .sum();

            stats = ProfileResponse.StatsInfo.builder()
                    .roomCount((long) createdRooms.size())
                    .totalClientCards(totalClientCards)
                    .build();

            rooms = createdRooms.stream().map(room -> {
                long cardCount = dbtCardRepository.countByRoom(room);
                return ProfileResponse.RoomInfo.builder()
                        .roomId(room.getRoomId())
                        .name(room.getName())
                        .createdAt(room.getCreatedAt())
                        .cardCount(cardCount)
                        .build();
            }).collect(Collectors.toList());
        } else {
            List<RoomMember> members = roomMemberRepository.findByUser(user);
            long dbtCardCount = dbtCardRepository.countByClient(user);

            stats = ProfileResponse.StatsInfo.builder()
                    .dbtCardCount(dbtCardCount)
                    .build();

            rooms = members.stream().map(member -> {
                Room room = member.getRoom();
                long cardCount = dbtCardRepository.countByRoom(room);
                return ProfileResponse.RoomInfo.builder()
                        .roomId(room.getRoomId())
                        .name(room.getName())
                        .joinedAt(member.getJoinedAt())
                        .cardCount(cardCount)
                        .build();
            }).collect(Collectors.toList());
        }

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .stats(stats)
                .rooms(rooms)
                .build();
    }

    @Transactional
    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        userRepository.save(user);

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .userType(user.getUserType().name())
                .build();
    }
}

