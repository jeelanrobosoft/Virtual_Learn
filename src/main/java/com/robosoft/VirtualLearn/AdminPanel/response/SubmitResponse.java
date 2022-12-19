package com.robosoft.VirtualLearn.AdminPanel.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitResponse {
    private float chapterTestPercentage;
    private Integer chapterNumber;
    private String courseName;
    private String chapterName;
}
