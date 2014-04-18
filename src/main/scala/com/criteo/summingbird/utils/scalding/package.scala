package com.criteo
package summingbird
package utils

import com.criteo.summingbird.jobs.AbstractJob

/**
 * Created by s.djamaa on 02/04/14.
 */
package object scalding {

  // Scalding configuration

  val inputDir = conf.getString("scalding.job.input.path")

  val waitstateDir = conf.getString("scalding.job.waitstate.path")

  val outputDir = conf.getString("scalding.job.output.path")
}
