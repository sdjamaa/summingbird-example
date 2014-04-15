package com.company.summingbird.client

import org.slf4j.LoggerFactory
import java.util.Properties
import kafka.serializer.StringEncoder
import kafka.producer.{ ProducerData, ProducerConfig, Producer }

/**
 * Created by s.djamaa on 09/04/14.
 */
object RunKafkaStream extends App {
  KafkaStream.run()
}

object KafkaStream {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val props = new Properties()
  props.put("zk.connect", zkHost)
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  props.put("producer.type", "async")

  lazy val producer = new Producer[String, String](new ProducerConfig(props))

  def run() = {
    println(kafkaInputDir)
    new java.io.File(kafkaInputDir).listFiles.foreach {
      file =>
        // Retrieve file from folder
        scala.io.Source.fromFile(file).getLines().foreach {
          line =>
            producer.send(new ProducerData(zkTopic, line))
            Thread.sleep(10000)
        }
    }
  }
}