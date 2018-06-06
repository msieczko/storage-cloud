package com.github.mikee2509.storagecloud.admin.domain.security;

import com.github.mikee2509.storagecloud.admin.domain.dto.UserDto;
import com.github.mikee2509.storagecloud.proto.UserDetails;
import com.google.protobuf.ByteString;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionDetails implements org.springframework.security.core.userdetails.UserDetails {
    private String username;
    private String firstName;
    private String lastName;
    private ByteString sid;

    public static SessionDetails create(UserDetails user, ByteString sid) {
        return new SessionDetails(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                sid
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
        return Collections.singleton(new SimpleGrantedAuthority("ADMIN"));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return null;
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
        SessionDetails that = (SessionDetails) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public ByteString getSid() {
        return sid;
    }
}
