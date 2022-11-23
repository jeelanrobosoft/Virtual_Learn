package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OngoingResponse {
    private Integer courseId;
    private String courseName;
    private String coursePhoto;
    private Integer completedChapter;
    private Integer totalChapter;
}
