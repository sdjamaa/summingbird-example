package com.company.summingbird.utils.storm

import com.twitter.summingbird.storm.{Executor, StormExecutionConfig, Storm, StormStore}
import com.twitter.summingbird.batch.BatchID
import com.twitter.summingbird.{TailProducer, Options, Producer}
import com.twitter.tormenta.spout.KafkaSpout
import com.twitter.tormenta.scheme.Scheme
import com.twitter.scalding.Args
import backtype.storm.{Config => BTConfig}
import com.twitter.summingbird.storm.option.{SpoutParallelism, FlatMapParallelism, SummerParallelism}
import com.twitter.summingbird.option.CacheSize
import com.twitter.storehaus.memcache.MemcacheStore
import com.company.summingbird.jobs.JsonParsingJob._

/**
 * Created by s.djamaa on 31/03/14.
 */

object StormExecutor {
  def apply(args: Array[String]) {//, job: (Producer[Storm, String], StormStore[String, Long]) => TailProducer[Storm, Any]) {
    Executor(args, StormRunner(_))
  }
}

object StormRunner {

  import com.company.summingbird.jobs.JsonParsingJob._

  lazy val stringLongStore =
    MemcacheStore.mergeable[(String, BatchID), Long](MemcacheStore.defaultClient("memcached", "localhost:11211"), "timestampCount")

  val scheme: Scheme[String] = Scheme { bytes => Some(new String(bytes)) }

  val spout = new KafkaSpout(scheme, "127.0.0.1:2181", "/brokers", "test", "0", "")

  val storeSupplier: StormStore[String, Long] = Storm.store(stringLongStore)

  def apply(args: Args) : StormExecutionConfig = //, job: (Producer[Storm, String], StormStore[String, Long]) => TailProducer[Storm, Any]): StormExecutionConfig = {
    new StormExecutionConfig {
      override val name = "SummingbirdExample"

      // No Ackers
      override def transformConfig(config: Map[String, AnyRef]): Map[String, AnyRef] = {
        config ++ List((BTConfig.TOPOLOGY_ACKER_EXECUTORS -> (new java.lang.Integer(0))))
      }

      override def getNamedOptions: Map[String, Options] = Map(
        "DEFAULT" -> Options().set(SummerParallelism(2))
          .set(FlatMapParallelism(80))
          .set(SpoutParallelism(16))
          .set(CacheSize(100))
      )
      override def graph = jsonKeyCount[Storm](spout, storeSupplier) //job(spout, storeSupplier)
    }

  def lookup(word: String) = StormRunner.stringLongStore.get(word -> batcher.currentBatch)

}