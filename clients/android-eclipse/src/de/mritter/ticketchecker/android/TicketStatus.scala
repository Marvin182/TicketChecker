package de.mritter.ticketchecker.console

class TicketStatus(val value: Int)
case object TSUnkown extends TicketStatus(0)
case object TKInvalid extends TicketStatus(1)
case object CheckInSuccess extends TicketStatus(2)
case object CheckInFailed extends TicketStatus(3)
