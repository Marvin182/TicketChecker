package de.mritter.android

import java.util.{Timer, TimerTask}

import android.util.Log

package object common {

	/*
	 * Log wrapper using always the same tag
	 * adb logcat -c && adb logcat de.mritter:D AndroidRuntime:E *:S
	 */
	object log {
		private val TAG = "de.mritter"
		def v(msg: String) { Log.v(TAG, msg) }
		def d(msg: String) { Log.d(TAG, msg) }
		def i(msg: String) { Log.i(TAG, msg) }
		def w(msg: String) { Log.w(TAG, msg) }
		def e(msg: String) { Log.e(TAG, msg) }
	}

	def tryOrLog(f: => Unit) {
		try {
			f
		} catch {
			case e: Throwable => log.w(e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\n"))
		}
	}

	/*
	 * Schedule a task for single execution after a specific delay.
	 */
	private lazy val timer = new Timer
	def scheduleTask(f: => Unit, delay: Int) {
		timer.schedule(new TimerTask {
			def run = f
		}, delay)
	}
}