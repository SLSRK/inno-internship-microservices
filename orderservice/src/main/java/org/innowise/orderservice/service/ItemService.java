package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;

public interface ItemService {

    ItemResponseDTO createItem(ItemRequestDTO itemRequestDTO);

    ItemResponseDTO getItem(Long id);
}
