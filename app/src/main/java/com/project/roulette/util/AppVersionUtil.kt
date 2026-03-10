// util/AppVersionUtil.kt
package com.project.roulette.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.P)
fun getCurrentVersionCode(context: Context): Int {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.longVersionCode.toInt()
    } catch (e: Exception) {
        1
    }
}