package de.mritter.ticketchecker.android

import scala.collection.JavaConversions._

import android.os.{Bundle, Handler}
import android.app.Activity
import android.widget._
import android.view.{View, Menu}
import android.hardware._
import android.hardware.Camera._
import android.content.pm.ActivityInfo

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
  
	val TAG = "de.mritter"
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

	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)

		hostAddress.setText("192.168.137.1")
		// hostAddress.setText("192.168.178.34")
		connectButton.setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				ticketApi.connect(hostAddress.getText.toString)
			}
		})
		// ticketApi.connect(hostAddress.getText.toString)

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
			QrCodes.tickets(results).foreach{ t =>
				// if (ticketAdapter.add(t)) {
				// 	ticketApi.send(CheckInTicket(t.order, t.code))
				// }
			}
		}
	}

	// val scanner: ImageScanner = new ImageScanner
	// lazy val cameraPreview = new SFrameLayout
	// lazy val urlEdit = new SEditText
	// lazy val ticketList = new SListView
	// lazy val ticketAdapter = new TicketListAdapter

	// override def onCreateOptionsMenu(menu: Menu) = {
	// 	// Inflate the menu; this adds items to the action bar if it is present.
	// 	getMenuInflater().inflate(R.menu.main, menu)
	// 	true
	// }

}




// 	// GUI elements
// 	var preview: CameraPreview = null
// 	// val scanner: ImageScanner = new ImageScanner
// 	lazy val cameraPreview = new SFrameLayout
// 	lazy val urlEdit = new SEditText
// 	lazy val ticketList = new SListView
// 	lazy val ticketAdapter = new TicketListAdapter

// 	info("Main")
// 	onCreate {
// 		info("onCreate")
// 		contentView = new SVerticalLayout {
// 			this += urlEdit
// 			SButton("Verbinden").onClick(ticketApi.connect(urlEdit.text.toString))
// 			this += ticketList
// 			SButton("Ticketliste Leeren").onClick(ticketAdapter.clearTickets)
// 			this += cameraPreview
// 		}
	
// 		ticketList.setAdapter(ticketAdapter)

// 		// setting up camera preview
// 		preview = new CameraPreview(onPreviewFrame)
// 		cameraPreview.addView(preview)

// 		// set up qr code scanner
// 		// scanner.setConfig(0, Config.X_DENSITY, 3)
// 		// scanner.setConfig(0, Config.Y_DENSITY, 3)

// 		try {
// 			ticketApi.connect()
// 			info("connected")
// 		} catch {
// 			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
// 		}
// 	}

// 	onPause {
// 		preview.pause
// 	}

// 	onResume {
// 		preview.resume
// 	}

// 	def onPreviewFrame(data: Array[Byte], camera: Camera) {
// 		val size = camera.getParameters.getPreviewSize
// 		// val barcode = new Image(size.width, size.height, "Y800")
// 		// barcode.setData(data)

// 		// if (scanner.scanImage(barcode) != 0) {				
// 		// 	val results = scanner.getResults.iterator.map(_.getData).toArray
// 		// 	QrCodes.tickets(results).foreach{ t =>
// 		// 		if (ticketAdapter.add(t)) {
// 		// 			ticketApi.send(CheckInTicket(t.order, t.code))
// 		// 		}
// 		// 	}
// 		// }
// 	}

// 	ticketApi.onTicketStatusChange = ((ticket: Ticket, status: Int, details: Option[TicketDetails]) =>
// 		runOnUiThread(ticketAdapter.update(ticket, status, details))
// 	)
// }