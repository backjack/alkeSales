package com.alkefp.sales.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.alkefp.sales.security.RESTAuthenticationEntryPoint;
import com.alkefp.sales.security.RESTAuthenticationSuccessHandler;
import com.alkefp.sales.security.RestAuthenticationFailureHandler;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private RESTAuthenticationSuccessHandler restAuthenticationSuccessHandler;
	
	@Autowired
	private RestAuthenticationFailureHandler restAuthenticationFailureHandler;
	
	
	@Autowired
	private JdbcTemplate  jdbcTemplate;
	
	
	
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
	
	@Autowired
	public void configureAuthentication(AuthenticationManagerBuilder auth) throws Exception {

	
		auth.jdbcAuthentication().dataSource(jdbcTemplate.getDataSource()).usersByUsernameQuery(
				"select username, password,'true' from user_profile where username =?"
				).authoritiesByUsernameQuery("select username,'USER' from user_profile where username =?");
	}
	
	 /*@Override
	    public void addViewControllers(ViewControllerRegistry registry) {
	        registry.addViewController("/login.html").setViewName("login");
	        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
	    }*/

}
