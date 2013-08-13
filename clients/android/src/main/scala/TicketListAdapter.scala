package de.mritter.ticketchecker.android

import scala.collection.mutable.HashMap

import android.widget.BaseAdapter
import android.view.{View, ViewGroup}

import org.scaloid.common._

import de.mritter.ticketchecker.api.{Ticket, QrTicket, TicketDetails}

class BigTicket(val id: Int,
				val order: Int,
				val code: String,
				var status: Int = 0,
				var details: Option[TicketDetails] = None) extends Ticket

class TicketListAdapter extends BaseAdapter {

	implicit val tag = LoggerTag("de.mritter")

	private var lastId = -1
	
	private val tickets = new HashMap[String, BigTicket]

	private def bigTicket(t: Ticket): BigTicket = {
		lastId += 1
		tickets.getOrElse(t.order.toString + t.code, new BigTicket(lastId, t.order, t.code))
	}
	private def bigTicket(id: Int): BigTicket = tickets.find(_._2.id == id).get._2

	def add(t: Ticket): Boolean = if (contains(t)) false else {
		info("add" + t.toString)
		val key = t.order.toString + t.code
		tickets.+=((key, bigTicket(t)))
		notifyDataSetChanged
		true
	}

	def clearTickets {
		lastId = -1
		tickets.empty
		notifyDataSetChanged
	}

	def contains(t: Ticket) = tickets.contains(t.order.toString + t.code)

	def update(t: Ticket, status: Int, details: Option[TicketDetails]) {
		if (contains(t)) {
			val ticket = bigTicket(t)
			ticket.status = status
			ticket.details = details
			notifyDataSetChanged
		}
	}
	
	// list adapter stuff
	def getCount = tickets.size
	def getItem(position: Int): Object = bigTicket(position)
	def getItemId(position: Int): Long = position

	def getView(position: Int, convertView: View, parent: ViewGroup): View = {
			// convertView might be null or an old view that be can reuse
			implicit val ctx = parent.getContext
			val v = convertView match {
				case v: View => v
				case _ => new SLinearLayout {
					STextView()
				}
			}
			val textView = v.asInstanceOf[ViewGroup].getChildAt(0).asInstanceOf[STextView]
			
			// update ticket view
			val t = bigTicket(position)

			t.status match {
				case 0 => textView.text =  s"$t.id: $t.order $t.code"
				case 1 => textView.text =  s"$t.id: $t.order $t.code Invalid"
				case 2 => textView.text =  s"$t.id: $t.order $t.code CheckInSuccess"
				case 3 => textView.text =  s"$t.id: $t.order $t.code CheckInFailed"
			}
			
			// val (text, backgroundColor) = ticket match {
			// 	case t: Ticket => (t.toString, 0xff00ff00)
			// 	case t: QrTicket => (t.toString, 0xff000000)
			// }
			// textView.backgroundColor = backgroundColor

			v
	}

	override def hasStableIds = true
	override def isEmpty = tickets.isEmpty
}
