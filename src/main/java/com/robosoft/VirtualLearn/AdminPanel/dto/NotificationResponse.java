package com.robosoft.VirtualLearn.AdminPanel.dto;


import lombok.Data;

@Data
public class NotificationResponse
{
    private Integer notificationId;
    private String description;
    private String timeStamp;
    private String notificationUrl;
    private boolean readStatus;
}
