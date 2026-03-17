package org.innowise.userservice.mapper;

import org.innowise.userservice.dto.PaymentCardDTO;
import org.innowise.userservice.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDTO(PaymentCard paymentCard);

    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);
}
