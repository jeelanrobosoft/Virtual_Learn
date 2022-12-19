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
public class OverviewResponse {
    private Boolean enrolled;
    private Integer courseId;
    private String coursePhoto;
    private String courseName;
    private String categoryName;
    private Integer chapterCount;
    private Integer lessonCount;
    private String courseTagLine;
    private String previewVideo;
    private String previewVideoName;
    private String previewVideoDuration;
    private String description;
    private String courseDuration;
    private Integer courseMaterialId;
    private Integer testCount;
    private List<String> learningOutCome;
    private List<String> requirements;
    private String instructorName;
    private String designation;
    private String url;
    private String instructorDescription;
    private String profilePhoto;
}
