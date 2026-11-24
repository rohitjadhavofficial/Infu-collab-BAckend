package com.example.influencer.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PasswordColumnInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PasswordColumnInitializer.class);

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public PasswordColumnInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (columnExists("password")) {
			return;
		}
		jdbcTemplate.execute("ALTER TABLE Influ_login ADD COLUMN password VARCHAR(255) NULL");
		log.info("Added password column to Influ_login");
	}

	private boolean columnExists(String columnName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			String[] tables = { "Influ_login", "influ_login", "INFLU_LOGIN" };
			String[] columns = { columnName, columnName.toUpperCase(), columnName.toLowerCase() };
			for (String table : tables) {
				for (String column : columns) {
					try (ResultSet resultSet = metaData.getColumns(catalog, null, table, column)) {
						if (resultSet.next()) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}
}
