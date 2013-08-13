package de.mritter.ticketchecker.console

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

class WS(url: String, onMsg: String => Unit) extends WebSocketClient(new URI(url)) {
	
	connect

	def sendText(msg: String) {
		println(s"WS.send($msg)")
		try {
			send(msg)
		} catch {
			case e: Throwable => error(e.toString)
		}
	}

	override def onMessage(message: String) = onMsg(message)

	override def onOpen(handshake: ServerHandshake) {
		println("WS.open()")
	}

	override def onClose(code: Int, reason: String, remote: Boolean) {
		println(s"WS.onClose($code, $reason, $remote")
	}

	override def onError(x: Exception) {
		println("WS.onError " + x.toString)
	}
}
