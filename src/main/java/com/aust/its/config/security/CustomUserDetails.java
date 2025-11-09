package com.aust.its.config.security;

import com.aust.its.entity.HelpDeskRole;
import com.aust.its.entity.HelpDeskUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final HelpDeskUser user;
    private final HelpDeskRole role;

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role.getRoleLabel()));
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleLabel;

        // Map roleId to Help Desk roles
        long roleId = user.getRoleId();
        if (roleId == 1) roleLabel = "ROLE_ADMIN";
        else if (roleId == 2) roleLabel = "ROLE_DEVELOPER";
        else if (roleId == 3) roleLabel = "ROLE_USER";
        else roleLabel = "ROLE_USER"; // fallback for unknown role

        return List.of(new SimpleGrantedAuthority(roleLabel));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}