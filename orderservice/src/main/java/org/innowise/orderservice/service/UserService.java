package org.innowise.orderservice.service;

import org.innowise.orderservice.dto.UserResponseDTO;

public interface UserService {

    /**
     * Get a user by e-mail if his account exists;
     *
     * @param email the e-mail of a user to get;
     * @return returns user's data (without cards).
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Get a user by id if his account exists;
     *
     * @param id the id of a user to get;
     * @return returns user's data (without cards).
     */
    UserResponseDTO getUserById(Long id);
}
