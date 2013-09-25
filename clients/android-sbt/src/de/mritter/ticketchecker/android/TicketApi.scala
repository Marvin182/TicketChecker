package de.mritter.ticketchecker.android

import java.net.URI

import scala.reflect._
import scala.collection.mutable.{Publisher, Subscriber}

import play.api.libs.json._

import de.mritter.android.common._
import de.mritter.ticketchecker.api._

class TicketApi extends Publisher[TicketApiEvent] with Subscriber[WebSocketEvent, WebSocket] {
	
	type Pub = TicketApi // publisher type definition, use publish(TicketApiEvent)

	private var ws: WebSocket = null
	@volatile var connected = false
	var autoReconnect = true

	def reconnect {
		if (connected)
			disconnect()
		connect(ws.getURI)
	}

	def connect(host: String, username: String, password: String = "") {
		connect(new URI(s"ws://$host:9000/api?username=$username&password=$password"))
	}

	def connect(uri: URI) {
		if (connected)
			disconnect()
		try {		
			log.d(s"TicketApi: connecting ...")
			if (ws != null)
				ws.removeSubscriptions
			ws = new WebSocket(uri)
			ws.subscribe(this)
			ws.connect
		} catch {
			case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(4).mkString("\n"))
		}
	}

	def disconnect(closeSocket: Boolean = true) {
		connected = false
		if (ws != null) {
			ws.removeSubscriptions
			if (closeSocket) {
				log.d("TicketApi: disconnecting (closing socket) ...")
				ws.close
			}
		}
		publish(TicketApiDisconnected)
	}

	def send[T](msg: T)(implicit write: Writes[T]) {
		if (connected) {
			val msgJson = Json.toJson(msg).asInstanceOf[JsObject]
			val text = (msgJson + typ(msg)).toString
			log.v(s"TicketApi: sending '$text'")
			ws sendText text
		}
	}

	private	def typ[T](msg: T) = ("typ", Json.toJson(msg.getClass.getSimpleName))

	private def receive(msg: String) {
		log.v(s"TicketApi: receive: '$msg'")
		val json = Json parse msg
		(json \ "typ").asOpt[String].map{ typ => 
			def isAnsweredBy[T : ClassTag](callback: T => TicketApiEvent)(implicit readT: Reads[T]): Boolean = {
				val requiredTyp = classTag[T].runtimeClass.getSimpleName
				if (typ != requiredTyp) return false
				Json.fromJson[T](json).foreach{ t =>
					val event = callback(t)
					if (event != null)
						publish(event)
				}
				true
			}
			isAnsweredBy[CheckInTicketSuccess](t => TicketStatusChangeEvent(t.details, TSValid, Some(t.details))) ||
			isAnsweredBy[CheckInTicketFailed](t => TicketStatusChangeEvent(t.details, TSUsed, Some(t.details))) ||
			isAnsweredBy[CheckInTicketInvalid](t => TicketStatusChangeEvent(QrTicket(t.order, t.code), TSInvalid, None)) ||
			isAnsweredBy[EventStats](t => EventStatsUpdateEvent(t)) ||
			isAnsweredBy[Projection](t => null) || // ignore this for now
			{ log.d(s"TicketApi: Unknown server message: $msg"); true }
		}
	}
	def notify(ws: WebSocket, event: WebSocketEvent) {
		event match {
			case WebSocketOpenEvent(_) => {
				connected = true
				log.d("TicketApi: connected")
				publish(TicketApiConnected)
			}
			case WebSocketCloseEvent(_, _, _) => {
				disconnect(false)
				log.d(s"TicketApi: disconnected")
				if (autoReconnect)
					scheduleTask(reconnect, 5000)
			}
			case WebSocketMessageEvent(msg) => receive(msg)
			case _ => // ignore everything else (e.g. WebSocketErrorEvent)
		}
	}
}

sealed trait TicketStatus
case object TSUnknown extends TicketStatus
case object TSValid extends TicketStatus
case object TSUsed extends TicketStatus
case object TSInvalid extends TicketStatus

sealed trait TicketApiEvent
case object TicketApiConnected extends TicketApiEvent
case object TicketApiDisconnected extends TicketApiEvent
case class TicketStatusChangeEvent(ticket: Ticket, status: TicketStatus, detailsOpt: Option[TicketDetails]) extends TicketApiEvent
case class EventStatsUpdateEvent(stats: EventStats) extends TicketApiEvent