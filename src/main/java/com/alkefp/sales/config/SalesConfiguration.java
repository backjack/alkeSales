package com.alkefp.sales.config;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SalesConfiguration implements WebMvcConfigurer {
	
	private String url ;
	private String port;
	private String user;
	private String password;
	private String database;


	@Bean
	@Profile("!local")
	public DataSource dataSource() {

		DriverManagerDataSource  dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		String url = System.getenv("url");
		dataSource.setUrl(url);
		String user = System.getenv("user");
		String password = System.getenv("password");
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Bean(destroyMethod = "close")
	@Profile("local")
	public DataSource localDataSource(@Value("${app.datasource.maximum-pool-size:2}") int maximumPoolSize,
			@Value("${app.datasource.connection-timeout-ms:10000}") long connectionTimeoutMs,
			@Value("${app.datasource.idle-timeout-ms:60000}") long idleTimeoutMs,
			@Value("${app.datasource.max-lifetime-ms:300000}") long maxLifetimeMs) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setJdbcUrl(System.getenv("url"));
		dataSource.setUsername(System.getenv("user"));
		dataSource.setPassword(System.getenv("password"));
		dataSource.setMaximumPoolSize(maximumPoolSize);
		dataSource.setMinimumIdle(0);
		dataSource.setConnectionTimeout(connectionTimeoutMs);
		dataSource.setIdleTimeout(idleTimeoutMs);
		dataSource.setMaxLifetime(maxLifetimeMs);
		dataSource.setPoolName("alke-local");
		return dataSource;
	}

	@Bean
	public JdbcTemplate getJDBCTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
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

	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoggingInterceptor()).addPathPatterns("/**");
	}
}
