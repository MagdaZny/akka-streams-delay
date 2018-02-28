package spike

import java.time.{LocalDateTime, ZoneOffset}

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.testkit.javadsl.TestSink
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.common.serialization.StringDeserializer
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class DelayTest extends WordSpec with Matchers with EmbeddedKafka {
  import actorSystem.dispatcher

  implicit val actorSystem: ActorSystem = ActorSystem("my-happy-actor-system")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
  implicit val config: EmbeddedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 9092)

  private lazy val InputTopic = "delay-topic"
  private lazy val source = Consumer.committableSource(consumerSettings, Subscriptions.topics(InputTopic))

  "Delay app should not block consumption of other messages" in withRunningKafka {
    publishStringMessageToKafka(InputTopic, "message one")
    publishStringMessageToKafka(InputTopic, "message two")
    consumeNumberStringMessagesFrom(InputTopic, 2)

    val app = new Delay()
    val output = source.via(app())
      .runWith(TestSink.probe(actorSystem))
      .request(2)
      .receiveWithin(10 seconds)

    val firstMessage = output(0)
    val secondMessage = output(1)

    println(firstMessage)
    println(secondMessage)

    firstMessage.timeAfterDelay - secondMessage.timeAfterDelay shouldBe <(1 second)
  }

  private implicit class Seconds(time: LocalDateTime) {
    def -(another: LocalDateTime) = (time.toEpochSecond(ZoneOffset.UTC) - another.toEpochSecond(ZoneOffset.UTC)).seconds
  }

}
