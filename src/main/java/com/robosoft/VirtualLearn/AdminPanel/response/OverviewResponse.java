package com.robosoft.VirtualLearn.AdminPanel.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverviewResponse
{
    private String coursePhoto;
    private String courseName;
    private String categoryName;
    private Integer chapterCount;
    private Integer lessonCount;
    private String courseTagLine;
    private String previewVideo;
    private String description;
    private String courseDuration;
    private Integer courseMaterialId;
    private Integer  testCount;
    private String  learningOutCome;
    private String requirements;
    private String instructorName;
    private String url;
    private String instructorDescription;
    private String profilePhoto;


}
