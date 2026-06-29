package org.acme.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.acme.dao.MyEntityDaoService;

@Slf4j
@Path("/api/other")
@RequestScoped
public class Other {

    @Inject
    MyEntityDaoService  myEntityDaoService;

    @GET
    public String getDbVersion() {
        return myEntityDaoService.getDatabaseVersion();
    }

}
