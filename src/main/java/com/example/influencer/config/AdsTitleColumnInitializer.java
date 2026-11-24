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
public class AdsTitleColumnInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdsTitleColumnInitializer.class);
	private static final String[] TABLE_NAMES = { "ads", "ADS", "Ads" };

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public AdsTitleColumnInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!tableExists()) {
			return;
		}
		addColumnIfMissing("ad_title", "ALTER TABLE ads ADD COLUMN ad_title VARCHAR(255) NOT NULL DEFAULT ''");
		addColumnIfMissing("business_email",
				"ALTER TABLE ads ADD COLUMN business_email VARCHAR(255) NOT NULL DEFAULT ''");
	}

	private void addColumnIfMissing(String columnName, String alterSql) throws Exception {
		if (columnExists(columnName)) {
			return;
		}
		jdbcTemplate.execute(alterSql);
		log.info("Added {} column to ads without dropping existing records", columnName);
	}

	private boolean tableExists() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			for (String table : TABLE_NAMES) {
				try (ResultSet resultSet = metaData.getTables(catalog, null, table, new String[] { "TABLE" })) {
					if (resultSet.next()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private boolean columnExists(String columnName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			String[] columns = { columnName, columnName.toUpperCase(), columnName.toLowerCase() };
			for (String table : TABLE_NAMES) {
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
