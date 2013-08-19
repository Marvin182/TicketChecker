package de.mritter.android

import android.util.Log

package object common {
	private val TAG = "de.mritter"

	object log {
		// adb logcat -c && adb logcat de.mritter:V de.mritter.ticketchecker.android:V *:S
		def v(msg: String) { Log.v(TAG, msg) }
		def d(msg: String) { Log.d(TAG, msg) }
		def i(msg: String) { Log.i(TAG, msg) }
		def w(msg: String) { Log.w(TAG, msg) }
		def e(msg: String) { Log.e(TAG, msg) }
	}
}