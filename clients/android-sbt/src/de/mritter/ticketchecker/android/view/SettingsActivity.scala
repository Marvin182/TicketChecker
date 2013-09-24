package de.mritter.ticketchecker.android

import android.os.Bundle
import android.preference.PreferenceActivity

import de.mritter.android.common._

class SettingsActivity extends PreferenceActivity {
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		addPreferencesFromResource(R.layout.settings)
	}
}