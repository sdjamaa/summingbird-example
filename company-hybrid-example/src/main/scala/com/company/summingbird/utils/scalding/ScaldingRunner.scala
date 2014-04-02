package com.company.summingbird.utils.scalding

import com.twitter.summingbird.Producer
import com.twitter.summingbird.scalding.Scalding
import com.twitter.scalding.{Hdfs, TextLine}
import com.twitter.summingbird.scalding.store.{DirectoryBatchedStore, InitialBatchedStore, VersionedStore}
import com.twitter.storehaus.algebra.MergeableStore
import com.twitter.storehaus.memcache.MemcacheStore
import org.apache.hadoop.conf.Configuration
import com.twitter.summingbird.batch.state.HDFSState
import com.twitter.summingbird.batch.Timestamp
import com.company.summingbird.utils.reader.SequenceFileReader
import org.apache.hadoop.io.{Writable, Text, LongWritable}
import com.twitter.summingbird.batch.BatchID
import com.company.summingbird

/**
 * Created by s.djamaa on 31/03/14.
 */
object ScaldingExecutor {
  def apply(args: Array[String]) {
    ScaldingRunner(args)
  }
}

object ScaldingRunner {

  import com.company.summingbird.jobs.JsonParsingJob._, com.company.summingbird.serialization.StringToBytesSerialization._

  var hasBeenLaunched = false

  val src = Producer.source[Scalding, String](Scalding.pipeFactoryExact[String]( _ => TextLine(inputDir)))

  val actualStore = VersionedStore[String, Long](outputDir)

  val store = new InitialBatchedStore(batcher.currentBatch, actualStore)

  val servingStore = MemcacheStore.typed[String, (BatchID, Long)](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), "scaldingLookCount")

  val mode = Hdfs(true, new Configuration)

  val job = Scalding("billable")

  def apply(args: Array[String]) {
    println("Current batch is : " + batcher.currentBatch)
  }

  def runJob = {

    println("date is : " + batcher.earliestTimeOf(batcher.currentBatch).toDate + " | with timestamp is : " + batcher.earliestTimeOf(batcher.currentBatch).milliSinceEpoch + " | with batch : " + batcher.currentBatch + " | with store : " + store)

    if (!hasBeenLaunched)
      job
        .run(HDFSState(waitstateDir, startTime = Some(Timestamp(System.currentTimeMillis())) ),
          mode,
          job.plan(jsonKeyCount[Scalding](src, store)))
    else {
      job
        .run(HDFSState(waitstateDir),
          mode,
          job.plan(jsonKeyCount[Scalding](src, store)))
      hasBeenLaunched = true
    }
  }

  def queryFiles(arg: Option[String] = None) = {
    arg match {
      case Some(path) => SequenceFileReader(outputDir + path + "/part-00000")
      case None => SequenceFileReader(outputDir + batcher.earliestTimeOf(batcher.currentBatch).milliSinceEpoch + "/part-00000")
    }
  }

  def lookup = {
    def results = ScaldingRunner.store.readLast(batcher.currentBatch + 1, ScaldingRunner.mode)
    println("Results : " + results)
  }
}
