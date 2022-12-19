package com.robosoft.VirtualLearn.AdminPanel.dto;


import lombok.Data;

@Data
public class SideBarResponse {
    private String profilePhoto;
    private String fullName;
    private String occupation;
    private Integer notificationCount;
}
