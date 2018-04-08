package com.myalarmapp.root.safetrekfb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.system.ErrnoException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.myalarmapp.root.safetrekfb.models.OAuthToken;

import java.util.ArrayList;
import java.util.List;
import com.myalarmapp.root.safetrekfb.retrofit.OAuthServerIntf;
import com.myalarmapp.root.safetrekfb.retrofit.converters.StringConverterFactory;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static android.content.Intent.ACTION_VIEW;


public class LoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager = CallbackManager.Factory.create();

    private static final String CLIENT_ID = "gk1nFtbQr4pBpJD0rzAp3vaSi555sm4s";
    private static final String SCOPE = "openid+phone+offline_access";
    private static final String REDIRECT_URI = "safetrekfb://callback";
    private static final String REDIRECT_URI_ROOT = "safetrekfb://";
    private static final String AUDIENCE = "https://api-sandbox.safetrek.io";

    private String code;
    private String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List permissions = new ArrayList<String>();
        permissions.add("publish_actions");

        Uri data = getIntent().getData();

        // if application started by Browser
        if (data != null && !TextUtils.isEmpty(data.getScheme())) {
            // check if URL fits data scheme
//            Log.e("data scheme", data.getScheme());
//            Log.e("Redirect URI Root", REDIRECT_URI_ROOT);
            if (REDIRECT_URI_ROOT.equals(data.getScheme()+ "://")) {

                code = data.getQueryParameter("code");
                error = data.getQueryParameter("error");

                Log.e("onCreate:", "handle result of authorization with code :" + code);
                if (!TextUtils.isEmpty(code)) {
                    getOAuthToken();
                }
                if (!TextUtils.isEmpty(error)) {
                    //a problem occurs, the user reject our granting request or something like tha
                    Log.e("onCreate:", "an Error occurd during authorization:" + error);
                    //then die
                    finish();
                }
            }
            Log.d("Check", "Done ");
        } else {
            //Manage the start application case:
            //If you don't have a token yet or if your token has expired , ask for it
            OAuthToken oauthToken = OAuthToken.Factory.create();
            Log.e("getAccessToken()", "accessToken: " + oauthToken.getAccessToken());
            Log.e("refreshToken", "refreshToken: " + oauthToken.getRefreshToken());
            if (oauthToken == null
                    || oauthToken.getAccessToken() == null) {
                //first case==first token request
                if (oauthToken == null || oauthToken.getRefreshToken() == null) {
                    Log.d("onCreate:", "Launching authorization (first step)");
                    //first step of OAUth: the authorization step
                    makeAuthRequest();
                } else {
                    Log.d("onCreate:", "refreshing the token :" + oauthToken);
                    // get new Access Token
                    // use refresh Token
                    String refreshToken = oauthToken.getRefreshToken();
                    refreshAccessToken(refreshToken);
                    finish();
                }
            }
            //else just launch your MainActivity
            else {
                Log.e("onCreate:", " Token available, just launch MainActivity");
                startMainActivity(true);
            }
        }


//        String loggedInFacebook = AccessToken.getCurrentAccessToken().getToken();
//        if (loggedInFacebook.length() >= 1) {
//            Log.d("Test", AccessToken.getCurrentAccessToken().getToken());
//            Uri data = getIntent().getData();
//            if (data != null && !TextUtils.isEmpty(data.getScheme())) {
//                if (REDIRECT_URI_ROOT.equals(data.getScheme())) {
//                    code = data.getQueryParameter(CODE);
//                    error=data.getQueryParameter(ERROR_CODE);
//                    Log.e(TAG, "onCreate: handle result of authorization with code :" + code);
//                    if (!TextUtils.isEmpty(code)) {
//                        getTokenFormUrl();
//                    }
//                    if(!TextUtils.isEmpty(error)) {
//                        //a problem occurs, the user reject our granting request or something like that
//                        Toast.makeText(this, R.string.loginactivity_grantsfails_quit,Toast.LENGTH_LONG).show();
//                        Log.e(TAG, "onCreate: handle result of authorization with error :" + error);
//                        //then die
//                        finish();
//                    }
//                }
//            }
//        } else {
//            LoginManager lm = LoginManager.getInstance();
//            lm.logInWithPublishPermissions(LoginActivity.this, permissions);
//            lm.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//                @Override
//                public void onSuccess(LoginResult loginResult) {
//
//
//                }
//
//                @Override
//                public void onCancel() {
//
//                }
//
//                @Override
//                public void onError(FacebookException error) {
//
//                }
//            });
//        }
    }
    private void refreshAccessToken(String refreshToken) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .baseUrl("https://login-sandbox.safetrek.io/")
                .build();
        OAuthServerIntf oAuthServer = retrofit.create(OAuthServerIntf.class);
        Call<OAuthToken> refreshAccessTokenCall = oAuthServer.getNewAccessToken(
                refreshToken,
                "gk1nFtbQr4pBpJD0rzAp3vaSi555sm4s",
                "eWTSj_izMvD3nBJFXxkRDZF4aXDGKofYRZyzw_31oer31kuoY6-OVDs27nEHJu0B",
                "refresh_token"

        );
        refreshAccessTokenCall.enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                Log.e("Response:", response.code() + "  | body = " + response.body());

                String newAccessToken = response.body().getAccessToken();
                long expiresIn = response.body().getExpiresIn();
                long expiredAfter = System.currentTimeMillis() + expiresIn;
                SharedPreferences sp = MyApplication.instance.getSharedPreferences("OAuthStorge", Context.MODE_PRIVATE);
                SharedPreferences.Editor spEditor = sp.edit();
                spEditor.putString("accessToken", newAccessToken);
                spEditor.putLong("expiredAfter", expiredAfter);
                spEditor.apply();
                startMainActivity(true);

            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {

            }
        });


    }
    private void getOAuthToken() {


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .baseUrl("https://login-sandbox.safetrek.io/")
                .build();

        OAuthServerIntf oAuthServer = retrofit.create(OAuthServerIntf.class);
        Call<OAuthToken> requestTokenCall = oAuthServer.requestToken(
                "authorization_code",
                code,
               "gk1nFtbQr4pBpJD0rzAp3vaSi555sm4s",
                "eWTSj_izMvD3nBJFXxkRDZF4aXDGKofYRZyzw_31oer31kuoY6-OVDs27nEHJu0B",
                "safetrekfb://callback"
        );
        requestTokenCall.enqueue(new Callback<OAuthToken>() {
            @Override
            public void onResponse(Call<OAuthToken> call, Response<OAuthToken> response) {
                Log.e("Response:", response.code() + "  | body = " + response.body());
                //ok we have the token now store it
                response.body().save();
                startMainActivity(true);
            }

            @Override
            public void onFailure(Call<OAuthToken> call, Throwable t) {
                Log.e("Error with Request", "The call getRequestTokenFormCall failed", t);
            }
        });
    }

    private void makeAuthRequest() {
        Log.e("makeAuthRequest","opening browser");
        HttpUrl authorizeUrl = HttpUrl.parse("https://account-sandbox.safetrek.io/authorize?scope=openid+phone+offline_access&response_type=code&redirect_uri=safetrekfb://callback") //
                .newBuilder()
                .addQueryParameter("audience", AUDIENCE)
                .addQueryParameter("client_id", CLIENT_ID)
                .build();
        Log.e("End of Request", "the url is : " + String.valueOf(authorizeUrl.url()));
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(String.valueOf(authorizeUrl.url())));
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void startMainActivity(boolean newtask) {
        Intent i = new Intent(this, MainActivity.class);
        if (newtask) {
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivity(i);
        //you can die so
        finish();
    }

}