package actor

import akka.actor.{Actor, ActorRef}
import scala.concurrent.duration._

class ClientActor extends Actor {

  implicit val dispatcher = context.dispatcher

  // this actor corresponds with a system actor representing the websocket session
  //   the actorRef is sent to this actor upon initialization
  override def receive = {
    case websockActor: ActorRef =>
      context.system.scheduler.scheduleOnce(5 seconds, self, "Welcome, this is an automatically scheduled message to be sent to this client once")
      context.become(connected(websockActor))
  }

  def connected(out: ActorRef): Receive = {
    // use out actor to send messages to the WebSocket client
    case msg => out ! msg.toString
  }
}
