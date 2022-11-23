package com.robosoft.VirtualLearn.AdminPanel.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChapterResponse {
    private Integer chapterId;
    private Integer chapterNumber;
    private String chapterName;
    private Boolean chapterStatus;
    private Boolean chapterCompletedStatus;
    private List<LessonResponse> lessonResponses;
    private Integer testId;
    private String testName;
    private String testDuration;
    private Float chapterTestPercentage;
    private Integer questionCount;
    private Boolean disableStatus = true;
}