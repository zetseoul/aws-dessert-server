package com.dessert.repository;

import com.dessert.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.usersPkId = :id")
    Optional<User> findForPreferenceUpdate(@org.springframework.data.repository.query.Param("id") Integer id);
    boolean existsByUsersId(String usersId);

    boolean existsByNickname(String nickname);

    Optional<User> findByUsersId(String usersId);
}
