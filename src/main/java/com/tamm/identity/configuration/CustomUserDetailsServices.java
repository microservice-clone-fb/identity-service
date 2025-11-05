package com.tamm.identity.configuration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.tamm.identity.entity.Permission;
import com.tamm.identity.entity.Role;
import com.tamm.identity.entity.User;
import com.tamm.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CustomUserDetailsServices implements UserDetailsService {
    UserRepository userRepository;

    @Override
    //    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //        User user = userRepository.findByUsernameWithRolesAndPermissions(username)
    //                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    //        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
    //        // roles
    //        user.getRoles().forEach(role -> {
    //            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    //
    //            // permissions
    //            role.getPermissions().forEach(permission -> grantedAuthorities
    //                    .add(new SimpleGrantedAuthority(permission.getName())));
    //        });
    //
    //        return new org.springframework.security.core.userdetails.User(
    //                user.getUsername(),
    //                user.getPassword(),
    //                grantedAuthorities
    //        );
    //    }
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameWithRolesAndPermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // Lấy roles thông qua UserRole
        if (!CollectionUtils.isEmpty(user.getUserRoles())) {
            user.getUserRoles().forEach(userRole -> {
                Role role = userRole.getRole();

                // Thêm role authority
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

                // Thêm permissions của role
                if (!CollectionUtils.isEmpty(role.getRolePermissions())) {
                    role.getRolePermissions().forEach(rolePermission -> {
                        Permission permission = rolePermission.getPermission();
                        grantedAuthorities.add(new SimpleGrantedAuthority(permission.getName()));
                    });
                }
            });
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}
