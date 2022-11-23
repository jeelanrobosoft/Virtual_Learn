package com.robosoft.VirtualLearn.AdminPanel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Question {
    private Integer questionId;
    private String questionName;
    private Integer testId;
    private String option_1;
    private String option_2;
    private String option_3;
    private String option_4;
    private String correctAnswer;

    public Question(Integer questionId, String questionName, String option_1, String option_2, String option_3, String option_4, String correctAnswer) {
        this.questionId = questionId;
        this.questionName = questionName;
        this.option_1 = option_1;
        this.option_2 = option_2;
        this.option_3 = option_3;
        this.option_4 = option_4;
        this.correctAnswer = correctAnswer;
    }
}
