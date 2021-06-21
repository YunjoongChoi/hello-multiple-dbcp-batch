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
import org.springframework.context.annotation.Primary;
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
@EnableJpaRepositories(basePackages = MasterDbConfig.PACKAGE, entityManagerFactoryRef = MasterDbConfig.ENTITY_MANAGER, transactionManagerRef = MasterDbConfig.TRANSACTION_MANAGER)
public class MasterDbConfig {
    public static final String PACKAGE = "com.choimory.hellomultipledbcpbatch.master";
    public static final String ENTITY_MANAGER = "masterEntityManager";
    public static final String TRANSACTION_MANAGER = "masterTransactionManager";
    public static final String DATA_SOURCE = "masterDataSource";
    public static final String VENDOR_ADAPTER = "masterJpaVendorAdapter";
    public static final String DATA_SOURCE_PROPERTIES = "masterDataSourceProperties";

    @Primary
    @Bean(DATA_SOURCE_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSourceProperties dataSourceProperties(){
        return new DataSourceProperties();
    }

    @Primary
    @Bean(DATA_SOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    public DataSource dataSource(){
        return dataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(VENDOR_ADAPTER)
    public JpaVendorAdapter jpaVendorAdapter(@Qualifier(DATA_SOURCE) DataSource dataSource, JpaProperties jpaProperties){
        AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
        return adapter;
    }

    @Primary
    @Bean(ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean entityManageFactory(@Qualifier(VENDOR_ADAPTER) JpaVendorAdapter jpaVendorAdapter
                                                                        , ObjectProvider<PersistenceUnitManager> persistenceUnitManager
                                                                        , JpaProperties jpaProperties
                                                                        , @Qualifier(DATA_SOURCE) DataSource dataSource){
        LocalContainerEntityManagerFactoryBean obj = new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties.getProperties(), persistenceUnitManager.getIfAvailable()).dataSource(dataSource)
                                                                                                                                        .packages(PACKAGE)
                                                                                                                                        .build();
        return obj;
    }

    @Primary
    @Bean(TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER) LocalContainerEntityManagerFactoryBean entityManager){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManager.getObject());
        return transactionManager;
    }
}
