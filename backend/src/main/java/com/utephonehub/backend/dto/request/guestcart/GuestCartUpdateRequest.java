package com.utephonehub.backend.dto.request.guestcart;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCartUpdateRequest {

    /**
     * Danh sách items của guest cart.
     * Cho phép empty để clear cart.
     */
    @Valid
    private List<GuestCartItemRequest> items;
}
