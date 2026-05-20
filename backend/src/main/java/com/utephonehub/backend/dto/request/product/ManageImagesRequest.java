package com.utephonehub.backend.dto.request.product;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManageImagesRequest {

    @NotEmpty(message = "Danh sách hình ảnh không được để trống")
    @Size(max = 10, message = "Tối đa 10 ảnh cho 1 sản phẩm")
    @Valid
    private List<ProductImageRequest> images;
}
