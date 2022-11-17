package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User
{
    private String mobileNo;
    private String fullName;
    private String userName;
    private String email;
    private Date dateOfBirth;
    private String profilePhoto;
    private Integer occupation;
    private String gender;
    private String twitterLink;
    private String faceBookLink;

    public User(int occupation)
    {
        this.occupation = occupation;
    }
}
