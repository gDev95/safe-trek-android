package com.myalarmapp.root.safetrekfb.models;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.myalarmapp.root.safetrekfb.MyApplication;
import com.squareup.moshi.Json;

public class OAuthToken {
    @Json(name = "access_token")
    private String accessToken;

    @Json(name = "token_type")
    private String tokenType;

    @Json(name = "expires_in")
    private long expiresInMill;
    private long expiredAfterMill = 0;

    @Json(name = "refresh_token")
    private String refreshToken;
    public void save(){
        expiredAfterMill = System.currentTimeMillis() + expiresInMill;
        // get the access to storage
        SharedPreferences sp = MyApplication.instance.getSharedPreferences("OAuthStorage", Context.MODE_PRIVATE);
        // store OAuth Token
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString("accessToken", accessToken);
        spEditor.putString("refreshToken", refreshToken);
        spEditor.putString("tokenType", tokenType);
        spEditor.putLong("expiredAfter", expiredAfterMill);
        spEditor.apply();
    }
    // Getters
    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresInMill;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }
    // Setters
    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    void setExpiredAfterMill(long expiredInMill) {
        if(expiredInMill <= 0 ) {this.expiredAfterMill = 0;}
        else {
            this.expiredAfterMill = System.currentTimeMillis() + expiredInMill;
        }
    }

    void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public static class Factory {
        public static OAuthToken create(){
            long expiredAfter = 0;
            SharedPreferences sp =MyApplication.instance.getSharedPreferences("OAuthStorage", Context.MODE_PRIVATE);
            expiredAfter = sp.getLong("expiredAfter", 0);
            long now = System.currentTimeMillis();
            OAuthToken authToken = new OAuthToken();
            if(expiredAfter == 0 || expiredAfter < now){
                Log.e("Has Token expired?", "yes");
                SharedPreferences.Editor spEditor = sp.edit();
                // override token from storage
                spEditor.putString("accessToken", null);
                spEditor.apply();

                authToken.setAccessToken(null);
                authToken.setTokenType(null);
                authToken.setRefreshToken(sp.getString("refreshToken", null));
                authToken.setExpiredAfterMill(0);
                return authToken;
            }
            else {
                Log.e("Factory:Token expired?", "no");

                String accessToken = sp.getString("accessToken", null);
                String refreshToken = sp.getString("refreshToken", null);
                String tokenType = sp.getString("tokenType", null);

                authToken.setAccessToken(accessToken);
                authToken.setTokenType(tokenType);
                authToken.setRefreshToken(refreshToken);
                authToken.setExpiredAfterMill(expiredAfter);
                return authToken;
            }
        }

    }
}

