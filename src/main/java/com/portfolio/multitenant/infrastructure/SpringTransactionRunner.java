package com.portfolio.multitenant.infrastructure;

import com.portfolio.multitenant.application.TransactionRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public final class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactions;

    public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T required(Supplier<T> work) {
        return transactions.execute(status -> work.get());
    }
}
