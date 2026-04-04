package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;

public interface ItemService {

    /**
     * Creates new item for orders;
     *
     * @param itemRequestDTO properties of the new item;
     * @return returns full data of a created item.
     */
    ItemResponseDTO createItem(ItemRequestDTO itemRequestDTO);

    /**
     * Get an item of it exists
     *
     * @param id the id of item to get;
     * @return returns full data of an item.
     */
    ItemResponseDTO getItem(Long id);
}
