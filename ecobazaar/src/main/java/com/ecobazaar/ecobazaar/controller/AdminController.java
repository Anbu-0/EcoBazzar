package com.ecobazaar.ecobazaar.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecobazaar.ecobazaar.dto.AdminOverview;
import com.ecobazaar.ecobazaar.model.Role;
import com.ecobazaar.ecobazaar.model.User;
import com.ecobazaar.ecobazaar.repository.RoleRepository;
import com.ecobazaar.ecobazaar.repository.UserRepository;
import com.ecobazaar.ecobazaar.service.AdminOverviewService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	
    private final AdminOverviewService overviewService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AdminController(AdminOverviewService overviewService,
                           UserRepository userRepo,
                           RoleRepository roleRepo) {
        this.overviewService = overviewService;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/overview")
    public AdminOverview getOverview() {
    	return overviewService.getOverview();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/promote/{userId}")
    public String promoteToAdmin(@PathVariable Long userId,
    		@AuthenticationPrincipal UserDetails principal) {
    	
    	User target = userRepo.findById(userId)
    			.orElseThrow(()-> new RuntimeException("User not found"));
    	
    	if(principal!=null&&principal.getUsername().equalsIgnoreCase(target.getEmail())) {
    		throw new RuntimeException("Admins cannot promote themselves");
    	}
    	
    	Role roleAdmin = roleRepo.findByName("ROLE_ADMIN")
    			.orElseThrow(()->new RuntimeException("Role admin is missing"));
    	
    	if(target.getRoles().stream().noneMatch(r->"ROLE_ADMIN".equals(r.getName()))) {
    		target.getRoles().add(roleAdmin);
    		userRepo.save(target);
    		
    		return target.getEmail()+" promoted to Admin";
    	}
    		return target.getEmail()+" is already a admin";	
    }
    
    
}
