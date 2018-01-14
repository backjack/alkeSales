package com.alkefp.sales.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.alkefp.sales.beans.Client;

@Component
public class ClientDao {

	@Autowired
	public JdbcTemplate jdbcTemplate;
	private static final String GET_CLIENTS = "select clientId, clientName, address, telephone," +
			" emailId, contact_person, shortName, gstNo from client where groupId = ? and isActive = 'Y'";
	
	private static final String UPDATE_CLIENT = "update client set clientName =?, address =?, telephone =?," +
			" emailId =?, contact_person=?, shortName =?, gstNo =? where clientId = ? and groupId =?  and isActive = 'Y'";
	
	private static final String INSERT_CLIENT = "insert into client  (clientName ,address ,telephone," +
			" emailId ,contact_person,shortName,gstNo, clientId,groupId,isActive) values(?,?,?,?,?,?,?,?,?,'Y')";
	
	public List<Client> getClients(String groupId) {
		
		List<Client> clients = null;
		clients = jdbcTemplate.query(GET_CLIENTS, new ClientRowMapper());
		return clients;
	}
	
	private static class ClientRowMapper implements RowMapper<Client> {

		@Override
		public Client mapRow(ResultSet resultSet, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			Client client = new Client();
			client.setActive(true);
			client.setId(resultSet.getInt("clientId"));
			client.setClientName(resultSet.getString("clientName"));
			client.setAddress(resultSet.getString("address"));
			client.setTelehoneNo(resultSet.getString("telephone"));
			client.setGSTno(resultSet.getString("gstNo"));
			client.setShortName(resultSet.getString("shortName"));
			client.setContactPerson(resultSet.getString("contact_person"));
			client.setEmailAddress(resultSet.getString("emailId"));
			return client;
		}
		
	}
	
	public boolean updateClient(Client client) {
		
		int updatedRows = jdbcTemplate.update(UPDATE_CLIENT, new Object[]{ client.getClientName(),
				client.getAddress(),client.getTelehoneNo(),client.getEmailAddress(), client.getShortName(),
				client.getContactPerson(),client.getGSTno(),client.getId(), client.getGroupId()});
		if(updatedRows==1) {
			 return true;
		}
		return false;
	}
	
	public boolean insertClient(Client client) {
		
		int insertedRow = jdbcTemplate.update(INSERT_CLIENT, new Object[]{ client.getClientName(),
				client.getAddress(),client.getTelehoneNo(),client.getEmailAddress(), client.getShortName(),
				client.getContactPerson(),client.getGSTno(),client.getId(), client.getGroupId()});
		if(insertedRow == 1) {
			 return true;
		}
		return false;
	}
}
