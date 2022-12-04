package com.alkefp.sales.config;

import com.alkefp.sales.security.RESTAuthenticationSuccessHandler;
import com.alkefp.sales.security.RestAuthenticationFailureHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class WebConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private RESTAuthenticationSuccessHandler restAuthenticationSuccessHandler;
	
	@Autowired
	private RestAuthenticationFailureHandler restAuthenticationFailureHandler;


	@Autowired
	private UserDetailsService userService;
	
	

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		
		http
        .authorizeRequests()
            .antMatchers("/css/**","/js/**","/download/**").permitAll();
		
		
		http
        .authorizeRequests()
            .antMatchers("/home/**","/index/**","/graph/**","/index.html","/client.html").authenticated();
		
		http.csrf().disable();
		http.formLogin().loginPage("/login").usernameParameter("username").passwordParameter("password");
		
		/* http
	        .formLogin() .loginPage("/login")
            .permitAll();*/
		// http.exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint);
		 http.exceptionHandling().accessDeniedPage("/login");
		 http.formLogin().successHandler(restAuthenticationSuccessHandler);
		 http.formLogin().failureHandler(restAuthenticationFailureHandler);
		
	}

//	@Bean
//	public BCryptPasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {


		auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
//						("select username,'USER' from user_profile where username =?");
	}




}
