package hu.kszi2.android.schpincer.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import hu.kszi2.android.schpincer.databinding.ActivityMainBinding
import hu.kszi2.android.schpincer.services.SchService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var schServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //create channel for notifications
        createNotificationChannel()

        //start background service
        schServiceIntent = Intent(this, SchService::class.java)
        startService(schServiceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(schServiceIntent)
    }

    //because of api lvl
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SchPincerAlert"
            val descriptionText = "SchPincerAlert notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SchPincerAlert", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}