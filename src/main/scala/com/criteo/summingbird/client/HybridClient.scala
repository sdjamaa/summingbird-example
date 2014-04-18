package com.criteo.summingbird.client

import com.criteo.summingbird.jobs.AbstractJob
import com.twitter.summingbird.store.ClientStore
import com.twitter.util.Await
import com.criteo.summingbird.utils.reader.SequenceFileReader

/**
 * Created by s.djamaa on 31/03/14.
 */
object HybridClient {
  def apply[K, V](job: AbstractJob[K, V]) = {
    new HybridClient[K, V](job)
  }
}

class HybridClient[K, V](val job: AbstractJob[K, V]) {

/*  implicit val batcher = job.batcher

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
*/
  /*def stormLookup(word: String): Option[Long] =
    Await.result {
      job.stormLookup(word)
    }*/

  /*def processHadoop = {
    jsonJob.runScaldingJob
  }*/
}
