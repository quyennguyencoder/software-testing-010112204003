package com.utephonehub.backend.dto.response.category;

import java.time.LocalDateTime;

import com.utephonehub.backend.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private Boolean hasChildren;
    private Integer childrenCount;  // Số lượng danh mục con trực tiếp
    private Integer productCount;   // Số lượng sản phẩm trong danh mục
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

