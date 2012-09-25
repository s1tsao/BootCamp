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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.proofpoint.bootcamp.monitor.PersonStoreMonitor;
import com.proofpoint.bootcamp.monitor.PersonStoreStats;
import com.proofpoint.event.client.EventClient;
import org.weakref.jmx.Flatten;
import org.weakref.jmx.Managed;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PersonStore
{
    private final Map<String, Person> persons;
    private final PersonStoreStats stats;
    private final PersonStoreMonitor monitor;

    @Inject
    public PersonStore(EventClient eventClient)
    {
        Preconditions.checkNotNull(eventClient, "eventClient is null");

        Map<String, Person> personMap = Maps.newHashMap();
        this.persons = Collections.synchronizedMap(personMap);
        this.stats = new PersonStoreStats();
        this.monitor = new PersonStoreMonitor(eventClient, stats);
    }

    @Managed
    @Flatten
    public PersonStoreStats getStats()
    {
        return stats;
    }

    /**
     * @return null if the entry was not found
     */
    public Person get(String id)
    {
        Preconditions.checkNotNull(id, "id is null");

        Person person = persons.get(id);
        if (person != null) {
            monitor.personFetched(person);
        }

        return person;
    }

    /**
     * @return true if the entry was created for the first time
     */
    public boolean put(Person person)
    {
        Preconditions.checkNotNull(person, "person is null");

        boolean added = persons.put(person.getId(), person) == null;
        if (added) {
            monitor.personAdded(person);
        }
        else {
            monitor.personUpdated(person);
        }
        return added;
    }

    /**
     * @return true if the entry was removed
     */
    public boolean delete(String id)
    {
        Preconditions.checkNotNull(id, "id is null");

        Person removedPerson = persons.remove(id);
        if (removedPerson != null) {
            monitor.personDeleted(removedPerson);
        }

        return removedPerson != null;
    }

    public Collection<Person> getAll()
    {
        monitor.allPersonsFetched();
        return ImmutableList.copyOf(persons.values());
    }
}
