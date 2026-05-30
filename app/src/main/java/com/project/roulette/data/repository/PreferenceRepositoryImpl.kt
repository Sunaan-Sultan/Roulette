package com.project.roulette.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.project.roulette.data.local.database.RouletteDatabase
import com.project.roulette.domain.repository.PreferenceRepository
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.viewmodel.WheelViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: RouletteDatabase
) : PreferenceRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("roulette_prefs", Context.MODE_PRIVATE)

    private fun <T> preferenceFlow(key: String, defaultValue: T, getter: (SharedPreferences, String, T) -> T): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (key == changedKey) {
                trySend(getter(sharedPreferences, key, defaultValue))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getter(prefs, key, defaultValue))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override val paletteIndex: Flow<Int> = preferenceFlow("palette_index", 0) { p, k, d -> p.getInt(k, d) }
    
    override val defaultAlgorithm: Flow<SelectionAlgorithmFactory.AlgorithmType> = preferenceFlow("default_algo", SelectionAlgorithmFactory.AlgorithmType.UNIFORM.name) { p, k, d -> p.getString(k, d) ?: d }
        .map { SelectionAlgorithmFactory.AlgorithmType.valueOf(it) }

    override val defaultSpinSpeed: Flow<WheelViewModel.SpinSpeed> = preferenceFlow("default_speed", WheelViewModel.SpinSpeed.MEDIUM.name) { p, k, d -> p.getString(k, d) ?: d }
        .map { WheelViewModel.SpinSpeed.valueOf(it) }

    override val spinSoundEnabled: Flow<Boolean> = preferenceFlow("spin_sound", true) { p, k, d -> p.getBoolean(k, d) }
    override val confettiEnabled: Flow<Boolean> = preferenceFlow("confetti", true) { p, k, d -> p.getBoolean(k, d) }
    override val removeAfterPickEnabled: Flow<Boolean> = preferenceFlow("remove_after", false) { p, k, d -> p.getBoolean(k, d) }

    override suspend fun updatePaletteIndex(index: Int) {
        prefs.edit().putInt("palette_index", index).apply()
    }

    override suspend fun updateDefaultAlgorithm(algorithm: SelectionAlgorithmFactory.AlgorithmType) {
        prefs.edit().putString("default_algo", algorithm.name).apply()
    }

    override suspend fun updateDefaultSpinSpeed(speed: WheelViewModel.SpinSpeed) {
        prefs.edit().putString("default_speed", speed.name).apply()
    }

    override suspend fun updateSpinSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("spin_sound", enabled).apply()
    }

    override suspend fun updateConfettiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("confetti", enabled).apply()
    }

    override suspend fun updateRemoveAfterPickEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("remove_after", enabled).apply()
    }

    override suspend fun clearAllData() {
        database.clearAllTables()
        prefs.edit().clear().apply()
    }
}
