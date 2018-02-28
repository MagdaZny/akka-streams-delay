package spike

import java.time.LocalDateTime

case class OutputMessage(message: String, timeBeforeDelay: LocalDateTime, timeAfterDelay: LocalDateTime)

