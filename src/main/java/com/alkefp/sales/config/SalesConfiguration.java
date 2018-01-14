package com.alkefp.sales.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class SalesConfiguration {

	/*******Dev
	 *  jdbc:mysql://db4free.net:3306/alke 
	 *  alke
	 *  alke#22
	 * 
	 */
	
	/*******Prod
	 *  jdbc:mysql://alke.cmex3ogn91i5.us-west-2.rds.amazonaws.com:3306/alke
	 *  alke
	 *  alke#123
	 * 
	 */
	
	private String url ="jdbc:mysql://alke.cmex3ogn91i5.us-west-2.rds.amazonaws.com:3306/alke ";
	private String port;
	private String user="alke";
	private String password ="alke#123";
	
	/*private String url ="jdbc:mysql://localhost:3306/alke";
	private String port;
	private String user="root";
	private String password ="";*/
	
	private String database;


	@Bean
	public DataSource dataSource() {

		DriverManagerDataSource  dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Bean
	public JdbcTemplate getJDBCTemplate() {

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
		return jdbcTemplate;

	}

	@Bean
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		
		NamedParameterJdbcTemplate  namedjdbcTemplate = new NamedParameterJdbcTemplate(dataSource());
		return namedjdbcTemplate;
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}
}
