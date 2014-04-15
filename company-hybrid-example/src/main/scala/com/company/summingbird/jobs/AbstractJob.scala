package com.company.summingbird.jobs

import com.twitter.summingbird.batch.{ BatchID, Batcher }
import com.twitter.summingbird.{ TailProducer, Producer, Platform, TimeExtractor }
import com.twitter.scalding.Args
import com.company.summingbird.utils.scalding.ScaldingJob
import com.twitter.summingbird.scalding.{ Scalding, Store }
import com.twitter.storehaus.ReadableStore
import com.company.summingbird.utils.storm.StormJob
import com.twitter.summingbird.storm.StormStore
import com.twitter.algebird.Monoid
import com.company.summingbird.client.HybridClient
import com.twitter.bijection._

abstract class AbstractJob[V] extends java.io.Serializable {

  // TODO: I don't like this...
  lazy implicit val timeOf: TimeExtractor[String] = TimeExtractor(extractTime)

  implicit val batcher: Batcher

  def extractTime(timeType: String): Long

  def job[P <: Platform[P]](source: Producer[P, String], store: P#Store[String, V]): TailProducer[P, (String, (Option[V], V))]

  def buildInputDir(batch: BatchID): String

  val jobName: String

  implicit val monoid: Monoid[V]

  def main(inargs: Array[String]) = {
    // Parse arguments to define which runner
    val args = Args(inargs)

    // Initialize the runner
    if (args.boolean("scalding")) {
      ScaldingJob(this).runJob
    } else if (args.boolean("client")) {
      HybridClient(this).get(args.required("client"))
    } else if (args.boolean("storm")) {
      StormJob(this).runJob(inargs)
    } else
      throw new RuntimeException("nul")
  }

  // TODO: everything here must be moved elsewhere
  val store: Store[String, V]

  val src: Producer[Scalding, String]

  val onlineStore: ReadableStore[String, (BatchID, V)]

  val onlineStormStore: ReadableStore[(String, BatchID), V]

  val stormStore: StormStore[String, V]

  def writeToStore: Unit
}