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