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

import com.google.common.collect.ImmutableList;
import com.proofpoint.bootcamp.monitor.PersonEvent;
import com.proofpoint.event.client.InMemoryEventClient;
import com.proofpoint.jaxrs.testing.MockUriInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.net.URI;

public class TestPersonResource
{
    private PersonResource resource;
    private PersonStore store;
    private InMemoryEventClient eventClient;

    @BeforeMethod
    public void setup()
    {
        eventClient = new InMemoryEventClient();
        store = new PersonStore(eventClient);
        resource = new PersonResource(store);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetNullId()
    {
        resource.get(null, MockUriInfo.from(URI.create("http://localhost:8080/v1/person/foo")));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetNullContext()
    {
        resource.get("foo", null);
    }

    @Test
    public void testGetNotFound()
    {
        Response response = resource.get("foo", MockUriInfo.from(URI.create("http://localhost:8080/v1/person/foo")));
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetSuccess()
    {
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));

        Response response = resource.get("foo", MockUriInfo.from(URI.create("http://localhost:8080/v1/person/foo")));
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        Assert.assertEquals(response.getEntity(), new PersonRepresentation("foo", "foo@example.com", "Mr Foo", URI.create("http://localhost:8080/v1/person/foo")));
        Assert.assertNull(response.getMetadata().get("Content-Type"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullId()
    {
        resource.put(null, new PersonRepresentation("foo", "foo@example.com", "Mr Foo", null));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testPutNullValue()
    {
        resource.put("foo", null);
    }

    @Test
    public void testAdd()
    {
        Response response = resource.put("foo", new PersonRepresentation("foo", "foo@example.com", "Mr Foo", null));

        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
        Assert.assertNull(response.getEntity());
        Assert.assertNull(response.getMetadata().get("Content-Type"));

        Assert.assertEquals(store.get("foo"), new Person("foo", "foo@example.com", "Mr Foo"));

        Assert.assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testReplace()
    {
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));

        Response response = resource.put("foo", new PersonRepresentation("foo", "bar@example.com", "Mr Bar", null));

        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        Assert.assertNull(response.getEntity());
        Assert.assertNull(response.getMetadata().get("Content-Type"));

        Assert.assertEquals(store.get("foo"), new Person("foo", "bar@example.com", "Mr Bar"));

        Assert.assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo")),
                PersonEvent.personUpdated(new Person("foo", "bar@example.com", "Mr Bar"))
        ));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testDeleteNullId()
    {
        resource.delete(null);
    }

    @Test
    public void testDeleteExisting()
    {
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));

        Response response = resource.delete("foo");
        Assert.assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
        Assert.assertNull(response.getEntity());

        Assert.assertNull(store.get("foo"));

        Assert.assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo")),
                PersonEvent.personDeleted(new Person("foo", "foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testDeleteMissing()
    {
        Response response = resource.delete("foo");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        Assert.assertNull(response.getEntity());
    }
}
