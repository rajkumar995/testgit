package com.medidropbox.security;

import com.medidropbox.entity.RolePermission;
import com.medidropbox.entity.User;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.RolePermissionRepository;
import com.medidropbox.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService for MediDropBox
 */
@Service
public class MediDropBoxUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    
    public MediDropBoxUserDetailsService(UserRepository userRepository, 
                                        RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Use query with JOIN FETCH to eagerly load hospital
        User user = userRepository.findByUsernameAndIsActiveTrueWithHospital(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        List<GrantedAuthority> authorities = getAuthorities(user.getRole());
        
        return new MediDropBoxUserDetails(user, authorities);
    }
    
    private List<GrantedAuthority> getAuthorities(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role as authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        
        // Add permissions as authorities
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);
        for (RolePermission rp : rolePermissions) {
            authorities.add(new SimpleGrantedAuthority(rp.getPermission().name()));
        }
        
        return authorities;
    }
    
    public List<String> getPermissions(Role role) {
        return rolePermissionRepository.findByRole(role).stream()
                .map(rp -> rp.getPermission().name())
                .collect(Collectors.toList());
    }
}
