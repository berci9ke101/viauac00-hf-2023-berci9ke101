package hu.kszi2.android.schpincer.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.preference.*
import hu.kszi2.android.schpincer.R
import hu.kszi2.android.schpincer.data.PreferenceAccessor
import kotlin.concurrent.thread

/**
 * **Source:** [Settings](https://developer.android.com/develop/ui/views/components/settings)
 */

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        const val notificationSwitch = "notifications"
        const val notificationInterval = "notification_interval"
        const val apiSwitch = "api"
        const val apiInterval = "api_interval"
        const val clearSwitch = "clear"
        const val developerSecret = "copyright"

        var developer = false
        private var cnt = 1
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        PreferenceAccessor.lockPreferences {
            createListeners()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun createListeners() {
        //disable interval when notifications are disabled
        findPreference<Preference>(notificationSwitch)?.run {
            setOnPreferenceChangeListener { _, newValue ->
                findPreference<Preference>(notificationInterval)?.run {
                    isEnabled = newValue as Boolean
                    isVisible = newValue
                }
                true
            }
            //needed for aesthetics
            performClick()
            performClick()
        }

        //disable interval when api is disabled
        findPreference<Preference>(apiSwitch)?.run {
            setOnPreferenceChangeListener { _, newValue ->
                findPreference<Preference>(apiInterval)?.run {
                    isEnabled = newValue as Boolean
                    isVisible = newValue
                }
                true
            }
            //needed for aesthetics
            performClick()
            performClick()
        }

        //clear database
        findPreference<Preference>(clearSwitch)?.setOnPreferenceClickListener {
            thread {
                WelcomeFragment.LoadOpenings.clearDb()
            }
            true
        }

        //developer settings
        findPreference<Preference>(developerSecret)?.run {
            setOnPreferenceClickListener {
                triggerDeveloper()
                true
            }
        }
    }

    private fun becomeDeveloper() {
        Toast.makeText(this.context, "DEVELOPER MODE ON", Toast.LENGTH_SHORT).show()
        developer = true
    }

    private fun triggerDeveloper() {
        when {
            cnt == 8 -> {
                cnt++
                becomeDeveloper()
            }

            cnt < 8 -> cnt++
            else -> return
        }
    }
}