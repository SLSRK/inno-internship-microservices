package org.innowise.orderservice.mapper;

import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "totalPrice", expression = "java(mapPrice(order.getTotalPrice()))")
    OrderResponseDTO toDTO(Order order);

    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderRequestDTO orderRequestDTO);

    default String mapPrice(Long priceInCents) {
        if (priceInCents == null) return null;
        return String.format("%.2f", priceInCents / 100.0);
    }
}
