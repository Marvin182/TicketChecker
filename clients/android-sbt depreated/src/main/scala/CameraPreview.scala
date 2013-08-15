package de.mritter.ticketchecker.android

import java.io.IOException

import android.os.Bundle
import android.os.Handler

import android.view._
import android.content.Context

import android.hardware.Camera
import android.hardware.Camera._

import org.scaloid.common._

class CameraPreview(protected val previewFrameCb: (Array[Byte], Camera) => Unit)
					(implicit context: Context) extends SSurfaceView with SurfaceHolder.Callback {

	protected var camera: Camera = null

	override implicit val tag = LoggerTag("de.mritter")

	def pause {
		if (camera != null) {
			camera.stopPreview
			camera.setPreviewCallback(null)
			camera.release
			camera = null
		}
	}

	def resume {
		if (camera == null) {
			// ToDo catch exceptions
			camera = Camera.open

			var params = camera.getParameters
			params.setFlashMode("off")
			params.setFlashMode("torch")
			params.setFocusMode("continuous-picture")
			camera.setParameters(params)

			// set camera surface rotation to 90 degs to match activity view in portrait
			camera.setDisplayOrientation(90)

			// camera.autoFocus(autoFocusCB)
			camera.setPreviewDisplay(holder)

			camera.setPreviewCallback(new PreviewCallback {
				def onPreviewFrame(data: Array[Byte], camera: Camera) = previewFrameCb(data, camera)
			})
			camera.startPreview
		}
	}

	protected val autoFocusHandler = new Handler
	protected val doAutoFocus = new Runnable {
		def run {
			if (camera != null)	camera.autoFocus(autoFocusCB)
		}
	}
	protected val autoFocusCB: AutoFocusCallback = new AutoFocusCallback {
			def onAutoFocus(success: Boolean, camera: Camera) {
				autoFocusHandler.postDelayed(doAutoFocus, 250)
			}
		}

	// register a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
	getHolder.addCallback(this)
	// deprecated setting, but required on Android versions prior to 3.0
	getHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

	def surfaceCreated(holder: SurfaceHolder) {
		camera.setPreviewDisplay(holder)
	}

	def surfaceDestroyed(holder: SurfaceHolder) {
	}

	def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
		// if (holder.getSurface == null) {
		// 	// preview surface does not exist
		// 	return
		// }

		// // stop preview before making changes
		// try {
		// 	camera.stopPreview
		// } catch {
		// 	// ignore: tried to stop a non-existent preview
		// 	case _: Throwable =>
		// }

		// try {
		// 	// Hard code camera surface rotation 90 degs to match Activity view in portrait
		// 	camera.setDisplayOrientation(90)

		// 	camera.setPreviewDisplay(holder)
		// 	camera.setPreviewCallback(new PreviewCallback {
		// 		def onPreviewFrame(data: Array[Byte], camera: Camera) = previewFrameCb(data, camera)
		// 	})
		// 	camera.startPreview
		// 	camera.autoFocus(autoFocusCb)
		// } catch {
		// 	case e: Exception => Log.d("DBG", s"Error starting camera preview: ${e.getMessage}")
		// }
	}
}