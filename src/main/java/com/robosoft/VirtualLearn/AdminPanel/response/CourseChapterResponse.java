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
public class CourseChapterResponse {
    private String courseName;
    private String categoryName;
    private Integer chapterCount;
    private Integer lessonCount;
    private Integer testCount;
    private String courseDuration;
    private List<ChapterResponse> chapterResponses;
    private Boolean courseCompletedStatus;
}
