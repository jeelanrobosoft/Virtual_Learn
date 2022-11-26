package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalTest {
    private Integer testId;
    private String testName;
    private String testDuration;
    private Integer questionsCount;
    private boolean state1=false;
    private boolean state2=false;
    private boolean state3=false;
    private boolean state4=false;
    private List<Question> questions;
}
