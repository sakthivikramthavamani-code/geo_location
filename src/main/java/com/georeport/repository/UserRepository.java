package com.georeport.repository;

import com.georeport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find users by ward
     */
    List<User> findByWard(String ward);

    /**
     * Find all admin users
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_ADMIN'")
    List<User> findAllAdmins();

    /**
     * Find all citizen users
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_CITIZEN'")
    List<User> findAllCitizens();

    /**
     * Search users by name or email
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);
}
