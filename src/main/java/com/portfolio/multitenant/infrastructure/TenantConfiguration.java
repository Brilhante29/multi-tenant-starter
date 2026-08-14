package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.application.TenantDataService;
import com.portfolio.multitenant.application.TenantService;
import com.portfolio.multitenant.application.TransactionRunner;
import com.portfolio.multitenant.domain.TenantIdGenerator;
import com.portfolio.multitenant.domain.TenantRecordRepository;
import com.portfolio.multitenant.domain.TenantRepository;
import com.portfolio.multitenant.domain.TenantSchemaManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class TenantConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TenantIdGenerator tenantIdGenerator() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean
    TenantRepository tenantRepository(JdbcTemplate jdbc) {
        return new JdbcTenantRepository(jdbc);
    }

    @Bean
    TenantSchemaManager tenantSchemaManager(JdbcTemplate jdbc) {
        return new JdbcTenantSchemaManager(jdbc);
    }

    @Bean
    TenantRecordRepository tenantRecordRepository(JdbcTemplate jdbc, TenantRepository tenants) {
        return new JdbcTenantRecordRepository(jdbc, tenants);
    }

    @Bean
    TransactionRunner transactionRunner(PlatformTransactionManager transactionManager) {
        return new SpringTransactionRunner(transactionManager);
    }

    @Bean
    TenantService tenantService(
        TenantRepository tenants,
        TenantSchemaManager schemas,
        TenantIdGenerator ids,
        TransactionRunner transactions,
        Clock clock
    ) {
        return new TenantService(tenants, schemas, ids, transactions, clock);
    }

    @Bean
    TenantDataService tenantDataService(
        TenantRepository tenants,
        TenantRecordRepository records,
        TenantIdGenerator ids,
        TransactionRunner transactions,
        Clock clock
    ) {
        return new TenantDataService(tenants, records, ids, transactions, clock);
    }
}
