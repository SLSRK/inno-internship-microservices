package org.innowise.userservice.mapper;

import org.innowise.userservice.dto.UserDTO;
import org.innowise.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {PaymentCardMapper.class})
public interface UserMapper {
    @Mapping(target = "cards", ignore = true)
    UserDTO toDTO(User user);

    @Mapping(target = "cards", source = "cards")
    UserDTO toDTOWithCards(User user);

    User toEntity(UserDTO userDTO);
}
