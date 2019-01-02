package com.alkefp.sales.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {


    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        MDC.put("startTime", System.currentTimeMillis()+"");
        MDC.put("url",request.getRequestURI());
        logger.info("Starting execution {}",request.getRequestURI());
        System.out.println(   "Starting execution");
        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        System.out.println("this is interceptor, postHandle method");
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        long startTime =  Long.valueOf(MDC.get("startTime"));
        logger.info("Execution Ended "+ (System.currentTimeMillis() - startTime) +" ms for "+ request.getRequestURI() );
        System.out.println("Execution Ended "+ (System.currentTimeMillis() - startTime) +" ms");
    }
}