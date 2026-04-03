package org.innowise.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemRequestDTO {

    @NotBlank(message = "Name cannot be empty")
    String name;

    @NotNull(message = "Price cannot be null")
    Long price;
}
