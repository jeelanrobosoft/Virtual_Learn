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
    private Integer chapterNumber;
    private String chapterName;
    private String testName;
    private String testDuration;
    private Integer questionsCount;
    private List<Question> questions;
}
