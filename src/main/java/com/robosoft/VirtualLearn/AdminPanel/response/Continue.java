package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Continue {
    private Integer chapterNumber;
    private Integer lessonNumber;
    private Integer lessonId;
    private String pauseTime;
    private String videoLink;
}
