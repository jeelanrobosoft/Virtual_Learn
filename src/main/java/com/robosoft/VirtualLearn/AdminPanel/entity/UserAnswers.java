package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class UserAnswers {
    private Integer testId;
    List<Answers> userAnswers;

    UserAnswers() {
        userAnswers = new ArrayList<>();
    }
}
