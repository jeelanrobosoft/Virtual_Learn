package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.web.PortResolverImpl;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleTest {
    private Integer testId;
    private Integer chapterNumber;
    private String chapterName;
    private String testName;
    private String testDuration;
    private Integer questionsCount;
    private List<Question> questions;
}
