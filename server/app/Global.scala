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

	override def onStart(app: App) {
		val settingsDir = new File(settingsPath)
		if (!settingsDir.exists) {
			settingsDir.mkdir.toString
		}
		
		SessionFactory.concreteFactory = Some( () =>
			Session.create(DB.getConnection()(app), new H2Adapter)
		)

		// read optional users file if available
		if (new File(settingsPath + "users.csv").exists)
			readUsersFromFile(settingsPath + "users.csv")

		// read optional tickets file if available
		if (new File(settingsPath + "tickets.csv").exists)
			readTicketsFromFile(settingsPath + "tickets.csv")
	}
	
	override def onStop(app: App) {
		Application.projectionTask.cancel
		Logger.info("Shutting down.")
	}

	def readUsersFromFile(fileName: String): Unit = inTransaction {
		val ids = new HashSet[Long]
		Db.users.where(u => 1 === 1).foreach(ids += _.id)
		
		// insert or update users in db
		forEachCSVEntry(fileName) { values =>
			// values = [name, password, isAdman]
			val user = new UserDb(0, values(0), values(1), values(2) == "1")
			ids -= (Db.users.where(u => u.name === user.name).headOption match {
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

	def readTicketsFromFile(fileName: String): Unit = inTransaction {
		val ids = new HashSet[Long]
		Db.tickets.where(t => 1 === 1).foreach(ids += _.id)
		
		// insert or update tickets in db
		forEachCSVEntry(fileName) { values =>
			// values = [order, code, forename, surname, student, table]
			val ticket = new TicketDb(0, values(0).toInt, values(1), values(2), values(3), values(4) == "1", values(5).toInt)
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
			f(lines.next.split(','))
	}
}
