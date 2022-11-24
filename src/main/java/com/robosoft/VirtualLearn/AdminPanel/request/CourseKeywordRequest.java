package com.robosoft.VirtualLearn.AdminPanel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.sql.In;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseKeywordRequest {
    private Integer courseId;
    private String keyword;
}
