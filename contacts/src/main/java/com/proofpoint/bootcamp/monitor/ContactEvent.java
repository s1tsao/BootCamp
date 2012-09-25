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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.proofpoint.event.client.EventField;
import com.proofpoint.event.client.EventType;

@EventType("test:type=contact")
public class ContactEvent
{
    public static ContactEvent contactAdded(String ownerId, String contactId)
    {
        return new ContactEvent(ownerId, contactId, Operation.ADD);
    }

    public static ContactEvent contactDeleted(String ownerId, String contactId)
    {
        return new ContactEvent(ownerId, contactId, Operation.DELETE);
    }

    public enum Operation
    {
        ADD,
        DELETE
    }

    private final String ownerId;
    private final String contactId;
    private final Operation operation;

    private ContactEvent(String ownerId, String contactId, Operation operation)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");
        Preconditions.checkNotNull(operation, "operation is null");

        this.ownerId = ownerId;
        this.contactId = contactId;
        this.operation = operation;
    }

    @EventField
    public String getOwnerId()
    {
        return ownerId;
    }

    @EventField
    public String getContactId()
    {
        return contactId;
    }

    @EventField
    public String getOperation()
    {
        return operation.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContactEvent that = (ContactEvent) o;

        if (!ownerId.equals(that.ownerId)) {
            return false;
        }
        if (!contactId.equals(that.contactId)) {
            return false;
        }
        if (!operation.equals(that.operation)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(ownerId, contactId, operation);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ContactEvent{ownerId=").append(ownerId);
        sb.append(", contactId=").append(contactId);
        sb.append(", operation=").append(operation);
        sb.append('}');
        return sb.toString();
    }
}
