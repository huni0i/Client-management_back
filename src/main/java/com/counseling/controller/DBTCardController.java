package com.counseling.controller;

import com.counseling.dto.ApiResponse;
import com.counseling.dto.DBTCardRequest;
import com.counseling.dto.DBTCardResponse;
import com.counseling.service.DBTCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}/dbt-cards")
@Tag(name = "DBT 일기카드", description = "DBT 일기카드 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class DBTCardController {

    private final DBTCardService dbtCardService;

    public DBTCardController(DBTCardService dbtCardService) {
        this.dbtCardService = dbtCardService;
    }

    @PostMapping
    @Operation(summary = "DBT 일기카드 작성/수정", description = "내담자가 DBT 일기카드를 작성하거나 수정합니다.")
    public ResponseEntity<ApiResponse<DBTCardResponse>> createOrUpdateCard(
            @PathVariable String roomId,
            @Valid @RequestBody DBTCardRequest request) {
        DBTCardResponse response = dbtCardService.createOrUpdateCard(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("DBT 일기카드가 저장되었습니다.", response));
    }

    @GetMapping("/my")
    @Operation(summary = "내 DBT 일기카드 조회", description = "내담자가 자신의 DBT 일기카드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<DBTCardResponse>>> getMyCards(
            @PathVariable String roomId,
            @RequestParam(required = false) String date) {
        List<DBTCardResponse> response = dbtCardService.getMyCards(roomId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "내담자별 DBT 일기카드 조회", description = "상담사가 내담자들의 DBT 일기카드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<DBTCardResponse>>> getCards(
            @PathVariable String roomId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String clientId) {
        List<DBTCardResponse> response = dbtCardService.getCards(roomId, date, clientId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

