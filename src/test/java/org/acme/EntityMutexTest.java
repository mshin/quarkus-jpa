package org.acme;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.dao.MyEntity;
import org.acme.rest.EntityCrud;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(EntityCrud.class)
public class EntityMutexTest {

    //@Test
    public void testList() {
        List<MyEntity> result = given()
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<List<MyEntity>>(){});
    }

    public static Stream<Arguments> getParams() {
        return Stream.of(
                Arguments.of(2, 10)
//                Arguments.of(3, 10),
//                Arguments.of(5, 10)

//                Arguments.of(10, 10, Duration.of(250, ChronoUnit.MILLIS)),
//                Arguments.of(20, 20, Duration.of(150, ChronoUnit.MILLIS))
        );
    }


    //@ParameterizedTest
    @MethodSource("getParams")
    public void threadTest(int numThreads, int numIterations) throws InterruptedException, ExecutionException {
        String mutexId = "testMutex2";
        List<Future<List<MyEntity>>> futures = new ArrayList<>(numThreads);
        Set<MyEntity> results = new HashSet<>();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        TestThread.TestThreadBuilder threadBuilder = TestThread.builder()
                .mutexId(mutexId)
                .numIterations(numIterations);

        for (int i = 1; i <= numThreads; i++) {
            threadBuilder.threadId("testThread-" + i);

            futures.add(executor.submit(threadBuilder.build()));
        }
        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            log.info("Still waiting on threads...");
        }

        for (Future<List<MyEntity>> future : futures) {
            results.addAll(future.get());
        }

        assertEquals(numIterations * numThreads, results.size());

        //TODO:: check results
        log.info("Results: {}", results);

        Iterator<MyEntity> iterator = results.iterator();
        MyEntity cur = iterator.next();
        while (iterator.hasNext()) {
            MyEntity next = iterator.next();

//            assertTrue(
//                    next.getStart().isAfter(cur.getStart()),
//                    "result " + cur + " start overlaps with the next result " + next + " (next start is before cur start)"
//            );
//            assertTrue(
//                    (next.getStart().isAfter(cur.getEnd()) || next.getStart().equals(cur.getEnd())),
//                    "result " + cur + " overlaps with the next result " + next + " (next start is before cur end)"
//            );

            cur = next;
        }

    }


    @Builder
    @Data
    @AllArgsConstructor
    static
    class ThreadResult implements Comparable<ThreadResult> {
        private String threadId;
        private LocalDateTime start;
        private LocalDateTime end;

        @Override
        public int compareTo(@NonNull ThreadResult threadResult) {
            return this.getStart().compareTo(threadResult.getStart());
        }
    }

    @Builder
    @Slf4j
    @AllArgsConstructor
    static class TestThread implements Callable<List<MyEntity>> {

        private String mutexId;
        private String threadId;
        private int numIterations;

        @SneakyThrows
        @Override
        public List<MyEntity> call() {
            log.info("Running test thread {}", this.threadId);

//			Thread.sleep(500);

            List<MyEntity> results = new ArrayList<>(this.numIterations);
            for (int i = 1; i <= this.numIterations; i++) {
                log.info("Thread {} waiting for lock on iteration {}", this.threadId, i);

                MyEntity entityOut = MyEntity.builder().field(this.threadId + "-" + i).build();

                MyEntity entityIn = given()
                        .contentType(ContentType.JSON)
                        .body(entityOut)
                        .when()
                        .post()
                        .then()
                        .statusCode(200)
                        .extract().body().as(MyEntity.class);


                assertEquals(entityOut.field, entityIn.field);


                log.info("Thread {} done doing work & released lock on iteration {}; {}/{}", this.threadId, i, entityOut.field, entityIn.id);
                results.add(entityIn);
            }
            log.info("DONE running test thread {}", this.threadId);
            return results;
        }
    }
}
