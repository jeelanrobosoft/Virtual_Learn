package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNotificationResponse {
    private Long multicast_id;
    private Integer success;
    private Integer failure;
    private Integer canonical_ids;
    private List<PushNotificationMessageId> results;

}
