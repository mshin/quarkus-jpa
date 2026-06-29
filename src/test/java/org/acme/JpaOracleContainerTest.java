package org.acme;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.sql.*;
import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaOracleContainerTest {

    private static final DockerImageName myImage = DockerImageName.parse("gvenzl/oracle-free").asCompatibleSubstituteFor("gvenzl/oracle-xe");//:23.3.0
    private static final OracleContainer oracle = new OracleContainer(myImage);//"gvenzl/oracle-xe");


    @BeforeAll
    static void startContainer() {
        oracle.setWaitStrategy(
            new LogMessageWaitStrategy()
                .withRegEx(".*DATABASE IS READY TO USE!.*")//".*Database mounted.*")//
                .withStartupTimeout(Duration.ofSeconds(120L))
        );
        oracle.start();
    }

    @AfterAll
    static void stopContainer() {
        //oracle.stop();
    }

    @Test
    void testOracleTableCRUD() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword());
             Statement stmt = conn.createStatement()) {

            // 1. Create table
            stmt.executeUpdate("CREATE TABLE test_table (id NUMBER PRIMARY KEY, name VARCHAR2(100))");

            // 2. Insert data
            stmt.executeUpdate("INSERT INTO test_table (id, name) VALUES (1, 'Alice')");
            stmt.executeUpdate("INSERT INTO test_table (id, name) VALUES (2, 'Bob')");

            // 3. Query data
            try (ResultSet rs = stmt.executeQuery("SELECT id, name FROM test_table ORDER BY id")) {
                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(1, rs.getInt("id"));
                Assertions.assertEquals("Alice", rs.getString("name"));

                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(2, rs.getInt("id"));
                Assertions.assertEquals("Bob", rs.getString("name"));

                Assertions.assertFalse(rs.next());
            }
        }
    }
}