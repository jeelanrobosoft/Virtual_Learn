package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.loading.PrivateClassLoader;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationBody {
    private String to;
    private Notification notification;
}
