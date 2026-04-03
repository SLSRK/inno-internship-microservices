package org.innowise.orderservice.mapper;

import org.innowise.orderservice.dto.OrderRequestDTO;
import org.innowise.orderservice.dto.OrderResponseDTO;
import org.innowise.orderservice.dto.OrderUpdateDTO;
import org.innowise.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    OrderResponseDTO toDTO(Order order);

    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderRequestDTO orderRequestDTO);

    @Mapping(target = "orderItems", ignore = true)
    Order toEntityWithStatus(OrderUpdateDTO orderUpdateDTO);
}
