package com.counseling.service;

import com.counseling.dto.DBTCardRequest;
import com.counseling.dto.DBTCardResponse;
import com.counseling.entity.DBTCard;
import com.counseling.entity.Room;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DBTCardService {

    private final DBTCardRepository dbtCardRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;

    public DBTCardService(DBTCardRepository dbtCardRepository, RoomRepository roomRepository,
                         UserRepository userRepository, RoomMemberRepository roomMemberRepository) {
        this.dbtCardRepository = dbtCardRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMemberRepository = roomMemberRepository;
    }

    @Transactional
    public DBTCardResponse createOrUpdateCard(String roomId, DBTCardRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.client) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 상담방 참가 확인
        if (!roomMemberRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식을 사용하세요.");
        }
        DBTCard card = dbtCardRepository.findByRoomAndClientAndDate(room, user, date)
                .orElse(null);

        if (card == null) {
            card = DBTCard.builder()
                    .cardId(UUID.randomUUID().toString())
                    .room(room)
                    .client(user)
                    .date(date)
                    .build();
        }

        // Header 정보 설정
        if (request.getHeader() != null) {
            DBTCardRequest.HeaderInfo header = request.getHeader();
            card.setHeaderName(header.getName());
            if (header.getWrittenDuringCounseling() != null) {
                card.setHeaderWrittenDuringCounseling(DBTCard.YesNo.valueOf(header.getWrittenDuringCounseling()));
            }
            if (header.getFrequency() != null) {
                card.setHeaderFrequency(header.getFrequency());
            }
        }

        // DayData 정보 설정
        if (request.getDayData() != null) {
            DBTCardRequest.DayDataInfo dayData = request.getDayData();
            card.setImpulse1Text(dayData.getImpulse1Text());
            card.setImpulse1Intensity(dayData.getImpulse1Intensity());
            card.setAction1Text(dayData.getAction1Text());
            card.setAction1Intensity(dayData.getAction1Intensity());
            card.setThoughtText(dayData.getThoughtText());
            card.setThoughtIntensity(dayData.getThoughtIntensity());
            card.setAction2Text(dayData.getAction2Text());
            card.setAction2Intensity(dayData.getAction2Intensity());
            card.setImpulse2Text(dayData.getImpulse2Text());
            card.setImpulse2Intensity(dayData.getImpulse2Intensity());
            card.setAction3Text(dayData.getAction3Text());
            card.setAction3Intensity(dayData.getAction3Intensity());
            if (dayData.getMedication() != null) {
                card.setMedication(DBTCard.YesNo.valueOf(dayData.getMedication()));
            }
            card.setTargetBehavior1(dayData.getTargetBehavior1());
            card.setTargetBehavior2(dayData.getTargetBehavior2());
            card.setSkillUse(dayData.getSkillUse());
            card.setSleepTime(dayData.getSleepTime());
            card.setWakeTime(dayData.getWakeTime());
            card.setAnger(dayData.getAnger());
            card.setAngerKeyword(dayData.getAngerKeyword());
            card.setFear(dayData.getFear());
            card.setFearKeyword(dayData.getFearKeyword());
            card.setJoy(dayData.getJoy());
            card.setJoyKeyword(dayData.getJoyKeyword());
            card.setAnxiety(dayData.getAnxiety());
            card.setAnxietyKeyword(dayData.getAnxietyKeyword());
            card.setSadness(dayData.getSadness());
            card.setSadnessKeyword(dayData.getSadnessKeyword());
        }

        dbtCardRepository.save(card);

        return toResponse(card);
    }

    public List<DBTCardResponse> getMyCards(String roomId, String dateStr) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.client) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!roomMemberRepository.existsByRoomAndUser(room, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<DBTCard> cards;
        if (dateStr != null && !dateStr.isEmpty()) {
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식을 사용하세요.");
            }
            cards = dbtCardRepository.findByRoomAndClientAndDate(room, user, date)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            cards = dbtCardRepository.findByRoomAndClient(room, user);
        }

        return cards.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DBTCardResponse> getCards(String roomId, String dateStr, String clientId) {
        String userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getUserType() != User.UserType.counselor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.getCreatedBy().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<DBTCard> cards;
        if (clientId != null && !clientId.isEmpty()) {
            User client = userRepository.findById(clientId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CLIENT_NOT_FOUND));
            if (dateStr != null && !dateStr.isEmpty()) {
                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식을 사용하세요.");
                }
                cards = dbtCardRepository.findByRoomAndClientAndDate(room, client, date)
                        .map(List::of)
                        .orElse(List.of());
            } else {
                cards = dbtCardRepository.findByRoomAndClient(room, client);
            }
        } else {
            if (dateStr != null && !dateStr.isEmpty()) {
                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR, "날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식을 사용하세요.");
                }
                cards = dbtCardRepository.findByRoomAndDate(room, date);
            } else {
                cards = dbtCardRepository.findByRoom(room);
            }
        }

        return cards.stream().map(this::toResponseWithClientInfo).collect(Collectors.toList());
    }

    private DBTCardResponse toResponse(DBTCard card) {
        DBTCardRequest.HeaderInfo header = null;
        if (card.getHeaderName() != null || card.getHeaderWrittenDuringCounseling() != null || card.getHeaderFrequency() != null) {
            header = new DBTCardRequest.HeaderInfo();
            header.setName(card.getHeaderName());
            if (card.getHeaderWrittenDuringCounseling() != null) {
                header.setWrittenDuringCounseling(card.getHeaderWrittenDuringCounseling().name());
            }
            if (card.getHeaderFrequency() != null) {
                header.setFrequency(card.getHeaderFrequency());
            }
        }

        DBTCardRequest.DayDataInfo dayData = new DBTCardRequest.DayDataInfo();
        dayData.setImpulse1Text(card.getImpulse1Text());
        dayData.setImpulse1Intensity(card.getImpulse1Intensity());
        dayData.setAction1Text(card.getAction1Text());
        dayData.setAction1Intensity(card.getAction1Intensity());
        dayData.setThoughtText(card.getThoughtText());
        dayData.setThoughtIntensity(card.getThoughtIntensity());
        dayData.setAction2Text(card.getAction2Text());
        dayData.setAction2Intensity(card.getAction2Intensity());
        dayData.setImpulse2Text(card.getImpulse2Text());
        dayData.setImpulse2Intensity(card.getImpulse2Intensity());
        dayData.setAction3Text(card.getAction3Text());
        dayData.setAction3Intensity(card.getAction3Intensity());
        if (card.getMedication() != null) {
            dayData.setMedication(card.getMedication().name());
        }
        dayData.setTargetBehavior1(card.getTargetBehavior1());
        dayData.setTargetBehavior2(card.getTargetBehavior2());
        dayData.setSkillUse(card.getSkillUse());
        dayData.setSleepTime(card.getSleepTime());
        dayData.setWakeTime(card.getWakeTime());
        dayData.setAnger(card.getAnger());
        dayData.setAngerKeyword(card.getAngerKeyword());
        dayData.setFear(card.getFear());
        dayData.setFearKeyword(card.getFearKeyword());
        dayData.setJoy(card.getJoy());
        dayData.setJoyKeyword(card.getJoyKeyword());
        dayData.setAnxiety(card.getAnxiety());
        dayData.setAnxietyKeyword(card.getAnxietyKeyword());
        dayData.setSadness(card.getSadness());
        dayData.setSadnessKeyword(card.getSadnessKeyword());

        return DBTCardResponse.builder()
                .cardId(card.getCardId())
                .roomId(card.getRoom().getRoomId())
                .clientId(card.getClient().getUserId())
                .date(card.getDate())
                .header(header)
                .dayData(dayData)
                .submittedAt(card.getSubmittedAt())
                .build();
    }

    private DBTCardResponse toResponseWithClientInfo(DBTCard card) {
        DBTCardResponse response = toResponse(card);
        response.setClientName(card.getClient().getName());
        response.setClientEmail(card.getClient().getEmail());
        return response;
    }
}

