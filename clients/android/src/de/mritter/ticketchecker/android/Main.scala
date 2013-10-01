package de.mritter.ticketchecker.android

import scala.collection.JavaConversions._
import scala.collection.mutable.Subscriber

import android.os.{Bundle, Handler}
import android.widget._
import android.view.{View, SurfaceView, WindowManager}
import android.content.{Context, Intent, SharedPreferences}
import android.graphics.PixelFormat

import android.hardware.Camera
import android.hardware.Camera._

import net.sourceforge.zbar._

import com.actionbarsherlock.app.{SherlockFragmentActivity, SherlockDialogFragment, ActionBar}
import com.actionbarsherlock.view.{Menu, MenuItem, MenuInflater}

import de.mritter.android.common._
import de.mritter.ticketchecker.api._

object Main {
  	// load native library for zbar
	System.loadLibrary("iconv")
}

class Main extends SherlockFragmentActivity with Subscriber[TicketApiEvent, TicketApi] with Camera.PreviewCallback {
	Main  
  
	val ticketApi = new TicketApi
	ticketApi.subscribe(this)
	lazy val preferences = getPreferences(Context.MODE_PRIVATE)

	lazy val cameraPreview = new CameraPreview(find[SurfaceView](R.id.surfaceview), this)
	var tickets: TicketListAdapter = null
	val scanner = new ImageScanner

	// GUI elements
	def find[T](id: Int) = findViewById(id).asInstanceOf[T]
	lazy val ticketList = find[ListView](R.id.ticket_list)
	lazy val checkinProgressBar = find[ProgressBar](R.id.checkin_progress)
	var menuConnectionItem: Option[MenuItem] = None
	var menuTorchItem: MenuItem = null

	lazy val actionBar = getSupportActionBar

	override protected def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)

		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM)

		tickets = new TicketListAdapter(this)
		ticketList.setAdapter(tickets)

		cameraPreview.setTorch(preferences.getBoolean("torch", false))

		/* For taking screenshots */
		// tickets add QrTicket(4, "FDP")
		// tickets add QrTicket(3, "CDU")
		// tickets add QrTicket(2, "Grüne")
		// tickets add QrTicket(1, "Linke")
		// tickets.update(QrTicket(4, "FDP"), TSInvalid, None)
		// tickets.update(QrTicket(3, "CDU"), TSUsed, Some(TicketDetails(1, 3, "CDU", "Angela", "Merkel", false, 1, true, None, Some(System.currentTimeMillis))))
		// tickets.update(QrTicket(2, "Grüne"), TSValid, Some(TicketDetails(2, 2, "Grüne", "Claudia", "Rothe", false, 1, true, None, Some(System.currentTimeMillis))))
	}

	override protected def onResume {
		super.onResume
		cameraPreview.resume
		connectApiFromPreferences
	}

	override protected def onPause {
		super.onPause
		cameraPreview.pause
		ticketApi.disconnect
	}

	override protected def onCreateOptionsMenu(menu: Menu) = {
		super.onCreateOptionsMenu(menu)

		getSupportMenuInflater.inflate(R.menu.menu, menu)
		menuConnectionItem = Some(menu.findItem(R.id.menu_connection))
		menuConnectionItem.map(_.setIcon(if (ticketApi.connected) R.drawable.device_access_flash_on else R.drawable.device_access_flash_off))

		menuTorchItem = menu.findItem(R.id.menu_torch)
		menuTorchItem.setIcon(if (cameraPreview.torch) R.drawable.device_access_brightness_high else R.drawable.device_access_brightness_low)

		true
	}

	override protected def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
		case R.id.menu_connection => {
			cameraPreview.pause
			new ConnectionSettings(preferences, {ok => if (ok) connectApiFromPreferences; cameraPreview.resume}).show(getSupportFragmentManager, "connection")
			true
		}
		case R.id.menu_torch => {
			cameraPreview.toggleTorch
			menuTorchItem.setIcon(if (cameraPreview.torch) R.drawable.device_access_brightness_high else R.drawable.device_access_brightness_low)
			val editor = preferences.edit
			editor.putBoolean("torch", cameraPreview.torch)
			editor.commit
			true
		}
		case _ => super.onOptionsItemSelected(item)
	}

	def notify(api: TicketApi, event: TicketApiEvent) = runOnGUiThread {
		event match {
			case TicketApiConnected => {
				shortToast(getString(R.string.connected))
				menuConnectionItem.map(_.setIcon(R.drawable.device_access_flash_on))
			}
			case TicketApiDisconnected => {
				shortToast(getString(R.string.disconnected))
				menuConnectionItem.map(_.setIcon(R.drawable.device_access_flash_off))
			}
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

		if (scanner.scanImage(barcode) != 0) {				
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
				case e: Throwable => log.e(e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\n"))
			}
		}
	}

	protected def connectApiFromPreferences {
		ticketApi.connect(preferences.getString("host_address", ""), preferences.getString("username", ""), preferences.getString("password", ""))
	}

	protected def shortToast(message: String) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show
	}

	protected def runOnGUiThread(f: => Unit) {
		runOnUiThread {
			new Runnable() {
				def run() {	f }
			}
		}
	}
}


