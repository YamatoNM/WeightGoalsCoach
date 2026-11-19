package com.polar.pulsecoach.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.polar.polarsensordatacollector.BuildConfig
import com.polar.polarsensordatacollector.di.IoDispatcher
import com.polar.pulsecoach.account.AuthRepository
import com.polar.pulsecoach.account.KeyValueStore
import com.polar.pulsecoach.account.LocalAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): AuthRepository = LocalAuthRepository(
        store = SharedPreferencesStore(encryptedPrefs(context)),
        adminEmails = LocalAuthRepository.parseAdminEmails(BuildConfig.PULSE_ADMIN_EMAILS),
        ioDispatcher = ioDispatcher
    )

    /** Account and profile data is health data, so it is kept in encrypted preferences. */
    private fun encryptedPrefs(context: Context): SharedPreferences = EncryptedSharedPreferences.create(
        "pulse_coach_account",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

private class SharedPreferencesStore(private val prefs: SharedPreferences) : KeyValueStore {
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String?) {
        prefs.edit().apply { if (value == null) remove(key) else putString(key, value) }.commit()
    }
}
