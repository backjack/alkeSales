package com.alkefp.sales.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import com.alkefp.sales.security.RESTAuthenticationSuccessHandler;
import com.alkefp.sales.security.RestAuthenticationFailureHandler;

@Configuration
public class WebConfig {
    @Autowired
    private RESTAuthenticationSuccessHandler restAuthenticationSuccessHandler;
    @Autowired
    private RestAuthenticationFailureHandler restAuthenticationFailureHandler;
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/download/**", "/login").permitAll()
                .requestMatchers("/home/**", "/index/**", "/graph/**", "/index.html", "/client.html").authenticated()
                .anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.loginPage("/login")
                .usernameParameter("username").passwordParameter("password")
                .successHandler(restAuthenticationSuccessHandler)
                .failureHandler(restAuthenticationFailureHandler))
            .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/login"));
        return http.build();
    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.setUsersByUsernameQuery("select username, password,'true' from user_profile where username =?");
        users.setAuthoritiesByUsernameQuery("select username,'USER' from user_profile where username =?");
        return users;
    }

    @Bean
    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
