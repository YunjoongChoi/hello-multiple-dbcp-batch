package com.choimory.hellomultipledbcpbatch.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(JpaProperties.class)
@EnableJpaRepositories(basePackages = SlaveDbConfig.PACKAGE, entityManagerFactoryRef = SlaveDbConfig.ENTITY_MANAGER, transactionManagerRef = SlaveDbConfig.TRANSACTION_MANAGER)
public class SlaveDbConfig {
    public static final String PACKAGE = "com.choimory.hellomultipledbcpbatch.slave";
    public static final String ENTITY_MANAGER = "slaveEntityManager";
    public static final String TRANSACTION_MANAGER = "slaveTransactionManager";
    public static final String DATA_SOURCE = "slaveDataSource";
    public static final String VENDOR_ADAPTER = "slaveJpaVendorAdapter";
    public static final String DATA_SOURCE_PROPERTIES = "slaveDataSourceProperties";

    @Bean(DATA_SOURCE_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSourceProperties dataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean(DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.slave.hikari")
    public DataSource dataSource(){
        return dataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(VENDOR_ADAPTER)
    public JpaVendorAdapter jpaVendorAdapter(@Qualifier(DATA_SOURCE) DataSource dataSource, JpaProperties jpaProperties){
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return adapter;
    }

    @Bean(ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean entityManageFactory(@Qualifier(VENDOR_ADAPTER) JpaVendorAdapter jpaVendorAdapter
            , ObjectProvider<PersistenceUnitManager> persistenceUnitManager
            , JpaProperties jpaProperties
            , @Qualifier(DATA_SOURCE) DataSource dataSource){
        return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties.getProperties(), persistenceUnitManager.getIfAvailable()).dataSource(dataSource)
                .packages(PACKAGE)
                .build();
    }

    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER) LocalContainerEntityManagerFactoryBean entityManager){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManager.getObject());
        return transactionManager;
    }
}
