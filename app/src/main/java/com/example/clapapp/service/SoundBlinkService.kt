package com.example.clapapp.service

import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.clapapp.R
import com.example.clapapp.activities.MP3_RESOURCE_ID
import com.example.clapapp.activities.MainActivity
import com.example.clapapp.activities.isBlinking
import com.example.clapapp.key
import kotlin.math.abs

class SoundBlinkService : Service() {
    private lateinit var cameraManager: CameraManager
    private var isFlashing = false
    private var isListening = false
    private lateinit var audioRecord: AudioRecord
    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    private val RINGING_AND_BLINKING_DURATION = 20000 // 30 seconds
    private var lastClapTime: Long = 0
    private val MIN_TIME_BETWEEN_CLAPS = 1000
    private val audioData = ShortArray(BUFFER_SIZE)
    private var isClapDetectionActive = false
    private var flashBlinkHandler: Handler? = null
    private var isFlashOn = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the sound and blinking functionalities
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        startClapDetection()

        val notificationIntent =
            Intent(this, MainActivity::class.java) // Replace with your actual activity
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, "my_channel_id")
            .setContentTitle("Foreground Service Title")
            .setContentText("Foreground Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build()
        runCatching {
            startForeground(1, notification) // Use a unique notification ID
        }
        Log.d("dhaush9876543", "onStartComand: ")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("dhaush9876543", "onBind: ")
        return null
    }

    override fun onDestroy() {
        stopClapDetection()
        stopSoundAndFlash()
        Log.d("dhaush9876543", "onDestroy: ")
        stopFlashBlinking()
        runCatching {
            stopForeground(true)
        }
        saveBooleanToSharedPreferences(this, key, false)
        super.onDestroy()
    }

    private fun saveBooleanToSharedPreferences(context: Context, key: String, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("MySelection", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    private fun startClapDetection() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show()
            return
        }
        isListening = true
        isClapDetectionActive = true
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )
        val handler = Handler(Looper.getMainLooper())
        audioRecord.startRecording()
        Thread {
            while (isListening) {
                audioRecord.read(audioData, 0, BUFFER_SIZE)
                if (isClapDetected(audioData)) {
                    handler.post {
                        if (isBlinking) {
                            playSoundAndFlash2()
                        } else {
                            playSoundAndFlash()
                        }
                    }
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    private fun isClapDetected(audioData: ShortArray): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClapTime < MIN_TIME_BETWEEN_CLAPS) {
            return false // Avoid detecting multiple claps in quick succession
        }
        val threshold = 10000 // Adjust this threshold based on your audio data characteristics
        val clapDetected = audioData.any { abs(it.toInt()) > threshold }
        if (clapDetected) {
            lastClapTime = currentTime
        }
        return clapDetected
    }

    private var mediaPlayer: MediaPlayer? = null
    private fun playSoundAndFlash() {
        stopSoundAndFlash()
        // Play sound using MediaPlayer
        mediaPlayer = MediaPlayer.create(this, MP3_RESOURCE_ID)
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release() // Release the MediaPlayer when done
        }
        mediaPlayer?.start()

        // Flashlight blinking using CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0] // Use the first camera
            val parameters = cameraManager.getCameraCharacteristics(cameraId)
            val flashAvailable = parameters.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

            if (flashAvailable == true) {
                cameraManager.setTorchMode(cameraId, true) // Turn on the flashlight

                // Stop flashing after 30 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    stopSoundAndFlash()
                }, RINGING_AND_BLINKING_DURATION.toLong())
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, isFlashOn)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun startFlashBlinking() {
        stopFlashBlinking()
        flashBlinkHandler = Handler(Looper.getMainLooper())
        val flashBlinkRunnable = object : Runnable {
            override fun run() {
                toggleFlash()
                flashBlinkHandler?.postDelayed(this, 200)
            }
        }
        flashBlinkHandler?.post(flashBlinkRunnable)
        // Stop blinking after a certain duration
        flashBlinkHandler?.postDelayed({
            stopFlashBlinking()
        }, RINGING_AND_BLINKING_DURATION.toLong())
    }

//    private fun stopFlashBlinking() {
//        flashBlinkHandler?.removeCallbacksAndMessages(null)
//        flashBlinkHandler = null
//        // Ensure the flashlight is turned off when stopping blinking
//        try {
//            val cameraId = cameraManager.cameraIdList[0]
//            cameraManager.setTorchMode(cameraId, false)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }


    private fun stopFlashBlinking() {
        flashBlinkHandler?.removeCallbacksAndMessages(null)
        flashBlinkHandler = null

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            try {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

                // Check if this camera has a flash
                hasFlash
            } catch (e: CameraAccessException) {
                false
            }
        }

        if (cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                Log.e(ContentValues.TAG, "CameraAccessException: ${e.message}")
            }
        } else {
            Log.w(ContentValues.TAG, "No suitable camera with flashlight found.")
        }
    }

    private fun playSoundAndFlash2() {
        stopSoundAndFlash()
        // Play sound using MediaPlayer
        mediaPlayer = MediaPlayer.create(this, MP3_RESOURCE_ID)
        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.start()
        }
        mediaPlayer?.start()
        // Start Flash Blinking
        startFlashBlinking()
    }

    private fun stopSoundAndFlash() {
        mediaPlayer?.release()
        mediaPlayer = null
        if (isFlashing) {
            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, false)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            isFlashing = false
        }
    }

    private fun stopClapDetection() {
        isListening = false
        isClapDetectionActive = false
    }
}
