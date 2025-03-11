package com.api.api.DTO;

import lombok.Getter;
import lombok.Setter;

public class FitBitDTO {

    // SleepDTO.java
    @Getter @Setter
    public static class SleepDTO {
        private String startTime;
        private String endTime;
        private long duration;
        private int efficiency;
        private LevelsDTO levels;
    }

    // LevelsDTO.java
    @Getter @Setter
    public static class LevelsDTO {
        private SummaryDTO summary;
    }

    // SummaryDTO.java
    @Getter @Setter
    public static class SummaryDTO {
        private LevelDetailDTO deep;
        private LevelDetailDTO light;
        private LevelDetailDTO rem;
        private LevelDetailDTO wake;
    }

    // LevelDetailDTO.java
    @Getter @Setter
    public static class LevelDetailDTO {
        private int count;
        private int minutes;
        private int thirtyDayAvgMinutes;
    }

    // FoodDTO.java
    @Getter @Setter
    public static class FoodDTO {
        private int calories;
    }
    
}
