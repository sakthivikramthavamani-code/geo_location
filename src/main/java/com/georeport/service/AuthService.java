package com.georeport.service;

import com.georeport.dto.AuthResponse;
import com.georeport.dto.LoginRequest;
import com.georeport.dto.RegisterRequest;
import com.georeport.dto.GoogleLoginRequest;
import com.georeport.entity.Role;
import com.georeport.entity.RoleType;
import com.georeport.entity.User;
import com.georeport.exception.BadRequestException;
import com.georeport.exception.ResourceNotFoundException;
import com.georeport.repository.RoleRepository;
import com.georeport.repository.UserRepository;
import com.georeport.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Service for authentication operations.
 * Handles user registration, login, and token management.
 */
@Service
public class AuthService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private JwtTokenProvider tokenProvider;

        /**
         * Register a new user.
         * The first registered user automatically becomes an admin.
         * All subsequent users are registered as citizens.
         * Uses SERIALIZABLE isolation to prevent race condition for first user admin
         * assignment.
         */
        @Transactional(isolation = Isolation.SERIALIZABLE)
        public AuthResponse register(RegisterRequest request) {
                // Check if email already exists
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new BadRequestException("Email is already registered");
                }

                // Get citizen role (all users get this)
                Role citizenRole = roleRepository.findByName(RoleType.ROLE_CITIZEN)
                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_CITIZEN"));

                // Create new user
                User user = User.builder()
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .fullName(request.getFullName())
                                .phone(request.getPhone())
                                .address(request.getAddress())
                                .ward(request.getWard())
                                .isActive(true)
                                .emailVerified(true)
                                .build();

                // Add citizen role to all users
                user.getRoles().add(citizenRole);

                // If this is the first user, also make them admin
                if (userRepository.count() == 0) {
                        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));
                        user.getRoles().add(adminRole);
                        System.out.println("==============================================");
                        System.out.println("  First user registered as ADMIN: " + request.getEmail());
                        System.out.println("==============================================");
                }

                user = userRepository.save(user);

                // Generate token
                String token = tokenProvider.generateToken(user.getEmail());

                return AuthResponse.of(
                                token,
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()),
                                tokenProvider.getExpirationMs());
        }

        /**
         * Authenticate user and return JWT token
         */
        @Transactional
        public AuthResponse login(LoginRequest request) {
                // Authenticate
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Get user
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Generate token
                String token = tokenProvider.generateToken(authentication);

                return AuthResponse.of(
                                token,
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()),
                                tokenProvider.getExpirationMs());
        }

        /**
         * Authenticate user via Google and return JWT token.
         * Auto-registers the user if they don't exist yet.
         */
        @Transactional
        public AuthResponse googleLogin(GoogleLoginRequest request) {
                // Find user by email or create new
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseGet(() -> {
                                        // Auto-register new Google user
                                        Role citizenRole = roleRepository.findByName(RoleType.ROLE_CITIZEN)
                                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_CITIZEN"));
                                        
                                        User newUser = User.builder()
                                                .email(request.getEmail())
                                                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // Random password
                                                .fullName(request.getFullName())
                                                .isActive(true)
                                                .emailVerified(true)
                                                .build();
                                        
                                        newUser.getRoles().add(citizenRole);
                                        return userRepository.save(newUser);
                                });

                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                // Generate token directly for the user's email
                String token = tokenProvider.generateToken(user.getEmail());

                return AuthResponse.of(
                                token,
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()),
                                tokenProvider.getExpirationMs());
        }

        /**
         * Get current authenticated user
         */
        public User getCurrentUser() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        }
}
