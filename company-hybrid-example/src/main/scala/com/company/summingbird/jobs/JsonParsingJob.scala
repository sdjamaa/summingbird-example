package com.company.summingbird.jobs

import com.twitter.summingbird.{ Producer, Platform }
import java.util.{ Date, TimeZone }
import com.twitter.summingbird.batch.{ BatchID, Batcher }
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.collection.JavaConversions._
import com.company.summingbird.utils.scalding._
import java.text.SimpleDateFormat
import com.twitter.summingbird.scalding.Scalding
import com.twitter.summingbird.scalding.store.{ VersionedStore, InitialBatchedStore }
import com.twitter.storehaus.memcache.MemcacheStore
import com.company.summingbird
import com.twitter.scalding.{ MultipleTextLineFiles, DateRange }
import com.company.summingbird.utils.reader.SequenceFileReader
import com.twitter.summingbird.storm.{ Storm, StormStore }
import com.twitter.algebird.Monoid
import com.twitter.storehaus.WritableStore
import com.twitter.summingbird.sink.CompoundSink

/**
 * Created by s.djamaa on 31/03/14.
 */
object JsonParsingJob extends AbstractJob[Long] with java.io.Serializable {

  import com.twitter.algebird.Monoid._, com.company.summingbird.serialization.StringToBytesSerialization._

  def inj = kInjection[String]

  override val monoid = longMonoid

  // JSON related things
  lazy val mapper = new ObjectMapper()

  // Batcher to define for the current job
  lazy override implicit val batcher: Batcher = Batcher.ofMinutes(2)

  // The actual job
  override def job[P <: Platform[P]](source: Producer[P, String],
    store: P#Store[String, Long]) = {
    source
      .map { jsonString: String => mapper.registerModule(DefaultScalaModule); mapper.readTree(jsonString) }
      .flatMap { jsonNode: JsonNode => jsonNode.fieldNames() map (_ -> 1L) } // Seq("lol" -> 1L) } //
      .sumByKey(store)
  }

  // Method to retrieve input files
  // TODO: use a trait to declare the input dir (can be different depending on the log type, the partitions and the input formats)
  override def buildInputDir(batch: BatchID) = {
    val DataFileDateFormat = new SimpleDateFormat("yyyyMMdd")
    DataFileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    inputDir + "/" + DataFileDateFormat.format(batcher.earliestTimeOf(batch.next).toDate) + "/"
  }

  // The job name
  lazy override val jobName = "billable"

  override def extractTime(jsonString: String) = {
    new Date().getTime
    //mapper.readTree(jsonString).findValue("timestamp").asLong()
  }

  // TODO: move all the following definitions to a Scalding trait (directory or something...)
  def buildFolders(range: DateRange) = {

    val DataFileDateFormat = new SimpleDateFormat("yyyyMMdd")
    DataFileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

    val start = batcher.batchOf(range.start.value)
    val end = batcher.batchOf(range.end.value)

    val batches: Seq[BatchID] = Stream.iterate(start)(_ + 1).takeWhile(_ <= end)

    val files = batches.map { batch => inputDir + "/" + DataFileDateFormat.format(batcher.earliestTimeOf(batch.next).toDate) + "/" }
    MultipleTextLineFiles(files: _*)
  }

  lazy override val src = Producer.source[Scalding, String](Scalding.pipeFactory(buildFolders(_)))

  lazy override val store = new InitialBatchedStore(batcher.currentBatch, VersionedStore[String, Long](outputDir + "/" + jobName))

  lazy override val onlineStore = MemcacheStore.typed[String, (BatchID, Long)](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), jobName)

  override def writeToStore = {
    SequenceFileReader[String, Long](outputDir + "/" + jobName + "/" + batcher.earliestTimeOf(batcher.currentBatch + 1).milliSinceEpoch + "/part-00000")((k, bAndV) => { println("I'm writing: " + k + "_" + bAndV); onlineStore.put((k, Some(bAndV))) })
  }

  // TODO: and the following lines should be moved as well to a Storm trait... (fucking type-safety language!!!)
  lazy override val stormStore: StormStore[String, Long] = Storm.store(onlineStormStore)

  lazy override val onlineStormStore = MemcacheStore.mergeable[(String, BatchID), Long](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), "timestampCount")
}
