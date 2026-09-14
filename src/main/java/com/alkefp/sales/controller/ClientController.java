package com.alkefp.sales.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alkefp.sales.beans.BaseResponse;
import com.alkefp.sales.beans.Client;
import com.alkefp.sales.beans.User;
import com.alkefp.sales.dao.ClientDao;
import com.alkefp.sales.dao.SummaryDao;

@RestController
@RequestMapping("/client")
public class ClientController {

	@Autowired
	private ClientDao clientDao;

	@Autowired
	private SummaryDao summaryDao;

	@GetMapping("/list")
	public BaseResponse<List<Client>> getClients() {
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = summaryDao.getUserMap().get(principal.getUsername());
		BaseResponse<List<Client>> response = new BaseResponse<>();
		response.setData(clientDao.getClients(user.getGroupId()));
		response.setSuccess(true);
		return response;
	}

	@PostMapping("/create")
	public BaseResponse<Client> create(@RequestBody Client client) {
		User user = currentUser(); client.setGroupId(user.getGroupId());
		int nextId = clientDao.jdbcTemplate.queryForObject("select coalesce(max(clientId),0)+1 from client where groupId=?", Integer.class, user.getGroupId());
		client.setId(nextId); clientDao.insertClient(client);
		return reply(client);
	}

	@PostMapping("/update")
	public BaseResponse<Client> update(@RequestBody Client client) {
		User user = currentUser(); client.setGroupId(user.getGroupId()); clientDao.updateClient(client); return reply(client);
	}

	private User currentUser() {
		UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return summaryDao.getUserMap().get(principal.getUsername());
	}
	private BaseResponse<Client> reply(Client client) { BaseResponse<Client> response=new BaseResponse<>(); response.setData(client); response.setSuccess(true); return response; }
	
}
