package de.mritter.ticketchecker.android

// import android.os.{Bundle, Handler}
import android.view.{SurfaceHolder, SurfaceView}
import android.hardware.Camera
import android.hardware.Camera._

import de.mritter.android.common._

case class PreviewFrame(data: Array[Byte], camera: Camera);

class CameraPreview(val surfaceView: SurfaceView, val previewCallback: Camera.PreviewCallback) extends SurfaceHolder.Callback {
	
	protected val surfaceHolder = surfaceView.getHolder
	surfaceHolder.addCallback(this)
	surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS) 

	protected var camera: Camera = null
	protected var previewing = false
	protected var surfaceCreated = false
	protected var torchOn = false

	def resume {
		startPreview
	}

	def pause {
		stopPreview
	}

	def toggleTorch {
		setTorch(!torch)
	}

	def torch = torchOn

	def setTorch(on: Boolean) {
		torchOn = on
		withCameraParameters{ p =>
			p.setFlashMode(if (torchOn) "torch" else "off")
		}
	}

	override def surfaceCreated(holder: SurfaceHolder) {
		surfaceCreated = true
		// startPreview is called in surfaceChanged(), which is called after surfaceCreated()
	}

	override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		stopPreview
		
		// change settings

		startPreview
	}

	override def surfaceDestroyed(holder: SurfaceHolder) {
		surfaceCreated = false
		stopPreview
	}

	protected def startPreview {
		if (!previewing && surfaceCreated) {
			camera = Camera.open
			if (camera != null) {
				try {
					withCameraParameters { p =>
						if (android.os.Build.VERSION.SDK_INT >= 14) {
							p.setFocusMode("continuous-picture")
						} else {
							p.setFocusMode("auto")
						}
						p.setFlashMode(if (torchOn) "torch" else "off")
					}
					camera.setDisplayOrientation(90)
					camera.setPreviewDisplay(surfaceHolder)
					camera.setPreviewCallback(previewCallback)
					camera.startPreview
					previewing = true
					if (android.os.Build.VERSION.SDK_INT < 14) {
						doContinuousAutoFocus // executing is only valid after startPreview and before stopPreview
					}
				} catch {
					case e: Throwable => log.w("Could not start camera preview: " + e.toString + "\n" + e.getStackTrace.take(5).mkString("\t\n"))
				}
			}
		}
	}

	protected def stopPreview {
		if (camera != null && previewing) {
			previewing = false
			if (android.os.Build.VERSION.SDK_INT < 14) {
				camera.cancelAutoFocus // needed to stop simulated continuous auto focus
			}
			camera.stopPreview
			camera.setPreviewCallback(null)
			camera.release
			camera = null
		}
	}

	protected def withCameraParameters(f: (Camera#Parameters) => Unit) {
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

	// just simulate a continuous auto focus as with "continuous-picture" focus mode (available for API level >= 14)
	protected def doContinuousAutoFocus {
		if (previewing) {
			camera.autoFocus(autoFocusCallback)
		}
	}

	protected val autoFocusCallback = new AutoFocusCallback {
		def onAutoFocus(success: Boolean, camera: Camera) {
			scheduleTask(doContinuousAutoFocus, 100)
		}
	}
}


