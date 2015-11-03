# SimpleStore

Overview

SimpleStore is a library that provides opportunity easy persist model objects to different data stores. 

Available stores:
Local store (SQLite)
Cloud store (in future)

How to start:
SimpleStoreFactory factory = SimpleStoreFactory.instance(context);

factory.initLocalStore()
