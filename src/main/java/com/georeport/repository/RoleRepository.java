package com.georeport.repository;

import com.georeport.entity.Role;
import com.georeport.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its name
     */
    Optional<Role> findByName(RoleType name);

    /**
     * Check if a role exists by name
     */
    boolean existsByName(RoleType name);
}
