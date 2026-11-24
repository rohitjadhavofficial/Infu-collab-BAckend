package com.example.influencer.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdsTitleColumnInitializerTest {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void addsMissingAdTitleColumnWithoutDeletingExistingRows() throws Exception {
		jdbcTemplate.execute("DROP TABLE IF EXISTS ads");
		jdbcTemplate.execute("""
				CREATE TABLE ads (
					id BIGINT PRIMARY KEY,
					business_name VARCHAR(255) NOT NULL,
					category VARCHAR(100) NOT NULL,
					budget DECIMAL(12,2) NOT NULL,
					ad_type VARCHAR(100) NOT NULL,
					description TEXT,
					created_at TIMESTAMP NOT NULL
				)
				""");
		jdbcTemplate.update("""
				INSERT INTO ads (id, business_name, category, budget, ad_type, description, created_at)
				VALUES (1, 'Legacy Store', 'Retail', 1000.00, 'Story', 'Existing record', CURRENT_TIMESTAMP)
				""");

		new AdsTitleColumnInitializer(dataSource, jdbcTemplate).run(null);

		assertThat(columnExists("ad_title")).isTrue();
		assertThat(columnExists("business_email")).isTrue();
		Integer remainingRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ads WHERE business_name = 'Legacy Store'",
				Integer.class);
		assertThat(remainingRows).isEqualTo(1);
		String title = jdbcTemplate.queryForObject("SELECT ad_title FROM ads WHERE id = 1", String.class);
		assertThat(title).isEmpty();
		String email = jdbcTemplate.queryForObject("SELECT business_email FROM ads WHERE id = 1", String.class);
		assertThat(email).isEmpty();
	}

	private boolean columnExists(String columnName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String catalog = connection.getCatalog();
			String[] tables = { "ads", "ADS", "Ads" };
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
