package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreNotification {
    private String description;
    private Timestamp timestamp;
    private String notificationUrl;
}
