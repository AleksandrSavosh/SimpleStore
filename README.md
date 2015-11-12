# SimpleStore

## Overview

SimpleStore is a library that provides handy way to save model objects to different data stores.

**Available stores:** 
 - Local store (SQLite)
 - Cloud store (parse.com)

## Requirements for model:
 * Model must extends from "Base model". 
 * Model can use next types: Integer.class, String.class, java.util.Date.class 
 * Model also can use other models.
 * And model can use collections of model. (Collection.class, List.class, Set.class)
 * For files model can use byte array (byte[].class)


```java
import com.github.aleksandrsavosh.simplestore.Base;
public class MyModel extends Base {

    private Integer ints;
    private String str;
    private Date dat;
    
    private byte[] file;
    
    private MyOtherModel myOtherModel
    
    private Collection<MyOtherOtherModel> myOtherOtherModels;
    
}
```

## How to start:
```java
class MyModel extends Base {}

SimpleStoreFactory factory = SimpleStoreFactory.instance(<context>);
factory.initLocalStore(1, new HashSet(){{ add(MyModel.class); }});
SimpleStore<MyModel, Long> store = factory.getLocalStore(MyModel.class);

MyModel myModel = store.create(new MyModel());
MyModel myModel2 = store.read(myModel.getLocalId());

factory.destroy();
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
	compile 'com.github.AleksandrSavosh:SimpleStore:v1.0.0-alpha'
}
```
