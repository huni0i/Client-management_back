package com.counseling.controller;

import com.counseling.dto.ApiResponse;
import com.counseling.dto.JoinRoomRequest;
import com.counseling.dto.RoomRequest;
import com.counseling.dto.RoomResponse;
import com.counseling.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "상담방", description = "상담방 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @Operation(summary = "상담방 생성", description = "상담사만 상담방을 생성할 수 있습니다.")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("상담방이 생성되었습니다.", response));
    }

    @GetMapping
    @Operation(summary = "상담방 목록 조회", description = "사용자가 참가한 상담방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRooms() {
        List<RoomResponse> response = roomService.getRooms();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "상담방 상세 조회", description = "상담방의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomDetail(@PathVariable String roomId) {
        RoomResponse response = roomService.getRoomDetail(roomId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/join")
    @Operation(summary = "상담방 참가", description = "내담자가 초대코드로 상담방에 참가합니다.")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(@Valid @RequestBody JoinRoomRequest request) {
        RoomResponse response = roomService.joinRoom(request);
        return ResponseEntity.ok(ApiResponse.success("상담방에 참가했습니다.", response));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "상담방 삭제", description = "상담사가 자신이 생성한 상담방을 삭제합니다.")
    public ResponseEntity<ApiResponse<Object>> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("상담방이 삭제되었습니다.", null));
    }

    @DeleteMapping("/{roomId}/leave")
    @Operation(summary = "상담방 나가기", description = "내담자가 참가한 상담방에서 나갑니다.")
    public ResponseEntity<ApiResponse<Object>> leaveRoom(@PathVariable String roomId) {
        roomService.leaveRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("상담방에서 나갔습니다.", null));
    }
}

