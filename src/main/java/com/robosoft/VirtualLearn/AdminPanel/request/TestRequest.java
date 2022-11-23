package com.robosoft.VirtualLearn.AdminPanel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestRequest {
    private Integer testId;
    private String testName;
    private Integer chapterId;
    private Time testDuration;
    private Integer passingGrade;
}
