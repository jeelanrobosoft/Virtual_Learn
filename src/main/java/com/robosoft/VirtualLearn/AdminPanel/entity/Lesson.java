package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lesson implements Comparable<Lesson>{
    private Integer lessonId;
    private Integer lessonNumber;
    private Integer chapterId;
    private String lessonName;
    private String lessonDuration;
    private String videoLink;

    @Override
    public int compareTo(Lesson o) {
      return this.getLessonId().compareTo(o.lessonId);

    }


}
