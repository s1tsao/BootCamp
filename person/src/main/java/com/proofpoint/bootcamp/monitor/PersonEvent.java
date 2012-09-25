package com.proofpoint.bootcamp.monitor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.proofpoint.bootcamp.Person;
import com.proofpoint.event.client.EventField;
import com.proofpoint.event.client.EventType;

@EventType("test:type=person")
public class PersonEvent
{
    public static PersonEvent personAdded(Person person)
    {
        return new PersonEvent(person, Operation.ADD);
    }

    public static PersonEvent personUpdated(Person person)
    {
        return new PersonEvent(person, Operation.UPDATE);
    }

    public static PersonEvent personDeleted(Person person)
    {
        return new PersonEvent(person, Operation.DELETE);
    }

    public enum Operation
    {
        ADD,
        UPDATE,
        DELETE
    }

    private final Person person;
    private final Operation operation;

    private PersonEvent(Person person, Operation operation)
    {
        Preconditions.checkNotNull(person, "person is null");
        Preconditions.checkNotNull(operation, "operation is null");

        this.person = person;
        this.operation = operation;
    }

    @EventField
    public String getPersonId()
    {
        return person.getId();
    }

    @EventField
    public String getPersonEmail()
    {
        return person.getEmail();
    }

    @EventField
    public String getPersonName()
    {
        return person.getName();
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

        PersonEvent that = (PersonEvent) o;

        if (!person.equals(that.person)) {
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
        return Objects.hashCode(person, operation);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("PersonEvent{person=").append(person);
        sb.append(", operation=").append(operation);
        sb.append('}');
        return sb.toString();
    }
}
