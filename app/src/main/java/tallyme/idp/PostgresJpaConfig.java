package tallyme.idp;

import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@PropertySource("classpath:conf/jpa.properties")
public class PostgresJpaConfig {

  @Value("${db.driver_class_name}") private String driverClassName;
  @Value("${db.url}") private String url;
  @Value("${db.username}") private String username;
  @Value("${db.password}") private String password;
  @Value("${db.connect_timeout}") private int connectTimeout;
  @Value("${db.socket_timeout}") private int socketTimeout;

  @Value("${cp.max_total}") private int maxTotal;
  @Value("${cp.max_idle}") private int maxIdle;
  @Value("${cp.min_idle}") private int minIdle;
  @Value("${cp.max_wait_mills}") private long maxWaitMills;

  @Value("${hibernate.dialect}") private String dialect;
  @Value("${hibernate.hbm2ddl.auto}") private String hbm2ddlAuto;
  @Value("${hibernate.show_sql}") private String showSql;
  @Value("${hibernate.format_sql}") private String formatSql;
  @Value("${hibernate.default_schema}") private String defaultSchema;

  @Value("${spring.jpa.packages.entities}") private String packagesToScan;

  /**
   * The data source used by JPA.
   *
   * @return BasicDataSource
   */
  @Bean(destroyMethod = "close") // invoke BasicDataSource.close() on destroy to release the data source
  public BasicDataSource dataSource(){
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(this.driverClassName);
    dataSource.setUrl(this.url);
    dataSource.setUsername(this.username);
    dataSource.setPassword(this.password);
    dataSource.setDefaultAutoCommit(false);
    dataSource.setMaxTotal(this.maxTotal);
    dataSource.setMaxIdle(this.maxIdle);
    dataSource.setMinIdle(this.minIdle);
    dataSource.setMaxWaitMillis(this.maxWaitMills);
    dataSource.addConnectionProperty("connectTimeout", String.valueOf(this.connectTimeout));
    dataSource.addConnectionProperty("socketTimeout", String.valueOf(this.socketTimeout));
    return dataSource;
  }

  /**
   * the EntityManagerFactory which Spring Data JPA needs on the DI container.
   * EntityManager synchronises entities in PersistenceContexts (entities managed by the EntityManager) and RDB by executing SQL queries.
   * Note that a PersistenceContext is made for every transaction.
   *
   * @return LocalContainerEntityManagerFactoryBean
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(BasicDataSource dataSource) {
    Properties jpaProperties = new Properties();
    jpaProperties.setProperty("hibernate.dialect", this.dialect);
    jpaProperties.setProperty("hibernate.hbm2ddl.auto", this.hbm2ddlAuto);
    jpaProperties.setProperty("hibernate.show_sql", this.showSql);
    jpaProperties.setProperty("hibernate.format_sql", this.formatSql);
    jpaProperties.setProperty("hibernate.default_schema", this.defaultSchema);

    LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource);
    entityManagerFactory.setPackagesToScan(this.packagesToScan);
    entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactory.setJpaProperties(jpaProperties);
    return entityManagerFactory;
  }

  /**
   * the PlatformTransactionManager, via which JpaTransactionManager calls EntityTransaction APIs.
   * @return PlatformTransactionManager
   */
  @Bean
  public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
    return transactionManager;
  }
}
