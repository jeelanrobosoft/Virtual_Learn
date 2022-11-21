package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category
{
    private Integer categoryId;
    private String categoryName;
    private String categoryPhoto;
    public Category(Integer categoryId)
    {
        this.categoryId = categoryId;
    }

}
