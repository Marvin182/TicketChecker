package de.mritter.ticketchecker.android

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.scaloid.common._

class WebSocket(url: String, onMsg: String => Unit) extends WebSocketClient(new URI(url)) {

	implicit val tag = LoggerTag("de.mritter")

	connect

	def sendText(msg: String) {
		debug(s"WebSocket.send($msg)")
		try {
			send(msg)
		} catch {
			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
		}
	}

	override def onMessage(message: String) = {
		debug(s"WebSocket.onMessage($message)")
		try {
			onMsg(message)
		} catch {
			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
		}
	}

	override def onOpen(handshake: ServerHandshake) {
		debug("WebSocket.open()")
	}

	override def onClose(code: Int, reason: String, remote: Boolean) {
		debug(s"WebSocket.onClose($code, $reason, $remote")
	}

	override def onError(x: Exception) {
		debug("WebSocket.onError " + x.toString)
	}
}