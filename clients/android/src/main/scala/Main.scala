package de.mritter.ticketchecker.android

import scala.collection.JavaConversions._

import android.os.Handler
import android.content.pm.ActivityInfo
import android.hardware._
import android.hardware.Camera._

import net.sourceforge.zbar._

import org.scaloid.common._

// import android.support.v7.app.ActionBar

import de.mritter.ticketchecker.api._

object Main {
	// load native library for zbar
	System.loadLibrary("iconv")
}

class Main extends SActivity with SContext  {
	Main

	// implicit logger tag, allows the use of debug(), into(), warn(), error(), ...
	// example command for using logcat to listen to debug and above: "adb logcat -c && adb logcat de.mritter:D *:S"
	implicit val tag = LoggerTag("de.mritter")

	val ticketApi = new TicketApi

	// GUI elements
	var preview: CameraPreview = null
	val scanner: ImageScanner = new ImageScanner
	lazy val cameraPreview = new SFrameLayout
	lazy val message = new STextView
	lazy val ticketList = new SListView
	lazy val ticketAdapter = new TicketListAdapter

	onCreate {
		contentView = new SVerticalLayout {
			this += message
			this += ticketList
			SButton("clear").onClick(ticketAdapter.clearTickets)
			this += cameraPreview
		}
	
		ticketList.setAdapter(ticketAdapter)

		// setting up camera preview
		preview = new CameraPreview(onPreviewFrame)
		cameraPreview.addView(preview)

		// set up qr code scanner
		scanner.setConfig(0, Config.X_DENSITY, 3)
		scanner.setConfig(0, Config.Y_DENSITY, 3)

		// val bar = getActionBar

		// java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
		// java.lang.System.setProperty("java.net.preferIPv4Stack", "true");

		try {
			ticketApi.connect()
			info("connected")
		} catch {
			case e: Throwable => error(e.toString + "\n" + e.getStackTrace)
		}
	}

	onPause {
		preview.pause
	}

	onResume {
		preview.resume
	}

	def onPreviewFrame(data: Array[Byte], camera: Camera) {
		val size = camera.getParameters.getPreviewSize
		val barcode = new Image(size.width, size.height, "Y800")
		barcode.setData(data)

		if (scanner.scanImage(barcode) != 0) {				
			val results = scanner.getResults.iterator.map(_.getData).toArray
			message.text = results.mkString(" | ")
			QrCodes.tickets(results).foreach{ t =>
				if (ticketAdapter.add(t)) {
					ticketApi.send(CheckInTicket(t.order, t.code))
				}
			}
		}
	}

	ticketApi.onTicketStatusChange = ((ticket: Ticket, status: Int, details: Option[TicketDetails]) =>
		ticketAdapter.update(ticket, status, details)
	)
}