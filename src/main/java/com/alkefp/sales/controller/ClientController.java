package com.alkefp.sales.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alkefp.sales.beans.BaseResponse;
import com.alkefp.sales.beans.Client;

@RestController
@RequestMapping("/client")
public class ClientController {

	
	public BaseResponse<List<Client>> getClients() {
		
		return  null;
	}
	
}
