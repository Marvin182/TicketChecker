package de.mritter.ticketchecker.server

import play.api.Play.current

import org.squeryl.{Schema, KeyedEntity}
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

object Db extends Schema {
	val users = table[User]("users")
	val sessions = table[SessionDb]("sessions")
	val tickets = table[TicketDb]("tickets")

	val userToSessions = oneToManyRelation(users, sessions).via((u, t) => u.id === t.userId)
	val userToCheckInTickets = oneToManyRelation(users, tickets).via((u, t) => u.id === t.checkedInById)
}
   
class User(val id: Long,
			val name: String,
			val password: String,
			val isAdmin: Boolean) extends KeyedEntity[Long] {
	def this() = this(0, "", "", false)

	lazy val sessions: OneToMany[SessionDb] = Db.userToSessions.left(this)
	lazy val ticketsCheckedIn: OneToMany[TicketDb] = Db.userToCheckInTickets.left(this)

	override def hashCode = id.toInt
	override def toString = s"User($id, $name, $isAdmin)"
}

class SessionDb(val id: String,
			val userId: Long,
			val timestamp: Long = System.currentTimeMillis / 1000) extends KeyedEntity[String] {
	def this() = this("", 0, 0)

	def valid = timestamp + 864000 > System.currentTimeMillis / 1000

	lazy val user: ManyToOne[User] = Db.userToSessions.right(this)
}

class TicketDb(val id: Long,
				@Column("orderNumber") val order: Int,
				val code: String,
				val forename: String,
				val surname: String,
				@Column("tableNumber") var table: Int,
				var checkedIn: Boolean,
				var checkedInById: Option[Long],
				var checkInTime: Option[Long]) extends KeyedEntity[Long] {
	def this() = this(0, 0, "","", "", -1, false, None, None)

	lazy val checkedInBy: ManyToOne[User] = Db.userToCheckInTickets.right(this)

	def checkIn(user: User) = inTransaction {
		checkedIn = true
		checkedInById = Some(user.id)
		checkInTime = Some(System.currentTimeMillis / 1000)
		Db.tickets.update(this)
	}
	def decline = inTransaction {
		checkedIn = false
		checkedInById = None
		checkInTime = None
		Db.tickets.update(this)
	}
}
