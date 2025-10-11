package com.ecobazaar.ecobazaar.service;

import com.ecobazaar.ecobazaar.dto.AuthResponse;
import com.ecobazaar.ecobazaar.dto.LoginRequest;
import com.ecobazaar.ecobazaar.dto.RegisterRequest;
import com.ecobazaar.ecobazaar.model.Role;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.RoleRepository;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, 
                       RoleRepository roleRepository, 
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return "Email already exists!";
            }

            if(request.getRole() == null || request.getRole().trim().isEmpty()) {
                return "Role is required!";
            }
            
            String chosenRole = request.getRole().toUpperCase();
            
            if(chosenRole.equals("ADMIN")) {
                return "Cannot register as Admin!";
            }
            
            String roleName = "ROLE_" + chosenRole;
            
            Role userRole = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            userRepository.save(user);
            return "User registered successfully as " + chosenRole + "!";

        } catch (RuntimeException e) {
            e.printStackTrace();
            return "Registration failed: " + e.getMessage();
        }
    }

    // NEW LOGIN METHOD
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 3. Generate token
        String token = jwtUtil.generateToken(user.getEmail());

        // 4. Get role (first role from set)
        String role = user.getRoles().iterator().next().getRoleName();

        // 5. Return token + role + email
        return new AuthResponse(token, role, user.getEmail());
    }
}