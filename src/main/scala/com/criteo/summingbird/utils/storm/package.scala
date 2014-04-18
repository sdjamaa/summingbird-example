package com.criteo
package summingbird
package utils


import com.twitter.summingbird.Options
import com.twitter.summingbird.storm.option.{SpoutParallelism, FlatMapParallelism, SummerParallelism}
import com.twitter.summingbird.option.CacheSize

/**
 * Created by s.djamaa on 02/04/14.
 */
package object storm {
  // Storm configuration

  val zkHost = conf.getString("storm.zk.host")

  val zkBrokerPath = conf.getString("storm.zk.brokerpath")

  val zkTopic = conf.getString("storm.zk.topic")

  val zkAppId = conf.getString("storm.zk.appid")

  val zkRoot = conf.getString("storm.zk.root")

  val stormOpts = Map(
    "DEFAULT" -> Options().set(SummerParallelism(2))
      .set(FlatMapParallelism(80))
      .set(SpoutParallelism(16))
      .set(CacheSize(100)))

}
