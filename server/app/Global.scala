package de.mritter.ticketchecker.server

import java.io.File

import scala.collection.mutable.HashSet

import play.api.{Application => App, GlobalSettings, Logger}
import play.api.db.DB

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

object Global extends GlobalSettings {

	val settingsPath = System.getProperty("user.home") + "/.ticketchecker/"
	var testMode = false

	override def onStart(app: App) {
		testMode = app.configuration.getString("mode").map(_ == "TEST") getOrElse false

		val settingsDir = new File(settingsPath)
		if (!settingsDir.exists) {
			settingsDir.mkdir.toString
		}
		
		SessionFactory.concreteFactory = Some( () =>
			Session.create(DB.getConnection()(app), new H2Adapter)
		)

		// read optional users file if available
		if (new File(settingsPath + "users.csv").exists) {
			Logger.info("Syncing users from file.")
			syncUsersFromFile(settingsPath + "users.csv")
		}

		// read optional tickets file if available
		if (new File(settingsPath + "tickets.csv").exists) {
			Logger.info("Syncing tickets from file.")
			syncTicketsFromFile(settingsPath + "tickets.csv")
		}


		if (testMode) {
			inTransaction {
				Logger.info("Setting test data.")
				Db.tickets.update(t => setAll(t.checkedIn := false, t.checkedInById := None, t.checkInTime := None))
				Db.tickets.update(t => where(t.order gte 100) set(t.checkedIn := true, t.checkedInById := Some(1L), t.checkInTime := Some(System.currentTimeMillis / 1000 - 600)))
			}
		}
	}
	
	override def onStop(app: App) {
		Application.projectionTask.cancel
		Logger.info("Shutting down.")
	}

	def syncUsersFromFile(fileName: String): Unit = inTransaction {
		val ids = new HashSet[Long]
		Db.users.where(u => 1 === 1).foreach(ids += _.id)
		
		// insert or update users in db
		forEachCSVEntry(fileName) { values =>
			// values = [username, password, isAdman]
			val user = new UserDb(0, values(0), sha1(values(1)), values(2) == "1")
			ids -= (Db.users.where(u => u.username === user.username).headOption match {
				case Some(u) => {
					u.password = user.password
					u.isAdmin = user.isAdmin
					Db.users.update(u)
					u.id
				}
				case None => Db.users.insert(user).id
			})
		}

		// remove all users that haven't been updated
		ids.foreach(Db.users.delete(_))
	}

	def syncTicketsFromFile(fileName: String): Unit = inTransaction {
		val ids = new HashSet[Long]
		Db.tickets.where(t => 1 === 1).foreach(ids += _.id)
		
		var inserts = 0

		// insert or update tickets in db
		forEachCSVEntry(fileName) { values =>
			// values = [order, code, forename, surname, student, table]
			val table = if (testMode) 1 + inserts / 10 else values(5).toInt
			inserts += 1
			val ticket = new TicketDb(0, values(0).toInt, values(1), values(2), values(3), values(4) == "1", table)
			ids -= (Db.tickets.where(t => t.order === ticket.order and t.code === ticket.code).headOption match {
				case Some(t) => {
					t.table = ticket.table
					Db.tickets.update(t)
					t.id
				}
				case None => Db.tickets.insert(ticket).id
			})
		}

		// remove all tickets that haven't been updated
		ids.foreach(Db.tickets.delete(_))
	}

	private def forEachCSVEntry(fileName: String)(f: Array[String] => Unit) {
		val lines = scala.io.Source.fromFile(fileName, "UTF-8").getLines

		// remove header line
		if (lines.hasNext)
			lines.next

		while (lines.hasNext)
			f(lines.next.split(';'))
	}
			
	def sha1(s: String) = java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes).map((b: Byte) => (if (b >= 0 & b < 16) "0" else "") + (b & 0xFF).toHexString).mkString
}
