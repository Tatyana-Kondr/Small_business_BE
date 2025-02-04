package de.ait.smallBusiness_be.security.details;

import de.ait.smallBusiness_be.users.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
public class AuthenticatedUser implements UserDetails {

    private final User user;

    public AuthenticatedUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // права пользователя
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().toString()));
    }

    @Override
    public String getPassword() { // в качестве пароля берем наш хэш
        return user.getPassword();
    }

    @Override
    public String getUsername() { // в качестве имени пользователя в нашем случае выступает email
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getState().equals(User.State.BANNED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public Long getId() {
        return this.user.getId();
    }

    public User getUser() {
        return this.user;
    }
}
