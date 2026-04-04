package org.innowise.userservice.repository;

import org.innowise.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    List<User> findByName(String name);

    @Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.cards
    WHERE u.id = :id
    """)
    Optional<User> findByIdWithCards(@Param("id") Long id);

    Optional<User> findByEmail(@Param("email") String email);
}
