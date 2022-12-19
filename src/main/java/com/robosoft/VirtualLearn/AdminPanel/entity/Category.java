package com.robosoft.VirtualLearn.AdminPanel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    private Integer categoryId;
    private String categoryName;
    private String categoryPhoto;
    private Boolean status = false;

    public Category(Integer categoryId, String categoryName, String categoryPhoto) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryPhoto = categoryPhoto;
    }

    public Category(Integer categoryId) {
        this.categoryId = categoryId;
    }

}
