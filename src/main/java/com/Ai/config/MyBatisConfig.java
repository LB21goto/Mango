package com.Ai.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.example.mapper", sqlSessionFactoryRef = "mysqlSqlSessionFactory")
public class MyBatisConfig {

    @Bean
    @Primary
    public SqlSessionFactory mysqlSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // 如果有 XML mapper 文件，取消下面注释并配置路径
        // factoryBean.setMapperLocations(
        //     new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml")
        // );

        return factoryBean.getObject();
    }

    @Bean
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
