package de.mritter.ticketchecker.android

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import de.mritter.android.common._

class WebSocket(url: String, onMsg: String => Unit) extends WebSocketClient(new URI(url)) {

	def sendText(msg: String) {
		log.v(s"WebSocket.send($msg)")
		try {
			send(msg)
		} catch {
			case e: ExceptionInInitializerError => error("ExceptionInInitializerError3:" + e.getCause.toString + "\n" + e.getCause.getStackTrace.mkString("\n"))
			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
		}
	}

	override def onMessage(message: String) = {
		log.v(s"WebSocket.onMessage($message)")
		try {
			onMsg(message)
		} catch {
			case e: ExceptionInInitializerError => error("ExceptionInInitializerError4:" + e.getCause.toString + "\n" + e.getCause.getStackTrace.mkString("\n"))
			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
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