package de.mritter.ticketchecker.android

import java.io.IOException

import android.os.Bundle
import android.os.Handler

import android.view._
import android.content.Context

import android.hardware.Camera._
import android.hardware.{Camera, Sensor, SensorEventListener, SensorEvent, SensorManager}

import de.mritter.android.common._

class CameraPreview(context: Context, protected val previewFrameCb: (Array[Byte], Camera) => Unit) extends SurfaceView(context) with SurfaceHolder.Callback {

	private var camera: Camera = null

	getHolder.addCallback(this)
	// deprecated setting, but required on Android versions prior to 3.0
	// getHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

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
			params.setFocusMode("continuous-picture")
			camera.setParameters(params)

			// set camera surface rotation to 90 degrees to match activity view in portrait
			camera.setDisplayOrientation(90)

			// camera.autoFocus(autoFocusCB)
			camera.setPreviewDisplay(getHolder)

			camera.setPreviewCallback(new PreviewCallback {
				def onPreviewFrame(data: Array[Byte], camera: Camera) = previewFrameCb(data, camera)
			})
			camera.startPreview
		}
	}

	def setTorch(on: Boolean) {
		var params = camera.getParameters
		if (on)
			params.setFlashMode("torch")
		else
			params.setFlashMode("off")
		camera.setParameters(params)
	}
	
	// regularly trigger autofocus
	private val autoFocusHandler = new Handler
	private val doAutoFocus = new Runnable {
		def run {
			if (camera != null)	camera.autoFocus(autoFocusCB)
		}
	}
	private val autoFocusCB: AutoFocusCallback = new AutoFocusCallback {
		def onAutoFocus(success: Boolean, camera: Camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 250)
		}
	}

	// SurfaceHolder.Callback
	def surfaceCreated(holder: SurfaceHolder) {
		camera.setPreviewDisplay(holder)
	}

	def surfaceDestroyed(holder: SurfaceHolder) {
	}

	def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = {
		log.d(s"CameraPreview.surfaceChanged($format, $width, $height)")
	// 	if (holder.getSurface == null) {
	// 		// preview surface does not exist
	// 		return
	// 	}

	// 	if (camera == null) { 
	// 		return
	// 	}
	// 	camera.stopPreview

	// 	try {
	// 		// Hard code camera surface rotation 90 degs to match Activity view in portrait
	// 		camera.setDisplayOrientation(90)

	// 		camera.setPreviewDisplay(holder)
	// 		camera.setPreviewCallback(new PreviewCallback {
	// 			def onPreviewFrame(data: Array[Byte], camera: Camera) = previewFrameCb(data, camera)
	// 		})
	// 		camera.startPreview
	// 		camera.autoFocus(autoFocusCb)
	// 	} catch {
	// 		case e: Exception => Log.d("DBG", s"Error starting camera preview: ${e.getMessage}")
	// 	}
	}
}




    