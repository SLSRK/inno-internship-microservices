package org.innowise.userservice.service;

import org.innowise.userservice.dto.UserDTO;
import org.springframework.data.domain.Page;

public interface UserService {

    /**
     * Create a new user;
     *
     * @param userDTO data of the user to create;
     * @return returns the result of creating user.
     */
    UserDTO createUser(UserDTO userDTO);

    /**
     * Get an existing user by ID;
     *
     * @param id ID of the user to get;
     * @param withCards get with of without owned cards;
     * @return returns the user if it exists.
     */
    UserDTO getUserById(Long id, boolean withCards);

    /**
     * Get an existing user by email;
     *
     * @param email email of the user to get;
     * @return returns the user if it exists.
     */
    UserDTO getUserByEmail(String email);

    /**
     * Retrieves a paginated list of users with optional filtering;
     *
     * @param name optional  name to filter results;
     * @param surname optional surname to filter results;
     * @param active optional flag to filter users by active status;
     * @param page optional flag to filter users by active status;
     * @param size the number of records per page (must be > 0);
     * @return the users, that who match the given criteria.
     */
    Page<UserDTO> getAllUsers(String name, String surname, int page, int size, Boolean active);

    /**
     * Update a user's data;
     *
     * @param id ID of the user to update;
     * @param userDTO new data to be uploaded;
     * @return returns the result of updating the user.
     */
    UserDTO updateUser(Long id, UserDTO userDTO);

    /**
     * Activate/deactivate a user;
     *
     * @param id ID of a user;
     * @param active new state to be uploaded;
     * @return returns the result of changing the user's state.
     */
    UserDTO setActive(Long id, boolean active);
}
