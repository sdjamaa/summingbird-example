package com.criteo.summingbird.jobs

import com.twitter.summingbird.{ Producer, Platform }
import java.util.{ Date, TimeZone }
import com.twitter.summingbird.batch.{ BatchID, Batcher }
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.collection.JavaConversions._
import com.criteo.summingbird.utils.scalding._
import java.text.SimpleDateFormat
import com.twitter.scalding.MultipleTextLineFiles
import com.criteo.summingbird.serialization.StringToBytesSerialization._

/**
 * Created by s.djamaa on 31/03/14.
 */
object JsonParsingJob extends AbstractJob[String, Long] with java.io.Serializable {

  import com.twitter.algebird.Monoid._

  val monoid = longMonoid

  // JSON related things
  lazy val mapper = new ObjectMapper()

  // Batcher to define for the current job
  lazy override implicit val batcher: Batcher = Batcher.ofMinutes(2)

  // The actual job
  override def job[P <: Platform[P]](source: Producer[P, String],
    store: P#Store[String, Long]) = {
    source
      .map { jsonString: String => mapper.registerModule(DefaultScalaModule); mapper.readTree(jsonString) }
      .flatMap { jsonNode: JsonNode => jsonNode.fieldNames() map (_ -> 1L) }
      .sumByKey(store)
  }

  // Method to retrieve input files
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
}
