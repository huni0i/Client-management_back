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
public class RoomResponse {
    private String roomId;
    private String name;
    private String inviteCode;
    private LocalDateTime createdAt;
    private String createdBy;
    private Integer clientCount;
    private LocalDateTime joinedAt; // 내담자일 경우만
    private UserInfo createdByInfo; // 상세 조회 시
    private List<ClientInfo> clients; // 상세 조회 시
}

