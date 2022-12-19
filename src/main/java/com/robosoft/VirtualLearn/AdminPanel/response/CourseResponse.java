package com.robosoft.VirtualLearn.AdminPanel.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponse {
    private Integer courseId;
    private String coursePhoto;
    private String previewVideo;
    private String courseName;
    private Integer chapterCount;
    private String courseDuration;
}
