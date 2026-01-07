package com.counseling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DBTCardResponse {
    private String cardId;
    private String roomId;
    private String clientId;
    private String clientName; // 상담사 조회 시
    private String clientEmail; // 상담사 조회 시
    private LocalDate date;
    private DBTCardRequest.HeaderInfo header;
    private DBTCardRequest.DayDataInfo dayData;
    private LocalDateTime submittedAt;
}

