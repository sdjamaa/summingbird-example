package com.company
package summingbird

/**
 * Created by s.djamaa on 10/04/14.
 */
package object client {

  val zkHost = conf.getString("storm.zk.host")

  val zkTopic = conf.getString("storm.zk.topic")

  val kafkaInputDir = conf.getString("kafka.local.inputdir")

}
