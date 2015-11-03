# SimpleStore

##Overview

SimpleStore is a library that provides opportunity easy persist model objects to different data stores. 

** Available stores: ** 
 - Local store (SQLite)
 - Cloud store (in future)

##How to start:
`class MyModel extends Base {}

SimpleStoreFactory factory = SimpleStoreFactory.instance(<context>);
factory.initLocalStore(1, new HashSet(){{ add(MyModel.class); }});
SimpleStore<MyModel, Long> store = factory.getLocalStore(MyModel.class);

MyModel myModel = store.create(new MyModel());
MyModel myModel2 = store.read(myModel.getLocalId());

factory.destroy();`
