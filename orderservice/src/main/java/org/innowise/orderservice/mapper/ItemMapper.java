package org.innowise.orderservice.mapper;

import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;
import org.innowise.orderservice.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "price", expression = "java(mapPrice(item.getPrice()))")
    ItemResponseDTO toDTO(Item item);

    Item toEntity(ItemRequestDTO itemRequestDTO);

    default String mapPrice(Long priceInCents) {
        if (priceInCents == null) return null;
        return String.format(Locale.US,"%.2f", priceInCents / 100.0);
    }
}
