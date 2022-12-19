package com.robosoft.VirtualLearn.AdminPanel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonRequest {
    private int lessonNumber;
    private int courseId;
    private int chapterId;
    private String lessonName;
    private String lessonDuration;
    private MultipartFile videoLink;
}
