package org.acme.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@ApplicationScoped
public class TransactionService {
    private final ReentrantLock lock = new ReentrantLock();

    @WithSpan
    public void startTransaction() {
        log.info("Starting transaction...");

        log.debug("Waiting for lock...");
        lock.lock();
        log.debug("Lock acquired.");

        QuarkusTransaction.begin();

        log.info("Transaction started.");
    }

    private void unlockMutex() {
        log.info("Stopping transaction...");
        lock.unlock();
    }

    public void commitTransaction() {
        log.info("Committing transaction...");
        QuarkusTransaction.commit();
        this.unlockMutex();
    }

    public void rollbackTransaction() {
        log.info("Rolling back transaction...");
        QuarkusTransaction.rollback();
        this.unlockMutex();
    }

    public TransactionResource getTransaction(boolean started) {
        log.info("Getting transaction...");
        TransactionResource output = TransactionResource.builder()
                .transactionService(this)
                .build();

        if(started) {
            this.startTransaction();
        }

        return output;
    }

    @Builder(access = AccessLevel.PROTECTED)
    public static class TransactionResource implements Closeable {
        private TransactionService transactionService;
        @Builder.Default
        private boolean failed = false;

        public void failed(Throwable throwable) {
            if(throwable != null) {
                log.error("Transaction failed: {}", throwable.getMessage(), throwable);
            } else {
                log.error("Transaction failed.");
            }
            this.failed = true;
        }

        @Override
        public void close() {
            if(failed) {
                this.transactionService.rollbackTransaction();
            } else {
                this.transactionService.commitTransaction();
            }
        }
    }
}
