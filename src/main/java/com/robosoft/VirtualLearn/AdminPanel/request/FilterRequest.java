package com.robosoft.VirtualLearn.AdminPanel.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest
{
    private List<Integer> categoryId;
    private List<Integer> chapterStartCount;
    private List<Integer> chapterEndCount;
}