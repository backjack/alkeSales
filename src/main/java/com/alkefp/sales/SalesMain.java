package com.alkefp.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class SalesMain extends SpringBootServletInitializer{


    public static void main(String[] args) throws Exception {
        SpringApplication.run(SalesMain.class, args);
    }
    
    
    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
    	
        return application.sources(SalesMain.class);
    }


}