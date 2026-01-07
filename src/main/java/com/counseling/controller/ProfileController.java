package com.counseling.controller;

import com.counseling.dto.ApiResponse;
import com.counseling.dto.ProfileResponse;
import com.counseling.dto.ProfileUpdateRequest;
import com.counseling.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "프로필", description = "프로필 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "프로필 조회", description = "현재 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        ProfileResponse response = profileService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    @Operation(summary = "프로필 수정", description = "현재 사용자의 프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@RequestBody ProfileUpdateRequest request) {
        ProfileResponse response = profileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다.", response));
    }
}

