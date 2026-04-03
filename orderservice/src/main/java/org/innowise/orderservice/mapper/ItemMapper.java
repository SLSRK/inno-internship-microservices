package org.innowise.orderservice.mapper;

import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;
import org.innowise.orderservice.model.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    //@Mapping(target = "orderItems", source = "orderItems")
    ItemResponseDTO toDTO(Item item);

    //@Mapping(target = "orderItems", source = "orderItems")
    Item toEntity(ItemRequestDTO itemRequestDTO);
}
