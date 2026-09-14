package com.alkefp.sales.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RESTAuthenticationEntryPoint implements AuthenticationEntryPoint {

	

	@Override
	public void commence(HttpServletRequest arg0, HttpServletResponse response,
			AuthenticationException arg2)
			throws IOException, ServletException {
	
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		
	}
}
