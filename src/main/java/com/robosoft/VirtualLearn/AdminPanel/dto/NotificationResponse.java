package com.robosoft.VirtualLearn.AdminPanel.dto;


import lombok.Data;

import java.util.Date;

@Data
public class NotificationResponse
{
    private Integer notificationId;
    private String description;
    private Date timeStamp;
    private String notificationUrl;
}
