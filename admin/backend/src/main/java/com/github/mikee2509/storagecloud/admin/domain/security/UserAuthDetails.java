package com.github.mikee2509.storagecloud.admin.domain.security;

import com.github.mikee2509.storagecloud.admin.domain.dto.UserDto;
import com.github.mikee2509.storagecloud.proto.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAuthDetails implements UserDetails {
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    public static UserAuthDetails create(User user, String password) {
        return new UserAuthDetails(
                user.getUsername(),
                password,
                user.getFirstName(),
                user.getLastName()
        );
    }

    public UserDto dto() {
        return UserDto.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthDetails that = (UserAuthDetails) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
