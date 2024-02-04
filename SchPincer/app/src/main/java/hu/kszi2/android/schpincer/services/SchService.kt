package hu.kszi2.android.schpincer.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BADGE_ICON_LARGE
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import hu.kszi2.android.schpincer.R
import hu.kszi2.android.schpincer.data.PreferenceAccessor
import hu.kszi2.android.schpincer.fragments.SettingsFragment
import hu.kszi2.android.schpincer.fragments.SettingsFragment.Companion.apiSwitch
import hu.kszi2.android.schpincer.fragments.SettingsFragment.Companion.notificationSwitch
import hu.kszi2.android.schpincer.fragments.WelcomeFragment
import kotlin.concurrent.thread

/**
 * **Source:** [Extending the Service class](https://developer.android.com/guide/components/services#ExtendingService)
 */

class SchService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private lateinit var preferences: MutableMap<String, *>
    private var notification = false
    private var notificationInterval = 30 //in minutes
    private var api = false
    private var apiInterval = 10 //in minutes

    companion object {
        private const val minuteToMillis = (60 * 1000).toLong()
    }

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            //notification
            thread {
                while (true) {
                    getPreferences()
                    if (notification && WelcomeFragment.LoadOpenings.anyNewOpening()) { //only if it is enabled and there is a new opening
                        alert()
                    }
                    Thread.sleep(notificationInterval.toLong() * minuteToMillis)
                }
            }

            //api
            thread {
                while (true) {
                    getPreferences()
                    if (api) { //only if it is enabled
                        WelcomeFragment.LoadOpenings.loadOpeningsIntoDB()
                    }
                    Thread.sleep(apiInterval.toLong() * minuteToMillis)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun alert() {
        val builder = NotificationCompat.Builder(this, "SchPincerAlert")
            .setSmallIcon(R.drawable.ic_stat_noti)
            .setBadgeIconType(BADGE_ICON_LARGE)
            .setContentTitle("Alert")
            .setContentText("There is a new opening")
        NotificationManagerCompat.from(this).notify(69, builder.build())
    }

    override fun onCreate() {
        getPreferences()
        HandlerThread("SchDaemon").apply {
            start()
            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        serviceHandler?.obtainMessage()?.also { msg -> serviceHandler?.sendMessage(msg) }
        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getPreferences() {
        PreferenceAccessor.lockPreferences {
            preferences = PreferenceManager.getDefaultSharedPreferences(this).all
            //set them...
            try {
                notification = preferences[notificationSwitch] as Boolean
                api = preferences[apiSwitch] as Boolean

                val nint = preferences[SettingsFragment.notificationInterval] as String
                if (nint == "0") { //error
                    return@lockPreferences
                }
                notificationInterval = nint.toInt()
                val aint = preferences[SettingsFragment.apiInterval] as String
                if (aint == "0") { //error
                    return@lockPreferences
                }
                apiInterval = aint.toInt()
            } catch (e: Exception) {
                Log.d("Error", "${e.cause},${e.localizedMessage}")
            }
        }
    }
}