package org.acme.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
@ApplicationScoped
public class MyEntityDaoService {

    @Inject
    DataSource dataSource;

    public String getDatabaseVersion() {
        // Use try-with-resources to ensure the connection is returned to the pool
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT version()");
             ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception e) {
            log.error("Failed to run database command: {}", e.getMessage(), e);
        }
        return "Unknown";
    }
}
