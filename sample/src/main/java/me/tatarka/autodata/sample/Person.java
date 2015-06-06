package me.tatarka.autodata.sample;

import me.tatarka.autodata.base.AutoData;

/**
 * Created by evan on 6/6/15.
 */
@AutoData
public abstract class Person {
    
//    public static Builder builder() {
//        return new AutoData_Person.Builder();
//    }

    public abstract String name();

    public abstract int age();

    @AutoData.Builder
    public interface Builder {
        Builder name(String name);

        Builder age(int age);

        Person build();
    }
}
