package com.api.api.DTO;

import com.api.api.model.SleepLogAnswer;

import lombok.Getter;
import lombok.Setter;

//DTO que se va a utilizar para obtener las respuestas de un objeto SleepLogAnswer
@Getter @Setter
public class SleepLogAnswerDTO {

    private Long id;
    private String sleepTime;
    private String wakeUpTime;
    private float duration;
    private String answer1;
    private String answer2;

    public SleepLogAnswerDTO(SleepLogAnswer sleepLogAnswer) {
        this.id = sleepLogAnswer.getId();
        this.sleepTime = sleepLogAnswer.getSleepTime().toString();
        this.wakeUpTime = sleepLogAnswer.getWakeUpTime().toString();
        this.duration = sleepLogAnswer.getDuration();
        this.answer1 = sleepLogAnswer.getAnswer1();
        this.answer2 = sleepLogAnswer.getAnswer2();
    } 
}
