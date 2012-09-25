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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.proofpoint.bootcamp.monitor.ContactMonitor;

import java.util.Collection;

public class ContactStore
{
    private final Multimap<String, String> contacts;
    private final ContactMonitor monitor;
    private final Object lock;

    @Inject
    public ContactStore(ContactMonitor monitor)
    {
        Preconditions.checkNotNull(monitor, "monitor is null");

        this.monitor = monitor;
        this.contacts = HashMultimap.create();
        this.lock = new Object();
    }

    public Collection<String> getContacts(String ownerId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");

        synchronized (lock) {
            return contacts.get(ownerId);
        }
    }

    public boolean contactExists(String ownerId, String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        synchronized (lock) {
            return contacts.containsEntry(ownerId, contactId);
        }
    }

    public boolean putContact(String ownerId, String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        synchronized (lock) {
            Collection<String> existing = contacts.get(ownerId);
            if (existing.contains(contactId)) {
                return false;
            }

            if (contacts.put(ownerId, contactId)) {
                monitor.contactAdded(ownerId, contactId);
            }
            return true;
        }
    }

    public boolean deleteContact(String ownerId, String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        synchronized (lock) {
            boolean deleted = contacts.remove(ownerId, contactId);
            if (deleted) {
                monitor.contactDeleted(ownerId, contactId);
            }
            return deleted;
        }
    }
}
