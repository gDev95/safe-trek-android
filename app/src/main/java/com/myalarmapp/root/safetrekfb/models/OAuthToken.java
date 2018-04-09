package com.myalarmapp.root.safetrekfb.models;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.myalarmapp.root.safetrekfb.MyApplication;
import com.squareup.moshi.Json;

import bolts.Bolts;

public class OAuthToken {

    /********* CONSTANT *********/
    private static final String TAG = "OAuthToken";

    /********** ATTRIBUTES *******/
    @Json(name = "access_token")
    private String accessToken;

    @Json(name = "token_type")
    private String tokenType;

    @Json(name = "expires_in")
    private long expiresInMill;

    private long expiredAfterMill = 0;

    @Json(name = "refresh_token")
    private String refreshToken;

    /********* MEMBER FUNCTIONS ********/
    public void save(){
        expiredAfterMill = System.currentTimeMillis() + (expiresInMill * 100);
        // get the access to storage
        SharedPreferences sp = MyApplication.instance.getSharedPreferences("OAuthStorage", Context.MODE_PRIVATE);
        // Build Editor to write to storage
        SharedPreferences.Editor spEditor = sp.edit();
        // store OAuth Token
        spEditor.putString("accessToken", accessToken);
        spEditor.putString("refreshToken", refreshToken);
        spEditor.putString("tokenType", tokenType);
        spEditor.putLong("expiredAfter", expiredAfterMill);
        spEditor.apply();
    }
    // Getters
    /*
     * @params none
     * @return String AccessToken
     */
    public String getAccessToken() {
        return accessToken;
    }
    /*
     * @params none
     * @return long expiresInMill
     */
    public long getExpiresIn() {
        return expiresInMill;
    }
    /*
     * @params none
     * @return String refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    /*
     * @params none
     * @return String tokenType
     */
    public String getTokenType() {
        return tokenType;
    }
    // Setters
    /*
     * @params String accessToken
     * @return nothing
     */
    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    /*
     * @params long expiredinMill
     * @return nothing
     */
    void setExpiredAfterMill(long expiredInMill) {
        if(expiredInMill <= 0 ) {this.expiredAfterMill = 0;}
        else {
            this.expiredAfterMill = System.currentTimeMillis() + expiredInMill;
        }
    }
    /*
     * @params String refreshToken
     * @return nothing
     */
    void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    /*
     * @params String tokenType
     * @return nothing
     */
    void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /************* FACTORY ********/
    public static class Factory {
        public static OAuthToken create(){
            long expiredAfter = 0;

            SharedPreferences sp = MyApplication.instance.getSharedPreferences("OAuthStorage", Context.MODE_PRIVATE);

            expiredAfter = sp.getLong("expiredAfter", 0);

            long now = System.currentTimeMillis();

            // Log.e(TAG, "Is token expired? " + Boolean.toString(expiredAfter == 0 || expiredAfter < now));

            OAuthToken authToken = new OAuthToken();

            if(expiredAfter == 0 || expiredAfter < now){
                Log.e(TAG, "Token has expired");

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
                Log.e(TAG, "Token is valid");

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

