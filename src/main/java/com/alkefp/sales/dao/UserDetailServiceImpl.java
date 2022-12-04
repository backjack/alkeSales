package com.alkefp.sales.dao;


import com.alkefp.sales.beans.LoginUser;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class UserDetailServiceImpl implements UserDetailsService {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<LoginUser> users = jdbcTemplate.query("select username, password from user_profile where username =? ",
                new Object[]{username},
                new BeanPropertyRowMapper(LoginUser.class));


        if(CollectionUtils.isEmpty(users)){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
//        Map<String,String> output = (Map)user;
//        System.out.println(output);
        LoginUser lgUser = users.get(0);
        String encodedPassword = new BCryptPasswordEncoder().encode(lgUser.getPassword());
        lgUser.setPassword(encodedPassword);

        return new org.springframework.security.core.userdetails.User(lgUser.getUsername(),
                lgUser.getPassword(),
                Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
