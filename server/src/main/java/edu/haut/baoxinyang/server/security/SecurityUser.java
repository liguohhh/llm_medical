package edu.haut.baoxinyang.server.security;

import edu.haut.baoxinyang.server.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security用户实体类
 */
public class SecurityUser implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    @Getter
    private final User user;
    
    private final List<SimpleGrantedAuthority> authorities;
    
    public SecurityUser(User user) {
        this.user = user;
        this.authorities = new ArrayList<>();
        
        // 根据用户类型添加不同的权限
        switch (user.getUserType()) {
            case 0 -> this.authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
            case 1 -> this.authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
            case 2 -> this.authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            default -> this.authorities.add(new SimpleGrantedAuthority("ROLE_UNKNOWN"));
        }
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
    
    @Override
    public String getPassword() {
        return this.user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return this.user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
} 