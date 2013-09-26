package de.mritter.ticketchecker.android

import java.io.IOException

import android.os.Bundle
import android.os.Handler

import android.view._
import android.content.Context

import android.hardware.Camera
import android.hardware.Camera._

import de.mritter.android.common._

class CameraPreview(context: Context, protected val previewFrameCallback: (Array[Byte], Camera) => Unit) extends SurfaceView(context) with SurfaceHolder.Callback {

	protected var camera: Camera = null
	protected var previewIsRunning = false
	protected var previewCallback = new PreviewCallback {
		def onPreviewFrame(data: Array[Byte], camera: Camera) = previewFrameCallback(data, camera)
	}

	getHolder.addCallback(this) // will trigger surfaceCreated, surfaceChanged, surfaceDestroyed

	var wasCreated = false

	def resume {
		log.d("CameraPreview.resume()")
		if (camera == null) {
			// ToDo catch exceptions
			camera = Camera.open
			log.w("camera opened")
			withCameraParameters { p =>
				if (android.os.Build.VERSION.SDK_INT >= 14) {
					p.setFocusMode("continuous-picture")
				} else {
					// camera.autoFocus(autoFocusCB)
				}
			}

			camera.setDisplayOrientation(90)

			startPreview
		}
	}

	def pause {
		log.d("CameraPreview.pause()")
		if (camera != null) {
			stopPreview
			camera.release
			camera = null
		}
	}

	var mHolder: SurfaceHolder = null

	def surfaceCreated(holder: SurfaceHolder): Unit = {
		log.d("CameraPreview.surfaceCreated()")
		
		wasCreated = true
		mHolder = holder

		// works for galaxy
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
		// camera.setPreviewDisplay(holder)



		// tryOrLog {
		// 	if (holder == null)
		// 		log.w("surfaceCreated holder is fucking null")
		// 	camera.setPreviewDisplay(holder)
		// }

		// val p = camera.getParameters();

		// p.setPictureSize(IMAGE_WIDTH, IMAGE_HEIGHT);

		// camera.getParameters().setRotation(90)

		// Camera.Size s = p.getSupportedPreviewSizes().get(0)
		// p.setPreviewSize(s.width, s.height)

		// p.setPictureFormat(PixelFormat.JPEG)
		// p.set("flash-mode", "auto")

		// camera.setParameters(p)
	}

	def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		log.d("CameraPreview.surfaceChanged()")

		mHolder = holder

		stopPreview


		// do stuff
		// tryOrLog {
		// 	if (getHolder == null)
		// 		log.w("surfaceChanged holder is fucking null")
		// 	holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
		// 	camera.setPreviewDisplay(holder)
		// }

		startPreview
	}
	
	def surfaceDestroyed(holder: SurfaceHolder) {
		log.d("CameraPreview.surfaceDestroyed()")
		stopPreview
	}

	def setTorch(on: Boolean) {
		var params = camera.getParameters
		if (on)
			params.setFlashMode("torch")
		else
			params.setFlashMode("off")
		camera.setParameters(params)
	}

	def startPreview {
		log.d("CameraPreview.startPreview()")
		if (previewIsRunning)
			stopPreview
		if (camera != null && wasCreated) {

			log.w("mHolder=" + mHolder.toString)
			log.w("getHolder=" + getHolder.toString)
			tryOrLog {
				// if (android.os.Build.VERSION.SDK_INT < 11)
				mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
				camera.setPreviewDisplay(mHolder)
			}


			camera.setPreviewCallback(previewCallback)
			camera.startPreview
			previewIsRunning = true
		}
	}

	private def stopPreview {
		log.d("CameraPreview.stopPreview()")
		if (camera != null) {
			camera.stopPreview
			camera.setPreviewCallback(null)
			previewIsRunning = false
		}
	}

	private def withCameraParameters(f: (Camera#Parameters) => Unit) {
		if (camera != null) {
			try {
				var p = camera.getParameters
				f(p)
				camera.setParameters(p)
			} catch {
				case e: Throwable => log.w("Error while changing camera parameters: " + e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\t\n"))
			}
		}
	}
	
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
}




    