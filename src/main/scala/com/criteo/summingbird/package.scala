package com.criteo

import com.typesafe.config.ConfigFactory

/**
 * Created by s.djamaa on 02/04/14.
 */
package object summingbird {

  val conf = ConfigFactory.load("application.properties")

  // Common configuration

  val memcachedHost = conf.getString("memcached.host")
}
