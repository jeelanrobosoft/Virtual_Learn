package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularCourseInEachCategory {
    private String courseName;
    private Integer courseId;
    private String coursePhoto;
    private Integer chapterCount;
    private String courseDuration;
    private String previewVideo;
}
