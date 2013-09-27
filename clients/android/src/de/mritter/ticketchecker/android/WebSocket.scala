package de.mritter.ticketchecker.android

import java.net.URI
import java.net.URISyntaxException

import scala.collection.mutable.Publisher

import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake

import de.mritter.android.common._

class WebSocket(uri: URI) extends WebSocketClient(uri, new Draft_17, new java.util.HashMap[String, String], 5000) with Publisher[WebSocketEvent] {

	type Pub = WebSocket // publisher type definition, use publish(WebSocketEvent)

	def sendText(msg: String) {
		log.v(s"WebSocket.send($msg)")
		try {
			send(msg)
		} catch {
			case e: Throwable => log.e("WebSocket.send" + e.toString + "\n" + e.getStackTrace.take(4).mkString("\n"))
		}
	}

	override def onMessage(message: String) = {
		log.v(s"WebSocket.onMessage($message)")
		publish(WebSocketMessageEvent(message))
	}

	override def onOpen(handshake: ServerHandshake) {
		log.v("WebSocket.open()")
		publish(WebSocketOpenEvent(handshake))
	}

	override def onClose(code: Int, reason: String, remote: Boolean) {
		log.v(s"WebSocket.onClose($code, $reason, $remote)")
		publish(WebSocketCloseEvent(code, reason, remote))
	}

	override def onError(x: Exception) {
		log.v(s"WebSocket.onError(${x.toString})")
		publish(WebSocketErrorEvent(x))
	}
}

sealed trait WebSocketEvent
case class WebSocketOpenEvent(handshake: ServerHandshake) extends WebSocketEvent
case class WebSocketErrorEvent(x: Exception) extends WebSocketEvent
case class WebSocketCloseEvent(code: Int, reason: String, remote: Boolean) extends WebSocketEvent
case class WebSocketMessageEvent(message: String) extends WebSocketEvent