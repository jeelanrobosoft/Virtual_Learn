package com.robosoft.VirtualLearn.AdminPanel.dto;


import lombok.Data;

@Data
public class ResultAnswerRequest
{
    private Integer questionId;
    private String questionName;
    private String option_1;
    private String option_2;
    private String option_3;
    private String option_4;
    private String correctAnswer;
    private String userAnswer;
    private String userAnswerStatus;
}
