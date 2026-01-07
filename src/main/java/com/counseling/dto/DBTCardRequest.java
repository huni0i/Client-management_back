package com.counseling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DBTCardRequest {
    @NotBlank(message = "날짜는 필수입니다.")
    private String date;

    private HeaderInfo header;
    private DayDataInfo dayData;

    @Data
    public static class HeaderInfo {
        private String name;
        private String writtenDuringCounseling; // "yes" or "no"
        private String frequency; // "daily", "2-3days", "all-at-once"
    }

    @Data
    public static class DayDataInfo {
        private String impulse1Text;
        private String impulse1Intensity;
        private String action1Text;
        private String action1Intensity;
        private String thoughtText;
        private String thoughtIntensity;
        private String action2Text;
        private String action2Intensity;
        private String impulse2Text;
        private String impulse2Intensity;
        private String action3Text;
        private String action3Intensity;
        private String medication; // "yes" or "no"
        private String targetBehavior1;
        private String targetBehavior2;
        private String skillUse;
        private String sleepTime;
        private String wakeTime;
        private String anger;
        private String angerKeyword;
        private String fear;
        private String fearKeyword;
        private String joy;
        private String joyKeyword;
        private String anxiety;
        private String anxietyKeyword;
        private String sadness;
        private String sadnessKeyword;
    }
}

