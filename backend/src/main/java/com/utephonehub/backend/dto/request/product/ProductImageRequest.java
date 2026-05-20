package com.utephonehub.backend.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    @NotBlank(message = "URL hình ảnh không được để trống")
    @Size(max = 255, message = "URL hình ảnh không được vượt quá 255 ký tự")
    private String imageUrl;

    @Size(max = 255, message = "Alt text không được vượt quá 255 ký tự")
    private String altText;

    @NotNull(message = "Thứ tự ảnh không được để trống")
    private Integer imageOrder;

    @NotNull(message = "Trạng thái ảnh chính không được để trống")
    private Boolean isPrimary;
}
