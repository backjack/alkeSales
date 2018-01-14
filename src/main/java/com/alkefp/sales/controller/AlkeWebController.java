package com.alkefp.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AlkeWebController {

	
	@GetMapping("/index")
	public String indexOf() {
		
		return "index";
	}
	

	@GetMapping("/login")
	public String login() {
		
		return "login";
	}
	
	@GetMapping("/download/Alke_.docx")
	public String download() {
		
		return "download";
	}
	
	@GetMapping("/client")
	public String client() {
		
		return "client";
	}
	
	

}
