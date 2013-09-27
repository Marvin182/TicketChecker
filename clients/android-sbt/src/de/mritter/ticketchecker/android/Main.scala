package de.mritter.ticketchecker.android

import scala.collection.JavaConversions._
import scala.collection.mutable.Subscriber

import android.os.{Bundle, Handler}
import android.widget._
import android.view.{View, SurfaceView}
import android.content.Context
import android.graphics.PixelFormat

import android.hardware.Camera
import android.hardware.Camera._

import net.sourceforge.zbar._

import com.actionbarsherlock.app.{SherlockActivity, ActionBar}
import com.actionbarsherlock.view.{Menu, MenuItem, MenuInflater}

import de.mritter.android.common._
import de.mritter.ticketchecker.api._

object Main {
  	// load native library for zbar
	System.loadLibrary("iconv")
}

class Main extends SherlockActivity with Subscriber[TicketApiEvent, TicketApi] with Camera.PreviewCallback {
	Main  
  
	val ticketApi = new TicketApi
	ticketApi.subscribe(this)
	lazy val preferences = getPreferences(Context.MODE_PRIVATE)

	lazy val cameraPreview = new CameraPreview(find[SurfaceView](R.id.surfaceview), this)
	var tickets: TicketListAdapter = null
	val scanner = new ImageScanner

	// GUI elements
	def find[T](id: Int) = findViewById(id).asInstanceOf[T]
	lazy val hostAddress = find[EditText](R.id.host)
	lazy val connectButton = find[Button](R.id.connect)
	lazy val ticketList = find[ListView](R.id.ticket_list)
	lazy val clearButton = find[Button](R.id.clear)
	lazy val checkinProgressBar = find[ProgressBar](R.id.checkin_progress)
	var apiConnectedMenuItem: Option[MenuItem] = None

	lazy val actionBar = getSupportActionBar

	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)

		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM)

		hostAddress.setText(preferences.getString("host", "192.168.42.1"))
		// username.setText(preferences.getString("username", ""))
		connectButton.setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				val editor = preferences.edit
				editor.putString("host", hostAddress.getText.toString)
				editor.commit
				ticketApi.connect(hostAddress.getText.toString, "Einlass1")
			}
		})

		// toggle torch listener
		find[CheckBox](R.id.toggle_torch).setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				cameraPreview.setTorch(v.asInstanceOf[CheckBox].isChecked)
			}	
		})

		tickets = new TicketListAdapter(this)
		ticketList.setAdapter(tickets)
		clearButton.setOnClickListener(new View.OnClickListener() {
			def onClick(v: View) {
				tickets.clear
			}
		})

		getWindow.setFormat(PixelFormat.UNKNOWN)
	}

	override protected def onResume {
		super.onResume
		cameraPreview.resume
		ticketApi.autoReconnect = true
		ticketApi.connect(preferences.getString("host", "192.168.42.1"), "Einlass1")
	}

	override protected def onPause {
		super.onPause
		cameraPreview.pause
		ticketApi.autoReconnect = false
		ticketApi.disconnect()
	}

	override protected def onCreateOptionsMenu(menu: Menu) = {
		getSupportMenuInflater.inflate(R.menu.main, menu)
		apiConnectedMenuItem = Some(menu.findItem(R.id.menu_api_connection))
		apiConnectedMenuItem.map(_.setIcon(if (ticketApi.connected) R.drawable.rating_good else R.drawable.rating_bad))
		super.onCreateOptionsMenu(menu)
	}

	override protected def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
		case _ => super.onOptionsItemSelected(item)
	}

	def notify(api: TicketApi, event: TicketApiEvent) = runOnGUiThread {
		event match {
			case TicketApiConnected => apiConnectedMenuItem.map(_.setIcon(R.drawable.rating_good))
			case TicketApiDisconnected => apiConnectedMenuItem.map(_.setIcon(R.drawable.rating_bad))
			case TicketStatusChangeEvent(ticket, status, details) => tickets.update(ticket, status, details)
			case EventStatsUpdateEvent(stats) => {
				checkinProgressBar.setMax(stats.ticketsTotal)
				checkinProgressBar.setProgress(stats.ticketsCheckedIn)
			}
		}
	}
	
	def onPreviewFrame(data: Array[Byte], camera: Camera) {
		val size = camera.getParameters.getPreviewSize
		val barcode = new Image(size.width, size.height, "Y800")
		barcode.setData(data)

		if (scanner.scanImage(barcode) != 0 && ticketApi.connected) {				
			val results = scanner.getResults.iterator.map(_.getData).toArray
			log.v("scanned: " + results.mkString(" | "))
			try {
				QrCodes.tickets(results).foreach{ t =>
					if (tickets.add(t)) {
						log.i(s"checking $t")
						ticketApi.send(CheckInTicket(t.order, t.code))
					}
				}
			} catch {
				case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(4).mkString("\t\n"))
			}
		}
	}

	protected def runOnGUiThread(f: => Unit) {
		runOnUiThread {
			new Runnable() {
				def run() {	f }
			}
		}
	}
}


