package com.example.sec_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class LoginSessionManager {
    private static final String PREFERENCES_NAME = "secure_login_session";
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences preferences;

    public LoginSessionManager(Context context) {
        preferences = createEncryptedPreferences(context.getApplicationContext());
    }

    private SharedPreferences createEncryptedPreferences(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            return EncryptedSharedPreferences.create(
                    PREFERENCES_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("Unable to initialize encrypted login storage", exception);
        }
    }

    public void saveLogin(String account, String password) {
        preferences.edit()
                .putBoolean(KEY_REMEMBER_LOGIN, true)
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ACCOUNT, account)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public boolean restoreSession() {
        String account = getAccount();
        boolean canRestore = isRememberLoginEnabled()
                && preferences.getBoolean(KEY_LOGGED_IN, false)
                && !TextUtils.isEmpty(account);
        if (canRestore) {
            Constant.landing = true;
            Constant.account = account;
        }
        return canRestore;
    }

    public boolean isRememberLoginEnabled() {
        return preferences.getBoolean(KEY_REMEMBER_LOGIN, false);
    }

    public String getAccount() {
        return preferences.getString(KEY_ACCOUNT, "");
    }

    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, "");
    }

    public void clear() {
        preferences.edit().clear().apply();
        Constant.landing = false;
        Constant.account = "";
    }
}
