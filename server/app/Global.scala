package de.mritter.ticketchecker.server

import play.api.{Application => App, GlobalSettings, Logger}
import play.api.db.DB

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter

object Global extends GlobalSettings {

	override def onStart(app: App) {
		SessionFactory.concreteFactory = Some( () =>
			Session.create(DB.getConnection()(app), new H2Adapter)
		)
	}
	
	override def onStop(app: App) {
		Logger.info("Shutting down.")
	}
}