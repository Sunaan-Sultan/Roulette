
package com.project.roulette.util

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object RemoteConfigUtil {

    private const val MINIMUM_VERSION_CODE_KEY = "minimum_version_code"

    fun init() {
        val remoteConfig = Firebase.remoteConfig
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(settings)
    }

    fun fetchMinimumVersionCode(onResult: (Int) -> Unit) {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetch(0).addOnCompleteListener { fetchTask ->
            Log.d("RemoteConfig", "Fetch done: ${fetchTask.isSuccessful}, error: ${fetchTask.exception?.message}")
            remoteConfig.activate().addOnCompleteListener { activateTask ->
                Log.d("RemoteConfig", "Activate done: ${activateTask.result}")
                Log.d("RemoteConfig", "All keys: ${remoteConfig.all.keys}")
                val minVersion = remoteConfig.getString(MINIMUM_VERSION_CODE_KEY).toIntOrNull() ?: 1
                Log.d("RemoteConfig", "Min version: $minVersion")
                onResult(minVersion)
            }
        }
    }
}