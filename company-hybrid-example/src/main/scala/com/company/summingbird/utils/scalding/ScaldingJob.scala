package com.company.summingbird.utils.scalding

import org.apache.hadoop.conf.Configuration
import com.twitter.summingbird.batch.state.HDFSState
import com.twitter.summingbird.batch.{BatchID, Timestamp}
import com.twitter.summingbird.scalding.Scalding
import com.twitter.summingbird.scalding.store.VersionedStore
import com.company.summingbird.jobs.{JobRunner, AbstractJob}
import com.twitter.scalding.Hdfs
import scala.Some
import com.twitter.storehaus.memcache.MemcacheStore
import com.company.summingbird
import com.company.summingbird.utils.reader.SequenceFileReader
import com.twitter.storehaus.WritableStore
import com.twitter.bijection._
import com.twitter.scalding.Hdfs
import scala.Some
import com.twitter.scalding.Hdfs
import scala.Some

/**
 * Created by s.djamaa on 07/04/14.
 */

object ScaldingJob {
  def apply[V](job: AbstractJob[V]) = {
    new ScaldingJob[V](job)
  }
}

class ScaldingJob[V](val job: AbstractJob[V]) extends java.io.Serializable {

  implicit var batcher = job.batcher

  implicit var timeOf = job.timeOf

  lazy val jobName = job.jobName

  lazy val scaldingJob = Scalding(jobName)

  //TODO: to move to a HDFSScaldingJob or use a configuration object
  lazy val mode = Hdfs(true, new Configuration)

  var hasBeenLaunched = false

  def runJob() = {
    if (!hasBeenLaunched)
      scaldingJob
        .run(HDFSState(waitstateDir, startTime = Some(Timestamp(System.currentTimeMillis())) ),
          mode,
          scaldingJob.plan(job.job[Scalding](job.src, job.store)))
    else {
      scaldingJob
        .run(HDFSState(waitstateDir),
          mode,
          scaldingJob.plan(job.job[Scalding](job.src, job.store)))
      hasBeenLaunched = true
    }

    job.writeToStore
  }

}
