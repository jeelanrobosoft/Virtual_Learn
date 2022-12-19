package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Counts {
    private Integer chapterCount;
    private Integer lessonCount;
    private Integer testCount;
}
