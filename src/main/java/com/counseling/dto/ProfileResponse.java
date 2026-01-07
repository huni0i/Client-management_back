package com.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private String userId;
    private String name;
    private String email;
    private String userType;
    private StatsInfo stats;
    private List<RoomInfo> rooms;

    @Data
    @Builder
    public static class StatsInfo {
        private Long roomCount;
        private Long dbtCardCount; // 내담자일 경우만
        private Long totalClientCards; // 상담사일 경우만
    }

    @Data
    @Builder
    public static class RoomInfo {
        private String roomId;
        private String name;
        private LocalDateTime createdAt; // 상담사일 경우
        private LocalDateTime joinedAt; // 내담자일 경우
        private Long cardCount;
    }
}

