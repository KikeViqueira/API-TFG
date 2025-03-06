package com.api.api.DTO;

import java.util.HashMap;
import java.util.List;

import com.api.api.model.OnboardingAnswer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
 * DTO que sirve para pasarle al user las respuestas que ha dado en el Onboarding a las correspondientes preguntas 
*/
public class OnboardingAnswerDTO {

    private HashMap<String, String> answers;

    public OnboardingAnswerDTO(List<OnboardingAnswer> onboardingAnswer) {
        this.answers = new HashMap<>();
        for (OnboardingAnswer answer : onboardingAnswer) {
            this.answers.put(answer.getQuestion(), answer.getAnswer());
        }
    }
}
