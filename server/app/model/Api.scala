package de.mritter.ticketchecker.server

import scala.reflect._
import scala.collection.mutable.HashSet

import akka.actor.Actor

import play.api.Logger
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.concurrent.Execution.Implicits._

import org.squeryl.PrimitiveTypeMode._

import de.mritter.ticketchecker.api._

class Api extends Actor {

	private val predictionMinMeasureTime = 10
	private val perdictionMaxMeasureTime = 300

	private val connections = new HashSet[Connection]

	def receive = {
		case Connect(user) => {
			val (out, con) = createConnection(user)

			val in = Iteratee.foreach[JsValue] { msg =>
				receiveMessage(con, msg)
			}.map { _ =>
				dispatchConnection(con)
			}

			sender ! Connected(in, out)
			initConnection(con)
		}
		case SendProjection => sendAll(projection)
	}

	private def receiveMessage(con: Connection, msg: JsValue) {
		// Logger.info(s"api receive $msg")
		(msg \ "typ").asOpt[String].map{ typ =>
			def isAnsweredBy[T : ClassTag](needsAdmin: Boolean, callback: (Connection, T) => Unit)(implicit readT: Reads[T]): Boolean = {
				val requiredTyp = classTag[T].runtimeClass.getSimpleName
				if (typ != requiredTyp)
					false
				else {
					if (needsAdmin && !con.user.isAdmin)
						con send ApiError(-4, s"Request $requiredTyp needs higher permissions!")
					else
						Json.fromJson[T](msg).map(t => callback(con, t)).getOrElse(con send ApiError(-3, s"Invalid $requiredTyp request!", Some(msg)))
					true
				}
			}
			isAnsweredBy[CheckInTicket](false, tryCheckInTicket) ||
			isAnsweredBy[DeclineTicket](true, declineTicket) ||
			{con send ApiError(-2, s"$typ is not a valid request typ!"); false}
		}.getOrElse(con send ApiError(-1, "Missing typ parameter in request!", Some(msg)))
	}

	private def createConnection(user: UserDb) = {
		val (out, channel) = Concurrent.broadcast[JsValue]
		val con = new Connection(user, channel)
		connections += con
		(out, con)
	}

	private def initConnection(con: Connection) {
		con.send(eventStats)
	}
	
	private def dispatchConnection(con: Connection) {
		connections -= con
	}

	private def eventStats = inTransaction {
		val checkedIn = from(Db.tickets)(t => where(t.checkedIn === true) compute(count)).single.measures.toInt
		val total = from(Db.tickets)(t => compute(count)).single.measures.toInt
		EventStats(checkedIn, total)
	}

	private def projection: Projection = inTransaction {
		val now = (System.currentTimeMillis / 1000).toInt
		val notCheckedIn = from(Db.tickets)(t => where(t.checkedIn === false) compute(count)).single.measures.toInt
		if (notCheckedIn == 0)
			return Projection(0, Some(now))

		val firstCheckInTime = from(Db.tickets)(t => where(t.checkedIn === true) compute(min(t.checkInTime))).single.measures.getOrElse(-1L).toInt
		if (firstCheckInTime == -1)
			return Projection(0, None)

		val diff = now - firstCheckInTime
		val measureTime =
			if (diff > perdictionMaxMeasureTime) perdictionMaxMeasureTime
			else if (diff < predictionMinMeasureTime) predictionMinMeasureTime
			else now - firstCheckInTime
		val measureStart = now - measureTime
		val measuredValue = from(Db.tickets)(t => where(nvl(t.checkInTime, 0) >= measureStart) compute(count)).single.measures.toInt

		if (measuredValue == 0)
			return Projection(0, None)
			
		val checkInsPerSec = measuredValue.toDouble / measureTime
		val predictedCheckInDone = (System.currentTimeMillis / 1000 + notCheckedIn / checkInsPerSec).toInt
		Projection(60 * checkInsPerSec, Some(predictedCheckInDone))
	}

	private def tryCheckInTicket(con: Connection, ct: CheckInTicket) = inTransaction {
		Db.tickets.where(t => (t.order === ct.order) and (t.code === ct.code)).headOption.map{ ticket =>
			if (ticket.checkedIn) {
				con send CheckInTicketFailed(ticket)
			} else {
				ticket.checkIn(con.user)
				sendAll(CheckInTicketSuccess(ticket))
				sendAll(eventStats)
			}
		}.getOrElse(con send CheckInTicketInvalid(ct.order, ct.code))
	}

	private def declineTicket(con: Connection, dt: DeclineTicket) = inTransaction {
		Db.tickets.lookup(dt.id).map{ ticket =>
			ticket.decline
			sendAll(DeclineTicketSuccess(ticket))
			sendAll(eventStats)
		}.getOrElse(con send TicketNotFound(dt.id))
	}

	private def sendAll[T](msg: T)(implicit write: Writes[T]) {
		connections.foreach(_.send(msg))
	}

	implicit def ticketDb2TicketDetails(t: TicketDb): TicketDetails = TicketDetails(t.id, t.order, t.code, t.forename, t.surname, t.isStudent, t.table, t.checkedIn, t.checkedInBy.headOption.map(_ username), t.checkInTime)
}

case class Connect(user: UserDb)
case class Connected(in: Iteratee[JsValue, Unit], out: Enumerator[JsValue])
case object SendProjection

class Connection(val user: UserDb, private val channel: Channel[JsValue]) {
	def send[T](msg: T)(implicit write: Writes[T]) {
		val msgJson = Json.toJson(msg).asInstanceOf[JsObject]
		channel.push(msgJson + typ(msg))
		// Logger.info("api send to " + user.username + " " + (msgJson + typ(msg)))
	}

	private def typ[T](msg: T) = ("typ", Json.toJson(msg.getClass.getSimpleName))
}

