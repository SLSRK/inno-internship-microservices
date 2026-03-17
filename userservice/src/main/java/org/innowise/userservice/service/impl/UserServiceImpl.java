package org.innowise.userservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.innowise.userservice.dto.UserDTO;
import org.innowise.userservice.exception.NotFoundException;
import org.innowise.userservice.mapper.UserMapper;
import org.innowise.userservice.model.PaymentCard;
import org.innowise.userservice.model.User;
import org.innowise.userservice.repository.PaymentCardRepository;
import org.innowise.userservice.repository.UserRepository;
import org.innowise.userservice.service.UserService;
import org.innowise.userservice.specification.UserSpecification;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PaymentCardRepository paymentCardRepository;
    private final UserMapper userMapper;

    private final String notFound = "User not found";
    private final String notActive = "User not active";

    @Transactional
    @CachePut(value = "users", key = "#result.id")
    public UserDTO createUser(UserDTO userDTO){
        User user = userMapper.toEntity(userDTO);

        if(user.getCards() != null){
            for(PaymentCard paymentCard : user.getCards()){
                paymentCard.setUser(user);
            }
        }
        return userMapper.toDTO(userRepository.save(user));
    }

    @Cacheable(value = "users", key = "#id + '_' + #withCards")
    public UserDTO getUserById(Long id, boolean withCards){
        User user;

        if (withCards) {
            user = userRepository.findByIdWithCards(id)
                    .orElseThrow(() -> new NotFoundException(notFound));
        } else {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(notFound));
        }

        if(!user.getActive()){
            throw new RuntimeException(notActive);
        }
        return withCards
                ? userMapper.toDTOWithCards(user)
                : userMapper.toDTO(user);
    }

    @Cacheable(value = "users")
    public Page<UserDTO> getAllUsers(String name, String surname, int page, int size, Boolean active) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname))
                .and(UserSpecification.isActive(active));

        return userRepository.findAll(spec, pageable)
                .map(userMapper :: toDTO);
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDTO updateUser(Long id, UserDTO userDTO){
        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException(notFound));

        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setBirthDate(userDTO.getBirthDate());
        user.setEmail(userDTO.getEmail());

        user.getCards().clear();
        if (userDTO.getCards() != null) {
            for (PaymentCard paymentCard : userMapper.toEntity(userDTO).getCards()) {
                paymentCard.setUser(user);
                user.getCards().add(paymentCard);
            }
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDTO setActive(Long id, boolean active){
        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException(notFound));

        for(PaymentCard paymentCard : user.getCards()){
            paymentCard.setActive(active);
        }
        user.setActive(active);
        return userMapper.toDTO(userRepository.save(user));
    }
}
