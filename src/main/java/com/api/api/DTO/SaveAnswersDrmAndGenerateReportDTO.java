package com.api.api.DTO;

import java.util.HashMap;
import java.util.List;

import com.api.api.model.DrmAnswer;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SaveAnswersDrmAndGenerateReportDTO {
    
    private HashMap<String, String> answersDRM;

    private String reportGenerated;

    public SaveAnswersDrmAndGenerateReportDTO(List<DrmAnswer> drmAnswers, String reportGenerated){
        this.answersDRM = new HashMap<>();
        for (DrmAnswer answer : drmAnswers){
            this.answersDRM.put(answer.getQuestion(), answer.getAnswer());
        }
        this.reportGenerated = reportGenerated;
    }
}
