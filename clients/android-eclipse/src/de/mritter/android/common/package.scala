package de.mritter.android

import android.util.Log

package object common {
	private val TAG = "de.mritter"

	// adb logcat -c && adb logcat de.mritter:V *:S
	object log {
		def v(msg: String) { Log.v(TAG, msg) }
		def d(msg: String) { Log.d(TAG, msg) }
		def i(msg: String) { Log.i(TAG, msg) }
		def w(msg: String) { Log.w(TAG, msg) }
		def e(msg: String) { Log.e(TAG, msg) }
	}
}