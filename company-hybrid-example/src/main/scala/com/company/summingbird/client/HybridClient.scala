package com.company.summingbird.client

import com.company.summingbird.jobs.AbstractJob
import com.twitter.summingbird.store.ClientStore
import com.twitter.util.Await
import com.company.summingbird.utils.reader.SequenceFileReader

/**
 * Created by s.djamaa on 31/03/14.
 */
object HybridClient {
  def apply[V](job: AbstractJob[V]) = {
    new HybridClient[V](job)
  }
}

class HybridClient[V](val job: AbstractJob[V]) {

  implicit val batcher = job.batcher

  implicit val mno = job.monoid

  val clientStore = {
    ClientStore(job.onlineStore, job.onlineStormStore, 3)
  }

  def get(word: String) = println(lookup(word))

  def lookup(word: String) =
    Await.result {
      println("Batcher :" + batcher.currentBatch)
      clientStore.get(word)
    }

  /*def stormLookup(word: String): Option[Long] =
    Await.result {
      job.stormLookup(word)
    }*/

  /*def processHadoop = {
    jsonJob.runScaldingJob
  }*/
}

