package org.innowise.orderservice.mapper;

import org.innowise.orderservice.dto.OrderItemRequestDTO;
import org.innowise.orderservice.dto.OrderItemResponseDTO;
import org.innowise.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface OrderItemMapper {

    @Mapping(source = "order.id", target = "orderId")
    OrderItemResponseDTO toDTO(OrderItem orderItem);

    @Mapping(source = "orderId", target = "order.id")
    //@Mapping(target = "item", ignore = true)
    OrderItem toEntity(OrderItemRequestDTO orderItemRequestDTO);
}
