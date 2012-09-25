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
package com.proofpoint.bootcamp.monitor;

import com.google.common.base.Preconditions;
import com.proofpoint.bootcamp.Person;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.log.Logger;

public class PersonStoreMonitor
{
    private final EventClient eventClient;
    private final PersonStoreStats stats;

    private final static Logger logger = Logger.get(PersonStoreMonitor.class);

    public PersonStoreMonitor(EventClient eventClient, PersonStoreStats stats)
    {
        Preconditions.checkNotNull(eventClient, "eventClient is null");
        Preconditions.checkNotNull(stats, "stats is null");

        this.eventClient = eventClient;
        this.stats = stats;
    }

    public void personFetched(Person person)
    {
        stats.personFetched();
        logger.debug("Fetched " + person);
    }

    public void personAdded(Person person)
    {
        stats.personAdded();
        eventClient.post(PersonEvent.personAdded(person));
        logger.debug("Added " + person);
    }

    public void personUpdated(Person person)
    {
        stats.personUpdated();
        eventClient.post(PersonEvent.personUpdated(person));
        logger.debug("Updated " + person);
    }

    public void personDeleted(Person person)
    {
        stats.personDeleted();
        eventClient.post(PersonEvent.personDeleted(person));
        logger.debug("Deleted " + person);
    }

    public void allPersonsFetched()
    {
        stats.allPersonsFetched();
        logger.debug("All fetched");
    }
}
