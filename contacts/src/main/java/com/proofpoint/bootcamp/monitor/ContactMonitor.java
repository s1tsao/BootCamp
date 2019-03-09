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
import com.google.inject.Inject;
import com.proofpoint.event.client.EventClient;
import com.proofpoint.log.Logger;
import com.proofpoint.units.Duration;

public class ContactMonitor
{
    private final EventClient eventClient;
    private final ContactStats stats;

    private final static Logger logger = Logger.get(ContactMonitor.class);

    @Inject
    public ContactMonitor(EventClient eventClient, ContactStats stats)
    {
        Preconditions.checkNotNull(eventClient, "eventClient is null");
        Preconditions.checkNotNull(stats, "stats is null");

        this.eventClient = eventClient;
        this.stats = stats;
    }

    public ContactStats getStats()
    {
        return stats;
    }

    public void contactAdded(String ownerId, String contactId)
    {
        stats.contactAdded();
        eventClient.post(ContactEvent.contactAdded(ownerId, contactId));
        logger.debug("Added ownerId=%s, contactId=%s", ownerId, contactId);
    }

    public void contactDeleted(String ownerId, String contactId)
    {
        stats.contactDeleted();
        eventClient.post(ContactEvent.contactDeleted(ownerId, contactId));
        logger.debug("Deleted ownerId=%s, contactId=%s", ownerId, contactId);
    }

    public void contactExistsRequestSucceeded(String ownerId, String contactId, Duration duration)
    {
        stats.contactExistsRequestSucceeded(duration);
        logger.debug("Contact exists ownerId=%s, contactId=%s (duration=%s)", ownerId, contactId, duration);
    }

    public void contactsRequested(String ownerId, Duration duration)
    {
        stats.contactExistsRequestSucceeded(duration);
        logger.debug("Contact requested ownerId=%s (duration=%s)", ownerId, duration);

    }

    public void contactExistsRequestFailed(Throwable err, String ownerId, String contactId)
    {
        stats.contactExistsRequestFailed();
        logger.error(err, "Contact exists failed ownerId=%s, contactId=%s", ownerId, contactId);
    }
}
