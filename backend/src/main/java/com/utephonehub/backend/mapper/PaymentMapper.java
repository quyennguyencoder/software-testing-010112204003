package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.payment.PaymentResponse;
import com.utephonehub.backend.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.paymentMethod", target = "paymentMethod")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    @Mapping(target = "amount", expression = "java(payment.getAmount() != null ? payment.getAmount().longValue() : null)")
    PaymentResponse toPaymentResponse(Payment payment);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
