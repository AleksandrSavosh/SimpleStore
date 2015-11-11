package com.github.aleksandrsavosh.simplestore;

import java.util.*;

public abstract class Const {

    public static final Set<Class> fields = new HashSet<Class>();
    static {
        fields.add(Integer.class);
        fields.add(String.class);
        fields.add(Date.class);
    }
    public static final Set<Class> collections = new HashSet<Class>(){{
        add(Collection.class);
        add(List.class);
        add(Set.class);
    }};

    public static final Set<Class> dataFields = new HashSet<Class>(){{
        add(byte[].class);
    }};

    public static Set<Class> modelClasses = new HashSet<Class>();

}
