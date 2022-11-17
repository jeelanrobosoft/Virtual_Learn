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
    private String previewVideo;
    private String courseName;
    private String chapterCount;
    private String courseDuration;
}
