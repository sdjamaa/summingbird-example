package com.company.summingbird.jobs

import com.twitter.summingbird.{Producer, Platform, TimeExtractor}
import java.util.Date
import com.twitter.summingbird.batch.{BatchID, Batcher}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.collection.JavaConversions._
import com.twitter.bijection._

/**
 * Created by s.djamaa on 31/03/14.
 */
object JsonParsingJob {

  implicit val timeOf: TimeExtractor[String] = TimeExtractor(_ => new Date().getTime)//extractTime)
  implicit val batcher = Batcher.ofMinutes(2)

  def extractTime(jsonString: String) = {
    mapper.readTree(jsonString).findValue("timestamp").asLong()
  }

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def jsonKeyCount[P <: Platform[P]](
                                      source: Producer[P, String],
                                      store: P#Store[String, Long]) = {
    source
      .map { jsonString: String =>  mapper.readTree(jsonString) }
      .flatMap { jsonNode: JsonNode => jsonNode.fieldNames() map (_ -> 1L) }
      .sumByKey(store)
  }
}
