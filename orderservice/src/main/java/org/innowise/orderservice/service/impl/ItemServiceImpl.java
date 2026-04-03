package org.innowise.orderservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;
import org.innowise.orderservice.exception.NotFoundException;
import org.innowise.orderservice.mapper.ItemMapper;
import org.innowise.orderservice.repository.ItemRepository;
import org.innowise.orderservice.service.ItemService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemResponseDTO createItem(ItemRequestDTO itemRequestDTO){
        return itemMapper.toDTO(itemRepository
                .save(itemMapper.toEntity(itemRequestDTO)));
    }

    public ItemResponseDTO getItem(Long id){
        return itemMapper.toDTO(itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found")));
    }
}
