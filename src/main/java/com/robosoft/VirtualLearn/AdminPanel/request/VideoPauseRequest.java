package com.robosoft.VirtualLearn.AdminPanel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPauseRequest {
    private Time pauseTime;
    private Integer lessonId;
    private Integer chapterId;
    private Integer courseId;
}
