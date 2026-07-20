// util/AppVersionUtil.kt
package com.project.roulette.util

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

fun getCurrentVersionCode(context: Context): Int {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
    } catch (e: Exception) {
        1
    }
}

fun getCurrentVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: ""
    } catch (e: Exception) {
        ""
    }
}

/**
 * True only for a genuine first install. When the app has been updated over an
 * existing install, firstInstallTime and lastUpdateTime differ.
 */
fun isFreshInstall(context: Context): Boolean {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.firstInstallTime == packageInfo.lastUpdateTime
    } catch (e: Exception) {
        false
    }
}