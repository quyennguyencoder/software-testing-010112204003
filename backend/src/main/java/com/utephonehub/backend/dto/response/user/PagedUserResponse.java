package com.utephonehub.backend.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedUserResponse {

    private List<UserResponse> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
