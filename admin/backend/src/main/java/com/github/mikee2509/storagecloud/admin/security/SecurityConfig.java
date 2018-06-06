package com.github.mikee2509.storagecloud.admin.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@AllArgsConstructor
@EnableWebSecurity(debug = true)
class SecurityConfig extends WebSecurityConfigurerAdapter {
    private ServerAuthenticationProvider serverAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http.csrf().disable()
            .authorizeRequests()
            .regexMatchers("/api/login").permitAll()
            .anyRequest().authenticated()
            .and().requestCache()
            .requestCache(new NullRequestCache())
            .and()
            .formLogin(); // TODO can be removed
        //@formatter:on
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(serverAuthenticationProvider);
    }


    //TODO check if necessary?
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
