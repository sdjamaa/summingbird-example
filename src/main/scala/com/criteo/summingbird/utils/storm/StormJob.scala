package com.criteo.summingbird.utils.storm

import com.criteo.summingbird.jobs.AbstractJob
import com.twitter.storehaus.memcache.MemcacheStore
import com.twitter.summingbird.batch.BatchID
import com.criteo.summingbird
import com.twitter.tormenta.scheme.Scheme
import com.twitter.tormenta.spout.KafkaSpout
import com.twitter.summingbird.storm.{ Executor, StormExecutionConfig, Storm, StormStore }
import backtype.storm.{ Config => BTConfig }
import com.twitter.summingbird.Options
import com.twitter.scalding.Args
import com.twitter.bijection.Injection
import com.twitter.algebird.Monoid

/**
 * Created by s.djamaa on 09/04/14.
 */
object StormJob {
  def apply[K, V](job: AbstractJob[K, V])
                 (implicit kInj: Injection[(K, BatchID), Array[Byte]], vInj: Injection[V, Array[Byte]], monoid: Monoid[V]) = {
    new StormJob[K, V](job)
  }
}

class StormJob[K, V](job: AbstractJob[K, V])
                    (implicit kInj: Injection[(K, BatchID), Array[Byte]], vInj: Injection[V, Array[Byte]], monoid: Monoid[V]) extends java.io.Serializable {
  lazy implicit val batcher = job.batcher

  lazy implicit val timeOf = job.timeOf

  lazy val jobName = job.jobName

  lazy val scheme: Scheme[String] = Scheme { bytes => Some(new String(bytes)) }

  lazy val src = new KafkaSpout(scheme, zkHost, zkBrokerPath, zkTopic, zkAppId, zkRoot)

  lazy val stormStore: StormStore[K, V] = Storm.store(onlineStormStore)

  lazy val onlineStormStore = MemcacheStore.mergeable[(K, BatchID), V](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), "timestampCount")

  def runJob(args: Array[String]) = {
    Executor(args, createStormConfig(_))
  }

  def createStormConfig(args: Args): StormExecutionConfig =
    new StormExecutionConfig {
      override val name = "CriteoSummingbird"

      // No Ackers
      override def transformConfig(config: Map[String, AnyRef]): Map[String, AnyRef] = {
        config ++ List((BTConfig.TOPOLOGY_ACKER_EXECUTORS -> (new java.lang.Integer(0))))
      }

      override def getNamedOptions: Map[String, Options] = stormOpts

      override def graph = job.job[Storm](src, stormStore)
    }

  //def lookup(word: String) = StormRunner.stringLongStore.get(word -> batcher.currentBatch)
}