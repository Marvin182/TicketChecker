package de.mritter.ticketchecker.android

import scala.collection.JavaConversions._

import android.os.{Bundle, Handler}
import android.app.Activity
import android.widget._
import android.view.{View, Menu}
import android.hardware._
import android.hardware.Camera._
import android.content.pm.ActivityInfo

import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.WebSocketImpl
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

import net.sourceforge.zbar._

// import android.support.v7.app.ActionBar

import de.mritter.android.common._
import de.mritter.ticketchecker.api._

object Main {
  	// load native library for zbar
	System.loadLibrary("iconv")
}

class Main extends Activity {
	Main  
  
	val ticketApi = new TicketApi

	var cameraPreview: CameraPreview = null
	var tickets: TicketListAdapter = null
	val scanner = new ImageScanner

	// GUI elements
	def find[T](id: Int) = findViewById(id).asInstanceOf[T]
	lazy val hostAddress = find[EditText](R.id.host)
	lazy val connectButton = find[Button](R.id.connect)
	lazy val previewFrame = find[FrameLayout](R.id.preview)
	lazy val ticketList = find[ListView](R.id.ticket_list)
	lazy val clearButton = find[Button](R.id.clear)
	lazy val checkinProgressBar = find[ProgressBar](R.id.checkin_progress)

	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)

		// hostAddress.setText("192.168.173.1")
		hostAddress.setText("192.168.178.34")
		connectButton.setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				ticketApi.connect(hostAddress.getText.toString)
			}
		})
		ticketApi.connect(hostAddress.getText.toString)

		cameraPreview = new CameraPreview(this, onPreviewFrame)
		previewFrame.addView(cameraPreview)

		tickets = new TicketListAdapter(this)
		ticketList.setAdapter(tickets)
		clearButton.setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				tickets.clear
			}
		})
	}

	override def onCreateOptionsMenu(menu: Menu) = {
		getMenuInflater().inflate(R.menu.main, menu)
		true
	}

	override def onResume() {
		super.onResume()
		cameraPreview.resume
	}

	override def onPause() {
		super.onPause()
		cameraPreview.pause
	}
	
	def onPreviewFrame(data: Array[Byte], camera: Camera) {
		val size = camera.getParameters.getPreviewSize
		val barcode = new Image(size.width, size.height, "Y800")
		barcode.setData(data)

		if (scanner.scanImage(barcode) != 0) {				
			val results = scanner.getResults.iterator.map(_.getData).toArray
			log.v("scanned: " + results.mkString(" | "))
			try {
				QrCodes.tickets(results).foreach{ t =>
					if (tickets.add(t)) {
						log.i(s"added $t")
						ticketApi.send(CheckInTicket(t.order, t.code))
					}
				}
			} catch {
			case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(4).mkString("\n"))
			}
		}
	}

	ticketApi.onTicketStatusChange = ((ticket: Ticket, status: Int, details: Option[TicketDetails]) =>
		runOnUiThread(tickets.update(ticket, status, details))
	)

	ticketApi.onEventStatsUpdate = (stats: EventStats) => runOnUiThread {
		checkinProgressBar.setMax(stats.ticketsTotal)
		checkinProgressBar.setProgress(stats.ticketsCheckedIn)
	}

	private def runOnUiThread(f: => Unit) {
		runOnUiThread {
			new Runnable() {
				def run() {	f }
			}
		}
	}
}
