package de.mritter.ticketchecker.console

import scala.reflect._

import play.api.libs.json._

import de.mritter.ticketchecker.api._

class TicketApi {
	
	private var ws: WS = null

	def connect(url: String = "ws://192.168.39.1:9000/api") {
		ws = new WS(url, receive)
	}

	def send[T](msg: T)(implicit write: Writes[T]) {
		val msgJson = Json.toJson(msg).asInstanceOf[JsObject]
		val text = (msgJson + typ(msg)).toString
		println("api send " + text)
		ws sendText text
	}

	private	def typ[T](msg: T) = ("typ", Json.toJson(msg.getClass.getSimpleName))

	private def receive(msg: String) {
		println(s"api receive $msg")
		val json = Json.parse(msg)
		(json \ "typ").asOpt[String].map{ typ => 
			def isAnsweredBy[T : ClassTag](callback: T => Unit)(implicit readT: Reads[T]): Boolean = {
				val requiredTyp = classTag[T].runtimeClass.getSimpleName
				if (typ != requiredTyp) return false
				Json.fromJson[T](json).map(callback)
				true
			}
			isAnsweredBy[CheckInTicketSuccess](t => onTicketStatusChange(t.details, 1, Some(t.details))) ||
			isAnsweredBy[CheckInTicketFailed](t => onTicketStatusChange(t.details, 2, Some(t.details))) ||
			isAnsweredBy[CheckInTicketInvalid](t => onTicketStatusChange(QrTicket(t.order, t.code), 3, None))
		}
	}

	var onTicketStatusChange: (Ticket, Int, Option[TicketDetails]) => Unit = (ticket, status, details) => Unit
}

