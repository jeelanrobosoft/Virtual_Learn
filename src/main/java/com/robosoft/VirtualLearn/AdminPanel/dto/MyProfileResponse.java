package com.robosoft.VirtualLearn.AdminPanel.dto;


import lombok.Data;

import java.util.Date;

@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyProfileResponse {
    private String profilePhoto;
    private String fullName;
    private String occupation;
    private Integer courseCompleted;
    private Integer chaptersCompleted;
    private Integer testsCompleted;
    private String userName;
    private String email;
    private String mobileNumber;
    private Date dateOfBirth;
}
