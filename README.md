summingbird-example
===================

Example of SummingBird in hybrid mode.

I'm currently improving it to be used as a production-ready bootstrap for any job.

See logged issues for expected improvements. Feel free as well to add any other!

Prerequisite
============

 - Get Kafka 0.7.2 here: 

Unzip it anywhere and follow instructions until step 4: http://kafka.apache.org/07/quickstart.html

To test it, run a producer and a consumer in two separate shell windows and write anything to the producer shell window. You should see what was sent in the consumer shell window.

Note: the example is currently incompatible with Kafka 0.8.x due to KafkaSpout being available only for version 0.7.x. Some examples are available but not production ready.

 - Get Memcached here: http://memcached.org/

On OSX you can use brew to get it:

```
   brew install memcached
```

Installation
============

 - First get summingbird from my cloned repository: https://github.com/sdjamaa/summingbird
This clone uses version 0.8.0 of Storehaus library as there is a compatibility issue with Memcached store.

 - Build the project using the following command:
```
    cd summingbird
   ./sbt update compile
```

 - Compile summingbird-example project:
```
   cd summingbird-example
   ./sbt update compile
```

Configuration
=============

Configuration strings are located in src/main/resources/application.properties file.

All configuration strings are retrieved from package objects.

 - Change all paths for Scalding. Storm and memcached configuration values should be the same (default local mode).


Running everything
==================

 - Start ZooKeeper, Kafka server and a Kafka producer: follow instructions from step 2 and step 3 (http://kafka.apache.org/07/quickstart.html)

 - Start memcached service: go to memcached folder and run ```memcached``` command

 - Start example "service":
```
    ./sbt "company-hybrid-example/run --local"
```

This starts Storm and Scalding platforms

 - Start example console in another term window to send data and test Kafka (this will be soon replaced by a real test producer)
```
    ./sbt "company-hybrid-example/console"
```

You can now send messages from your Kafka producer which will be consumed by Summingbird.

To test the Storm consumer, type in the Scala REPL:
```scala
    scala> import com.company.summingbird.client._
    import com.company.summingbird.client._
    scala> HybridClient.stormLookup("timestamp") // This tests the Storm store linked to the ClientStore
    res1: Option[Long] = Some(4)
    scala> HybridClient.lookup("timestamp") // This tests the ClientStore (merge between Storm and Scalding stores for an implicit BatchID)
    res2: Option[Long] = Some(4)
```

To test the Scalding consumer, type in the Scala REPL (to be replaced by a real consumer App as well)
```scala
    scala> HybridClient.processHadoop
    [... some logging ...]
    scala> HybridClient.hadoopLookup
    Results : Right((BatchID.11636597,ReaderFn(<function1>)))
    scala> ScaldingRunner.queryFiles()
    14/04/02 00:36:35 INFO compress.CodecPool: Got brand-new decompressor
    14/04/02 00:36:35 INFO compress.CodecPool: Got brand-new decompressor
    14/04/02 00:36:35 INFO compress.CodecPool: Got brand-new decompressor
    lolstamp : 1
    timestamp : 2
```


Troubleshooting
===============

For Storm: make sure everything is already launched (Kafka + memcached)

For Scalding: change directories in Scalding package object and create a file to be consumed (in a JSON format) somewhere.
