package de.mritter.ticketchecker.server

import java.security.SecureRandom;
import java.math.BigInteger;

import play.api._
import play.api.mvc._
import play.api.Logger
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.future
import scala.concurrent.duration._

import akka.util.Timeout
import akka.pattern.ask
import akka.actor.{ActorSystem, Props}

import org.squeryl.PrimitiveTypeMode._

object Application extends Controller {

	val system = ActorSystem("actorSystem")
	val apiActor = system.actorOf(Props[Api], "api")

	def index = Action { implicit request =>
		getUserOptFromSession(request.session) match {
			case None => Ok(views.html.login())
			case Some(user) => inTransaction { Ok(views.html.index(user.name)) }
		}
		
	}

	def login(name: String, password: String) = Action { implicit request =>
		Logger.info(s"login($name, $password)")
		getUserOpt(name, password) match {
			case None => Ok("0")
			case Some(user) => {
				val session = new SessionDb(newSessionId, user.id)
				inTransaction { Db.sessions.insert(session :: Nil) }
				Ok("1").withSession(
					"id" -> session.id
				)
			}
		}
	}

	def logout = Action { request =>
		Ok(views.html.login()).withNewSession
	}

	def api(name: String = "", password: String = "") = WebSocket.async[JsValue] { request => 
		getUserOptFromSession(request.session).orElse(getUserOpt(name, password)) match {
			case None => future { error(-4, "Not authenficated. Please sign in again.") }
			case Some(user) => {
				try {
					implicit val timeout = Timeout(5 seconds)
					(apiActor ? Connect(user)).map {
						case Connected(in, out) => (in, out)
						case _ => error(500, "Invalid actor response.")
					}
				} catch {
					case _ : Throwable => future { error(500, "Internal Server Error") }
				}
			}
		}
	}

	private def getUserOpt(name: String, password: String): Option[User] = inTransaction {
		Db.users.where(u => lower(u.name) === lower(name) and u.password === password).headOption
	}

	private def getUserOptFromSession(session: Session): Option[User] = session.get("id") match {
		case Some(id: String) => inTransaction {
			Db.sessions.lookup(id) match {
				case None => None
				case Some(s) => if (s.valid) Some(s.user.head) else None
			}
		}
		case _ => None
	}

	private def error(code: Int, msg: String) = {
		val in = Done[JsValue, Unit]((), Input.EOF)
		val out = Enumerator[JsValue](Json.obj(
			"typ" -> "ApiError",
			"code" -> code,
			"msg" -> msg
			)).andThen(Enumerator.enumInput(Input.EOF))

	    (in, out)
	}

	private lazy val secureRandom = new SecureRandom
	private def newSessionId = new BigInteger(130, secureRandom).toString(32)
}
