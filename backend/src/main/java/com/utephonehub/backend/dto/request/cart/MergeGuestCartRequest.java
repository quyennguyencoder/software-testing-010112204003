package com.utephonehub.backend.dto.request.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeGuestCartRequest {

    private List<GuestCartItem> guestCartItems;

    /**
     * Optional guestCartId lưu trong Redis. Nếu có, backend sẽ load items từ Redis để merge.
     * Giữ backward-compatible: nếu không có guestCartId thì dùng guestCartItems như trước.
     */
    private String guestCartId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestCartItem {
        private Long productId;
        private Integer quantity;
    }
}
