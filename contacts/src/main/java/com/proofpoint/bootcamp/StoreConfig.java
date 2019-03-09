package com.proofpoint.bootcamp;

import com.proofpoint.configuration.Config;
import com.proofpoint.units.Duration;
import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;


public class StoreConfig {
    private int capacity = 1;
    private Duration ttl = new Duration(1, TimeUnit.HOURS)
            ;
    /*@Config("store.ttl")
    StoreConfig setTtl(Duration ttl)
    {
        this.ttl = Preconditions.checkNotNull(ttl, "ttl must not be null");
        return this;
    }*/

    @Config("Contacts.Size")
    public StoreConfig setCapacity(int capacity){
        this.capacity = capacity;
        return this;
    }

    public int getCapacity() {
        return capacity;
    }

}
