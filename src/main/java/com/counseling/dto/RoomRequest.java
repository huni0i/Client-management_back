package com.counseling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomRequest {
    @NotBlank(message = "상담방 이름은 필수입니다.")
    private String name;
}

