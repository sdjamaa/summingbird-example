package com.criteo.summingbird.jobs

import com.twitter.summingbird.batch.{ BatchID, Batcher }
import com.twitter.summingbird.{ TailProducer, Producer, Platform, TimeExtractor }
import com.twitter.scalding.Args
import com.criteo.summingbird.utils.scalding.ScaldingJob
import com.twitter.summingbird.scalding.{ Scalding, Store }
import com.twitter.storehaus.ReadableStore
import com.criteo.summingbird.utils.storm.StormJob
import com.twitter.summingbird.storm.StormStore
import com.twitter.algebird.Monoid
import com.criteo.summingbird.client.HybridClient
import com.twitter.bijection._
import com.twitter.storehaus.algebra.MergeableStore

abstract class AbstractJob[K, V](implicit kInj: Injection[(K, BatchID), Array[Byte]], kInj2: Injection[(BatchID, V), Array[Byte]], vInj: Injection[V, Array[Byte]], monoid: Monoid[V], oInj: Injection[K, Array[Byte]], ord: Ordering[K]) extends java.io.Serializable {

  // TODO: I don't like this...
  lazy implicit val timeOf: TimeExtractor[String] = TimeExtractor(extractTime)

  implicit val batcher: Batcher

  def extractTime(timeType: String): Long

  def job[P <: Platform[P]](source: Producer[P, String], store: P#Store[K, V]): TailProducer[P, (K, (Option[V], V))]

  def buildInputDir(batch: BatchID): String

  val jobName: String

  def main(inargs: Array[String]) = {
    // Parse arguments to define which runner
    val args = Args(inargs)

    // Initialize the runner
    if (args.boolean("scalding")) {
      ScaldingJob(this).runJob
    } /*else if (args.boolean("client")) {
      HybridClient(this).get(args.required("client"))
    }*/ else if (args.boolean("storm")) {
      StormJob(this).runJob(inargs)
    } else
      throw new RuntimeException("nul")
  }
}