package com.criteo.summingbird.utils.scalding

import org.apache.hadoop.conf.Configuration
import com.twitter.summingbird.batch.state.HDFSState
import com.twitter.summingbird.batch.{BatchID, Timestamp}
import com.twitter.summingbird.scalding.Scalding
import com.twitter.summingbird.scalding.store.{InitialBatchedStore, VersionedStore}
import com.criteo.summingbird.jobs.AbstractJob
import com.twitter.scalding.{MultipleTextLineFiles, DateRange, Hdfs}
import com.twitter.storehaus.memcache.MemcacheStore
import com.criteo.summingbird.utils.reader.SequenceFileReader
import com.twitter.bijection._
import java.text.SimpleDateFormat
import java.util.TimeZone
import scala.Some
import com.twitter.summingbird.Producer
import com.criteo.summingbird

/**
 * Created by s.djamaa on 07/04/14.
 */

object ScaldingJob {
  def apply[K, V](job: AbstractJob[K, V])
                 (implicit kInj: Injection[(K, BatchID), Array[Byte]], vInj: Injection[(BatchID, V), Array[Byte]], oInj: Injection[K, Array[Byte]], ord: Ordering[K]) = {
    new ScaldingJob[K, V](job)
  }
}

class ScaldingJob[K, V](val job: AbstractJob[K, V])
                       (implicit kInj: Injection[(K, BatchID), Array[Byte]], vInj: Injection[(BatchID, V), Array[Byte]], oInj: Injection[K, Array[Byte]], ord: Ordering[K]) extends java.io.Serializable {

  implicit var batcher = job.batcher

  implicit var timeOf = job.timeOf

  lazy val jobName = job.jobName

  lazy val scaldingJob = Scalding(jobName)

  lazy val mode = Hdfs(true, new Configuration)

  var hasBeenLaunched = false

  def runJob() = {
    if (!hasBeenLaunched)
      scaldingJob
        .run(HDFSState(waitstateDir, startTime = Some(Timestamp(System.currentTimeMillis())) ),
          mode,
          scaldingJob.plan(job.job[Scalding](src, store)))
    else {
      scaldingJob
        .run(HDFSState(waitstateDir),
          mode,
          scaldingJob.plan(job.job[Scalding](src, store)))
      hasBeenLaunched = true
    }

    writeToStore
  }

  def buildFolders(range: DateRange) = {

    val DataFileDateFormat = new SimpleDateFormat("yyyyMMdd")
    DataFileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

    val start = batcher.batchOf(range.start.value)
    val end = batcher.batchOf(range.end.value)

    val batches: Seq[BatchID] = Stream.iterate(start)(_ + 1).takeWhile(_ <= end)

    val files = batches.map { job.buildInputDir(_) }
    MultipleTextLineFiles(files: _*)
  }

  lazy val src = Producer.source[Scalding, String](Scalding.pipeFactory(buildFolders(_)))

  lazy val store = new InitialBatchedStore(batcher.currentBatch, VersionedStore[K, V](outputDir + "/" + jobName))

  lazy val onlineStore = MemcacheStore.typed[K, (BatchID, V)](MemcacheStore.defaultClient("memcached", summingbird.memcachedHost), jobName)

  def writeToStore = {
    SequenceFileReader[K, V](outputDir + "/" + jobName + "/" + batcher.earliestTimeOf(batcher.currentBatch + 1).milliSinceEpoch + "/part-00000")((k, bAndV) => { println("I'm writing: " + k + "_" + bAndV); onlineStore.put((k, Some(bAndV))) })
  }

}
