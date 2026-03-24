package com.georeport.security;

import com.georeport.entity.User;
import com.georeport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation.
 * Loads user details from database for Spring Security authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "User not found with email: " + email));

                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                user.getIsActive(),
                                true,
                                true,
                                true,
                                user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                                .collect(Collectors.toList()));
        }

        /**
         * Load user by ID
         */
        @Transactional(readOnly = true)
        public UserDetails loadUserById(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                user.getIsActive(),
                                true,
                                true,
                                true,
                                user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                                                .collect(Collectors.toList()));
        }
}
