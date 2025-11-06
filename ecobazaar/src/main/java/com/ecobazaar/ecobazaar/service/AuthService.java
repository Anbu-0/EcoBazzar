package com.ecobazaar.ecobazaar.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ecobazaar.ecobazaar.dto.AuthResponse;
import com.ecobazaar.ecobazaar.dto.LoginRequest;
import com.ecobazaar.ecobazaar.dto.RegisterRequest;
import com.ecobazaar.ecobazaar.model.Role;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.RoleRepository;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.security.JwtUtil;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        // ✅ Only allow 4 roles during register
        if (!Set.of("CONSUMER", "FARMER", "DISTRIBUTOR", "RETAILER")
                .contains(request.getRole().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot register as ADMIN!");
        }

        Role role = roleRepository.findByName("ROLE_" + request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(role));
        userRepository.save(user);

        return new AuthResponse(null, role.getName(), request.getEmail(), user.getId());
    }




    public AuthResponse login(LoginRequest login) {

        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String role = user.getRoles()
                .stream()
                .map(Role::getName)
                .findFirst()
                .orElse("ROLE_CONSUMER");

        // ✅ If user has ROLE_ADMIN because he was approved → he can login
        String token = jwtUtil.generateToken(login.getEmail(), role, user.getId());

        return new AuthResponse(token, role, login.getEmail(), user.getId());
    }



}