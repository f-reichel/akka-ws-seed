package app

import actor.{ClientActor, ResponderActor}
import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._


object Start extends App with SprayJsonSupport with DefaultJsonProtocol {

  println(s"Start")

  // setting up implicit values for the actor system
  implicit val system = ActorSystem("akka-ws-seed")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(5 seconds)

  // Json support
  final case class MyMessage(receiver: String, sender: String, text: String, sent: Boolean = false)
  implicit val myMessageFormat = jsonFormat4(MyMessage)

  // Actors used by the webserver
  val responderActor = system.actorOf(Props[ResponderActor])

  // Routing table for the HTTP server
  val route =
    pathPrefix("api" / "v1") {
      path("hello") {
        get {
          complete("Simple response")
        }
      } ~
      path("users" / Segment / "messages") { senderId => // Segment representing a variable String in the path
        post {
          entity(as[MyMessage]) { msg =>
            complete {
              (responderActor ? msg.copy(sender = senderId)).mapTo[MyMessage]
            }
          }
        }
      } ~
      path("client") {
        println(s"new websocket client conntected")
        handleWebSocketMessages(asynWebsock)
      } ~
      path("testfile") {
        getFromFile("src/main/resources/index.html")
      }
    }

  // starting the webserver
  val httpServer: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

  // handler method for new websocket clients
  //   this handler sends and receives String messages
  def asynWebsock: Flow[Message, Message, Any] = {

    val clientActor = system.actorOf(Props[ClientActor])

    val incoming: Sink[Message, NotUsed] =
      Flow[Message].map {
        // transform websocket message to domain message
        case TextMessage.Strict(text) => text
      }.to(Sink.actorRef[String](clientActor, PoisonPill))

    val outgoing: Source[Message, NotUsed] =
      Source.actorRef[String](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          // send the actor the technical actor representing the websocket (which it needs to send messages)
          clientActor ! outActor
          NotUsed
        }.map(
        // transform domain message to web socket message
        (outMsg: String) => TextMessage(outMsg))

    Flow.fromSinkAndSource(incoming, outgoing)
  }
}