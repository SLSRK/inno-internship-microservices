package org.innowise.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.orderservice.dto.ItemRequestDTO;
import org.innowise.orderservice.dto.ItemResponseDTO;
import org.innowise.orderservice.exception.AccessDeniedException;
import org.innowise.orderservice.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO itemRequestDTO,
                                                      Authentication authentication){
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!isAdmin){
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(itemService.createItem(itemRequestDTO));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItem(@PathVariable Long id){
        return ResponseEntity.ok(itemService.getItem(id));
    }
}
