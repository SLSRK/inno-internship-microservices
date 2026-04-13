package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    /**
     * Get a user by e-mail if his account exists;
     *
     * @param email the e-mail of a user to get;
     * @return returns user's data (without cards).
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Get a user by ID if his account exists;
     *
     * @param id the ID of a user to get;
     * @return returns user's data (without cards).
     */
    UserResponseDTO getUserById(Long id);

    /**
     * Get users by IDs if theis accounts exist;
     *
     * @param ids the list of IDs of users to get;
     * @return returns users' data (without cards).
     */
    List<UserResponseDTO> getUsersByIds(List<Long> ids);
}
