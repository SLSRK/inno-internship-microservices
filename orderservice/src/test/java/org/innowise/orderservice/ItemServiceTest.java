package org.innowise.orderservice;

import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;
import org.innowise.orderservice.exception.NotFoundException;
import org.innowise.orderservice.mapper.ItemMapper;
import org.innowise.orderservice.model.Item;
import org.innowise.orderservice.repository.ItemRepository;
import org.innowise.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;
    private ItemRequestDTO requestDTO;
    private ItemResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setPrice(500L);

        requestDTO = new ItemRequestDTO("Test Item", 500L);

        responseDTO = new ItemResponseDTO(
                1L,
                "Test Item",
                "5.00",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createItem_shouldReturnSavedItem() {
        when(itemMapper.toEntity(requestDTO)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDTO(item)).thenReturn(responseDTO);

        ItemResponseDTO result = itemService.createItem(requestDTO);
        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("5.00", result.getPrice());

        verify(itemMapper).toEntity(requestDTO);
        verify(itemRepository).save(item);
        verify(itemMapper).toDTO(item);
    }

    @Test
    void getItem_shouldReturnItem_whenExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toDTO(item)).thenReturn(responseDTO);

        ItemResponseDTO result = itemService.getItem(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());

        verify(itemRepository).findById(1L);
        verify(itemMapper).toDTO(item);
    }

    @Test
    void getItem_shouldThrowException_whenNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(1L));
        verify(itemRepository).findById(1L);
        verifyNoInteractions(itemMapper);
    }
}
