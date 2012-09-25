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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.net.URI;

public class PersonRepresentation
{
    private final Person person;
    private final URI self;

    public static PersonRepresentation from(Person person, URI self)
    {
        Preconditions.checkNotNull(person, "person is null");

        return new PersonRepresentation(person, self);
    }

    @JsonCreator
    public PersonRepresentation(@JsonProperty("id") String id, @JsonProperty("email") String email,
                                @JsonProperty("name") String name, @JsonProperty("self") URI self)
    {
        this(new Person(id, email, name), self);
    }

    private PersonRepresentation(Person person, URI self)
    {
        this.person = person;
        this.self = self;
    }

    @JsonProperty
    @NotNull(message = "is missing")
    public String getId()
    {
        return person.getId();
    }

    @JsonProperty
    @NotNull(message = "is missing")
    @Pattern(regexp = "[^@]+@.+", message = "is malformed")
    public String getEmail()
    {
        return person.getEmail();
    }

    @JsonProperty
    @NotNull(message = "is missing")
    public String getName()
    {
        return person.getName();
    }

    @JsonProperty
    public URI getSelf()
    {
        return self;
    }

    public Person toPerson()
    {
        return person;
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

        PersonRepresentation that = (PersonRepresentation) o;

        if (!Objects.equal(person.getId(), that.person.getId())) {
            return false;
        }
        if (!Objects.equal(person.getEmail(), that.person.getEmail())) {
            return false;
        }
        if (!Objects.equal(person.getName(), that.person.getName())) {
            return false;
        }
        if (!Objects.equal(self, that.self)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(person.getId(), person.getEmail(), person.getName(), self);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("PersonRepresentation{person=").append(person);
        sb.append(", self=").append(self);
        sb.append('}');
        return sb.toString();
    }
}
