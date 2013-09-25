package de.mritter.ticketchecker.server

import play.api.Play.current

import org.squeryl.{Schema, KeyedEntity}
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

object Db extends Schema {
	val users = table[UserDb]("users")
	val sessions = table[SessionDb]("sessions")
	val tickets = table[TicketDb]("tickets")

	val userToSessions = oneToManyRelation(users, sessions).via((u, t) => u.id === t.userId)
	val userToCheckInTickets = oneToManyRelation(users, tickets).via((u, t) => u.id === t.checkedInById)
}
   
class UserDb(val id: Long,
			val username: String,
			var password: String,
			var isAdmin: Boolean = false) extends KeyedEntity[Long] {
	def this() = this(0, "", "", false)

	lazy val sessions: OneToMany[SessionDb] = Db.userToSessions.left(this)
	lazy val ticketsCheckedIn: OneToMany[TicketDb] = Db.userToCheckInTickets.left(this)

	override def hashCode = id.toInt
	override def toString = s"User($id, $username, $isAdmin)"
}

class SessionDb(val id: String,
			val userId: Long,
			val timestamp: Long = System.currentTimeMillis / 1000) extends KeyedEntity[String] {
	def this() = this("", 0, 0)

	def valid = timestamp + 864000 > System.currentTimeMillis / 1000

	lazy val user: ManyToOne[UserDb] = Db.userToSessions.right(this)
}

class TicketDb(val id: Long,
				// order, code, forename and surname are printed on the ticket and therefor immutable
				@Column("orderNumber") val order: Int,
				val code: String,
				val forename: String,
				val surname: String,
				var isStudent: Boolean,
				@Column("tableNumber") var table: Int,
				var checkedIn: Boolean = false,
				// optional values, should be Some() if checkedIn is true
				var checkedInById: Option[Long] = None,
				var checkInTime: Option[Long] = None) extends KeyedEntity[Long] {
	def this() = this(0, 0, "","", "", false, 0, false, None, None)

	lazy val checkedInBy: ManyToOne[UserDb] = Db.userToCheckInTickets.right(this)

	def checkIn(user: UserDb) = inTransaction {
		checkedIn = true
		checkedInById = Some(user.id)
		checkInTime = Some(System.currentTimeMillis / 1000)
		Db.tickets.update(this)
	}
	// "check out"
	def decline = inTransaction {
		checkedIn = false
		checkedInById = None
		checkInTime = None
		Db.tickets.update(this)
	}
}
