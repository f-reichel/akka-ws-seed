package actor

import akka.actor.{Actor, ActorLogging}
import app.Start.MyMessage

class ResponderActor extends Actor with ActorLogging {

  override def receive = {
    case msg: MyMessage =>
      log.info(s"$msg received and returned")
      sender() ! msg.copy(sent = true)
  }
}
