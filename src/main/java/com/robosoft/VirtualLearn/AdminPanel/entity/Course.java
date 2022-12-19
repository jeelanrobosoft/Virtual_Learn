package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    private Integer courseId;
    private String coursePhoto;
    private String courseName;
    private String previewVideo;
    private Integer categoryId;
    private Integer subCategoryId;
    private String courseDuration;

    public Course(Integer courseId) {
        this.courseId = courseId;
    }

    //chk
    public Course(String courseDuration) {
        this.courseDuration = courseDuration;
    }

    public Course(String coursePhoto, String courseName) {
        this.coursePhoto = coursePhoto;
        this.courseName = courseName;
    }
}
