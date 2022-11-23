package com.robosoft.VirtualLearn.AdminPanel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Overview {
    private Integer courseId;
    private String courseTagLine;
    private String description;
    private Integer chapterCount;
    private Integer lessonCount;
    private Integer courseMaterialId;
    private Integer testCount;
    private String learningOutCome;
    private String requirements;
    private Integer instructorId;
    private String difficultyLevel;

    public Overview(Integer courseId, String courseTagLine, String description, String learningOutCome, String requirements, Integer instructorId, String difficultyLevel) {
        this.courseId = courseId;
        this.courseTagLine = courseTagLine;
        this.description = description;
        this.learningOutCome = learningOutCome;
        this.requirements = requirements;
        this.instructorId = instructorId;
        this.difficultyLevel = difficultyLevel;
    }
}
