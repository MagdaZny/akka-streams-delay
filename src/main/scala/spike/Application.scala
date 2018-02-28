package spike

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object Application {
  implicit val actorSystem: ActorSystem = ActorSystem("my-happy-actor-system")
  implicit val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  import actorSystem.dispatcher

  private val source = Consumer.committableSource(consumerSettings, Subscriptions.topics("input"))
  private val producerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)

  def main(args: Array[String]): Unit = {
    val delay = new Delay()
    val timeRun = System.getenv("TIME")

    println(s"App is up for time: $timeRun")
    source
      .via(delay())
      .map { elem => new ProducerRecord[String, String]("output", s"${Option(timeRun).getOrElse("?unk?")}-${elem.message}") }
      .runWith(Producer.plainSink(producerSettings))
  }
}
