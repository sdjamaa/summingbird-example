package com.company.summingbird.utils.scalding

import com.twitter.summingbird.Producer
import com.twitter.summingbird.scalding.Scalding
import com.twitter.scalding.{Hdfs, TextLine}
import com.twitter.summingbird.scalding.store.{InitialBatchedStore, VersionedStore}
import com.twitter.storehaus.algebra.MergeableStore
import org.apache.hadoop.conf.Configuration
import com.twitter.summingbird.batch.state.HDFSState
import com.twitter.summingbird.batch.Timestamp
import com.company.summingbird.utils.reader.SequenceFileReader

/**
 * Created by s.djamaa on 31/03/14.
 */
object ScaldingExecutor {
  def apply(args: Array[String]) {
    ScaldingRunner(args)
  }
}

object ScaldingRunner {

  import com.company.summingbird.jobs.JsonParsingJob._

  var hasBeenLaunched = false

  val jobDir = "/home/s.djamaa/Tools/tmp/"

  val src = Producer.source[Scalding, String](Scalding.pipeFactoryExact[String]( _ => TextLine(jobDir + "input1")))

  val store = new InitialBatchedStore(batcher.currentBatch, VersionedStore[String, Long](jobDir + "output"))

  val servingStore = null// MergeableStore.fromStore(store)

  val mode = Hdfs(true, new Configuration)

  val job = Scalding("billable")

  def apply(args: Array[String]) {
    println("Current batch is : " + batcher.currentBatch)
  }

  def runJob = {

    println("date is : " + batcher.earliestTimeOf(batcher.currentBatch).toDate + " | with timestamp is : " + batcher.earliestTimeOf(batcher.currentBatch).milliSinceEpoch + " | with batch : " + batcher.currentBatch + " | with store : " + store)

    if (!hasBeenLaunched)
      job
        .run(HDFSState("/home/s.djamaa/Tools/tmp/waitstate", startTime = Some(Timestamp(System.currentTimeMillis())) ),
          mode,
          job.plan(jsonKeyCount[Scalding](src, store)))
    else {
      job
        .run(HDFSState("/home/s.djamaa/Tools/tmp/waitstate"),
          mode,
          job.plan(jsonKeyCount[Scalding](src, store)))
      hasBeenLaunched = true
    }
  }

  def queryFiles(arg: Option[String] = None) = {
    arg match {
      case Some(path) => SequenceFileReader(jobDir + "/output/" + path + "/part-00000")
      case None => SequenceFileReader(jobDir + "/output/" + batcher.earliestTimeOf(batcher.currentBatch).milliSinceEpoch + "/part-00000")
    }
  }

  def lookup = {
    def results = ScaldingRunner.store.readLast(batcher.currentBatch + 1, ScaldingRunner.mode)
    println("Results : " + results)
  }
}
