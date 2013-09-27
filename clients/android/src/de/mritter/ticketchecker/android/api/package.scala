package de.mritter.ticketchecker

import play.api.libs.json._
import play.api.libs.functional.syntax._

package object api {
	trait Ticket {
		val order: Int
		val code: String
	}
	case class QrTicket(order: Int, code: String) extends Ticket
	case class QrLogin(url: String, name: String, password: String)

	case class Debug(msg: String)
	case class ApiError(code: Int, msg: String, request: Option[JsValue] = None)

	case class TicketDetails(id: Long,
							order: Int,
							code: String,
							forename: String,
							surname: String,
							isStudent: Boolean,
							table: Int,
							checkedIn: Boolean,
							checkedInBy: Option[String],
							checkInTime: Option[Long]) extends Ticket
	case class CheckInTicket(order: Int, code: String)
	case class CheckInTicketFailed(details: TicketDetails)
	case class CheckInTicketSuccess(details: TicketDetails)
	case class CheckInTicketInvalid(order: Int, code: String)

	case class DeclineTicket(id: Long)
	case class DeclineTicketSuccess(details: TicketDetails)
	case class TicketNotFound(id: Long)

	case class EventStats(ticketsCheckedIn: Int, ticketsTotal: Int)
	case class Projection(checkInsPerMin: Double, predictedChechInDoneTime: Option[Int])

	implicit val debugFmt = Json.format[Debug]
	implicit val apiErrorFmt = Json.format[ApiError]

	implicit val ticketDetailsFmt = Json.format[TicketDetails]
	implicit val checkInTicketFmt = Json.format[CheckInTicket]
	implicit val checkInTicketFailedFmt = Json.format[CheckInTicketFailed]
	implicit val checkInTicketSuccessFmt = Json.format[CheckInTicketSuccess]
	implicit val checkInTicketInvalidFmt = Json.format[CheckInTicketInvalid]

	implicit val declineTicketFmt = Json.format[DeclineTicket]
	implicit val declineTicketSuccessFmt = Json.format[DeclineTicketSuccess]
	implicit val ticketNotFoundFmt = Json.format[TicketNotFound]

	implicit val eventStatsFmt = Json.format[EventStats]
	implicit val projectionFmt = Json.format[Projection]
}

