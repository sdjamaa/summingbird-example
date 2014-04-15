/*package com.company.summingbird.utils.scalding

import com.twitter.summingbird.Producer
import com.twitter.summingbird.scalding.Scalding
import com.twitter.scalding.{MultipleTextLineFiles, DateRange, Hdfs, TextLine}
import com.twitter.summingbird.scalding.store.{DirectoryBatchedStore, InitialBatchedStore, VersionedStore}
import com.twitter.storehaus.algebra.MergeableStore
import com.twitter.storehaus.memcache.MemcacheStore
import org.apache.hadoop.conf.Configuration
import com.twitter.summingbird.batch.state.HDFSState
import com.twitter.summingbird.batch.{BatchID, Timestamp}
import com.company.summingbird.utils.reader.SequenceFileReader
import org.apache.hadoop.io.{Writable, Text, LongWritable}
import com.company.summingbird
import java.text.SimpleDateFormat
import java.util.TimeZone
import com.twitter.scalding.Hdfs
import scala.Some
import com.twitter.scalding.MultipleTextLineFiles
import com.twitter.scalding.Hdfs
import scala.Some
import com.twitter.scalding.MultipleTextLineFiles
import com.twitter.scalding.Hdfs
import scala.Some
import com.twitter.scalding.MultipleTextLineFiles

/**
 * Created by s.djamaa on 31/03/14.
 */
object ScaldingExecutor {
  def apply() {
    // Should build Hadoop configuration with Executor from summingbird.scalding package
    // In the form of Args = Map("start-time" -> 13530350, "batches" -> 1 ...)
    //Executor(args, ScaldingRunner())
  }
}

object ScaldingRunner {

  import com.company.summingbird.jobs.JsonParsingJob._, com.company.summingbird.serialization.StringToBytesSerialization._

  val jobName = "billable"

  var hasBeenLaunched = false

  val DataFileDateFormat = new SimpleDateFormat("yyyyMMdd")
  DataFileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

  def buildFolders(range: DateRange) = {

    val start = batcher.batchOf(range.start.value)
    val end = batcher.batchOf(range.end.value)

    val batches: Seq[BatchID] = Stream.iterate(start)(_ + 1).takeWhile(_ <= end)

    val files = batches.map { batch => inputDir + "/" + DataFileDateFormat.format(batcher.earliestTimeOf(batch.next).toDate) + "/" }

    MultipleTextLineFiles(files: _*)
  }

  val src = Producer.source[Scalding, String](Scalding.pipeFactory(buildFolders(_)))

  val actualStore = VersionedStore[String, Long](outputDir + "/" + jobName)

  val store = new InitialBatchedStore(batcher.currentBatch, actualStore)

  val servingStore = MemcacheStore.typed[String, (BatchID, Long)](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), jobName)

  val mode = Hdfs(true, new Configuration)

  val job = Scalding(jobName)

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

    SequenceFileReader[String, Long](outputDir + "/" + jobName + "/" + batcher.earliestTimeOf(batcher.currentBatch + 1).milliSinceEpoch + "/part-00000")((k, bAndV) => servingStore.put((k,Some(bAndV))))
  }

  def queryFiles(arg: Option[String] = None) = {
    arg match {
      case Some(path) => SequenceFileReader[String, Long](outputDir + "/" + jobName + "/" + path + "/part-00000")((k, bAndV) => println(k + " : " + bAndV._2))
      case None => SequenceFileReader[String, Long](outputDir + "/" + jobName + "/" + batcher.earliestTimeOf(batcher.currentBatch).milliSinceEpoch + "/part-00000")((k, bAndV) => println(k + " : " + bAndV._2))
    }
  }

  def lookup = {
    def results = ScaldingRunner.store.readLast(batcher.currentBatch + 1, ScaldingRunner.mode)
    println("Results : " + results)
  }
}*/