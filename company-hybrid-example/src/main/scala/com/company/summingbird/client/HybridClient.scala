package com.company.summingbird.client

import com.twitter.summingbird.store.ClientStore
import com.twitter.util.Await
import com.company.summingbird.utils.storm.{StormExecutor, StormRunner}
import com.company.summingbird.utils.scalding.{ScaldingExecutor, ScaldingRunner}

/**
 * Created by s.djamaa on 31/03/14.
 */
object HybridClient {
  import com.company.summingbird.jobs.JsonParsingJob._

  lazy val clientStore = ClientStore(ScaldingRunner.servingStore, StormRunner.stringLongStore, 3)

  def main(args: Array[String]) {
    StormExecutor(args)
    ScaldingExecutor()
  }

  def lookup(word: String): Option[Long] =
    Await.result {
      clientStore.get(word)
    }

  def stormLookup(word: String): Option[Long] =
    Await.result {
      StormRunner.lookup(word)
    }

  def hadoopLookup = ScaldingRunner.lookup

  def processHadoop = {
    ScaldingRunner.runJob
  }
}
