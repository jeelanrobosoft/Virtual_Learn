package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {
    private String userName;
    private Integer courseId;
    private Date joinDate;
    private Date completedDate;
    private Integer courseScore;



    public Enrollment(String userName)
    {
        this.userName = userName;
    }

    public Enrollment(Integer courseId)
    {
        this.courseId = courseId;
    }

    public Enrollment(String userName, Integer courseId, Date joinDate, Integer courseScore)
    {
        this.userName = userName;
        this.courseId = courseId;
        this.joinDate = joinDate;
        this.courseScore = courseScore;
    }
}
