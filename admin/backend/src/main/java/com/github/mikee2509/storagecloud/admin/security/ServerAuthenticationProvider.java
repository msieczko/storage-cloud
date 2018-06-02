package com.github.mikee2509.storagecloud.admin.security;

import com.github.mikee2509.storagecloud.admin.domain.security.UserAuthDetails;
import com.github.mikee2509.storagecloud.proto.User;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ServerAuthenticationProvider implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        //TODO authenticate against TIN server
        // LOGIN username pass -> OK
        // LIST_USERS -> USERS [List<User>]
        // find current user in list

        User user = User.newBuilder()
                .setUsername(username)
                .setFirstName("Michał")
                .setLastName("Kowalski")
                .build();

        UserAuthDetails userAuthDetails = UserAuthDetails.create(user, password);
        return new UsernamePasswordAuthenticationToken(userAuthDetails, null, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
