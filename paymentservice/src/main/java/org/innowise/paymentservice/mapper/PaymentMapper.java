package org.innowise.paymentservice.mapper;

import org.innowise.paymentservice.dto.PaymentRequestDTO;
import org.innowise.paymentservice.dto.PaymentResponseDTO;
import org.innowise.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    public Payment toEntity(PaymentRequestDTO paymentRequestDTO);

    @Mapping(target = "status", expression = "java(payment.getStatus().name())")
    @Mapping(target = "paymentAmount", expression = "java(mapAmount(payment.getPaymentAmount()))")
    public PaymentResponseDTO toDTO(Payment payment);

    default String mapAmount(Long amountInCents) {
        if (amountInCents == null) return null;
        return String.format(Locale.US,"%.2f", amountInCents / 100.0);
    }
}
