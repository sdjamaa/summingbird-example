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

  def deserializeKey[V](bytes: Array[Byte])(implicit inj: Injection[V, Array[Byte]]) = inj.invert(bytes)
  def deserializeValue[V](bytes: Array[Byte])(implicit inj: Injection[(BatchID, V), Array[Byte]]) = inj.invert(bytes)

  def apply[K, V](args: String)(fn: (K,(BatchID,V)) => Unit)
     (implicit kInj: Injection[K, Array[Byte]], vInj: Injection[(BatchID, V), Array[Byte]]) =
  {
    val path = new Path(args)
    val config = new Configuration()
    val reader = new SequenceFile.Reader(FileSystem.get(config), path, config)
    val key = new BytesWritable()
    val value = new BytesWritable()

    while (reader.next(key, value)) {
      val keyBytes = Arrays.copyOfRange(key.getBytes, 0, key.getLength)
      val valueBytes = Arrays.copyOfRange(value.getBytes, 0, value.getLength)
      val decodedKey = deserializeKey[K](keyBytes).get
      val decodedBatchAndValue = deserializeValue[V](valueBytes).get
      fn(decodedKey, decodedBatchAndValue)
    }
    reader.close()
  }
}