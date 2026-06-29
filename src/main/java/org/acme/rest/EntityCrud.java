package org.acme.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.acme.dao.MyEntity;
import org.acme.service.EntityService;

import jakarta.inject.Inject;
import java.util.List;

@Slf4j
@Path("/api/entity")
@RequestScoped
public class EntityCrud {


    @Inject
    EntityService entityService;

    @POST
    public MyEntity newEntity(MyEntity myEntity) throws InterruptedException {
        return this.entityService.create(myEntity);
    }


    @GET
    public List<MyEntity> getEntities() {
        log.info("Getting entities.");

        return MyEntity.findAll().list();
    }

    @GET
    @Path("{id}")
    public MyEntity getEntity(@PathParam("id") String id) {
        log.info("Getting entity with id {}", id);

        return MyEntity.findById(id);
    }

}
