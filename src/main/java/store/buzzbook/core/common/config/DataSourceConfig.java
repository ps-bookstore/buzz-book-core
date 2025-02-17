package store.buzzbook.core.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Getter
@Setter
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {
	private final MySQLProperties mySQLProperties;

	@Value("${spring.datasource.url}")
	private String mysqlUrl;

	@Value("${spring.datasource.username}")
	private String mysqlUsername;

	@Value("${spring.datasource.password}")
	private String mysqlPassword;

	@Bean
	public DataSource dataSource() {
		BasicDataSource basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName(mySQLProperties.getDriverClassName());
		basicDataSource.setUrl(mysqlUrl);
		basicDataSource.setUsername(mysqlUsername);
		basicDataSource.setPassword(mysqlPassword);
		basicDataSource.setMaxIdle(mySQLProperties.getMaxIdle());
		basicDataSource.setMaxTotal(mySQLProperties.getMaxTotal());
		basicDataSource.setInitialSize(mySQLProperties.getInitialSize());
		basicDataSource.setMinIdle(mySQLProperties.getMinIdle());

		basicDataSource.setValidationQuery("SELECT 1");
		basicDataSource.setTestOnReturn(false);
		basicDataSource.setTestOnBorrow(false);
		basicDataSource.setTestWhileIdle(false);

		return basicDataSource;
	}
}
