package com.company.summingbird.utils.reader

import com.twitter.bijection._
import com.twitter.summingbird.batch.BatchID
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{BytesWritable, SequenceFile}
import java.util.Arrays

/**
 * Created by s.djamaa on 31/03/14.
 */
object SequenceFileReader {

  import com.company.summingbird.serialization.StringToBytesSerialization._

  def deserialize[V](bytes: Array[Byte])(implicit inj: Injection[(BatchID, V), Array[Byte]], c: Codec[V]) = inj.invert(bytes)

  def apply(args: String) {
    val path = new Path(args)
    val config = new Configuration()
    val reader = new SequenceFile.Reader(FileSystem.get(config), path, config)
    val key = new BytesWritable()
    val value = new BytesWritable()

    while (reader.next(key, value)) {
      val valueBytes = Arrays.copyOfRange(value.getBytes, 0, value.getLength)
      println(new String(key.getBytes, 0, key.getLength) + " : " + deserialize[Long](valueBytes).get._2)
    }
    reader.close()
  }
}