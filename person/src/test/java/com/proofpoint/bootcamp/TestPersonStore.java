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
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TestPersonStore
{
    @Test
    public void testStartsEmpty()
    {
        PersonStore store = new PersonStore(new InMemoryEventClient());
        assertTrue(store.getAll().isEmpty());
    }

    @Test
    public void testPut()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        PersonStore store = new PersonStore(eventClient);
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));

        assertEquals(new Person("foo", "foo@example.com", "Mr Foo"), store.get("foo"));
        assertEquals(store.getAll().size(), 1);

        assertEquals(eventClient.getEvents(), ImmutableList.of(PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo"))));
    }

    @Test
    public void testIdempotentPut()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        PersonStore store = new PersonStore(eventClient);
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));
        store.put(new Person("foo", "foo@example.com", "Mr Bar"));

        assertEquals(new Person("foo", "foo@example.com", "Mr Bar"), store.get("foo"));
        assertEquals(store.getAll().size(), 1);

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo")),
                PersonEvent.personUpdated(new Person("foo", "foo@example.com", "Mr Bar"))
        ));
    }

    @Test
    public void testDelete()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        PersonStore store = new PersonStore(eventClient);
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));
        store.delete("foo");

        assertNull(store.get("foo"));
        assertTrue(store.getAll().isEmpty());

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo")),
                PersonEvent.personDeleted(new Person("foo", "foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testIdempotentDelete()
    {
        InMemoryEventClient eventClient = new InMemoryEventClient();
        PersonStore store = new PersonStore(eventClient);
        store.put(new Person("foo", "foo@example.com", "Mr Foo"));

        store.delete("foo");
        assertTrue(store.getAll().isEmpty());
        assertNull(store.get("foo"));

        store.delete("foo");
        assertTrue(store.getAll().isEmpty());
        assertNull(store.get("foo"));

        assertEquals(eventClient.getEvents(), ImmutableList.of(
                PersonEvent.personAdded(new Person("foo", "foo@example.com", "Mr Foo")),
                PersonEvent.personDeleted(new Person("foo", "foo@example.com", "Mr Foo"))
        ));
    }

    @Test
    public void testGetAll()
    {
        PersonStore store = new PersonStore(new InMemoryEventClient());

        store.put(new Person("foo", "foo@example.com", "Mr Foo"));
        store.put(new Person("bar", "bar@example.com", "Mr Bar"));

        assertEquals(store.getAll().size(), 2);
        assertEquals(store.getAll(), asList(new Person("foo", "foo@example.com", "Mr Foo"), new Person("bar", "bar@example.com", "Mr Bar")));
    }

}
