package spike

import java.time.LocalDateTime

import akka.NotUsed
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Delay()(implicit ec: ExecutionContext) {
  def apply(): Flow[CommittableMessage[String, String], OutputMessage, NotUsed] = {
    Flow[CommittableMessage[String, String]]
      .map(message => OutputMessage(message.record.value(), LocalDateTime.now(), null))
      .delay(4 seconds)
      .map(message => message.copy(timeAfterDelay = LocalDateTime.now()))
  }
}
