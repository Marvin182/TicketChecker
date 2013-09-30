package de.mritter.ticketchecker.android

import java.net.URI

import scala.reflect._
import scala.collection.mutable.{Publisher, Subscriber}

import play.api.libs.json._

import de.mritter.android.common._
import de.mritter.ticketchecker.api._

class TicketApi extends Publisher[TicketApiEvent] with Subscriber[WebSocketEvent, WebSocket] {
	
	type Pub = TicketApi // publisher type definition, use publish(TicketApiEvent)

	val apiVersion = 1

	private var ws: WebSocket = null
	@volatile var connected = false
	var autoReconnect = true
	protected val autoReconnectDelay = 5000 // ms

	def reconnect {
		if (connected)
			disconnect
		connect(ws.getURI)
	}

	def connect(host: String, username: String, password: String) {
		connect(new URI(s"ws://$host:9000/api/v$apiVersion?username=$username&password=$password"))
	}

	def connect(uri: URI) {
		if (connected)
			disconnect
		autoReconnect = true
		try {
			log.d(s"TicketApi: connecting ...")
			ws = new WebSocket(uri)
			ws.subscribe(this)
			ws.connect
		} catch {
			case e: Throwable => log.e("Could connect to ticket server: " + e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\n"))
		}
	}

	def disconnect {
		connected = false
		autoReconnect = false
		if (ws != null) {
			ws.removeSubscriptions
			log.d("TicketApi: disconnecting")
			ws.close
		}
		publish(TicketApiDisconnected)
	}

	def send[T](msg: T)(implicit write: Writes[T]) {
		if (connected) {
			val msgJson = Json.toJson(msg).asInstanceOf[JsObject]
			val text = (msgJson + typFromClassName(msg)).toString
			log.v(s"TicketApi: sending '$text'")
			ws sendText text
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
				disconnect
				log.d(s"TicketApi: disconnected")
				if (autoReconnect) {
					log.d(s"TicketApi: auto reconnect in " + autoReconnectDelay + " ms")
					scheduleTask(reconnect, autoReconnectDelay)
				}
			}
			case WebSocketMessageEvent(msg) => receive(msg)
			case WebSocketErrorEvent(e) => log.d("TicketApi WebSocketError: " + e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\n"))
		}
	}

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
			isAnsweredBy[ApiError](handleApiError) ||
			{ log.i(s"TicketApi: Unknown server message: $msg"); true }
		}
	}

	private def handleApiError(e: ApiError) = {
		e.code match {
			case -4 => {
				log.i(s"Invalid login data.")
				connected = false
				autoReconnect = false
			}
			case _ => log.w(s"TicketApi: Unhandled api error: ${e.toString}")
		}
		null
	}

	private	def typFromClassName[T](msg: T) = ("typ", Json.toJson(msg.getClass.getSimpleName))
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