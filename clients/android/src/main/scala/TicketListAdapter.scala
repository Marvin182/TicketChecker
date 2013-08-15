package de.mritter.ticketchecker.android

import java.util.Date
import java.text.SimpleDateFormat

import scala.collection.mutable.ArrayBuffer

import android.widget.BaseAdapter
import android.view.{View, ViewGroup}

import org.scaloid.common._

import de.mritter.ticketchecker.api.{Ticket, QrTicket, TicketDetails}

class BigTicket(val order: Int,
				val code: String,
				var status: Int = 0,
				var details: Option[TicketDetails] = None) extends Ticket

class TicketListAdapter extends BaseAdapter {

	implicit val tag = LoggerTag("de.mritter")

	private val tickets = new ArrayBuffer[BigTicket]

	// just random colors to emphasize the ticket status in the list
	val colorDefault = 0x00
	val colorSuccess = 0xff00b30f // green
	val colorWarning = 0xffd65600 // orange
	val colorDanger  = 0xffb30000 // red
	val dt = new SimpleDateFormat("hh:mm:ss");

	private def bigTicket(t: Ticket): BigTicket = tickets.find(x => x.order == t.order && x.code == t.code).getOrElse(new BigTicket(t.order, t.code))

	def add(t: Ticket): Boolean = if (contains(t)) false else {
		tickets += bigTicket(t)
		notifyDataSetChanged
		true
	}

	def clearTickets {
		tickets.clear
		notifyDataSetChanged
	}

	def contains(t: Ticket) = tickets.exists(x => x.order == t.order && x.code == t.code)

	def update(t: Ticket, status: Int, details: Option[TicketDetails]) {
		if (contains(t)) {
			val ticket = bigTicket(t)
			ticket.status = status
			ticket.details = details
			notifyDataSetChanged
		}
	}
	
	// list adapter stuff
	def getCount = tickets.length
	def getItem(position: Int): Object = tickets(position)
	def getItemId(position: Int): Long = position

	def getView(position: Int, convertView: View, parent: ViewGroup): View = {
			// convertView might be null or an old view that be can reused
			implicit val ctx = parent.getContext
			val v = convertView match {
				case v: View => v
				case _ => new SLinearLayout {
					STextView().textSize(11 sp)
				}
			}
			val textView = v.asInstanceOf[ViewGroup].getChildAt(0).asInstanceOf[STextView]
			
			// update ticket view
			val t = tickets(position)
			val id = "%04d".format(t.order) + t.code

			try {
				val (text, backgroundColor) = t.status match {
					case 0 => (s"$id: Ckecking...", colorDefault)
					case 3 => (s"$id: Invalid!", colorDanger)
					case 1 => t.details.map(d => (s"$id: ${d.forename} ${d.surname} (Tisch ${d.table})", colorSuccess)).getOrElse{
						warn("No ticket details found for CheckInSuccess.")
						(s"$id: Error!", colorDefault)
					}
					case 2 => t.details.map { d =>
							val time = dt.format(new Date(1000 * d.checkInTime.getOrElse(0L)))
							(s"$id: ${d.forename} ${d.surname} Checked already in at $time", colorWarning)
						} getOrElse {
							warn("No ticket details found for CheckInFailed.")
							(s"$id: Error!", colorDefault)
						}
				}
				textView.text = text
				textView.backgroundColor = backgroundColor
			} catch {
				case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
			}

			v
	}

	override def hasStableIds = true
	override def isEmpty = tickets.isEmpty
}
