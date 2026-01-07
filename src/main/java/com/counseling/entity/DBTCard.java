package com.counseling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dbt_cards",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "client_id", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DBTCard {
    @Id
    @Column(name = "card_id", length = 255)
    private String cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private LocalDate date;

    // Header
    @Column(name = "header_name", length = 255)
    private String headerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "header_written_during_counseling", length = 10)
    private YesNo headerWrittenDuringCounseling;

    @Column(name = "header_frequency", length = 20)
    private String headerFrequency;

    // 행동 및 충동
    @Column(name = "impulse1_text", columnDefinition = "TEXT")
    private String impulse1Text;

    @Column(name = "impulse1_intensity", length = 10)
    private String impulse1Intensity;

    @Column(name = "action1_text", columnDefinition = "TEXT")
    private String action1Text;

    @Column(name = "action1_intensity", length = 10)
    private String action1Intensity;

    @Column(name = "thought_text", columnDefinition = "TEXT")
    private String thoughtText;

    @Column(name = "thought_intensity", length = 10)
    private String thoughtIntensity;

    @Column(name = "action2_text", columnDefinition = "TEXT")
    private String action2Text;

    @Column(name = "action2_intensity", length = 10)
    private String action2Intensity;

    @Column(name = "impulse2_text", columnDefinition = "TEXT")
    private String impulse2Text;

    @Column(name = "impulse2_intensity", length = 10)
    private String impulse2Intensity;

    @Column(name = "action3_text", columnDefinition = "TEXT")
    private String action3Text;

    @Column(name = "action3_intensity", length = 10)
    private String action3Intensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "medication", length = 10)
    private YesNo medication;

    @Column(name = "target_behavior1", length = 255)
    private String targetBehavior1;

    @Column(name = "target_behavior2", length = 255)
    private String targetBehavior2;

    @Column(name = "skill_use", length = 10)
    private String skillUse;

    // 수면 및 감정
    @Column(name = "sleep_time", length = 50)
    private String sleepTime;

    @Column(name = "wake_time", length = 50)
    private String wakeTime;

    @Column(name = "anger", length = 10)
    private String anger;

    @Column(name = "anger_keyword", columnDefinition = "TEXT")
    private String angerKeyword;

    @Column(name = "fear", length = 10)
    private String fear;

    @Column(name = "fear_keyword", columnDefinition = "TEXT")
    private String fearKeyword;

    @Column(name = "joy", length = 10)
    private String joy;

    @Column(name = "joy_keyword", columnDefinition = "TEXT")
    private String joyKeyword;

    @Column(name = "anxiety", length = 10)
    private String anxiety;

    @Column(name = "anxiety_keyword", columnDefinition = "TEXT")
    private String anxietyKeyword;

    @Column(name = "sadness", length = 10)
    private String sadness;

    @Column(name = "sadness_keyword", columnDefinition = "TEXT")
    private String sadnessKeyword;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum YesNo {
        yes, no
    }

    public enum Frequency {
        daily("daily"),
        TWO_TO_THREE_DAYS("2-3days"),
        ALL_AT_ONCE("all-at-once");

        private final String value;

        Frequency(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Frequency fromValue(String value) {
            for (Frequency frequency : Frequency.values()) {
                if (frequency.value.equals(value)) {
                    return frequency;
                }
            }
            throw new IllegalArgumentException("Unknown frequency: " + value);
        }
    }
}

