package com.robosoft.VirtualLearn.AdminPanel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopCourseResponse {
    private String categoryName;
    List<PopularCourseInEachCategory> popularCourseInEachCategoryList;
}
