package com.ecobazaar.ecobazaar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.ecobazaar.ecobazaar.model.Role;
import com.ecobazaar.ecobazaar.repository.RoleRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String[] roles = {"ROLE_CONSUMER", "ROLE_FARMER", "ROLE_DISTRIBUTOR", "ROLE_RETAILER", "ROLE_ADMIN"};
        
        for(String roleName : roles) {
        	// ✅ use the correct repository method and field name
            if(!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println(roleName + " created");
            } else {
                System.out.println(roleName + " already exists");
            }
        }
        
        System.out.println("✅ Role seeding completed successfully!");
    }
}