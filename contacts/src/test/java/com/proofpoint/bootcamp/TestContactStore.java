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

import com.proofpoint.bootcamp.monitor.ContactMonitor;
import com.proofpoint.bootcamp.monitor.ContactStats;
import com.proofpoint.event.client.InMemoryEventClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestContactStore
{
    private ContactStore store;

    @BeforeMethod
    public void setup()
    {
        ContactMonitor monitor = new ContactMonitor(new InMemoryEventClient(), new ContactStats());
        store = new ContactStore(monitor);
    }

    @Test
    public void testBadArguments()
    {
        try { store.getContacts(null); assertTrue(false); } catch (NullPointerException ex) { }

        try { store.contactExists(null, "bla"); assertTrue(false); } catch (NullPointerException ex) { }
        try { store.contactExists("foo", null); assertTrue(false); } catch (NullPointerException ex) { }

        try { store.putContact(null, "bla"); assertTrue(false); } catch (NullPointerException ex) { }
        try { store.putContact("foo", null); assertTrue(false); } catch (NullPointerException ex) { }

        try { store.deleteContact(null, "bla"); assertTrue(false); } catch (NullPointerException ex) { }
        try { store.deleteContact("foo", null); assertTrue(false); } catch (NullPointerException ex) { }
    }

    @Test
    public void testContactExists()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));
    }

    @Test
    public void testOwnerDoesNotExists()
    {
        store.putContact("foo", "bla");
        assertFalse(store.contactExists("bla", "hehe"));
    }

    @Test
    public void testContactDoesNotExists()
    {
        store.putContact("foo", "bla");
        assertFalse(store.contactExists("foo", "hehe"));
    }

    @Test
    public void testDeleteExistingContact()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));

        assertTrue(store.deleteContact("foo", "bla"));
        assertFalse(store.contactExists("foo", "bla"));
    }

    @Test
    public void testDeleteNonExistingContact()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));

        assertFalse(store.deleteContact("blah", "hehe"));
        assertTrue(store.contactExists("foo", "bla"));
    }

    @Test
    public void testRepetetivePuts()
    {
        assertTrue(store.putContact("foo", "bla"));
        assertFalse(store.putContact("foo", "bla"));
    }
}
