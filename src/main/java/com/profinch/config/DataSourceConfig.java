package com.profinch.config;
import com.profinch.config.DataSourceConfig;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jndi.JndiObjectFactoryBean;

public class DataSourceConfig {
  @Value("${spring.datasource.jndi-name}")
  private String jndiName;
  
  @Bean(destroyMethod = "")
  public DataSource dataSource() throws IllegalArgumentException, NamingException {
    JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
    bean.setJndiName(this.jndiName);
    bean.setProxyInterface(DataSource.class);
    bean.setLookupOnStartup(false);
    bean.afterPropertiesSet();
    return (DataSource)bean.getObject();
  }
}

				