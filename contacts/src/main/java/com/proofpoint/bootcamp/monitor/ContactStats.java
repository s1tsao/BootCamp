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

import com.google.inject.Inject;
import com.proofpoint.stats.TimedStat;
import com.proofpoint.units.Duration;
import org.weakref.jmx.Managed;
import org.weakref.jmx.Nested;

import java.util.concurrent.atomic.AtomicLong;

public class ContactStats
{
    private final AtomicLong contactCount;
    private final TimedStat checkContactStat;
    private final AtomicLong checkContactErrorCount;

    @Inject
    public ContactStats()
    {
        this.contactCount = new AtomicLong();
        this.checkContactStat = new TimedStat();
        this.checkContactErrorCount = new AtomicLong();
    }

    @Managed
    public long getContactCount()
    {
        return contactCount.get();
    }

    @Managed
    @Nested
    public TimedStat getCheckContactRequestTimes()
    {
        return checkContactStat;
    }

    @Managed
    public long getCheckContactRequestErrorCount()
    {
        return checkContactErrorCount.get();
    }

    public void contactAdded()
    {
        contactCount.incrementAndGet();
    }

    public void contactDeleted()
    {
        contactCount.decrementAndGet();
    }

    public void contactExistsRequestSucceeded(Duration duration)
    {
        checkContactStat.addValue(duration);
    }

    public void contactExistsRequestFailed()
    {
        checkContactErrorCount.incrementAndGet();
    }
}
