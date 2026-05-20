package com.utephonehub.backend.dto.response.guestcart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCartSessionResponse {
    private String guestCartId;
}
