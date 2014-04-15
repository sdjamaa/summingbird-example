import org.scalatest.{BeforeAndAfter, FunSpec}
import com.twitter.summingbird.storm.{TestStore, StormTestRun, Storm}
import com.twitter.summingbird.TimeExtractor
import com.twitter.summingbird.batch.Batcher
import com.twitter.summingbird.storm.spout.TraversableSpout
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EnrichTopologyTest extends FunSpec with BeforeAndAfter with java.io.Serializable {

  implicit def extractor[T]: TimeExtractor[T] = TimeExtractor(_ => 0L)

  implicit val batcher = Batcher.unit
  implicit val storm = Storm.local()

  def toWords(sentence: String): List[String] = {
    sentence.split(",").toList
  }

  describe("Topology test") {
    it("should be work like charm") {
      val source = Storm.source(TraversableSpout(List( """here,is,a,little,sample""")))
      val store = TestStore.createStore[String, Long]()._2
      val plan = source.flatMap {
        json =>
          //  json.split(",").toList.map(_ -> 1L)
          toWords(json).map(_ -> 1L)
      }.sumByKey(store)

      StormTestRun(plan)

    }
  }
}

/*object MyStormTest {

}*/