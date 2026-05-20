package com.utephonehub.backend.exception;

import lombok.Getter;

@Getter
public class MaxQuantityExceededException extends RuntimeException {
    
    private final Integer maxQuantity;
    private final Integer requestedQuantity;
    
    public MaxQuantityExceededException(Integer maxQuantity, Integer requestedQuantity) {
        super(String.format("Maximum %d items allowed per product", maxQuantity));
        this.maxQuantity = maxQuantity;
        this.requestedQuantity = requestedQuantity;
    }
}
