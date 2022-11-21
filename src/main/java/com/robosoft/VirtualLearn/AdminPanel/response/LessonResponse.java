package com.robosoft.VirtualLearn.AdminPanel.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LessonResponse
{
    private Integer lessonNumber;
    private String lessonName;
    private String lessonDuration;
    private String videoLink;
    private Boolean lessonCompletedStatus;
    private Boolean lessonStatus;
}
