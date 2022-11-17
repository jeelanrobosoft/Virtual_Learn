package com.robosoft.VirtualLearn.AdminPanel.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeAllCourse
{
    private Integer courseId;
    private String coursePhoto;
    private String courseName;
    private Integer categoryId;
    private Integer chapterCount;

    public HomeAllCourse(String coursePhoto, String courseName, int categoryId, int chapterCount)
    {
        this.coursePhoto = coursePhoto;
        this.courseName = courseName;
        this.categoryId = categoryId;
        this.chapterCount = chapterCount;
    }

    
}
