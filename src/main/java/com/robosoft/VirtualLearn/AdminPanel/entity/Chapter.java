package com.robosoft.VirtualLearn.AdminPanel.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chapter {
    private Integer chapterId;
    private Integer courseId;
    private Integer chapterNumber;
    private String chapterName;
    private String chapterDuration;

    public Chapter(String chapterDuration) {
        this.chapterDuration = chapterDuration;
    }


}
