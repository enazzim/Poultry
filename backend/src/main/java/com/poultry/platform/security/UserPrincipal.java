package com.poultry.platform.security;

import com.poultry.platform.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final UserRole role;
    private final Long organizationId;

    public UserPrincipal(Long id, String username, String password, UserRole role, Long organizationId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.organizationId = organizationId;
    }

    public Long getId() { return id; }
    public UserRole getRole() { return role; }
    public Long getOrganizationId() { return organizationId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
