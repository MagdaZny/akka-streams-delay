package spike

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.javadsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Attributes, DelayOverflowStrategy}
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.duration._

class DelayTest extends WordSpec with Matchers {

  implicit val actorSystem: ActorSystem = ActorSystem("my-happy-actor-system")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(actorSystem)
      .withInputBuffer(initialSize = 16, maxSize = 16)
  )

  "Delay app should not loose messages" in {
    val messages = (1 to 20).map(n => n + "message")

    val output = Source(messages.toList)
      .delay(4 seconds, DelayOverflowStrategy.emitEarly).addAttributes(Attributes.inputBuffer(16, 16))
      .runWith(TestSink.probe(actorSystem))
      .request(messages.size)
      .receiveWithin(10 seconds)

    output should be(messages)
  }
}
