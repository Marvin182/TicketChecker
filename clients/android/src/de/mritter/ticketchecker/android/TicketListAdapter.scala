package de.mritter.ticketchecker.android

import java.util.Date
import java.text.SimpleDateFormat

import scala.collection.mutable.ArrayBuffer

import android.widget.{BaseAdapter, TextView}
import android.view.{View, ViewGroup}
import android.util.Log
import android.view.LayoutInflater
import android.content.Context

import de.mritter.android.common._
import de.mritter.ticketchecker.api.{Ticket, QrTicket, TicketDetails}

object BigTicket {
	private var lastId = 0
	def apply(order: Int, code: String) = {
		lastId += 1
		new BigTicket(lastId, order, code)
	}
}

class BigTicket(val id: Long,
				val order: Int,
				val code: String,
				var status: TicketStatus = TSUnknown,
				var details: Option[TicketDetails] = None) extends Ticket

class TicketListAdapter(val context: Context) extends BaseAdapter {

	// implicit val tag = LoggerTag("de.mritter")

	val TAG = "de.mritter"
	def info(msg: String) = android.util.Log.e("de.mritter", msg)
	def error(msg: String) = android.util.Log.e("de.mritter", msg)

	private val tickets = new ArrayBuffer[BigTicket]

	// just random colors to emphasize the ticket status in the list
	val colorDefault = 0x00
	val colorSuccess = 0xff00b30f // green
	val colorWarning = 0xffd65600 // orange
	val colorDanger  = 0xffb30000 // red
	val dateFormat = new SimpleDateFormat("HH:mm:ss");

	private def bigTicket(t: Ticket): BigTicket = tickets.find(x => x.order == t.order && x.code == t.code).getOrElse(BigTicket(t.order, t.code))

	def add(t: Ticket): Boolean = if (contains(t)) false else {
		log.v("TicketListAdapter.add() " + t)
		tickets.synchronized {
			tickets prepend bigTicket(t)
			notifyDataSetChanged
		}
		true
	}

	def clear {
		tickets.clear
		notifyDataSetChanged
	}

	def contains(t: Ticket) = tickets.exists(x => x.order == t.order && x.code == t.code)

	def update(t: Ticket, status: TicketStatus, details: Option[TicketDetails]) {
		if (contains(t)) {
			log.v(s"TicketListAdapter.update($t)")
			val ticket = bigTicket(t)
			ticket.status = status
			ticket.details = details
			notifyDataSetChanged
		}
	}
	
	val removeTicketListener = new View.OnClickListener {
		def onClick(v: View) {
			val ticketItem = v.getParent.asInstanceOf[View]
			val position = ticketItem.getParent.asInstanceOf[ViewGroup].indexOfChild(ticketItem)
			tickets remove position
			notifyDataSetChanged
		}
    }

	// list adapter stuff
	def getCount = tickets.length
	def getItem(position: Int): Object = tickets(position)
	def getItemId(position: Int): Long = tickets(position).id

	val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
	def getView(position: Int, convertView: View, parent: ViewGroup): View = {
		var v: View = convertView
		if (v == null) { 
			v = inflater.inflate(R.layout.ticket_list_item, parent, false)
			v.findViewById(R.id.remove).setOnClickListener(removeTicketListener)
		} 

		val textView = v.findViewById(R.id.text).asInstanceOf[TextView]

		val t = tickets(position)
		val id = "%04d".format(t.order) + t.code

		val (text, backgroundColor) = t.status match {
			case TSUnknown => (s"$id: ${string(R.string.checking)}", colorDefault)
			case TSValid => t.details.map(d => (s"${d.forename} ${d.surname} (${string(R.string.table)} ${d.table})", colorSuccess)).getOrElse{
				Log.w(TAG, "No ticket details found for CheckInSuccess.")
				(s"$id: Error!", colorDefault)
			}
			case TSUsed => t.details.map { d =>
					val m = string(R.string.checked_in_at) format dateFormat.format(new Date(1000 * d.checkInTime.getOrElse(0L)))
					(s"${d.forename} ${d.surname} $m (${string(R.string.table)} ${d.table})", colorWarning)
				} getOrElse {
					Log.w(TAG, "No ticket details found for CheckInFailed.")
					(s"$id: Error!", colorDefault)
				}
			case TSInvalid => (s"$id: ${string(R.string.invalid)}", colorDanger)
		}
		textView.setText(text)
		v.setBackgroundColor(backgroundColor)

		v
	}

	protected def string(id: Int) = context.getString(id)

	override def hasStableIds = true
	override def isEmpty = tickets.isEmpty
}
