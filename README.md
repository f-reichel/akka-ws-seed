# Starter project for Webservices and WebSockets with Akka

## Actor system
Name of the project and actor system is `akka-ws-seed`. If you want to change this you have to update at least `build.sbt`,`application.conf`, and `Start.scala`. 

## RESTful Webservice connections
The object `app.Start` starts a webserver instance listening on port `8080` with a routing table of possible **paths** configured in value `path`.
A **path** is a URI path together with a HTTP variable `GET`, `POST`, `PUT`, or `DELETE`. The **path** can contain variable **path parameters**. Content passed to the webservice as a JSON data structure will be marshalled into a case class instance.
In order to call and test the webservices use a test tool like "Postman" or a browser extension like "Rester". 

Example:
````json
POST  http://localhost:8080/api/v1/users/+49170123456/messages
Content-type: application/json

{
  "sender" : "+49170123456",
  "receiver" : "+4916298765",
  "sent" : false,
  "text" : "Hello, how are you? :-)"
}
````
## WebSocket connections
Test the websocket connection using a browser extension (like "Simple WebSocket Client") or use [websocket.org](http://websocket.org/echo.html). The path to create a new websocket connection is `http://localhost:8080/api/v1/client`. After connecting you will receive a scheduled welcome message after 10 seconds.

Every websocket connection from a client will result in an instance of a streaming actor under the `/system/` guardian. This actor will instantiate one of your actors (the `ClientActor` class in this seed project) and sent its own actor ref. This actor ref is used to send messages to the client. Messages from the client are sent to your actor. 

## JSON support
Receiving and sending messages from/to client in the JSON format needs implicitly available `JsonFormatN` values (where `N` stands for the number of attributes of the case class). The basic JSON support is activated in `object Start` by extending `with SprayJsonSupport with DefaultJsonProtocol`.

For an example see object `app.Start` and the `case class MyMessage`. 