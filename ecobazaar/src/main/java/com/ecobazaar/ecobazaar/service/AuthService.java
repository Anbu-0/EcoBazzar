package com.ecobazaar.ecobazaar.service;

import com.ecobazaar.ecobazaar.dto.RegisterRequest;
import com.ecobazaar.ecobazaar.model.Role;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.RoleRepository;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest request) {
        // 1. Check if user already exists by email
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        // 2. Validate and process role
        if(request.getRole() == null || request.getRole().trim().isEmpty()) {
            return "Role is required!";
        }
        
        String chosenRole = request.getRole().toUpperCase();
        
        // 3. Prevent self-registration as ADMIN
        if(chosenRole.equals("ADMIN")) {
            return "Cannot register as Admin!";
        }
        
        String roleName = "ROLE_" + chosenRole;
        
        // 4. Fetch role from database
        Role userRole = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // 5. Create new User object
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // 6. Save to database
        userRepository.save(user);
        return "User registered successfully !";
    }
}