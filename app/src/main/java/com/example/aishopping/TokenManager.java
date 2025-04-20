package com.example.aishopping;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class TokenManager {
    private static final String PREFS_NAME = "secure_token_prefs";
    private static final String KEY_LOGIN_TOKEN = "login_token";
    private static TokenManager instance;
    private final SharedPreferences securePrefs;

    private TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            securePrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize secure preferences", e);
        }
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    // 保存token
    public void saveTokens(String loginToken) {
        securePrefs.edit()
                .putString(KEY_LOGIN_TOKEN, loginToken)
                .apply();
    }

    // 获取token
    public String getLoginToken() {
        return securePrefs.getString(KEY_LOGIN_TOKEN, null);
    }

    // 清除token
    public void clearTokens() {
        securePrefs.edit()
                .remove(KEY_LOGIN_TOKEN)
                .apply();
    }
}
