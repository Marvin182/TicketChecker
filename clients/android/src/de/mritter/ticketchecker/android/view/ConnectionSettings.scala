package de.mritter.ticketchecker.android

import android.os.Bundle
import android.widget.EditText
import android.app.{Activity, AlertDialog}
import android.view.View
import android.content.DialogInterface
import android.content.SharedPreferences
import com.actionbarsherlock.app.SherlockDialogFragment

import de.mritter.android.common._

class ConnectionSettings(val preferences: SharedPreferences, onClose: Boolean => Unit) extends SherlockDialogFragment {

	private var view: View = null
	private def find[T](id: Int) = view.findViewById(id).asInstanceOf[T]

	private lazy val hostAddress = find[EditText](R.id.server_address)
	private lazy val username = find[EditText](R.id.username)
	private lazy val password = find[EditText](R.id.password)

	override def onCreateDialog(savedInstanceState: Bundle) = {
		val builder = new AlertDialog.Builder(getActivity)
		val inflater = getActivity.getLayoutInflater
		view = inflater.inflate(R.layout.settings, null)

		val dialog = builder.setView(view)
			.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener {
				override def onClick(dialog: DialogInterface, id: Int) {
					savePreferences
					onClose(true)
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener {
				override def onClick(dialog: DialogInterface, id: Int) {
					onClose(false)
				}
			})
			.create
		loadPreferences
		dialog
	}

	private def loadPreferences {
		hostAddress.setText(preferences.getString("host_address", ""))
		username.setText(preferences.getString("username", ""))
		password.setText(preferences.getString("password", ""))
	}

	private def savePreferences {
		log.w("savePreferences")
		val editor = preferences.edit
		editor.putString("host_address", hostAddress.getText.toString)
		editor.putString("username", username.getText.toString)
		editor.putString("password", password.getText.toString)
		editor.commit
	}
}
