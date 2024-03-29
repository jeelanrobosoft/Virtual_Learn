package com.robosoft.VirtualLearn.AdminPanel.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalTestResultResponse {
    private String congratulations;
    private float approvalRate;
    private String certificateUrl;
    private Integer courseId;
}
