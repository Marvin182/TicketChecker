package de.mritter.ticketchecker.android

import scala.util.matching.Regex

import de.mritter.ticketchecker.api.{QrTicket, QrLogin}

object QrCodes {
	// private val ticketRgx = new Regex("""t:(\d+)(\w.+)""", "order", "code")
	private val ticketRgx = new Regex("""(\d{4})(.+)""", "order", "code")
	private val loginRgx = new Regex("""login:([^\|]+)\|(\w+)\|(\w+)""", "url", "name", "password")

	def tickets(results: Array[String]): Array[QrTicket] = for (r <- results; m <- ticketRgx.findFirstMatchIn(r)) yield {
		QrTicket(m.group("order").toInt, m.group("code"))
	}

	def logins(results: Array[String]): Array[QrLogin] = for (r <- results; m <- loginRgx.findFirstMatchIn(r)) yield {
		QrLogin(m.group("url"), m.group("name"), m.group("password"))
	}
}


