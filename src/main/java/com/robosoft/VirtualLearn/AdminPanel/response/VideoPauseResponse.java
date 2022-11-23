package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPauseResponse {
    private String userName;
    private String pauseTime;
    private Integer lessonId;
    private Integer chapterId;
    private Integer courseId;

    public VideoPauseResponse(String userName, String pauseTime, Integer lessonId, Integer chapterId) {
        this.userName = userName;
        this.pauseTime = pauseTime;
        this.lessonId = lessonId;
        this.chapterId = chapterId;
    }
}
