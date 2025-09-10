package com.lee.rankujp.core;

import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.spring.SpringConnectionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class QueryDslSqlConfig {
    private final DataSource dataSource;

    @Bean
    public SQLQueryFactory sqlQueryFactory(DataSource dataSource) {
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(new MySQLTemplates());
        SpringConnectionProvider provider = new SpringConnectionProvider(dataSource);
        return new SQLQueryFactory(configuration, provider);
    }

}
