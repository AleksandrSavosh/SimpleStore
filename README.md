# SimpleStore

## Overview

SimpleStore is a library that provides handy way to save model objects to different data stores.

**Available stores:** 
 - Local store (SQLite)
 - Cloud store (parse.com)

## Requirements for model:
 * Model must extends from "Base model". 
 * Model can use next types: Integer.class, Long.class, String.class, java.util.Date.class
 * Model also can use other models.
 * And model can use collections of model. (Collection.class, List.class, Set.class)
 * Model can use byte array for files (byte[].class)


```java
import com.github.aleksandrsavosh.simplestore.Base;
public class MyModel extends Base {

    private Integer ints;
    private Long lon;
    private String str;
    private Date dat;
    
    private byte[] file;
    
    private MyOtherModel myOtherModel
    
    private Collection<MyOtherOtherModel> myOtherOtherModels;
    
}
```

## How to start:
```java

//here we pass on context and set of all model classes
SimpleStoreManager manager = SimpleStoreManager.instance(context, new HashSet<Class>(){{
    add(AAA.class);
    add(BBB.class);
    add(CCC.class);
}});

//if you need you may use log info
manager.useLog(true);

//here we pass on database version, if we will use local store
manager.initLocalStore(13);

//local store
SimpleStore<Long> localStore = manager.getLocalStore();

//here init cloud store, pass clientId and applicationId from parse.com
manager.initCloudStore(applicationId, clientKey);

//cloud store
SimpleStore<String> cloudStore = manager.getCloudStore();

```

## How to add dependency?

This library is not released in Maven Central, but instead you can use [JitPack](https://jitpack.io)

add remote maven url
```groovy
repositories {
    maven {
        name "jitpack"
        url "https://jitpack.io"
    }
}
```

then add a library dependency
```groovy
dependencies {
	compile 'com.github.AleksandrSavosh:SimpleStore:v1.1.0-alpha'
}
```
