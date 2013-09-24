package de.mritter.ticketchecker.android

import java.net.URI
import java.net.URISyntaxException

import org.java_websocket.WebSocketImpl
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

import de.mritter.android.common._

class WebSocket(url: String, onMsg: String => Unit) extends WebSocketClient(new URI(url)) {

	def sendText(msg: String) {
		log.v(s"WebSocket.send($msg)")
		try {
			send(msg)
		} catch {
			case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(4).mkString("\n"))
		}
	}

	override def onMessage(message: String) = {
		log.v(s"WebSocket.onMessage($message)")
		try {
			onMsg(message)
		} catch {
			case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(4).mkString("\n"))
		}
	}

	override def onOpen(handshake: ServerHandshake) {
		log.v("WebSocket.open()")
	}

	override def onClose(code: Int, reason: String, remote: Boolean) {
		log.v(s"WebSocket.onClose($code, $reason, $remote")
	}

	override def onError(x: Exception) {
		log.v("WebSocket.onError " + x.toString)
	}
}

sealed trait WebSocketEvent
case class WebSocketClose(code: Int, reason: String, remote: Boolean)
case class WebSocketError(x: Exception)
c