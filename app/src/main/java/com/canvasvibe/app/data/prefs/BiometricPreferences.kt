package com.canvasvibe.app.data.prefs

import android.content.Context

object BiometricPreferences {

    private const val FILE = "biometric_prefs"
    private const val KEY_DECIDED_PREFIX = "decided:"
    private const val KEY_ENABLED_PREFIX = "enabled:"

    fun hasDecided(context: Context, uid: String): Boolean {
        if (uid.isBlank()) return false
        val prefs = context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DECIDED_PREFIX + uid, false)
    }

    fun isEnabled(context: Context, uid: String): Boolean {
        if (uid.isBlank()) return false
        val prefs = context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED_PREFIX + uid, false)
    }

    fun setDecision(context: Context, uid: String, enabled: Boolean) {
        if (uid.isBlank()) return
        context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DECIDED_PREFIX + uid, true)
            .putBoolean(KEY_ENABLED_PREFIX + uid, enabled)
            .apply()
    }

    fun reset(context: Context, uid: String) {
        if (uid.isBlank()) return
        context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_DECIDED_PREFIX + uid)
            .remove(KEY_ENABLED_PREFIX + uid)
            .apply()
    }
}
