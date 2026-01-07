package com.counseling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @NotBlank(message = "초대코드는 필수입니다.")
    private String inviteCode;
}

