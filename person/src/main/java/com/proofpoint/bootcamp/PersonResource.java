/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.bootcamp;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/v1/person/{id}")
public class PersonResource
{
    private final PersonStore store;

    @Inject
    public PersonResource(PersonStore store)
    {
        Preconditions.checkNotNull(store, "store is null");

        this.store = store;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id, @Context UriInfo uriInfo)
    {
        Preconditions.checkNotNull(id, "id is null");
        Preconditions.checkNotNull(uriInfo, "uriInfo is null");

        Person person = store.get(id);
        if (person != null) {
            return Response.ok(PersonRepresentation.from(person, uriInfo.getRequestUri())).build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).entity("[" + id + "]").build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@PathParam("id") String id, PersonRepresentation personRepresentation)
    {
        Preconditions.checkNotNull(id, "id is null");
        Preconditions.checkNotNull(personRepresentation, "personRepresentation is null");

        if (!id.equals(personRepresentation.getId())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("[ID mismatch]").build();
        }

        boolean added = store.put(personRepresentation.toPerson());
        if (added) {
            URI uri = UriBuilder.fromResource(PersonResource.class).build(id);
            return Response.created(uri).build();
        }
        else {
            return Response.noContent().build();
        }
    }

    @DELETE
    public Response delete(@PathParam("id") String id)
    {
        Preconditions.checkNotNull(id, "id is null");

        if (store.delete(id)) {
            return Response.noContent().build();
        }
        else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
