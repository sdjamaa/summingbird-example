package com.company.summingbird.utils.storm

import com.company.summingbird.jobs.AbstractJob
import com.twitter.storehaus.memcache.MemcacheStore
import com.twitter.summingbird.batch.BatchID
import com.company.summingbird
import com.twitter.tormenta.scheme.Scheme
import com.twitter.tormenta.spout.KafkaSpout
import com.twitter.summingbird.storm.{ Executor, StormExecutionConfig, Storm, StormStore }
import backtype.storm.{ Config => BTConfig }
import com.twitter.summingbird.Options
import com.twitter.scalding.Args

/**
 * Created by s.djamaa on 09/04/14.
 */
object StormJob {
  def apply[V](job: AbstractJob[V]) = {
    new StormJob[V](job)
  }
}

class StormJob[V](job: AbstractJob[V]) extends java.io.Serializable {
  lazy implicit val batcher = job.batcher

  lazy implicit val timeOf = job.timeOf

  lazy val jobName = job.jobName

  lazy val scheme: Scheme[String] = Scheme { bytes => Some(new String(bytes)) }

  lazy val src = new KafkaSpout(scheme, zkHost, zkBrokerPath, zkTopic, zkAppId, zkRoot)

  def runJob(args: Array[String]) = {
    Executor(args, createStormConfig(_))
  }

  def createStormConfig(args: Args): StormExecutionConfig =
    new StormExecutionConfig {
      override val name = "SummingbirdExample"

      // No Ackers
      override def transformConfig(config: Map[String, AnyRef]): Map[String, AnyRef] = {
        config ++ List((BTConfig.TOPOLOGY_ACKER_EXECUTORS -> (new java.lang.Integer(0))))
      }

      override def getNamedOptions: Map[String, Options] = stormOpts

      override def graph = job.job[Storm](src, job.stormStore)
    }

  //def lookup(word: String) = StormRunner.stringLongStore.get(word -> batcher.currentBatch)
}