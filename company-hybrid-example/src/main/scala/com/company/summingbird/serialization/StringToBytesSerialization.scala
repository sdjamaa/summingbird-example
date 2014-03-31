package com.company.summingbird.serialization

import com.twitter.bijection.{ Bufferable, Codec, Injection }
import com.twitter.summingbird.batch.BatchID

/**
 * Created by s.djamaa on 31/03/14.
 */
object StringToBytesSerialization {

  implicit def kInjection[T: Codec]: Injection[(T, BatchID), Array[Byte]] = {
    implicit val buf =
      Bufferable.viaInjection[(T, BatchID), (Array[Byte], Array[Byte])]
    Bufferable.injectionOf[(T, BatchID)]
  }

  implicit def vInj[V: Codec]: Injection[(BatchID, V), Array[Byte]] =
    Injection.connect[(BatchID, V), (V, BatchID), Array[Byte]]

}
