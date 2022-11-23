package com.robosoft.VirtualLearn.AdminPanel.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    private Integer categoryId;
    private String categoryName;
    private MultipartFile categoryPhoto;

    public CategoryRequest(String categoryName, MultipartFile categoryPhoto) {
        this.categoryName = categoryName;
        this.categoryPhoto = categoryPhoto;
    }
}
