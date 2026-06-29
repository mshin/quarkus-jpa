package org.acme.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.acme.dao.MyEntity;

import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Random;

@Slf4j
@ApplicationScoped
public class EntityService {

    private Random random = new Random();

    @Inject
    TransactionService transactionService;

    @WithSpan
    public void doExtraWork() throws InterruptedException {
        Duration worktime = Duration.ofSeconds(random.nextInt(2, 5));
        log.info("Doing {} of work...", worktime);
        Thread.sleep(
                worktime
        );
        log.info("Work done!");
    }

    public MyEntity create(MyEntity entity) throws InterruptedException {
        try(
                TransactionService.TransactionResource resource = this.transactionService.getTransaction(true)
        ) {
            log.info("Creating entity...");
            entity.persistAndFlush();
            log.info("Entity created!");
            this.doExtraWork();

            log.info("Created entity {}", entity);
        }
        return entity;
    }
}
